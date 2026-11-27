package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.engine.forecast.ForecastEngine
import io.github.khaledbahaaeldin.emberbyte.engine.model.DayUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.plan.CycleCalculator
import io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanCalculator
import io.github.khaledbahaaeldin.emberbyte.engine.plan.UsageSlice
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

internal data class PlanPreview(
    val state: PlanState,
    val forecast: Forecast,
    val card: PlanCardUi,
)

internal fun previewFor(
    form: PlanEditorForm,
    usage: List<UsageSlice>,
    now: Instant,
    zone: ZoneId,
    unitSystem: UnitSystem = UnitSystem.DECIMAL,
    locale: Locale = Locale.ENGLISH,
    previousCycleUnusedBytes: Long = 0L,
    simLabel: String? = null,
): PlanPreview? {
    val domain = form.toDomain(planId = 0L, zone = zone, system = unitSystem, archived = false)
    if (domain !is FormResult.Ok) return null

    val window = CycleCalculator.windowAt(domain.plan.cycle, now)
    val effectiveFirstCycle = when (form.firstCycleChoice) {
        FirstCycleChoice.PhoneRecord -> FirstCycle.PhoneRecord
        FirstCycleChoice.Entered -> {
            val enteredBytes = bytesOf(form.enteredAmount, form.enteredUnit, unitSystem) ?: 0L
            val enteredAt = (form.firstCycle as? FirstCycle.Entered)?.enteredAt ?: now
            FirstCycle.Entered(cycleStart = window.start, enteredAt = enteredAt, usedBytes = enteredBytes)
        }
        FirstCycleChoice.StartNextCycle -> {
            val startsAt = (form.firstCycle as? FirstCycle.StartsNextCycle)?.startsAt ?: window.end
            FirstCycle.StartsNextCycle(startsAt = startsAt)
        }
    }
    val plan = domain.plan.copy(firstCycle = effectiveFirstCycle)
    val isEstimated = form.firstCycleChoice == FirstCycleChoice.PhoneRecord

    val state = PlanCalculator.state(
        plan = plan,
        at = now,
        usage = usage,
        addOns = domain.addOns,
        freeRules = domain.freeRules,
        previousCycleUnusedBytes = previousCycleUnusedBytes,
        isApproximate = isEstimated,
    )

    val dailyHistory = usage
        .filter { it.packageName == null }
        .groupBy { it.start.atZone(zone).toLocalDate() }
        .map { (date, slices) -> DayUsage(date = date, mobileBytes = slices.sumOf { it.bytes }, wifiBytes = 0L) }
        .sortedBy { it.date }

    val forecast = ForecastEngine.forecast(
        state = state,
        dailyHistory = dailyHistory,
        now = now,
        zone = zone,
    )

    val card = planCardUi(
        state = state,
        forecast = forecast,
        addOns = domain.addOns,
        simLabel = simLabel,
        units = unitSystem.toByteUnits(),
        now = now,
        zone = zone,
        locale = locale,
    )

    return PlanPreview(state = state, forecast = forecast, card = card)
}
