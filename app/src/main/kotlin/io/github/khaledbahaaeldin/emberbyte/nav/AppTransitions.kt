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
 * The NavHost transition lambdas of the app, in one place so they can be tested.
 *
 * Moving between two different tabs always uses the directional tab transition, whichever lambda Navigation calls
 * (see [isTabSwitch]): going to Home through the navbar is reported as a pop. Everything else uses the one back motion
 * family in [BackTransitions]: push and pop are mirror images, and a predictive back gesture is linear so it follows
 * the finger.
 *
 * [onTransition] reports every decision as (lambda name, kind = tab | push | pop | predictive, from route, to route).
 */
internal class AppTransitions(
    private val motion: MotionScheme,
    private val reduceMotion: Boolean,
    private val layoutDirection: LayoutDirection,
    private val onTransition: (lambda: String, kind: String, from: String?, to: String?) -> Unit = { _, _, _, _ -> },
) {
    private val sign = BackTransitions.slideSignFor(layoutDirection)

    private fun tabEnter(from: String?, to: String?) = tabEnterTransition(motion, reduceMotion, tabDirection(from, to) * sign)
    private fun tabExit(from: String?, to: String?) = tabExitTransition(motion, reduceMotion, tabDirection(from, to) * sign)

    /** Predictive back starts from the left edge in LTR; a gesture from the right edge mirrors the motion. */
    private fun directionForEdge(edge: Int): LayoutDirection =
        if (edge == EDGE_RIGHT) LayoutDirection.Rtl else LayoutDirection.Ltr

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("enter", "tab", from, to)
            tabEnter(from, to)
        } else {
            onTransition("enter", "push", from, to)
            if (reduceMotion) EnterTransition.None else BackTransitions.pushEnter(motion, layoutDirection)
        }
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("exit", "tab", from, to)
            tabExit(from, to)
        } else {
            onTransition("exit", "push", from, to)
            if (reduceMotion) ExitTransition.None else BackTransitions.pushExit(motion)
        }
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("popEnter", "tab", from, to)
            tabEnter(from, to)
        } else {
            onTransition("popEnter", "pop", from, to)
            if (reduceMotion) BackTransitions.popEnterReducedMotion() else BackTransitions.popEnter(motion)
        }
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("popExit", "tab", from, to)
            tabExit(from, to)
        } else {
            onTransition("popExit", "pop", from, to)
            if (reduceMotion) BackTransitions.popExitReducedMotion() else BackTransitions.popExit(motion, layoutDirection)
        }
    }

    val predictivePopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> EnterTransition = { _ ->
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("predictivePopEnter", "tab", from, to)
            tabEnter(from, to)
        } else {
            onTransition("predictivePopEnter", "predictive", from, to)
            if (reduceMotion) BackTransitions.popEnterReducedMotion() else BackTransitions.predictivePopEnter()
        }
    }

    val predictivePopExit: AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> ExitTransition = { edge ->
        val from = initialState.destination.route
        val to = targetState.destination.route
        if (isTabSwitch(from, to)) {
            onTransition("predictivePopExit", "tab", from, to)
            tabExit(from, to)
        } else {
            onTransition("predictivePopExit", "predictive", from, to)
            if (reduceMotion) BackTransitions.popExitReducedMotion() else BackTransitions.predictivePopExit(directionForEdge(edge))
        }
    }

    private companion object {
        /** androidx.activity.BackEventCompat.EDGE_RIGHT. */
        const val EDGE_RIGHT = 1
    }
}
