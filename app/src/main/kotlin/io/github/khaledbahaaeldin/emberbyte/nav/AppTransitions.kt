@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavBackStackEntry

/**
 * The four NavHost transition lambdas of the app, in one place so they can be tested.
 *
 * Moving between two different tabs always uses the directional tab transition, whichever of the four lambdas
 * Navigation calls (see [isTabSwitch]): going to Home through the navbar is reported as a pop. Everything else keeps
 * the push (plain fade) and pop (shrink and slide) behaviour.
 *
 * [onTransition] reports every decision as (lambda name, kind = tab | push | pop, from route, to route).
 */
internal class AppTransitions(
    private val motion: MotionScheme,
    private val reduceMotion: Boolean,
    private val layoutDirection: LayoutDirection,
    private val onTransition: (lambda: String, kind: String, from: String?, to: String?) -> Unit = { _, _, _, _ -> },
) {
    private val sign = BackTransitions.slideSignFor(layoutDirection)

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("enter", "tab", from, to)
            tabEnterTransition(motion, reduceMotion, tabDirection(from, to) * sign)
        } else {
            onTransition("enter", "push", from, to)
            tabEnterTransition(motion, reduceMotion)
        }
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("exit", "tab", from, to)
            tabExitTransition(motion, reduceMotion, tabDirection(from, to) * sign)
        } else {
            onTransition("exit", "push", from, to)
            tabExitTransition(motion, reduceMotion)
        }
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("popEnter", "tab", from, to)
            tabEnterTransition(motion, reduceMotion, tabDirection(from, to) * sign)
        } else {
            onTransition("popEnter", "pop", from, to)
            if (reduceMotion) BackTransitions.popEnterReducedMotion()
            else BackTransitions.popEnter(layoutDirection)(this)
        }
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("popExit", "tab", from, to)
            tabExitTransition(motion, reduceMotion, tabDirection(from, to) * sign)
        } else {
            onTransition("popExit", "pop", from, to)
            if (reduceMotion) BackTransitions.popExitReducedMotion()
            else BackTransitions.popExit(layoutDirection)(this)
        }
    }
}
