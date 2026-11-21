package io.github.khaledbahaaeldin.emberbyte.history

import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryStatsTest {
    private val utc = ZoneOffset.UTC

    private fun point(day: LocalDate, total: Long, mobile: Long = 0L) =
        UsagePoint(day.atStartOfDay(utc).toInstant(), mobile, total - mobile)

    private fun day(date: LocalDate, total: Long, mobile: Long = 0L) = DayUsage(date, total, mobile, total - mobile)

    private val friday = LocalDate.of(2026, 11, 20)

    @Test
    fun fair_comparison_uses_the_same_elapsed_days_for_a_partial_week() {
        // Friday is the 5th day of the week: this week so far 500 against 7 days last week of 1400 -> 5/7 = 1000.
        val points = listOf(point(friday.minusDays(11), 1_400), point(friday.minusDays(4), 500))
        val result = fairComparison(Granularity.WEEK, points, friday, utc)!!
        assertEquals(TrendDirection.DOWN, result.direction)
        assertEquals("\u25BC 50% less than the same days last week", result.text)
    }

    @Test
    fun fair_comparison_for_daily_compares_seven_days_with_the_seven_before() {
        val points = (0 until 14).map { point(friday.minusDays((13 - it).toLong()), if (it < 7) 100 else 112) }
        val result = fairComparison(Granularity.DAY, points, friday, utc)!!
        assertEquals(TrendDirection.UP, result.direction)
        assertEquals("\u25B2 12% more than the 7 days before", result.text)
    }

    @Test
    fun fair_comparison_is_null_without_previous_data() {
        assertNull(fairComparison(Granularity.WEEK, listOf(point(friday, 500)), friday, utc))
        assertNull(fairComparison(Granularity.DAY, (0 until 7).map { point(friday.minusDays(it.toLong()), 5) }, friday, utc))
        assertNull(fairComparison(Granularity.MONTH, listOf(point(friday.withDayOfMonth(1).minusMonths(1), 0), point(friday, 90)), friday, utc))
    }

    @Test
    fun fair_comparison_says_about_the_same_under_3_percent() {
        val points = listOf(point(friday.minusDays(11), 1_400), point(friday.minusDays(4), 1_010))
        val result = fairComparison(Granularity.WEEK, points, friday, utc)!!
        assertEquals(TrendDirection.FLAT, result.direction)
        assertEquals("About the same as the same days last week", result.text)
    }

    @Test
    fun weekend_lift_needs_14_days() {
        val short = (0 until 10).map { day(friday.minusDays(it.toLong()), 100) }
        assertNull(weekendLift(short))
        val two = LocalDate.of(2026, 11, 2) // a Monday
        val rows = (0 until 28).map { i ->
            val d = two.plusDays(i.toLong())
            day(d, if (d.dayOfWeek.value >= 6) 150 else 100)
        }
        assertEquals(0.5f, weekendLift(rows)!!, 1e-4f)
    }

    @Test
    fun weekday_averages_start_on_monday() {
        val monday = LocalDate.of(2026, 11, 2)
        val rows = (0 until 14).map { day(monday.plusDays(it.toLong()), (it % 7 + 1) * 10L) }
        val averages = weekdayAverages(rows)
        assertEquals(7, averages.size)
        assertEquals(10L, averages[0])
        assertEquals(70L, averages[6])
    }

    @Test
    fun calendar_intensity_is_clamped_between_018_and_1() {
        assertEquals(0.18f, calendarIntensity(0, 100), 1e-6f)
        assertEquals(1f, calendarIntensity(100, 100), 1e-6f)
        assertEquals(0.18f, calendarIntensity(10, 0), 1e-6f)
        assertTrue(calendarIntensity(50, 100) in 0.18f..1f)
        assertEquals(1f, calendarIntensity(500, 100), 1e-6f)
    }

    @Test
    fun mobile_share_series_handles_zero_buckets() {
        val d = friday
        val series = mobileShareSeries(listOf(point(d, 100, 25), point(d.plusDays(1), 0), point(d.plusDays(2), 200, 100)))
        assertEquals(listOf(0.25f, 0f, 0.5f), series)
    }

    @Test
    fun longest_run_under_counts_consecutive_days() {
        val rows = listOf(100L, 50L, 40L, 30L, 200L, 10L, 20L).mapIndexed { i, t -> day(friday.minusDays((6 - i).toLong()), t) }
        assertEquals(3, longestRunUnder(rows, threshold = 60L))
        assertEquals(0, longestRunUnder(rows, threshold = 5L))
    }

    @Test
    fun records_find_busiest_lightest_and_peak_hour() {
        val rows = listOf(day(friday.minusDays(2), 300), day(friday.minusDays(1), 20), day(friday, 150))
        val hours = LongArray(24).also { it[21] = 9; it[3] = 1 }
        val r = records(rows, hours)
        assertEquals(friday.minusDays(2), r.busiest?.date)
        assertEquals(friday.minusDays(1), r.lightest?.date)
        assertEquals(21, r.peakHour)
        assertNull(records(emptyList(), LongArray(24)).busiest)
        assertNull(records(rows, LongArray(24)).peakHour)
    }

    @Test
    fun versus_average_says_near_within_5_percent() {
        assertEquals(VersusAverage.Near, versusAverage(total = 103, days = 1, dailyAverage = 100).kind)
        val above = versusAverage(total = 130, days = 1, dailyAverage = 100)
        assertEquals(VersusAverage.Above, above.kind)
        assertEquals(30, above.percent)
        val below = versusAverage(total = 140, days = 2, dailyAverage = 100)
        assertEquals(VersusAverage.Below, below.kind)
        assertEquals(30, below.percent)
        assertEquals(VersusAverage.Near, versusAverage(5, 1, 0).kind)
    }

    @Test
    fun top_apps_include_change_against_the_previous_period() {
        val now = listOf(
            AppUsage("a", "A", 1, 0, 600, null), AppUsage("b", "B", 2, 0, 300, null), AppUsage("c", "C", 3, 0, 100, null),
        )
        val before = listOf(AppUsage("a", "A", 1, 0, 400, null), AppUsage("b", "B", 2, 0, 300, null))
        val top = topAppsWithChange(now, before, limit = 2)
        assertEquals(listOf("a", "b"), top.map { it.packageName })
        assertEquals(60, top[0].percentOfAll)
        assertEquals(0.5f, top[0].change!!, 1e-4f)
        assertEquals(0f, top[1].change!!, 1e-4f)
        assertNull(topAppsWithChange(now, emptyList(), 3)[2].change)
    }

    @Test
    fun bars_carry_their_mobile_part_so_the_chart_can_stack_them() {
        val bars = historyBars(listOf(point(friday, 1_000, mobile = 250)), Granularity.DAY, io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits.DECIMAL, utc, java.util.Locale.ENGLISH)
        assertEquals(250L, bars.single().mobileBytes)
        assertEquals(1_000L, bars.single().bytes)
    }

    private fun planState(cap: Long, used: Long, start: LocalDate, end: LocalDate): io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState {
        val plan = io.github.khaledbahaaeldin.emberbyte.engine.model.Plan(
            id = 1, name = "Main", subscriptionId = null, capBytes = cap,
            cycle = io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle.MonthlyOnDay(1, java.time.LocalTime.MIDNIGHT, utc),
            rollover = io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover.None,
        )
        return io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState(
            plan = plan,
            window = io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow(start.atStartOfDay(utc).toInstant(), end.atStartOfDay(utc).toInstant()),
            effectiveCapBytes = cap, usedBytes = used, remainingBytes = cap - used, rolledOverBytes = 0, addOnBytes = 0,
            freeBytes = 0, daysLeft = 0, fractionUsed = used.toFloat() / cap, isApproximate = false,
        )
    }

    private fun forecast(projected: Long) = io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast(
        planId = 1, runOutEarliest = null, runOutExpected = null, runOutLatest = null,
        projectedCycleEndBytes = projected, confidence = io.github.khaledbahaaeldin.emberbyte.engine.model.Confidence.MEDIUM, historyDays = 20,
    )

    @Test
    fun cycle_pace_projects_the_end_of_the_cycle() {
        val start = LocalDate.of(2026, 11, 1)
        val rows = (0..19).map { day(start.plusDays(it.toLong()), 400L, mobile = 225L) } // 20 days, 4.5 GB in MB units
        val pace = cyclePace(planState(8_000L, 4_500L, start, start.plusDays(30)), forecast(9_100L), rows, start.plusDays(19), utc)!!
        assertEquals(20, pace.dayOfCycle)
        assertEquals(30, pace.cycleDays)
        assertEquals(20, pace.cumulative.size)
        assertEquals(4_500L, pace.cumulative.last())
        assertEquals(9_100L, pace.projectedEndBytes)
        assertTrue(pace.willPass)
        assertTrue("passes before the cycle ends (day ${pace.passDay})", pace.passDay!! in 20..29)
        assertTrue(pace.text, pace.text.contains("will pass the plan around day"))
    }

    @Test
    fun cycle_pace_says_under_when_the_projection_fits() {
        val start = LocalDate.of(2026, 11, 1)
        val rows = (0..9).map { day(start.plusDays(it.toLong()), 100L, mobile = 50L) }
        val pace = cyclePace(planState(8_000L, 500L, start, start.plusDays(30)), forecast(1_500L), rows, start.plusDays(9), utc)!!
        assertEquals(false, pace.willPass)
        assertNull(pace.passDay)
        assertTrue(pace.text, pace.text.contains("under the plan"))
    }

    @Test
    fun cycle_pace_is_null_without_a_plan() {
        assertNull(cyclePace(null, null, emptyList(), friday, utc))
        assertNull(cyclePace(planState(0L, 0L, friday, friday.plusDays(30)), null, emptyList(), friday, utc))
    }
}
