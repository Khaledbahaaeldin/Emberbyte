package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class PlansSummary(
    val totalRemainingBytes: Long,
    val activeCount: Int,
    val nextRenewal: Instant?,
)

data class PlansInsight(
    val worstPlanId: Long?,
    val worstPlanName: String?,
    val earlyDays: Int,
    val dailyAllowanceBytes: Long?,
    val allOnTrack: Boolean,
)

data class RenewalEvent(
    val planId: Long?,
    val label: String,
    val date: LocalDate,
    val daysFromToday: Int,
    val isToday: Boolean,
    val isUpcoming: Boolean,
)

data class LabelSlot(
    val event: RenewalEvent,
    val xFraction: Float,
    val row: Int,
)

internal fun dailyAllowance(remainingBytes: Long, daysLeft: Int): Long? {
    if (daysLeft <= 0 || remainingBytes <= 0L) return null
    return remainingBytes / daysLeft
}

internal fun statusOf(
    used: Long,
    cap: Long,
    projectedAtEnd: Long,
    runOutDay: Int?,
    cycleLength: Int,
): PlanStatusKind = when {
    cap <= 0L || used >= cap -> PlanStatusKind.OverCap
    projectedAtEnd > cap || (runOutDay != null && runOutDay < cycleLength) -> PlanStatusKind.RunsOutEarly
    else -> PlanStatusKind.OnTrack
}

internal fun summarize(states: List<PlanState>, now: Instant, zone: ZoneId): PlansSummary? {
    val active = states.filter { !it.isDormant }
    if (active.isEmpty()) return null
    val totalRemaining = active.sumOf { it.remainingBytes.coerceAtLeast(0L) }
    val nextRenewal = active
        .map { it.window.end }
        .filter { !it.isBefore(now) }
        .minOrNull()
        ?: active.minOfOrNull { it.window.end }
    return PlansSummary(
        totalRemainingBytes = totalRemaining,
        activeCount = active.size,
        nextRenewal = nextRenewal,
    )
}

internal fun worstPlan(states: List<PlanState>): PlanState? {
    val active = states.filter { !it.isDormant && it.effectiveCapBytes > 0L }
    if (active.isEmpty()) return null
    return active.maxByOrNull { state ->
        val totalSeconds = Duration.between(state.window.start, state.window.end).seconds.coerceAtLeast(1L)
        val remainingSeconds = (state.daysLeft.coerceAtLeast(0).toLong() * 86_400L).coerceAtMost(totalSeconds)
        val elapsedFraction = ((totalSeconds - remainingSeconds).toDouble() / totalSeconds.toDouble()).coerceAtLeast(1.0 / 30.0)
        val projectedFraction = (state.usedBytes.toDouble() / state.effectiveCapBytes.toDouble()) / elapsedFraction
        projectedFraction
    }
}

internal fun renewalEvents(states: List<PlanState>, now: Instant, zone: ZoneId): List<RenewalEvent> {
    val today = now.atZone(zone).toLocalDate()
    val events = mutableListOf(
        RenewalEvent(
            planId = null,
            label = "Today",
            date = today,
            daysFromToday = 0,
            isToday = true,
            isUpcoming = false,
        ),
    )
    states.forEach { state ->
        val targetInstant = if (state.isDormant) (state.startsAt ?: state.window.start) else state.window.end
        val targetDate = targetInstant.atZone(zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(today, targetDate).toInt().coerceAtLeast(0)
        events.add(
            RenewalEvent(
                planId = state.plan.id,
                label = state.plan.name,
                date = targetDate,
                daysFromToday = days,
                isToday = false,
                isUpcoming = state.isDormant,
            ),
        )
    }
    return events.sortedWith(compareBy<RenewalEvent> { it.daysFromToday }.thenByDescending { it.isToday }.thenBy { it.label })
}

internal fun layoutRenewalLabels(
    events: List<RenewalEvent>,
    widthPx: Float,
    labelPx: Float,
): List<LabelSlot> {
    if (events.isEmpty()) return emptyList()
    val maxDays = max(1, events.maxOf { it.daysFromToday })
    val usableWidth = widthPx.coerceAtLeast(1f)
    val rowLastX = mutableListOf<Float>()
    return events.map { event ->
        val xFraction = (event.daysFromToday.toFloat() / maxDays.toFloat()).coerceIn(0f, 1f)
        val xPx = xFraction * usableWidth
        var chosenRow = -1
        for (r in rowLastX.indices) {
            if (xPx - rowLastX[r] >= labelPx) {
                chosenRow = r
                rowLastX[r] = xPx
                break
            }
        }
        if (chosenRow == -1) {
            chosenRow = rowLastX.size
            rowLastX.add(xPx)
        }
        LabelSlot(event = event, xFraction = xFraction, row = chosenRow)
    }
}

internal fun frontPlan(plans: List<PlanState>): PlanState? {
    val active = plans.filter { !it.isDormant }
    return active.firstOrNull { it.plan.pinned } ?: active.firstOrNull() ?: plans.firstOrNull()
}

internal fun pinOnly(id: Long, plans: List<Plan>): List<Pair<Long, Boolean>> =
    plans.map { it.id to (it.id == id) }

internal fun withExtraCap(state: PlanState, extraBytes: Long): PlanStatusKind {
    if (state.isDormant) return PlanStatusKind.Upcoming
    val newCap = (state.effectiveCapBytes + extraBytes.coerceAtLeast(0L)).coerceAtLeast(0L)
    if (newCap <= 0L || state.usedBytes >= newCap) return PlanStatusKind.OverCap
    val totalDays = Duration.between(state.window.start, state.window.end).toDays().toInt().coerceAtLeast(1)
    val elapsedDays = (totalDays - state.daysLeft).coerceAtLeast(1)
    val projectedAtEnd = ((state.usedBytes.toDouble() / elapsedDays.toDouble()) * totalDays.toDouble()).toLong()
    return statusOf(
        used = state.usedBytes,
        cap = newCap,
        projectedAtEnd = projectedAtEnd,
        runOutDay = null,
        cycleLength = totalDays,
    )
}

internal fun restoredNotice(count: Int): String? = when {
    count <= 0 -> null
    count == 1 -> "1 archived plan was restored. You can delete it if you do not need it."
    else -> "$count archived plans were restored. You can delete the ones you do not need."
}
