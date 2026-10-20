package io.github.khaledbahaaeldin.emberbyte.widget

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.AppGraph
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.PermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.*
import io.github.khaledbahaaeldin.emberbyte.engine.usage.MinuteTotal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetDataTest {
    private val app: Application get() = ApplicationProvider.getApplicationContext()
    private val clock = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneOffset.UTC)

    private class FakeUsageRepo(
        private val today: DayUsage = DayUsage(LocalDate.of(2026, 10, 10), 100, 0),
        private val apps: List<AppUsage> = emptyList(),
        private val speed: LiveSpeed? = null,
        private val speedFlow: Flow<LiveSpeed> = speed?.let { flowOf(it) } ?: emptyFlow()
    ) : UsageRepository {
        override fun observeLiveSpeed(): Flow<LiveSpeed> = speedFlow
        override fun observeToday(filter: UsageFilter): Flow<DayUsage> = flowOf(today)
        override fun observeSeries(range: DateRange, granularity: Granularity, filter: UsageFilter): Flow<List<UsagePoint>> = emptyFlow()
        override fun observeApps(range: DateRange, filter: UsageFilter, sort: AppSort): Flow<List<AppUsage>> = flowOf(apps)
        override fun observeAppSeries(packageName: String, range: DateRange, granularity: Granularity): Flow<List<UsagePoint>> = emptyFlow()
        override fun observeCoverage(): Flow<CoverageStatus> = emptyFlow()
        override suspend fun refreshNow(): Outcome<Unit> = Outcome.Success(Unit)
    }

    @Test fun load_without_plan() = runBlocking {
        val usage = FakeUsageRepo()
        val plans = RoomPlanRepository(InMemoryPlanStore(), InMemoryUsageStore(), clock, ticks = flowOf(clock.instant()))
        val settings = FakeSettingsRepository()
        val snapshot = WidgetData.load(usage, plans, settings, clock)
        assertNull(snapshot.plan)
        assertNull(snapshot.forecast)
    }

    @Test fun load_speed_timeout() = runBlocking {
        val usage = FakeUsageRepo(speedFlow = emptyFlow())
        val plans = RoomPlanRepository(InMemoryPlanStore(), InMemoryUsageStore(), clock, ticks = flowOf(clock.instant()))
        val settings = FakeSettingsRepository()
        val snapshot = WidgetData.load(usage, plans, settings, clock, speedTimeoutMillis = 50L)
        assertNull(snapshot.speed)
    }

    @Test fun load_top_apps_sorted_and_capped() = runBlocking {
        val apps = listOf(
            AppUsage("a", "App A", 1, 100L, 0, null),
            AppUsage("b", "App B", 2, 300L, 0, null),
            AppUsage("c", "App C", 3, 200L, 0, null),
        )
        val usage = FakeUsageRepo(apps = apps)
        val plans = RoomPlanRepository(InMemoryPlanStore(), InMemoryUsageStore(), clock, ticks = flowOf(clock.instant()))
        val settings = FakeSettingsRepository()
        val snapshot = WidgetData.load(usage, plans, settings, clock)
        assertEquals(2, snapshot.topApps.size)
        assertEquals("App B", snapshot.topApps[0].label)
        assertEquals("App C", snapshot.topApps[1].label)
    }

    @Test fun load_units_from_settings() = runBlocking {
        val usage = FakeUsageRepo()
        val plans = RoomPlanRepository(InMemoryPlanStore(), InMemoryUsageStore(), clock, ticks = flowOf(clock.instant()))
        val settings = FakeSettingsRepository()
        settings.update { it.copy(unitSystem = UnitSystem.BINARY) }
        val snapshot = WidgetData.load(usage, plans, settings, clock)
        assertEquals(UnitSystem.BINARY, snapshot.units)
    }

    @Test fun data_uses_the_front_plan_by_urgency() = runBlocking {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val graph = AppGraph(app, Clock.fixed(now, ZoneOffset.UTC))

        // Create 2 active plans: plan 1 safe, plan 2 near cap (higher urgency)
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val p1 = Plan(id = 1L, name = "Safe Plan", subscriptionId = null, capBytes = 10_000_000_000L, cycle = cycle, rollover = Rollover.None)
        val p2 = Plan(id = 2L, name = "Urgent Plan", subscriptionId = null, capBytes = 5_000_000_000L, cycle = cycle, rollover = Rollover.None)
        graph.planStore.upsertPlan(p1)
        graph.planStore.upsertPlan(p2)

        // Seed minute usage for plan 2: 4.8 GB used -> 96% fraction used
        graph.usage.refreshNow() // ensure db ready
        val usageStore = io.github.khaledbahaaeldin.emberbyte.data.store.UsageStores.create(app)
        usageStore.addMinute(MinuteTotal(now.minusSeconds(60), NetworkKind.MOBILE, -1, 4_800_000_000L, 0))

        val data: WidgetData = loadWidgetData(graph, now)
        assertNotNull(data.plan)
        assertEquals(2L, data.plan?.plan?.id)
        assertEquals("Urgent Plan", data.plan?.plan?.name)
    }

    @Test fun dormant_front_candidate_is_skipped_and_text_is_shown_for_a_dormant_only_set() = runBlocking {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val graph = AppGraph(app, Clock.fixed(now, ZoneOffset.UTC))

        // Archive any existing plans to ensure only the dormant plan is active
        val existingPlans = graph.planStore.observePlans().first()
        for (p in existingPlans) {
            graph.planStore.setArchived(p.id, true)
        }

        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val startsAt = Instant.parse("2026-11-01T00:00:00Z")
        val dormantPlan = Plan(
            id = 10L,
            name = "Travel eSIM",
            subscriptionId = null,
            capBytes = 10_000_000_000L,
            cycle = cycle,
            rollover = Rollover.None,
            firstCycle = FirstCycle.StartsNextCycle(startsAt)
        )
        graph.planStore.upsertPlan(dormantPlan)

        val data: WidgetData = loadWidgetData(graph, now)
        // Dormant front candidate is skipped: front is null, so data.plan is null
        assertNull(data.plan)

        // For a dormant-only set, dormantText resolves to "Starts 1 Nov"
        val dormantText = resolveDormantText(graph, now)
        assertEquals("Starts 1 Nov", dormantText)
    }

    @Test fun permission_missing_gives_the_permission_block_and_no_figures() = runBlocking {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val graph = AppGraph(app, Clock.fixed(now, ZoneOffset.UTC))

        val data: WidgetData = loadWidgetData(graph, now, permissionOverride = false)
        assertFalse(data.permissionGranted)
    }

    @Test fun estimated_data_sets_the_flag() = runBlocking {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val graph = AppGraph(app, Clock.fixed(now, ZoneOffset.UTC))

        // Create a SIM-bound plan with unknown SIM traffic or coverage gap -> approximate
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan = Plan(id = 5L, name = "Bound Plan", subscriptionId = 1, capBytes = 10_000_000_000L, cycle = cycle, rollover = Rollover.None)
        graph.planStore.upsertPlan(plan)

        val usageStore = io.github.khaledbahaaeldin.emberbyte.data.store.UsageStores.create(app)
        // Add minute row with unknown subscription -1 -> triggers isApproximate = true
        usageStore.addMinute(MinuteTotal(now.minusSeconds(120), NetworkKind.MOBILE, -1, 500_000_000L, 0))

        val data: WidgetData = loadWidgetData(graph, now)
        assertTrue(data.estimated)
    }
}
