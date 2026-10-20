package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette

object BackTransitions {
    const val SHRINK = 0.90f
    const val SLIDE_DP = 28
    const val BEHIND_SCALE_START = 0.94f
    const val CORNER_DP = 32
    const val REDUCED_MOTION_DURATION_MS = 90
    const val SCRIM_START_ALPHA = 0.55f

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

    fun popExit(
        direction: LayoutDirection = LayoutDirection.Ltr,
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        scaleOut(
            targetScale = SHRINK,
            transformOrigin = transformOriginFor(direction),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        ) + slideOutHorizontally(
            targetOffsetX = { (SLIDE_DP * 3 * slideSignFor(direction)) },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        ) + fadeOut(
            targetAlpha = 0f,
            animationSpec = tween(durationMillis = 80, delayMillis = 220, easing = LinearEasing),
        )
    }

    fun popEnter(
        direction: LayoutDirection = LayoutDirection.Ltr,
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        scaleIn(
            initialScale = BEHIND_SCALE_START,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        ) + fadeIn(
            initialAlpha = SCRIM_START_ALPHA,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        )
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = popExit(LayoutDirection.Ltr)
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = popEnter(LayoutDirection.Ltr)

    fun popExitReducedMotion(): ExitTransition = fadeOut(animationSpec = tween(REDUCED_MOTION_DURATION_MS))
    fun popEnterReducedMotion(): EnterTransition = fadeIn(animationSpec = tween(REDUCED_MOTION_DURATION_MS))
}

@Composable
fun AnimatedVisibilityScope.PredictiveBackSubScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        },
        label = "subScreenBackProgress",
    ) { state ->
        if (state == EnterExitState.Visible) 0f else 1f
    }
    val cornerRadius = BackTransitions.cornerRadiusFor(progress)
    val shadowElevation = (8f * (progress * 4f).coerceAtMost(1f)).dp
    val palette = LocalEmberbytePalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                this.shadowElevation = shadowElevation.toPx()
                this.shape = RoundedCornerShape(cornerRadius)
                this.clip = cornerRadius > 0.dp
            }
            .background(palette.background),
    ) {
        content()
    }
}
