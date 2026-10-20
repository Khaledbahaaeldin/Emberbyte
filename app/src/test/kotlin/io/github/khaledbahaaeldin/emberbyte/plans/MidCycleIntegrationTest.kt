package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.alert.AlertEvaluator
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.AlertRule
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageStatus
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.DayUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.LiveSpeed
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsageFilter
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.widget.WidgetData
import io.github.khaledbahaaeldin.emberbyte.widget.heroWidgetModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MidCycleIntegrationTest {
    private val utc = ZoneOffset.UTC
    private val currentInstant = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = object : Clock() {
        override fun getZone(): ZoneId = utc
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = currentInstant
    }

    private class FakeUsageRepo(
        private val today: DayUsage = DayUsage(LocalDate.of(2026, 10, 15), 500_000_000L, 0L),
    ) : UsageRepository {
        override fun observeLiveSpeed(): Flow<LiveSpeed> = emptyFlow()
        override fun observeToday(filter: UsageFilter): Flow<DayUsage> = flowOf(today)
        override fun observeSeries(range: DateRange, granularity: Granularity, filter: UsageFilter): Flow<List<UsagePoint>> = flowOf(emptyList())
        override fun observeApps(range: DateRange, filter: UsageFilter, sort: AppSort): Flow<List<AppUsage>> = flowOf(emptyList())
        override fun observeAppSeries(packageName: String, range: DateRange, granularity: Granularity): Flow<List<UsagePoint>> = flowOf(emptyList())
        override fun observeCoverage(): Flow<CoverageStatus> = flowOf(CoverageStatus(emptyList(), null))
        override suspend fun refreshNow(): Outcome<Unit> = Outcome.Success(Unit)
    }

    @Test
    fun entered_over_cap_raises_one_alert_and_the_widget_data_says_over() = runBlocking {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(currentInstant))

        val cycleStart = Instant.parse("2026-10-01T00:00:00Z")
        val enteredAt = Instant.parse("2026-10-15T10:00:00Z")
        val plan = Plan(
            id = 0L,
            name = "Overcap Plan",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc),
            rollover = Rollover.None,
            firstCycle = FirstCycle.Entered(
                usedBytes = 11_000_000_000L,
                enteredAt = enteredAt,
                cycleStart = cycleStart,
            ),
        )
        val planId = planStore.upsertPlan(plan)

        planStore.upsertAlertRule(AlertRule(0L, planId, 50, enabled = true))
        planStore.upsertAlertRule(AlertRule(0L, planId, 80, enabled = true))
        planStore.upsertAlertRule(AlertRule(0L, planId, 100, enabled = true))

        // 1. Alert check: Evaluator raises exactly one alert at 100% threshold
        val evaluator = AlertEvaluator(repo, planStore)
        val notices1 = evaluator.evaluate()
        assertEquals(1, notices1.size)
        assertEquals(100, notices1.first().thresholdPercent)

        // Deduplication: re-evaluating does not alert repeatedly
        val notices2 = evaluator.evaluate()
        assertTrue(notices2.isEmpty())

        // 2. Widget data check: WidgetData says over (fractionUsed > 1, remaining = 0, caption = "No data left")
        val usageRepo = FakeUsageRepo()
        val settingsRepo = FakeSettingsRepository()
        val snapshot = WidgetData.load(usageRepo, repo, settingsRepo, clock)

        val widgetPlan = snapshot.plan
        assertNotNull(widgetPlan)
        assertEquals(11_000_000_000L, widgetPlan!!.usedBytes)
        assertEquals(10_000_000_000L, widgetPlan.effectiveCapBytes)
        assertTrue(widgetPlan.usedBytes > widgetPlan.effectiveCapBytes)
        assertEquals(0L, widgetPlan.remainingBytes)
        assertTrue(widgetPlan.fractionUsed > 1.0f)

        val heroModel = heroWidgetModel(snapshot)
        assertEquals("No data left", heroModel.caption)
    }
}
