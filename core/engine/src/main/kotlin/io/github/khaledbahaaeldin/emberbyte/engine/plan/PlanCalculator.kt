package io.github.khaledbahaaeldin.emberbyte.engine.plan

import io.github.khaledbahaaeldin.emberbyte.engine.model.AddOn
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FreeRule
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

object PlanCalculator {
    fun state(
        plan: Plan,
        at: Instant,
        usage: List<UsageSlice>,
        addOns: List<AddOn>,
        freeRules: List<FreeRule>,
        previousCycleUnusedBytes: Long,
        isApproximate: Boolean,
    ): PlanState {
        val window = CycleCalculator.windowAt(plan.cycle, at)
        val rolledOver = when (val rollover = plan.rollover) {
            Rollover.None -> 0L
            Rollover.Full -> previousCycleUnusedBytes
            is Rollover.Capped -> minOf(previousCycleUnusedBytes, rollover.maxBytes)
        }.coerceAtLeast(0L)
        val addOnBytes = addOns.filter { it.validFrom < window.end && it.validUntil > window.start }.sumOf { it.bytes }
        val effective = plan.capBytes + rolledOver + addOnBytes
        val secondsLeft = Duration.between(at, window.end).seconds
        val daysLeft = if (secondsLeft <= 0L) 0 else ((secondsLeft + 86_399L) / 86_400L).toInt()

        val firstCycle = plan.firstCycle
        if (firstCycle is FirstCycle.StartsNextCycle && at < firstCycle.startsAt) {
            return PlanState(
                plan = plan,
                window = window,
                effectiveCapBytes = effective,
                usedBytes = 0L,
                remainingBytes = effective,
                rolledOverBytes = rolledOver,
                addOnBytes = addOnBytes,
                freeBytes = 0L,
                daysLeft = daysLeft,
                fractionUsed = 0f,
                isApproximate = isApproximate,
                isDormant = true,
                startsAt = firstCycle.startsAt,
                enteredBytes = null,
            )
        }

        val totals = usage.filter { it.packageName == null }
        val perApp = usage.filter { it.packageName != null }

        if (firstCycle is FirstCycle.Entered && firstCycle.cycleStart == window.start) {
            val measureStart = if (firstCycle.enteredAt > window.start) firstCycle.enteredAt else window.start
            val measuredBytes = totals.sumOf { Slices.clippedBytes(it, measureStart, window.end) }
            val freeBytes = FreeWindows.freeBytes(totals, perApp, freeRules, CycleWindow(measureStart, window.end), zoneOf(plan.cycle))
            val netMeasured = maxOf(0L, measuredBytes - freeBytes)
            val used = firstCycle.usedBytes + netMeasured
            return PlanState(
                plan = plan,
                window = window,
                effectiveCapBytes = effective,
                usedBytes = used,
                remainingBytes = maxOf(0L, effective - used),
                rolledOverBytes = rolledOver,
                addOnBytes = addOnBytes,
                freeBytes = freeBytes,
                daysLeft = daysLeft,
                fractionUsed = if (effective > 0L) used.toFloat() / effective else 0f,
                isApproximate = isApproximate,
                isDormant = false,
                startsAt = null,
                enteredBytes = firstCycle.usedBytes,
            )
        }

        val totalBytes = totals.sumOf { Slices.clippedBytes(it, window.start, window.end) }
        val freeBytes = FreeWindows.freeBytes(totals, perApp, freeRules, window, zoneOf(plan.cycle))
        val used = maxOf(0L, totalBytes - freeBytes)
        val startsAt = if (firstCycle is FirstCycle.StartsNextCycle) firstCycle.startsAt else null

        return PlanState(
            plan = plan,
            window = window,
            effectiveCapBytes = effective,
            usedBytes = used,
            remainingBytes = maxOf(0L, effective - used),
            rolledOverBytes = rolledOver,
            addOnBytes = addOnBytes,
            freeBytes = freeBytes,
            daysLeft = daysLeft,
            fractionUsed = if (effective > 0L) used.toFloat() / effective else 0f,
            isApproximate = isApproximate,
            isDormant = false,
            startsAt = startsAt,
            enteredBytes = null,
        )
    }

    fun freeBytes(plan: Plan, usage: List<UsageSlice>, freeRules: List<FreeRule>, window: CycleWindow): Long =
        FreeWindows.freeBytes(usage.filter { it.packageName == null }, usage.filter { it.packageName != null }, freeRules, window, zoneOf(plan.cycle))

    fun zoneOf(cycle: Cycle): ZoneId = when (cycle) {
        is Cycle.MonthlyOnDay -> cycle.zone
        is Cycle.EveryNDays -> cycle.zone
    }
}

fun PlanState.percentUsed(): Int =
    if (effectiveCapBytes <= 0L) 0 else (usedBytes * 100 / effectiveCapBytes).coerceAtMost(9_999L).toInt()

