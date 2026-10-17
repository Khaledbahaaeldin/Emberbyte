package io.github.khaledbahaaeldin.emberbyte.engine.plan

import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlanUrgencyTest {
    private val utc = ZoneOffset.UTC
    private val cycleStart = Instant.parse("2026-10-01T00:00:00Z")
    private val cycleEnd = Instant.parse("2026-11-01T00:00:00Z")
    private val window = CycleWindow(cycleStart, cycleEnd)
    private val now = Instant.parse("2026-10-15T00:00:00Z") // 14 days elapsed

    private fun state(
        id: Long,
        cap: Long = 10_000_000_000L,
        used: Long = 1_000_000_000L,
        pinned: Boolean = false,
        isDormant: Boolean = false,
    ): PlanState {
        val remaining = maxOf(0L, cap - used)
        val plan = Plan(
            id = id,
            name = "Plan $id",
            subscriptionId = null,
            capBytes = cap,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc),
            rollover = Rollover.None,
            pinned = pinned,
        )
        return PlanState(
            plan = plan,
            window = window,
            effectiveCapBytes = cap,
            usedBytes = used,
            remainingBytes = remaining,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            daysLeft = 17,
            fractionUsed = if (cap > 0L) used.toFloat() / cap else 0f,
            isApproximate = false,
            isDormant = isDormant,
        )
    }

    @Test
    fun pinned_active_plan_leads() {
        val urgent = state(id = 1L, cap = 10_000_000_000L, used = 9_500_000_000L) // high usage
        val pinned = state(id = 2L, cap = 10_000_000_000L, used = 1_000_000_000L, pinned = true)
        val front = PlanUrgency.front(listOf(urgent, pinned), now)
        assertEquals(2L, front?.plan?.id)
    }

    @Test
    fun pinned_dormant_plan_does_not_lead() {
        val dormantPinned = state(id = 1L, cap = 10_000_000_000L, used = 0L, pinned = true, isDormant = true)
        val active = state(id = 2L, cap = 10_000_000_000L, used = 2_000_000_000L, pinned = false, isDormant = false)
        val front = PlanUrgency.front(listOf(dormantPinned, active), now)
        assertEquals(2L, front?.plan?.id)
    }

    @Test
    fun over_cap_beats_run_out_beats_fraction_beats_id() {
        // Now is 14 days into 31-day cycle (Oct 15).
        // 1. Over cap: cap 10 GB, used 11 GB
        val overCap = state(id = 10L, cap = 10_000_000_000L, used = 11_000_000_000L)

        // 2. Projected run out: cap 10 GB, used 7 GB in 14 days (0.5 GB/day).
        // Remaining 3 GB runs out in 6 days (Oct 21 < Nov 1 cycle end).
        val runsOut = state(id = 20L, cap = 10_000_000_000L, used = 7_000_000_000L)

        // 3. High fraction, won't run out: cap 10 GB, used 2 GB in 14 days (~0.14 GB/day).
        // Remaining 8 GB lasts 56 days (> 17 days left in cycle). Fraction = 0.2
        val highFraction = state(id = 30L, cap = 10_000_000_000L, used = 2_000_000_000L)

        // 4. Lower fraction, won't run out: cap 10 GB, used 1 GB in 14 days. Fraction = 0.1
        val lowFraction = state(id = 40L, cap = 10_000_000_000L, used = 1_000_000_000L)

        // 5. Same fraction as lowFraction, higher id
        val lowFractionHigherId = state(id = 50L, cap = 10_000_000_000L, used = 1_000_000_000L)

        val ordered = PlanUrgency.order(
            listOf(lowFractionHigherId, highFraction, overCap, lowFraction, runsOut),
            now,
        )
        val orderedIds = ordered.map { it.plan.id }
        assertEquals(listOf(10L, 20L, 30L, 40L, 50L), orderedIds)
    }

    @Test
    fun all_dormant_gives_null_front() {
        val d1 = state(id = 1L, isDormant = true)
        val d2 = state(id = 2L, isDormant = true)
        val front = PlanUrgency.front(listOf(d1, d2), now)
        assertNull(front)
    }

    @Test
    fun order_puts_front_first_active_next_dormant_last() {
        val active1 = state(id = 1L, cap = 10_000_000_000L, used = 2_000_000_000L)
        val activeUrgent = state(id = 2L, cap = 10_000_000_000L, used = 9_000_000_000L)
        val dormant1 = state(id = 3L, isDormant = true)
        val dormant2 = state(id = 4L, isDormant = true)

        val ordered = PlanUrgency.order(listOf(dormant2, active1, dormant1, activeUrgent), now)
        assertEquals(listOf(2L, 1L, 3L, 4L), ordered.map { it.plan.id })
    }

    @Test
    fun ties_are_stable_by_id() {
        val a = state(id = 5L, cap = 10_000_000_000L, used = 1_000_000_000L)
        val b = state(id = 3L, cap = 10_000_000_000L, used = 1_000_000_000L)
        val c = state(id = 8L, cap = 10_000_000_000L, used = 1_000_000_000L)

        val ordered = PlanUrgency.order(listOf(a, c, b), now)
        assertEquals(listOf(3L, 5L, 8L), ordered.map { it.plan.id })
    }
}
