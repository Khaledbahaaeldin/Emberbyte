package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.data.alert.AlertEvaluator
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.AlertRule
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.NetworkKind
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.usage.MinuteTotal
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanAlertDormantTest {
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val store = InMemoryPlanStore()
    private val usage = InMemoryUsageStore()

    private fun repo(clockInstant: Instant = now) =
        RoomPlanRepository(store, usage, Clock.fixed(clockInstant, ZoneOffset.UTC), ticks = flowOf(clockInstant))

    private fun evaluator(clockInstant: Instant = now) =
        AlertEvaluator(repo(clockInstant), store)

    private suspend fun use(bytes: Long, at: String = "2026-10-14T10:00:00Z") {
        usage.addMinute(MinuteTotal(Instant.parse(at), NetworkKind.MOBILE, 1, bytes, 0))
    }

    @Test
    fun dormant_plan_never_alerts_even_when_measured_bytes_exceed_the_cap() = runBlocking {
        val nextMonth = Instant.parse("2026-11-01T00:00:00Z")
        val plan = Plan(
            id = 0,
            name = "Next Cycle Plan",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
            rollover = Rollover.None,
            firstCycle = FirstCycle.StartsNextCycle(startsAt = nextMonth),
        )
        val planId = store.upsertPlan(plan)
        store.upsertAlertRule(AlertRule(0, planId, 50, enabled = true))
        store.upsertAlertRule(AlertRule(0, planId, 80, enabled = true))
        store.upsertAlertRule(AlertRule(0, planId, 100, enabled = true))

        use(12_000_000_000L) // Exceeds cap

        val notices = evaluator(now).evaluate()
        assertTrue(notices.isEmpty())
    }

    @Test
    fun plan_alert_resumes_on_the_first_evaluation_after_startsAt() = runBlocking {
        val nextMonth = Instant.parse("2026-11-01T00:00:00Z")
        val plan = Plan(
            id = 0,
            name = "Next Cycle Plan",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
            rollover = Rollover.None,
            firstCycle = FirstCycle.StartsNextCycle(startsAt = nextMonth),
        )
        val planId = store.upsertPlan(plan)
        store.upsertAlertRule(AlertRule(0, planId, 50, enabled = true))
        store.upsertAlertRule(AlertRule(0, planId, 80, enabled = true))
        store.upsertAlertRule(AlertRule(0, planId, 100, enabled = true))

        val evaluationTime = Instant.parse("2026-11-02T12:00:00Z")
        use(6_000_000_000L, at = "2026-11-02T10:00:00Z")

        val notices = evaluator(evaluationTime).evaluate()
        assertEquals(1, notices.size)
        assertEquals(50, notices.first().thresholdPercent)
        assertEquals(planId, notices.first().planId)
    }

    @Test
    fun entered_plan_over_cap_on_creation_alerts_once() = runBlocking {
        val cycleStart = Instant.parse("2026-10-01T00:00:00Z")
        val enteredAt = Instant.parse("2026-10-15T10:00:00Z")
        val plan = Plan(
            id = 0,
            name = "Overcap Plan",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
            rollover = Rollover.None,
            firstCycle = FirstCycle.Entered(
                usedBytes = 11_000_000_000L,
                enteredAt = enteredAt,
                cycleStart = cycleStart,
            ),
        )
        val planId = store.upsertPlan(plan)
        store.upsertAlertRule(AlertRule(0, planId, 50, enabled = true))
        store.upsertAlertRule(AlertRule(0, planId, 80, enabled = true))
        store.upsertAlertRule(AlertRule(0, planId, 100, enabled = true))

        val notices1 = evaluator(now).evaluate()
        assertEquals(1, notices1.size)
        assertEquals(100, notices1.first().thresholdPercent)

        // Does not alert repeatedly (existing deduplication key applies)
        val notices2 = evaluator(now.plusSeconds(60)).evaluate()
        assertTrue(notices2.isEmpty())
    }
}
