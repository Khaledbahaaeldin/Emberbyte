package io.github.khaledbahaaeldin.emberbyte.engine.plan

import io.github.khaledbahaaeldin.emberbyte.engine.forecast.ForecastEngine
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FreeRule
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstCycleTest {
    private val cairo = ZoneId.of("Africa/Cairo")
    private val newYork = ZoneId.of("America/New_York")

    private fun basePlan(
        firstCycle: FirstCycle = FirstCycle.PhoneRecord,
        cap: Long = 10_000_000_000L,
        zone: ZoneId = cairo,
    ) = Plan(
        id = 1L,
        name = "Primary",
        subscriptionId = null,
        capBytes = cap,
        cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, zone),
        rollover = Rollover.None,
        firstCycle = firstCycle,
    )

    private fun slice(start: String, end: String, bytes: Long, pkg: String? = null) = UsageSlice(
        start = Instant.parse(start),
        end = Instant.parse(end),
        packageName = pkg,
        bytes = bytes,
    )

    @Test
    fun phone_record_behaves_exactly_as_before() {
        val plan = basePlan(FirstCycle.PhoneRecord)
        val at = Instant.parse("2026-10-15T12:00:00Z")
        val usage = listOf(
            slice("2026-10-02T10:00:00Z", "2026-10-02T11:00:00Z", 2_000_000_000L),
            slice("2026-10-10T10:00:00Z", "2026-10-10T11:00:00Z", 1_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(3_000_000_000L, state.usedBytes)
        assertEquals(7_000_000_000L, state.remainingBytes)
        assertFalse(state.isDormant)
        assertNull(state.startsAt)
        assertNull(state.enteredBytes)
    }

    @Test
    fun entered_adds_to_measured_bytes_after_enteredAt() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-15T12:00:00Z"))
        val enteredAt = Instant.parse("2026-10-10T12:00:00Z")
        val plan = basePlan(FirstCycle.Entered(usedBytes = 4_000_000_000L, enteredAt = enteredAt, cycleStart = window.start))
        val at = Instant.parse("2026-10-15T12:00:00Z")
        val usage = listOf(
            slice("2026-10-05T10:00:00Z", "2026-10-05T11:00:00Z", 2_000_000_000L), // before enteredAt: ignored
            slice("2026-10-12T10:00:00Z", "2026-10-12T11:00:00Z", 1_500_000_000L), // after enteredAt: measured
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(4_000_000_000L + 1_500_000_000L, state.usedBytes)
        assertEquals(4_000_000_000L, state.enteredBytes)
        assertFalse(state.isDormant)
    }

    @Test
    fun bytes_measured_before_enteredAt_are_ignored() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-15T12:00:00Z"))
        val enteredAt = Instant.parse("2026-10-10T00:00:00Z")
        val plan = basePlan(FirstCycle.Entered(usedBytes = 2_000_000_000L, enteredAt = enteredAt, cycleStart = window.start))
        val at = Instant.parse("2026-10-15T12:00:00Z")
        val usage = listOf(
            slice("2026-10-08T00:00:00Z", "2026-10-09T00:00:00Z", 5_000_000_000L), // before enteredAt
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(2_000_000_000L, state.usedBytes)
    }

    @Test
    fun entered_above_the_cap_is_over_cap_and_remaining_is_zero_not_negative() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-15T12:00:00Z"))
        val plan = basePlan(
            firstCycle = FirstCycle.Entered(usedBytes = 12_000_000_000L, enteredAt = Instant.parse("2026-10-10T00:00:00Z"), cycleStart = window.start),
            cap = 10_000_000_000L,
        )
        val state = PlanCalculator.state(plan, Instant.parse("2026-10-15T12:00:00Z"), emptyList(), emptyList(), emptyList(), 0L, false)
        assertEquals(12_000_000_000L, state.usedBytes)
        assertEquals(0L, state.remainingBytes)
        assertTrue(state.fractionUsed > 1.0f)
    }

    @Test
    fun entered_at_the_first_minute_of_the_cycle_equals_phone_record_plus_entered() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-15T12:00:00Z"))
        val plan = basePlan(FirstCycle.Entered(usedBytes = 1_000_000_000L, enteredAt = window.start, cycleStart = window.start))
        val at = Instant.parse("2026-10-15T12:00:00Z")
        val usage = listOf(
            slice("2026-10-05T10:00:00Z", "2026-10-05T11:00:00Z", 2_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        val phoneRecordState = PlanCalculator.state(basePlan(FirstCycle.PhoneRecord), at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(phoneRecordState.usedBytes + 1_000_000_000L, state.usedBytes)
    }

    @Test
    fun entered_for_a_previous_cycle_is_ignored_in_the_next_cycle() {
        val previousCycleStart = Instant.parse("2026-09-01T00:00:00Z")
        val atInNextCycle = Instant.parse("2026-10-15T12:00:00Z")
        val plan = basePlan(FirstCycle.Entered(usedBytes = 5_000_000_000L, enteredAt = Instant.parse("2026-09-10T00:00:00Z"), cycleStart = previousCycleStart))
        val usage = listOf(
            slice("2026-10-05T10:00:00Z", "2026-10-05T11:00:00Z", 2_000_000_000L),
        )
        val state = PlanCalculator.state(plan, atInNextCycle, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(2_000_000_000L, state.usedBytes)
        assertNull(state.enteredBytes)
    }

    @Test
    fun entered_zero_equals_phone_record_after_enteredAt() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-15T12:00:00Z"))
        val enteredAt = Instant.parse("2026-10-10T00:00:00Z")
        val plan = basePlan(FirstCycle.Entered(usedBytes = 0L, enteredAt = enteredAt, cycleStart = window.start))
        val at = Instant.parse("2026-10-15T12:00:00Z")
        val usage = listOf(
            slice("2026-10-05T10:00:00Z", "2026-10-05T11:00:00Z", 1_000_000_000L),
            slice("2026-10-12T10:00:00Z", "2026-10-12T11:00:00Z", 2_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(2_000_000_000L, state.usedBytes)
    }

    @Test
    fun every_n_days_cycle_entered_uses_the_window_start() {
        val anchor = LocalDate.of(2026, 10, 1)
        val cycle = Cycle.EveryNDays(14, anchor, LocalTime.MIDNIGHT, cairo)
        val at = Instant.parse("2026-10-08T12:00:00Z")
        val window = CycleCalculator.windowAt(cycle, at)
        val enteredAt = Instant.parse("2026-10-05T12:00:00Z")
        val plan = Plan(1L, "N-Days", null, 10_000_000_000L, cycle, Rollover.None, firstCycle = FirstCycle.Entered(3_000_000_000L, enteredAt, window.start))
        val usage = listOf(
            slice("2026-10-06T10:00:00Z", "2026-10-06T11:00:00Z", 1_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(4_000_000_000L, state.usedBytes)
    }

    @Test
    fun dst_change_inside_the_cycle_keeps_the_window_start_and_the_sum() {
        // America/New_York transitions from EDT to EST on 2026-11-01
        val at = Instant.parse("2026-11-05T12:00:00Z")
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, newYork), at)
        val enteredAt = Instant.parse("2026-11-02T12:00:00Z")
        val plan = Plan(1L, "NY", null, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, newYork), Rollover.None, firstCycle = FirstCycle.Entered(2_000_000_000L, enteredAt, window.start))
        val usage = listOf(
            slice("2026-11-03T10:00:00Z", "2026-11-03T11:00:00Z", 1_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertEquals(3_000_000_000L, state.usedBytes)
        assertEquals(window.start, state.window.start)
    }

    @Test
    fun free_windows_apply_only_to_measured_bytes_not_to_the_entered_amount() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-15T12:00:00Z"))
        val enteredAt = Instant.parse("2026-10-10T12:00:00Z")
        val plan = basePlan(FirstCycle.Entered(usedBytes = 5_000_000_000L, enteredAt = enteredAt, cycleStart = window.start))
        val at = Instant.parse("2026-10-15T12:00:00Z")
        // Free rule for YouTube all week
        val freeRules = listOf(
            FreeRule(1L, 1L, "Night free", DayOfWeek.entries.toSet(), LocalTime.of(0, 0), LocalTime.of(23, 59), "com.google.android.youtube"),
        )
        val usage = listOf(
            slice("2026-10-12T10:00:00Z", "2026-10-12T11:00:00Z", 2_000_000_000L, pkg = null),
            slice("2026-10-12T10:00:00Z", "2026-10-12T11:00:00Z", 2_000_000_000L, pkg = "com.google.android.youtube"),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), freeRules, 0L, false)
        // Free bytes = 2 GB, measured bytes = 2 GB, net measured = 0 GB
        assertEquals(5_000_000_000L, state.usedBytes)
        assertEquals(2_000_000_000L, state.freeBytes)
    }

    @Test
    fun starts_next_cycle_is_dormant_with_zero_used_no_forecast_and_startsAt_set() {
        val startsAt = Instant.parse("2026-11-01T00:00:00Z")
        val plan = basePlan(FirstCycle.StartsNextCycle(startsAt = startsAt))
        val at = Instant.parse("2026-10-15T12:00:00Z")
        val usage = listOf(
            slice("2026-10-05T10:00:00Z", "2026-10-05T11:00:00Z", 5_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertTrue(state.isDormant)
        assertEquals(startsAt, state.startsAt)
        assertEquals(0L, state.usedBytes)
        assertEquals(plan.capBytes, state.remainingBytes)
        assertEquals(0f, state.fractionUsed, 0.001f)
    }

    @Test
    fun starts_next_cycle_created_on_the_last_day_becomes_active_tomorrow() {
        val startsAt = Instant.parse("2026-11-01T00:00:00Z")
        val plan = basePlan(FirstCycle.StartsNextCycle(startsAt = startsAt))
        val atBefore = Instant.parse("2026-10-31T23:59:00Z")
        val atAfter = Instant.parse("2026-11-01T00:01:00Z")
        val stateBefore = PlanCalculator.state(plan, atBefore, emptyList(), emptyList(), emptyList(), 0L, false)
        val stateAfter = PlanCalculator.state(plan, atAfter, emptyList(), emptyList(), emptyList(), 0L, false)
        assertTrue(stateBefore.isDormant)
        assertFalse(stateAfter.isDormant)
    }

    @Test
    fun starts_next_cycle_after_startsAt_behaves_as_phone_record_and_is_not_dormant() {
        val startsAt = Instant.parse("2026-11-01T00:00:00Z")
        val plan = basePlan(FirstCycle.StartsNextCycle(startsAt = startsAt))
        val at = Instant.parse("2026-11-10T12:00:00Z")
        val usage = listOf(
            slice("2026-11-05T10:00:00Z", "2026-11-05T11:00:00Z", 3_000_000_000L),
        )
        val state = PlanCalculator.state(plan, at, usage, emptyList(), emptyList(), 0L, false)
        assertFalse(state.isDormant)
        assertEquals(3_000_000_000L, state.usedBytes)
    }

    @Test
    fun forecast_for_entered_uses_the_elapsed_time_since_cycle_start_not_since_enteredAt_for_the_entered_part() {
        val window = CycleCalculator.windowAt(Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, cairo), Instant.parse("2026-10-11T00:00:00Z"))
        // Cycle started 2026-10-01 (10 days ago). Entered yesterday (2026-10-10, 1 day ago) with 10 GB.
        val enteredAt = Instant.parse("2026-10-10T00:00:00Z")
        val plan = basePlan(FirstCycle.Entered(usedBytes = 10_000_000_000L, enteredAt = enteredAt, cycleStart = window.start), cap = 20_000_000_000L)
        val now = Instant.parse("2026-10-11T00:00:00Z")
        val state = PlanCalculator.state(plan, now, emptyList(), emptyList(), emptyList(), 0L, false)

        val forecast = ForecastEngine.forecast(state, emptyList(), now, cairo)
        // Rate across 10 days since cycle start is ~1 GB/day.
        // Remaining 10 GB at 1 GB/day runs out in about 10 days (around Oct 21).
        // If it used time since enteredAt (1 day), rate would be 10 GB/day and run out in 1 day (Oct 12).
        assertNotNull(forecast.runOutExpected)
        val runOutDay = forecast.runOutExpected!!.atZone(cairo).dayOfMonth
        assertTrue("Expected run out around Oct 21 but got day $runOutDay", runOutDay in 19..23)
    }
}
