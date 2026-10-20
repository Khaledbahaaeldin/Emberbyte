package io.github.khaledbahaaeldin.emberbyte.engine.model

import java.time.Instant

sealed interface FirstCycle {
    data object PhoneRecord : FirstCycle
    /** The user typed what the carrier counted so far. Applies only while `cycleStart` equals the current cycle window start. */
    data class Entered(val usedBytes: Long, val enteredAt: Instant, val cycleStart: Instant) : FirstCycle
    /** Dormant until `startsAt` (the next cycle boundary at the time of creation); from then on the plan behaves as PhoneRecord. */
    data class StartsNextCycle(val startsAt: Instant) : FirstCycle
}
