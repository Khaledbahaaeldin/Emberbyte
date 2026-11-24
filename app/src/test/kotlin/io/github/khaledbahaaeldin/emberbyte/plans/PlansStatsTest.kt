package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlansStatsTest {
    private val zone = ZoneOffset.UTC
    private val now = Instant.parse("2026-11-15T12:00:00Z")

    private fun sampleState(
        id: Long = 1L,
        name: String = "Main",
        capBytes: Long = 10_000_000_000L,
        usedBytes: Long = 4_000_000_000L,
        daysLeft: Int = 15,
        pinned: Boolean = false,
        isDormant: Boolean = false,
        startsAt: Instant? = null,
        windowEnd: Instant = now.plusSeconds(daysLeft.toLong() * 86_400L),
    ): PlanState {
        val windowStart = now.minusSeconds(15L * 86_400L)
        val plan = Plan(
            id = id,
            name = name,
            subscriptionId = null,
            capBytes = capBytes,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, zone),
            rollover = Rollover.None,
            pinned = pinned,
        )
        return PlanState(
            plan = plan,
            window = CycleWindow(windowStart, windowEnd),
            effectiveCapBytes = capBytes,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            usedBytes = usedBytes,
            remainingBytes = capBytes - usedBytes,
            fractionUsed = usedBytes.toFloat() / capBytes.toFloat(),
            daysLeft = daysLeft,
            isApproximate = false,
            isDormant = isDormant,
            startsAt = startsAt,
        )
    }

    @Test
    fun daily_allowance_is_null_on_the_last_day() {
        assertNull(dailyAllowance(remainingBytes = 2_000_000_000L, daysLeft = 0))
        assertNull(dailyAllowance(remainingBytes = -100L, daysLeft = 5))
        assertEquals(500_000_000L, dailyAllowance(remainingBytes = 5_000_000_000L, daysLeft = 10))
    }

    @Test
    fun status_is_over_cap_when_used_reaches_the_cap() {
        assertEquals(
            PlanStatusKind.OverCap,
            statusOf(used = 10_000_000_000L, cap = 10_000_000_000L, projectedAtEnd = 12_000_000_000L, runOutDay = 15, cycleLength = 30),
        )
    }

    @Test
    fun status_is_runs_out_early_when_projected_above_cap() {
        assertEquals(
            PlanStatusKind.RunsOutEarly,
            statusOf(used = 6_000_000_000L, cap = 10_000_000_000L, projectedAtEnd = 12_000_000_000L, runOutDay = 25, cycleLength = 30),
        )
        assertEquals(
            PlanStatusKind.OnTrack,
            statusOf(used = 4_000_000_000L, cap = 10_000_000_000L, projectedAtEnd = 8_000_000_000L, runOutDay = null, cycleLength = 30),
        )
    }

    @Test
    fun summary_sums_remaining_and_picks_the_next_renewal() {
        val s1 = sampleState(id = 1L, capBytes = 10_000_000_000L, usedBytes = 4_000_000_000L, daysLeft = 10)
        val s2 = sampleState(id = 2L, capBytes = 5_000_000_000L, usedBytes = 2_200_000_000L, daysLeft = 4)
        val s3 = sampleState(id = 3L, isDormant = true, startsAt = now.plusSeconds(3L * 86_400L))
        val summary = summarize(listOf(s1, s2, s3), now, zone)!!
        assertEquals(8_800_000_000L, summary.totalRemainingBytes)
        assertEquals(2, summary.activeCount)
        assertEquals(s2.window.end, summary.nextRenewal)
        assertNull(summarize(emptyList(), now, zone))
    }

    @Test
    fun worst_plan_is_the_earliest_run_out() {
        val ok = sampleState(id = 1L, name = "Home", capBytes = 10_000_000_000L, usedBytes = 3_000_000_000L, daysLeft = 15)
        val fast = sampleState(id = 2L, name = "Work SIM", capBytes = 10_000_000_000L, usedBytes = 8_000_000_000L, daysLeft = 15)
        val worst = worstPlan(listOf(ok, fast))
        assertEquals(2L, worst?.plan?.id)
    }

    @Test
    fun renewal_events_are_sorted_and_include_upcoming_starts() {
        val s1 = sampleState(id = 1L, name = "Main", daysLeft = 10)
        val s2 = sampleState(id = 2L, name = "Travel", daysLeft = 3)
        val upcoming = sampleState(id = 3L, name = "Next", isDormant = true, startsAt = now.plusSeconds(5L * 86_400L))
        val events = renewalEvents(listOf(s1, s2, upcoming), now, zone)
        assertEquals(4, events.size) // Today + 3 plans
        assertTrue(events.first().isToday)
        for (i in 0 until events.size - 1) {
            assertTrue(events[i].daysFromToday <= events[i + 1].daysFromToday)
        }
        assertTrue(events.any { it.planId == 3L && it.isUpcoming })
    }

    @Test
    fun renewal_labels_stagger_when_closer_than_a_label_width() {
        val events = listOf(
            RenewalEvent(null, "Today", LocalDate.of(2026, 11, 15), 0, isToday = true, isUpcoming = false),
            RenewalEvent(1L, "Plan A", LocalDate.of(2026, 11, 16), 1, isToday = false, isUpcoming = false),
            RenewalEvent(2L, "Plan B", LocalDate.of(2026, 11, 25), 10, isToday = false, isUpcoming = false),
        )
        val slots = layoutRenewalLabels(events, widthPx = 300f, labelPx = 56f)
        assertEquals(3, slots.size)
        // Today (x=0) and Plan A (x=30) are closer than 56px, so they must sit on different rows
        assertNotEquals(slots[0].row, slots[1].row)
    }

    @Test
    fun front_plan_prefers_pinned_then_first_active() {
        val s1 = sampleState(id = 1L, pinned = false)
        val s2 = sampleState(id = 2L, pinned = true)
        assertEquals(2L, frontPlan(listOf(s1, s2))?.plan?.id)
        assertEquals(1L, frontPlan(listOf(s1, sampleState(id = 2L, pinned = false)))?.plan?.id)
    }

    @Test
    fun pin_only_unpins_everything_else() {
        val p1 = sampleState(id = 1L, pinned = true).plan
        val p2 = sampleState(id = 2L, pinned = false).plan
        val p3 = sampleState(id = 3L, pinned = true).plan
        val updates = pinOnly(2L, listOf(p1, p2, p3))
        assertEquals(listOf(1L to false, 2L to true, 3L to false), updates)
    }

    @Test
    fun what_if_extra_cap_can_turn_early_into_on_track() {
        // 8 GB used halfway through cycle (projected 16 GB > 10 GB cap -> RunsOutEarly). Adding +10 GB (cap 20 GB) -> OnTrack
        val s = sampleState(id = 1L, capBytes = 10_000_000_000L, usedBytes = 8_000_000_000L, daysLeft = 15)
        assertEquals(PlanStatusKind.OnTrack, withExtraCap(s, extraBytes = 10_000_000_000L))
    }

    @Test
    fun restored_notice_is_null_for_zero_and_plural_aware() {
        assertNull(restoredNotice(0))
        assertNull(restoredNotice(-1))
        val single = restoredNotice(1)!!
        val plural = restoredNotice(3)!!
        assertTrue(single.contains("1 archived plan"))
        assertTrue(plural.contains("3 archived plans"))
    }
}
