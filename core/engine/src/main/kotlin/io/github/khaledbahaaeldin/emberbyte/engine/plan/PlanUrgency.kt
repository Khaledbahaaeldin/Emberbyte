package io.github.khaledbahaaeldin.emberbyte.engine.plan

import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import java.time.Duration
import java.time.Instant

object PlanUrgency {
    fun front(states: List<PlanState>, now: Instant): PlanState? {
        val active = states.filter { !it.isDormant }
        if (active.isEmpty()) return null

        val pinned = active.firstOrNull { it.plan.pinned }
        if (pinned != null) return pinned

        return active.minWithOrNull(urgencyComparator(now))
    }

    fun order(states: List<PlanState>, now: Instant): List<PlanState> {
        val active = states.filter { !it.isDormant }
        val dormant = states.filter { it.isDormant }.sortedBy { it.plan.id }
        if (active.isEmpty()) return dormant

        val frontPlan = front(states, now)
        val comparator = urgencyComparator(now)
        val remainingActive = if (frontPlan != null) {
            active.filter { it.plan.id != frontPlan.plan.id }.sortedWith(comparator)
        } else {
            active.sortedWith(comparator)
        }

        val frontList = if (frontPlan != null) listOf(frontPlan) else emptyList()
        return frontList + remainingActive + dormant
    }

    private fun urgencyComparator(now: Instant): Comparator<PlanState> = Comparator { a, b ->
        // 1. Over cap first (used >= effectiveCap)
        val aOverCap = a.usedBytes >= a.effectiveCapBytes || a.fractionUsed >= 1.0f
        val bOverCap = b.usedBytes >= b.effectiveCapBytes || b.fractionUsed >= 1.0f
        if (aOverCap != bOverCap) {
            return@Comparator if (aOverCap) -1 else 1
        }

        // 2. Earliest projected run out
        val aRunOut = projectedRunOut(a, now)
        val bRunOut = projectedRunOut(b, now)
        if (aRunOut != null || bRunOut != null) {
            if (aRunOut != null && bRunOut != null) {
                val cmp = aRunOut.compareTo(bRunOut)
                if (cmp != 0) return@Comparator cmp
            } else {
                return@Comparator if (aRunOut != null) -1 else 1
            }
        }

        // 3. Highest fractionUsed
        val fracCmp = b.fractionUsed.compareTo(a.fractionUsed)
        if (fracCmp != 0) return@Comparator fracCmp

        // 4. Lowest id
        a.plan.id.compareTo(b.plan.id)
    }

    private fun projectedRunOut(state: PlanState, now: Instant): Instant? {
        if (state.usedBytes >= state.effectiveCapBytes || state.remainingBytes <= 0L) return null
        if (now <= state.window.start) return null
        val elapsedSeconds = Duration.between(state.window.start, now).seconds
        if (elapsedSeconds <= 0L || state.usedBytes <= 0L) return null

        val bytesPerSecond = state.usedBytes.toDouble() / elapsedSeconds
        if (bytesPerSecond <= 0.0) return null

        val secondsUntilRunOut = (state.remainingBytes / bytesPerSecond).toLong()
        val runOut = now.plusSeconds(secondsUntilRunOut)
        return if (runOut <= state.window.end) runOut else null
    }
}
