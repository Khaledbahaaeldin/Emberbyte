package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry

object BackTransitions {
    const val SHRINK = 0.90f
    const val SLIDE_DP = 28
    const val BEHIND_SCALE_START = 0.94f
    const val CORNER_DP = 32
    const val REDUCED_MOTION_DURATION_MS = 90

    val cancelSpring = spring<Float>(dampingRatio = 0.82f, stiffness = 380f)

    fun cornerRadiusFor(progress: Float): Dp {
        val clamped = progress.coerceIn(0f, 1f)
        val factor = (clamped * 4f).coerceAtMost(1f)
        return (CORNER_DP * factor).dp
    }

    fun transformOriginFor(direction: LayoutDirection): TransformOrigin {
        return if (direction == LayoutDirection.Ltr) {
            TransformOrigin(pivotFractionX = 1.0f, pivotFractionY = 0.5f)
        } else {
            TransformOrigin(pivotFractionX = 0.0f, pivotFractionY = 0.5f)
        }
    }

    fun slideSignFor(direction: LayoutDirection): Int = if (direction == LayoutDirection.Ltr) 1 else -1

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        scaleOut(
            targetScale = SHRINK,
            transformOrigin = transformOriginFor(LayoutDirection.Ltr),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + slideOutHorizontally(
            targetOffsetX = { (SLIDE_DP * 3 * slideSignFor(LayoutDirection.Ltr)) },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
        )
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        scaleIn(
            initialScale = BEHIND_SCALE_START,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 250, easing = LinearEasing)
        )
    }

    fun popExitReducedMotion(): ExitTransition = fadeOut(animationSpec = tween(REDUCED_MOTION_DURATION_MS))
    fun popEnterReducedMotion(): EnterTransition = fadeIn(animationSpec = tween(REDUCED_MOTION_DURATION_MS))
}
