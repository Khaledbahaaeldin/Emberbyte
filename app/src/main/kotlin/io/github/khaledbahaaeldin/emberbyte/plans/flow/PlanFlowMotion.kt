package io.github.khaledbahaaeldin.emberbyte.plans.flow

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
