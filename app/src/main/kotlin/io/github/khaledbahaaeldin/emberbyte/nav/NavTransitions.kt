@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/** Fraction of the screen width a tab slides while switching tabs. */
internal const val TAB_SLIDE_FRACTION = 0.10f

private val TAB_ROUTES: List<String> = Destination.entries.map { it.route }

/** True only for the exact routes of the three navbar tabs (not for their sub-screens). */
internal fun isTabRoute(route: String?): Boolean = route != null && route in TAB_ROUTES

/** +1 when [to] is later in navbar order than [from], -1 when earlier, 0 when either is not a tab or they are the same. */
internal fun tabDirection(from: String?, to: String?): Int {
    if (from == null || to == null || from == to) return 0
    val f = TAB_ROUTES.indexOf(from)
    val t = TAB_ROUTES.indexOf(to)
    if (f < 0 || t < 0) return 0
    return if (t > f) 1 else -1
}

/**
 * True when a navigation moves between two different top-level tabs. Navigation Compose reports such a
 * move as a pop whenever the target tab is already on top after `popUpTo` (the `isPop` flag is only reset by a
 * real push), so the tab transition must be chosen from the two routes, never from the pop flag.
 */
internal fun isTabSwitch(from: String?, to: String?): Boolean = isTabRoute(from) && isTabRoute(to) && from != to

/**
 * Tab-to-tab transitions come from the motion scheme (never Navigation's hand-written 700 ms default).
 * With [direction] != 0 the tab slides in from the side it comes from, otherwise it only fades.
 * With reduced motion, tabs switch instantly.
 */
internal fun tabEnterTransition(motion: MotionScheme, reduceMotion: Boolean, direction: Int = 0): EnterTransition =
    when {
        reduceMotion -> EnterTransition.None
        direction == 0 -> fadeIn(motion.defaultEffectsSpec())
        else -> slideInHorizontally(motion.defaultSpatialSpec<IntOffset>()) {
            (it * TAB_SLIDE_FRACTION * direction).roundToInt()
        } + fadeIn(motion.defaultEffectsSpec())
    }

internal fun tabExitTransition(motion: MotionScheme, reduceMotion: Boolean, direction: Int = 0): ExitTransition =
    when {
        reduceMotion -> ExitTransition.None
        direction == 0 -> fadeOut(motion.fastEffectsSpec())
        else -> slideOutHorizontally(motion.defaultSpatialSpec<IntOffset>()) {
            (-it * TAB_SLIDE_FRACTION * direction).roundToInt()
        } + fadeOut(motion.fastEffectsSpec())
    }
