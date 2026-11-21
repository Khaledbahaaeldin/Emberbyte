package io.github.khaledbahaaeldin.emberbyte.history

import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/** The comparison pill under the total, with an arrow and words so it never relies on colour alone. */
data class PeriodComparison(val text: String, val direction: TrendDirection)

/** An app's share of everything in a period and how its data changed against the equal period before (null = no data then). */
data class AppShare(val packageName: String, val label: String, val bytes: Long, val percentOfAll: Int, val change: Float?)

data class Records(val busiest: DayUsage?, val lightest: DayUsage?, val longestRunUnderAverage: Int, val peakHour: Int?)

data class VersusAverage(val kind: Kind, val percent: Int) {
    enum class Kind { Above, Below, Near }
    companion object {
        val Above = Kind.Above
        val Below = Kind.Below
        val Near = Kind.Near
    }
}

private fun comparisonOf(current: Long, previous: Long, phrase: String): PeriodComparison? {
    if (previous <= 0L) return null
    val change = (current - previous).toFloat() / previous.toFloat()
    val percent = (abs(change) * 100f).roundToInt()
    return when {
        abs(change) < 0.03f -> PeriodComparison("About the same as $phrase", TrendDirection.FLAT)
        change > 0f -> PeriodComparison("\u25B2 $percent% more than $phrase", TrendDirection.UP)
        else -> PeriodComparison("\u25BC $percent% less than $phrase", TrendDirection.DOWN)
    }
}

/**
 * Compares like with like: Daily is the last 7 days against the 7 before; Weekly and Monthly compare the current,
 * possibly partial, period against the same number of days of the one before (pro rata when only whole-period totals
 * exist). Null without previous data.
 */
internal fun fairComparison(granularity: Granularity, points: List<UsagePoint>, today: LocalDate, zone: ZoneId): PeriodComparison? {
    when (granularity) {
        Granularity.DAY, Granularity.HOUR -> {
            if (points.size < 14) return null
            val last = points.takeLast(7).sumOf { it.totalBytes }
            val before = points.takeLast(14).take(7).sumOf { it.totalBytes }
            return comparisonOf(last, before, "the 7 days before")
        }
        Granularity.WEEK -> {
            if (points.size < 2) return null
            val elapsed = today.dayOfWeek.value.coerceIn(1, 7)
            val previous = points[points.size - 2].totalBytes * elapsed / 7L
            return comparisonOf(points.last().totalBytes, previous, "the same days last week")
        }
        Granularity.MONTH -> {
            if (points.size < 2) return null
            val previousMonth = today.withDayOfMonth(1).minusMonths(1)
            val length = previousMonth.lengthOfMonth()
            val elapsed = minOf(today.dayOfMonth, length)
            val previous = points[points.size - 2].totalBytes * elapsed / length
            return comparisonOf(points.last().totalBytes, previous, "the same days last month")
        }
    }
}

private fun isWeekend(date: LocalDate) = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

/** Weekend average over weekday average, minus 1 (0.47 = 47 percent more at weekends); null below 14 days of data. */
internal fun weekendLift(rows: List<DayUsage>): Float? {
    val active = rows.filter { it.totalBytes > 0L }
    if (rows.size < 14 || active.isEmpty()) return null
    val weekend = rows.filter { isWeekend(it.date) }
    val weekday = rows.filterNot { isWeekend(it.date) }
    if (weekend.isEmpty() || weekday.isEmpty()) return null
    val weekdayAverage = weekday.sumOf { it.totalBytes }.toDouble() / weekday.size
    if (weekdayAverage <= 0.0) return null
    val weekendAverage = weekend.sumOf { it.totalBytes }.toDouble() / weekend.size
    return (weekendAverage / weekdayAverage - 1.0).toFloat()
}

/** Average total per weekday, Monday first. */
internal fun weekdayAverages(rows: List<DayUsage>): LongArray {
    val sums = LongArray(7)
    val counts = IntArray(7)
    rows.forEach {
        val i = it.date.dayOfWeek.value - 1
        sums[i] += it.totalBytes
        counts[i]++
    }
    return LongArray(7) { if (counts[it] == 0) 0L else sums[it] / counts[it] }
}

/** Opacity of a calendar cell: 0.18 up to 1.0 following `(total / max) ^ 0.9`. */
internal fun calendarIntensity(total: Long, max: Long): Float =
    if (max <= 0L) 0.18f else (0.18f + 0.82f * (total.coerceAtLeast(0L).toFloat() / max).coerceIn(0f, 1f).toDouble().pow(0.9).toFloat()).coerceIn(0.18f, 1f)

/** Mobile share (0..1) of each bucket; an empty bucket is 0. */
internal fun mobileShareSeries(points: List<UsagePoint>): List<Float> =
    points.map { if (it.totalBytes <= 0L) 0f else it.mobileBytes.toFloat() / it.totalBytes.toFloat() }

/** The longest stretch of consecutive days (in the order given) whose total is under [threshold]. */
internal fun longestRunUnder(rows: List<DayUsage>, threshold: Long): Int {
    var best = 0
    var run = 0
    rows.forEach {
        if (it.totalBytes < threshold) { run++; best = maxOf(best, run) } else run = 0
    }
    return best
}

internal fun records(rows: List<DayUsage>, hourTotals: LongArray): Records {
    val average = if (rows.isEmpty()) 0L else rows.sumOf { it.totalBytes } / rows.size
    val peak = hourTotals.withIndex().maxByOrNull { it.value }?.takeIf { it.value > 0L }?.index
    return Records(
        busiest = rows.maxByOrNull { it.totalBytes },
        lightest = rows.minByOrNull { it.totalBytes },
        longestRunUnderAverage = longestRunUnder(rows, average),
        peakHour = peak,
    )
}

/** How a bucket compares with the daily average: within 5 percent is "near". [days] is the bucket's length in days. */
internal fun versusAverage(total: Long, days: Int, dailyAverage: Long): VersusAverage {
    if (dailyAverage <= 0L || days <= 0) return VersusAverage(VersusAverage.Near, 0)
    val perDay = total.toDouble() / days
    val change = perDay / dailyAverage - 1.0
    val percent = (abs(change) * 100).roundToInt()
    return when {
        abs(change) < 0.05 -> VersusAverage(VersusAverage.Near, percent)
        change > 0 -> VersusAverage(VersusAverage.Above, percent)
        else -> VersusAverage(VersusAverage.Below, percent)
    }
}

/** The heaviest apps with their share of all data and the change against the previous period. */
internal fun topAppsWithChange(current: List<AppUsage>, previous: List<AppUsage>, limit: Int = 5): List<AppShare> {
    val total = current.sumOf { it.totalBytes }
    val before = previous.associate { it.packageName to it.totalBytes }
    return current.sortedByDescending { it.totalBytes }.take(limit).map { app ->
        val prev = before[app.packageName]
        AppShare(
            packageName = app.packageName,
            label = app.label,
            bytes = app.totalBytes,
            percentOfAll = if (total <= 0L) 0 else (app.totalBytes * 100.0 / total).roundToInt(),
            change = if (prev == null || prev <= 0L) null else (app.totalBytes - prev).toFloat() / prev.toFloat(),
        )
    }
}

/** Sums hourly points into 24 buckets by local hour of day. */
internal fun hourTotalsOf(points: List<UsagePoint>, zone: ZoneId): LongArray {
    val totals = LongArray(24)
    points.forEach { totals[it.start.atZone(zone).hour] += it.totalBytes }
    return totals
}
