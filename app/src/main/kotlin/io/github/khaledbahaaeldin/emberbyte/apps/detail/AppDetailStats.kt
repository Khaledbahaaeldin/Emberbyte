package io.github.khaledbahaaeldin.emberbyte.apps.detail

import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import java.time.DayOfWeek
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/** The ranges the App detail screen offers. */
enum class DetailRange(val days: Int) { Week(7), Month(30), Quarter(90) }

enum class ChangeDirection { Up, Down, Same, None }

/** The pill under the total: wording plus an arrow, so it never relies on colour alone. */
data class ChangeLabel(val text: String, val direction: ChangeDirection)

data class PartShare(val name: String, val startHour: Int, val endHour: Int, val bytes: Long, val percent: Int)

data class HourWindow(val startHour: Int, val width: Int, val bytes: Long, val percentOfTotal: Int)

/** [rank] 0 means the app is not in the list (for example a `uid:` bucket): the screen then hides rank and share. */
data class RankInfo(val rank: Int, val appCount: Int, val shareOfAll: Float)

data class PlanImpact(val mobileBytes: Long, val allowanceBytes: Long, val fraction: Float, val periodLabel: String)

/** Change of [current] against [previous] as a fraction (0.12 = 12 percent more); null when there is no previous data. */
internal fun changeFraction(current: Long, previous: Long): Float? =
    if (previous <= 0L) null else (current - previous).toFloat() / previous.toFloat()

private fun periodNoun(days: Int) = when (days) {
    7 -> "week"
    30 -> "month"
    else -> "quarter"
}

internal fun changeLabel(change: Float?, rangeDays: Int): ChangeLabel {
    if (change == null) return ChangeLabel("First ${periodNoun(rangeDays)} of data", ChangeDirection.None)
    val percent = (abs(change) * 100f).roundToInt()
    return when {
        abs(change) < 0.03f -> ChangeLabel("About the same as the $rangeDays days before", ChangeDirection.Same)
        change > 0f -> ChangeLabel("\u25B2 $percent% more than the $rangeDays days before", ChangeDirection.Up)
        else -> ChangeLabel("\u25BC $percent% less than the $rangeDays days before", ChangeDirection.Down)
    }
}

/** The day with the most data; ties go to the later day. */
internal fun busiestDay(series: List<UsagePoint>, zone: ZoneId): UsagePoint? =
    series.withIndex().maxWithOrNull(compareBy({ it.value.totalBytes }, { it.index }))?.value

internal fun dailyAverage(total: Long, days: Int): Long = if (days <= 0) 0L else total / days

/** What the range would add up to in 30 days if it is typical. */
internal fun pace30(total: Long, days: Int): Long = if (days <= 0) 0L else total * 30L / days

/** Up to 13 weekly buckets of [series] (oldest first), aligned so the last bucket ends with the last day. */
internal fun weeklyBuckets(series: List<UsagePoint>, zone: ZoneId): List<UsagePoint> {
    val recent = series.takeLast(13 * 7)
    val chunks = recent.reversed().chunked(7).reversed().map { it.reversed() }
    return chunks.map { chunk ->
        UsagePoint(chunk.first().start, chunk.sumOf { it.mobileBytes }, chunk.sumOf { it.wifiBytes })
    }
}

/** Splits 24 hourly totals into Night, Morning, Afternoon and Evening with percentages that add to 100. */
internal fun partsOfDay(hours: LongArray): List<PartShare> {
    val names = listOf("Night", "Morning", "Afternoon", "Evening")
    val sums = names.indices.map { part -> (part * 6 until part * 6 + 6).sumOf { hours.getOrElse(it) { 0L } } }
    val total = sums.sum()
    if (total <= 0L) return names.mapIndexed { i, n -> PartShare(n, i * 6, i * 6 + 6, 0L, 0) }
    // Largest-remainder rounding so the four shares always add up to exactly 100.
    val exact = sums.map { it * 100.0 / total }
    val floors = exact.map { it.toInt() }.toMutableList()
    var left = 100 - floors.sum()
    exact.withIndex().sortedByDescending { it.value - floors[it.index] }.forEach {
        if (left > 0) { floors[it.index]++; left-- }
    }
    return names.mapIndexed { i, n -> PartShare(n, i * 6, i * 6 + 6, sums[i], floors[i]) }
}

/** The [width] consecutive hours (within one day) with the most data. */
internal fun busiestWindow(hours: LongArray, width: Int = 3): HourWindow {
    var bestStart = 0
    var best = -1L
    for (start in 0..(24 - width)) {
        val sum = (start until start + width).sumOf { hours.getOrElse(it) { 0L } }
        if (sum > best) { best = sum; bestStart = start }
    }
    val total = hours.sum()
    return HourWindow(bestStart, width, best.coerceAtLeast(0L), if (total <= 0L) 0 else Math.round(best * 100.0 / total).toInt())
}

