package io.github.khaledbahaaeldin.emberbyte.apps.detail

import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDetailStatsTest {
    private val utc = ZoneOffset.UTC

    private fun point(day: LocalDate, mobile: Long, wifi: Long) =
        UsagePoint(day.atStartOfDay(utc).toInstant(), mobile, wifi)

    private fun plan(cap: Long) = Plan(
        id = 1, name = "Main", subscriptionId = null, capBytes = cap,
        cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc), rollover = Rollover.None,
    )

    @Test
    fun change_is_null_without_a_previous_period() {
        assertNull(changeFraction(current = 500L, previous = 0L))
        assertEquals(0.12f, changeFraction(current = 112L, previous = 100L)!!, 1e-4f)
        assertEquals(-0.08f, changeFraction(current = 92L, previous = 100L)!!, 1e-4f)
    }

    @Test
    fun change_label_says_about_the_same_under_3_percent() {
        assertEquals(ChangeDirection.Same, changeLabel(0.02f, 7).direction)
        assertEquals("About the same as the 7 days before", changeLabel(0.02f, 7).text)
        assertEquals(ChangeDirection.Up, changeLabel(0.12f, 7).direction)
        assertEquals("\u25B2 12% more than the 7 days before", changeLabel(0.12f, 7).text)
        assertEquals(ChangeDirection.Down, changeLabel(-0.08f, 30).direction)
        assertEquals("\u25BC 8% less than the 30 days before", changeLabel(-0.08f, 30).text)
        assertEquals(ChangeDirection.None, changeLabel(null, 7).direction)
        assertEquals("First week of data", changeLabel(null, 7).text)
        assertEquals("First month of data", changeLabel(null, 30).text)
    }

    @Test
    fun busiest_day_picks_the_largest_in_the_zone() {
        val d = LocalDate.of(2026, 11, 14)
        val series = listOf(point(d.minusDays(2), 10, 10), point(d, 100, 326), point(d.plusDays(1), 5, 5))
        val best = busiestDay(series, utc)!!
        assertEquals(d, best.start.atZone(utc).toLocalDate())
        assertNull(busiestDay(emptyList(), utc))
    }

    @Test
    fun averages_and_pace() {
        assertEquals(100L, dailyAverage(700L, 7))
        assertEquals(0L, dailyAverage(10L, 0))
        assertEquals(3_000L, pace30(700L, 7))
    }

    @Test
    fun weekly_buckets_cover_90_days_in_13_aligned_buckets() {
        val end = LocalDate.of(2026, 11, 20)
        val series = (0 until 91).map { point(end.minusDays((90 - it).toLong()), 1, 1) }
        val buckets = weeklyBuckets(series, utc)
        assertEquals(13, buckets.size)
        assertTrue(buckets.all { it.totalBytes == 14L })
        assertEquals(end.minusDays(90), buckets.first().start.atZone(utc).toLocalDate())
        // Fewer days than 13 weeks still aligns to the end and never exceeds 13 buckets.
        assertEquals(2, weeklyBuckets(series.takeLast(14), utc).size)
    }

    @Test
    fun parts_of_day_sum_to_100_percent() {
        val hours = LongArray(24) { 10L }
        val parts = partsOfDay(hours)
        assertEquals(listOf("Night", "Morning", "Afternoon", "Evening"), parts.map { it.name })
        assertEquals(100, parts.sumOf { it.percent })
        val skew = LongArray(24).also { it[19] = 700; it[2] = 100; it[9] = 100; it[14] = 100 }
        val skewed = partsOfDay(skew)
        assertEquals(100, skewed.sumOf { it.percent })
        assertEquals("Evening", skewed.maxByOrNull { it.percent }!!.name)
        assertEquals(listOf(0, 0, 0, 0), partsOfDay(LongArray(24)).map { it.percent })
    }

    @Test
    fun busiest_window_finds_the_best_three_hours() {
        val hours = LongArray(24).also { it[18] = 100; it[19] = 330; it[20] = 150; it[21] = 20; it[5] = 200 }
        val window = busiestWindow(hours)
        assertEquals(18, window.startHour)
        assertEquals(3, window.width)
        assertEquals(580L, window.bytes)
        assertEquals(0, busiestWindow(LongArray(24)).bytes.toInt())
    }

    @Test
    fun hour_intensity_is_clamped_between_014_and_1() {
        assertEquals(0.14f, hourIntensity(0, 100), 1e-6f)
        assertEquals(1f, hourIntensity(100, 100), 1e-6f)
        assertEquals(0.57f, hourIntensity(50, 100), 1e-6f)
        assertEquals(0.14f, hourIntensity(10, 0), 1e-6f)
        assertEquals(1f, hourIntensity(500, 100), 1e-6f)
    }

    @Test
    fun rank_and_share_use_the_apps_list() {
        val apps = listOf(
            AppUsage("a", "A", 1, 0, 600, null),
            AppUsage("b", "B", 2, 0, 300, null),
            AppUsage("c", "C", 3, 0, 100, null),
        )
        val b = rankOf("b", apps)
        assertEquals(2, b.rank)
        assertEquals(3, b.appCount)
        assertEquals(0.3f, b.shareOfAll, 1e-5f)
        val missing = rankOf("zzz", apps)
        assertEquals(0, missing.rank)
        assertEquals(0f, missing.shareOfAll, 0f)
        assertEquals(0, rankOf("a", emptyList()).appCount)
    }

    @Test
    fun rank_words_read_naturally() {
        assertEquals("biggest", rankWord(1))
        assertEquals("second biggest", rankWord(2))
        assertEquals("third biggest", rankWord(3))
        assertEquals("number 7", rankWord(7))
    }

    @Test
    fun plan_impact_divides_by_cycles_for_90_days() {
        val week = planImpact(mobileBytes = 340_000_000L, rangeDays = 7, plan = plan(20_000_000_000L))!!
        assertEquals(0.017f, week.fraction, 1e-4f)
        assertEquals("in a week", week.periodLabel)
        val month = planImpact(mobileBytes = 1_000_000_000L, rangeDays = 30, plan = plan(20_000_000_000L))!!
        assertEquals(0.05f, month.fraction, 1e-4f)
        assertEquals("this cycle", month.periodLabel)
        val quarter = planImpact(mobileBytes = 3_000_000_000L, rangeDays = 90, plan = plan(20_000_000_000L))!!
        assertEquals(0.05f, quarter.fraction, 1e-4f)
        assertEquals("in a typical cycle", quarter.periodLabel)
    }

    @Test
    fun plan_impact_is_null_without_a_plan() {
        assertNull(planImpact(1L, 7, null))
        assertNull(planImpact(1L, 7, plan(0L)))
        assertNotNull(planImpact(0L, 7, plan(10L)))
    }

    @Test
    fun suggested_limit_rounds_up_to_50_mb() {
        assertEquals(450_000_000L, suggestedLimit(322_000_000L))
        assertEquals(50_000_000L, suggestedLimit(1L))
        assertEquals(0L, suggestedLimit(0L))
        assertEquals(1_300_000_000L, suggestedLimit(1_000_000_000L))
    }

    @Test
    fun the_sentence_names_rank_share_network_and_time() {
        val text = summarySentence("YouTube", rank = 1, shareOfAll = 0.34f, wifiShare = 0.84f, busiestHour = 19, daysOfData = 7)!!
        assertTrue(text, text.startsWith("YouTube is your biggest data user: 34% of everything your apps used."))
        assertTrue(text, text.contains("About 84% goes over Wi-Fi"))
        assertTrue(text, text.contains("evening"))
        assertTrue(text, text.contains("7 PM"))
        assertNull(summarySentence("YouTube", 1, 0.34f, 0.84f, 19, daysOfData = 2))
        assertNull(summarySentence("YouTube", 0, 0.34f, 0.84f, 19, daysOfData = 7))
    }

    @Test
    fun hour_labels_are_12_hour_clock() {
        assertEquals("12 AM", hourLabel(0))
        assertEquals("6 AM", hourLabel(6))
        assertEquals("12 PM", hourLabel(12))
        assertEquals("7 PM", hourLabel(19))
        assertEquals("11 PM", hourLabel(23))
    }

    @Test
    fun hour_totals_use_the_given_zone() {
        // 23:00 UTC is 01:00 the next day in Cairo (UTC+2 in winter).
        val cairo = ZoneId.of("Africa/Cairo")
        val instant = Instant.parse("2026-11-20T23:00:00Z")
        val totals = hourTotals(listOf(UsagePoint(instant, 100, 50)), cairo)
        assertEquals(24, totals.size)
        assertEquals(150L, totals[1])
        assertEquals(150L, totals.sum())
    }
}
