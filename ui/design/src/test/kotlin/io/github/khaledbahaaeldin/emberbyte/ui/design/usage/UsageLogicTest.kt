package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageLogicTest {
    private fun row(pkg: String, mobile: Long, wifi: Long, system: Boolean = false) =
        AppUsageRow(pkg, pkg.uppercase(), mobile, wifi, system)

    @Test
    fun strip_bar_fractions_scale_to_the_max_with_a_floor() {
        val f = stripBarFractions(listOf(0L, 500L, 1_000L))
        assertEquals(0.06f, f[0], 1e-6f)
        assertEquals(500f / (1_000f * 1.12f), f[1], 1e-6f)
        assertEquals(1_000f / (1_000f * 1.12f), f[2], 1e-6f)
        assertEquals(listOf(0.06f, 0.06f), stripBarFractions(listOf(0L, 0L)))
        assertEquals(emptyList<Float>(), stripBarFractions(emptyList()))
    }

    @Test
    fun strip_label_uses_gb_above_95_mb_and_whole_mb_below() {
        assertEquals("3.7", stripLabel(3_710_000_000L, ByteUnits.DECIMAL))
        assertEquals("0.1", stripLabel(95_000_000L, ByteUnits.DECIMAL))
        assertEquals("94M", stripLabel(94_000_000L, ByteUnits.DECIMAL))
        assertEquals("0M", stripLabel(0L, ByteUnits.DECIMAL))
        assertEquals("63M", stripLabel(63_400_000L, ByteUnits.DECIMAL))
    }

    @Test
    fun rank_apps_orders_by_the_filtered_amount() {
        val rows = listOf(row("a", 10, 90), row("b", 0, 300), row("c", 50, 100), row("d", 5, 5), row("e", 1, 1))
        val ranked = rankApps(rows, UsageFilters())
        assertEquals(listOf("b", "c", "a", "d"), ranked.map { it.row.packageName })
        assertEquals(4, ranked.size)
        assertEquals(300L, ranked.first().amountBytes)
    }

    @Test
    fun mobile_filter_ranks_by_mobile_bytes() {
        val rows = listOf(row("a", 10, 900), row("b", 500, 0), row("c", 50, 100))
        val ranked = rankApps(rows, UsageFilters(network = NetworkFilter.Mobile))
        assertEquals(listOf("b", "c", "a"), ranked.map { it.row.packageName })
        assertEquals(500L, ranked.first().amountBytes)
        val wifi = rankApps(rows, UsageFilters(network = NetworkFilter.WiFi))
        assertEquals(listOf("a", "c"), wifi.map { it.row.packageName })
    }

    @Test
    fun system_apps_are_included_only_when_the_filter_is_on() {
        val rows = listOf(row("app", 0, 100), row("uid:1000", 0, 900, system = true))
        assertEquals(listOf("app"), rankApps(rows, UsageFilters()).map { it.row.packageName })
        assertEquals(
            listOf("uid:1000", "app"),
            rankApps(rows, UsageFilters(showSystemApps = true)).map { it.row.packageName },
        )
    }

    @Test
    fun day_total_follows_the_filters() {
        val rows = listOf(row("a", 10, 90), row("b", 0, 300), row("uid:1", 7, 3, system = true))
        assertEquals(400L, dayTotal(rows, UsageFilters()))
        assertEquals(10L, dayTotal(rows, UsageFilters(network = NetworkFilter.Mobile)))
        assertEquals(410L, dayTotal(rows, UsageFilters(showSystemApps = true)))
        assertEquals(17L, dayTotal(rows, UsageFilters(network = NetworkFilter.Mobile, showSystemApps = true)))
    }

    @Test
    fun share_fill_has_a_14_percent_minimum() {
        assertEquals(1f, shareFillFraction(100L, 100L), 1e-6f)
        assertEquals(0.5f, shareFillFraction(50L, 100L), 1e-6f)
        assertEquals(0.14f, shareFillFraction(1L, 100L), 1e-6f)
        assertEquals(0.14f, shareFillFraction(0L, 0L), 1e-6f)
    }

    @Test
    fun summary_says_so_far_only_for_today() {
        val today = LocalDate.of(2026, 11, 20)
        assertEquals("3.71 GB on Friday, so far", summaryText(today, today, 3_710_000_000L, ByteUnits.DECIMAL, java.util.Locale.ENGLISH))
        assertEquals("3.71 GB on Thursday", summaryText(today.minusDays(1), today, 3_710_000_000L, ByteUnits.DECIMAL, java.util.Locale.ENGLISH))
        assertEquals("63.4 MB on Friday, so far", summaryText(today, today, 63_400_000L, ByteUnits.DECIMAL, java.util.Locale.ENGLISH))
    }

    @Test
    fun share_percent_is_of_the_filtered_day_total() {
        val rows = listOf(row("a", 0, 300), row("b", 0, 100))
        val ranked = rankApps(rows, UsageFilters())
        assertEquals(75, ranked[0].percentOfDay)
        assertEquals(25, ranked[1].percentOfDay)
        assertTrue(rankApps(emptyList(), UsageFilters()).isEmpty())
    }
}
