package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.common.forecastSentence
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.engine.model.AddOn
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.plan.percentUsed
import io.github.khaledbahaaeldin.emberbyte.home.forecastToUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

internal fun planCardUi(
    state: PlanState,
    forecast: Forecast?,
    addOns: List<AddOn>,
    simLabel: String?,
    units: ByteUnits,
    now: Instant,
    zone: ZoneId,
    locale: Locale,
    weekBars: List<Float> = emptyList(),
): PlanCardUi {
    val percent = state.percentUsed()
    val cycleLength = Duration.between(state.window.start, state.window.end).toDays().toInt().coerceAtLeast(1)
    val dayOfCycle = (cycleLength - state.daysLeft).coerceIn(1, cycleLength)
    val timeFraction = (dayOfCycle.toFloat() / cycleLength.toFloat()).coerceIn(0f, 1f)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    val renewsDateText = dateFormatter.format(state.window.end.atZone(zone))

    val statusKind = when {
        state.isDormant -> PlanStatusKind.Upcoming
        percent >= 100 || state.remainingBytes <= 0L -> PlanStatusKind.OverCap
        forecast?.runOutExpected != null && forecast.runOutExpected!!.isBefore(state.window.end) -> PlanStatusKind.RunsOutEarly
        else -> PlanStatusKind.OnTrack
    }

    val runOutDateText = forecast?.runOutExpected?.let { dateFormatter.format(it.atZone(zone)) }
    val earlyDays = forecast?.runOutExpected?.let {
        ChronoUnit.DAYS.between(it.atZone(zone).toLocalDate(), state.window.end.atZone(zone).toLocalDate()).toInt().coerceAtLeast(0)
    } ?: 0
    val earlyDaysText = when {
        earlyDays == 1 -> "1 day early"
        earlyDays > 1 -> "$earlyDays days early"
        else -> null
    }
    val allowancePerDay = dailyAllowance(state.remainingBytes, state.daysLeft)?.let {
        "You can use ${plainBytes(it, units)} a day"
    }
    val rolloverText = when (val r = state.plan.rollover) {
        Rollover.None -> null
        Rollover.Full -> "Full rollover"
        is Rollover.Capped -> "Rollover up to ${plainBytes(r.maxBytes, units)}"
    }

    return PlanCardUi(
        id = state.plan.id,
        name = state.plan.name,
        simLabel = simLabel,
        fraction = state.fractionUsed,
        ringState = when {
            percent >= 100 -> BudgetStateUi.Exceeded
            percent >= 80 -> BudgetStateUi.Warning
            else -> BudgetStateUi.Ok
        },
        percentText = "$percent%",
        usedText = "${plainBytes(state.usedBytes, units)} of ${plainBytes(state.effectiveCapBytes, units)}",
        remainingText = remainingText(state.remainingBytes, units),
        daysLeftText = when (state.daysLeft) {
            0 -> "Renews today"
            1 -> "1 day left"
            else -> "${state.daysLeft} days left"
        },
        forecastText = forecast?.let { forecastSentence(forecastToUi(it, now, zone, locale, state.remainingBytes)) },
        isEstimated = state.isApproximate,
        chips = addOns.filter { it.validFrom < state.window.end && it.validUntil > state.window.start }
            .map { "${it.label} +${plainBytes(it.bytes, units)}" },
        isDormant = state.isDormant,
        startsAtText = state.startsAt?.let {
            "Starts ${dateFormatter.format(it.atZone(zone))}"
        },
        enteredNote = state.enteredBytes?.let {
            "Includes ${plainBytes(it, units)} you entered"
        },
        pinned = state.plan.pinned,
        allowancePerDayText = allowancePerDay,
        timeFraction = timeFraction,
        dayOfCycle = dayOfCycle,
        cycleLength = cycleLength,
        weekBars = weekBars,
        statusKind = statusKind,
        runOutDateText = runOutDateText,
        earlyDaysText = earlyDaysText,
        renewsText = "Renews $renewsDateText",
        capText = plainBytes(state.effectiveCapBytes, units),
        rolloverText = rolloverText,
    )
}
