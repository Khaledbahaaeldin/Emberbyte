package io.github.khaledbahaaeldin.emberbyte.plans.flow

import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.plan.CycleCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** How far a step slides, as a fraction of the content width. */
internal const val STEP_SLIDE_FRACTION = 0.33f

/**
 * +1 when moving forward through the steps, -1 when moving back, 0 when the step does not change.
 * Mirrored in right-to-left layouts, where "forward" is toward the left.
 */
internal fun stepDirection(from: Int, to: Int, rtl: Boolean): Int {
    val direction = when {
        to > from -> 1
        to < from -> -1
        else -> 0
    }
    return if (rtl) -direction else direction
}

internal fun flowPillWidthDp(index: Int, position: Float): Float {
    val dist = abs(position - index).coerceIn(0f, 1f)
    return 8f + (28f - 8f) * (1f - dist)
}

internal fun flowParallaxOffset(d: Float, widthPx: Float, isOutgoing: Boolean): Float =
    if (isOutgoing) d * widthPx * 0.3f else d * widthPx

internal fun flowPageAlpha(d: Float): Float =
    (1f - 1.15f * abs(d)).coerceIn(0f, 1f)

internal fun flowRevealFraction(q: Float, index: Int): Float =
    ((q - 0.22f - 0.07f * index) / 0.45f).coerceIn(0f, 1f)

internal fun ordinalDay(day: Int): String {
    val mod100 = day % 100
    val suffix = if (mod100 in 11..13) {
        "th"
    } else {
        when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
    return "$day$suffix"
}

internal fun nextResetDescription(
    cycle: Cycle,
    now: Instant,
    zone: ZoneId,
    locale: Locale,
): String {
    val window = CycleCalculator.windowAt(cycle, now)
    val nextDate = window.end.atZone(zone).toLocalDate()
    val fmt = DateTimeFormatter.ofPattern("EEE d MMMM", locale)
    return "Next reset: ${fmt.format(nextDate)}"
}

internal fun cycleSummaryLine(cycle: Cycle, locale: Locale): String = when (cycle) {
    is Cycle.MonthlyOnDay -> if (cycle.dayOfMonth >= 31) {
        "Resets on the last day of every month"
    } else {
        "Resets on the ${ordinalDay(cycle.dayOfMonth)} of every month"
    }
    is Cycle.EveryNDays -> if (cycle.days == 7) {
        val dow = cycle.anchor.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, locale)
        "Resets every week on $dow"
    } else {
        "Resets every ${cycle.days} days"
    }
}