/** Opacity of an hour cell: 0.14 up to 1.0. */
internal fun hourIntensity(value: Long, max: Long): Float =
    if (max <= 0L) 0.14f else (0.14f + 0.86f * (value.toFloat() / max.toFloat())).coerceIn(0.14f, 1f)

internal fun rankOf(packageName: String, apps: List<AppUsage>): RankInfo {
    val ranked = apps.sortedByDescending { it.totalBytes }
    val index = ranked.indexOfFirst { it.packageName == packageName }
    val total = apps.sumOf { it.totalBytes }
    if (index < 0) return RankInfo(0, apps.size, 0f)
    return RankInfo(index + 1, apps.size, if (total <= 0L) 0f else ranked[index].totalBytes.toFloat() / total.toFloat())
}

internal fun rankWord(rank: Int): String = when (rank) {
    1 -> "biggest"
    2 -> "second biggest"
    3 -> "third biggest"
    else -> "number $rank"
}

/** Mobile data of the app measured against the plan allowance; 90 days is divided into cycles. */
internal fun planImpact(mobileBytes: Long, rangeDays: Int, plan: Plan?): PlanImpact? {
    if (plan == null || plan.capBytes <= 0L) return null
    val (bytes, label) = when (rangeDays) {
        7 -> mobileBytes to "in a week"
        30 -> mobileBytes to "this cycle"
        else -> mobileBytes / 3 to "in a typical cycle"
    }
    return PlanImpact(bytes, plan.capBytes, bytes.toFloat() / plan.capBytes.toFloat(), label)
}

/** A budget suggestion: the daily average times 1.3, rounded up to the next 50 MB. */
internal fun suggestedLimit(averageDaily: Long): Long {
    if (averageDaily <= 0L) return 0L
    val step = 50_000_000.0
    return (ceil(averageDaily * 1.3 / step) * step).toLong()
}

internal fun hourLabel(hour: Int): String {
    val h = ((hour % 24) + 24) % 24
    val suffix = if (h < 12) "AM" else "PM"
    val twelve = if (h % 12 == 0) 12 else h % 12
    return "$twelve $suffix"
}

private fun partWord(hour: Int) = when (hour) {
    in 0..5 -> "night"
    in 6..11 -> "morning"
    in 12..17 -> "afternoon"
    else -> "evening"
}

/**
 * The one-sentence summary. Null (hidden) for an app that is not ranked or has under three days of data.
 */
internal fun summarySentence(
    label: String,
    rank: Int,
    shareOfAll: Float,
    wifiShare: Float,
    busiestHour: Int,
    daysOfData: Int,
): String? {
    if (rank <= 0 || daysOfData < 3) return null
    return "$label is your ${rankWord(rank)} data user: ${(shareOfAll * 100).roundToInt()}% of everything your apps used. " +
        "About ${(wifiShare * 100).roundToInt()}% goes over Wi-Fi, and most of it flows in the ${partWord(busiestHour)}, " +
        "peaking around ${hourLabel(busiestHour)}."
}

/** Sums hourly points into 24 buckets by local hour of day in [zone]. */
internal fun hourTotals(series: List<UsagePoint>, zone: ZoneId): LongArray {
    val totals = LongArray(24)
    series.forEach { totals[it.start.atZone(zone).hour] += it.totalBytes }
    return totals
}

/** True for Saturday and Sunday (used for the "a weekend" note on the busiest day). */
internal fun isWeekend(day: DayOfWeek): Boolean = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY

/** What a daily budget is on course for: staying under (by [bytesDelta]) or passing the limit around [passAtHour]. */
data class BudgetProjection(val willPass: Boolean, val bytesDelta: Long, val passAtHour: Int?)

/** Projects today's usage linearly from the minutes elapsed; null in the first hour, when a pace says nothing. */
internal fun dailyBudgetProjection(usedBytes: Long, limitBytes: Long, minutesIntoDay: Int): BudgetProjection? {
    if (limitBytes <= 0L || minutesIntoDay < 60) return null
    val projected = usedBytes * 1440L / minutesIntoDay
    if (projected <= limitBytes) return BudgetProjection(false, limitBytes - projected, null)
    val minutesToLimit = if (usedBytes <= 0L) 1440L else limitBytes * minutesIntoDay / usedBytes
    return BudgetProjection(true, projected - limitBytes, (minutesToLimit / 60L).toInt().coerceIn(0, 23))
}
