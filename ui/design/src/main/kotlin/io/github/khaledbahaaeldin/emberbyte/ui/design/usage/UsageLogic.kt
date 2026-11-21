package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** One day of the Home week strip (oldest first in a list of seven). */
data class DayUsage(val date: LocalDate, val totalBytes: Long, val mobileBytes: Long, val wifiBytes: Long)

/** A per-app amount for one day. System buckets (`uid:...`) have [isSystem] set. */
data class AppUsageRow(
    val packageName: String,
    val label: String,
    val mobileBytes: Long,
    val wifiBytes: Long,
    val isSystem: Boolean = false,
)

enum class NetworkFilter { All, Mobile, WiFi }

/** The filters the card can show as removable chips. */
enum class UsageFilter { Network, SystemApps }

data class UsageFilters(
    val network: NetworkFilter = NetworkFilter.All,
    val showSystemApps: Boolean = false,
) {
    val activeCount: Int get() = (if (network != NetworkFilter.All) 1 else 0) + (if (showSystemApps) 1 else 0)
}

/** A ranked app with its filtered amount and its percentage of the (filtered) day. */
data class RankedApp(val row: AppUsageRow, val amountBytes: Long, val percentOfDay: Int)

internal fun AppUsageRow.amount(filters: UsageFilters): Long = when (filters.network) {
    NetworkFilter.All -> mobileBytes + wifiBytes
    NetworkFilter.Mobile -> mobileBytes
    NetworkFilter.WiFi -> wifiBytes
}

private fun AppUsageRow.visible(filters: UsageFilters): Boolean = filters.showSystemApps || !isSystem

/** Bar height as a fraction of the strip: `total / (max x 1.12)`, never below 6 percent. */
internal fun stripBarFractions(totals: List<Long>): List<Float> {
    val max = (totals.maxOrNull() ?: 0L).coerceAtLeast(0L)
    return totals.map { total ->
        if (max == 0L) 0.06f else (total.toFloat() / (max * 1.12f)).coerceAtLeast(0.06f)
    }
}

/** Value above a strip bar: one decimal in GB from 0.095 GB, otherwise whole MB with an "M" suffix. */
internal fun stripLabel(bytes: Long, units: ByteUnits): String {
    val base = units.base.toDouble()
    val gb = bytes / (base * base * base)
    return if (gb >= 0.095) String.format(Locale.US, "%.1f", gb) else "${Math.round(bytes / (base * base))}M"
}

/** What the filters leave of one day: the sum of the visible apps' filtered amounts. */
internal fun dayTotal(rows: List<AppUsageRow>, filters: UsageFilters): Long =
    rows.filter { it.visible(filters) }.sumOf { it.amount(filters) }

/** The top [limit] visible apps by filtered amount, descending (ties by name); apps with nothing are left out. */
internal fun rankApps(rows: List<AppUsageRow>, filters: UsageFilters, limit: Int = 4): List<RankedApp> {
    val total = dayTotal(rows, filters)
    return rows
        .filter { it.visible(filters) && it.amount(filters) > 0L }
        .sortedWith(compareByDescending<AppUsageRow> { it.amount(filters) }.thenBy { it.label })
        .take(limit)
        .map { RankedApp(it, it.amount(filters), if (total == 0L) 0 else Math.round(it.amount(filters) * 100.0 / total).toInt()) }
}

/** Width of an app row's share fill relative to the first row; never below 14 percent. */
internal fun shareFillFraction(usage: Long, first: Long): Float =
    if (first <= 0L) 0.14f else (usage.toFloat() / first.toFloat()).coerceIn(0.14f, 1f)

/** "3.71 GB on Friday, so far" (", so far" only for today); MB below 1 GB. */
internal fun summaryText(day: LocalDate, today: LocalDate, total: Long, units: ByteUnits, locale: Locale = Locale.getDefault()): String {
    val amount = formatBytes(total, units)
    val weekday = day.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val tail = if (day == today) ", so far" else ""
    return "${amount.value} ${amount.unit} on $weekday$tail"
}
