@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette
import kotlin.math.roundToInt

/**
 * The one definition of what going into a sub-screen and coming back look like.
 *
 * - Pushing a screen is the mirror image of popping it: the new screen grows from [SHRINK] and slides in a little,
 *   the screen underneath settles back to [BEHIND_SCALE_START] and dims; popping reverses both.
 * - A committed push or pop uses the Material motion scheme.
 * - A predictive back gesture uses a LINEAR 300 ms animation, so the screen follows the finger one to one
 *   (an eased curve would run ahead of the finger).
 * - Corner rounding, shadow and the shrink of a guarded form are computed from the same progress by the same functions.
 */
object BackTransitions {
    const val SHRINK = 0.90f
    /** How far the leaving screen slides, as a fraction of the screen width. */
    const val SLIDE_FRACTION = 0.08f
    const val BEHIND_SCALE_START = 0.94f
    const val CORNER_DP = 32
    const val REDUCED_MOTION_DURATION_MS = 90
    const val SCRIM_START_ALPHA = 0.55f

    const val PREDICTIVE_DURATION_MS = 300
    /** The leaving screen stays opaque until this fraction of the gesture, then fades out. */
    const val PREDICTIVE_FADE_START = 0.85f
    val PREDICTIVE_EASING: Easing = LinearEasing

    const val PUSH_ENTER_SCALE = SHRINK
    const val PUSH_EXIT_SCALE = BEHIND_SCALE_START
    const val POP_ENTER_SCALE = BEHIND_SCALE_START
    const val POP_EXIT_SCALE = SHRINK

    val cancelSpring = spring<Float>(dampingRatio = 0.82f, stiffness = 380f)

    fun cornerRadiusFor(progress: Float): Dp {
        val clamped = progress.coerceIn(0f, 1f)
        val factor = (clamped * 4f).coerceAtMost(1f)
        return (CORNER_DP * factor).dp
    }

    fun shrinkFor(progress: Float): Float = 1f - (1f - SHRINK) * progress.coerceIn(0f, 1f)

    fun shadowDpFor(progress: Float): Dp {
        val factor = (progress.coerceIn(0f, 1f) * 4f).coerceAtMost(1f)
        return (8f * factor).dp
    }

    fun transformOriginFor(direction: LayoutDirection): TransformOrigin {
        return if (direction == LayoutDirection.Ltr) {
            TransformOrigin(pivotFractionX = 1.0f, pivotFractionY = 0.5f)
        } else {
            TransformOrigin(pivotFractionX = 0.0f, pivotFractionY = 0.5f)
        }
    }

    fun slideSignFor(direction: LayoutDirection): Int = if (direction == LayoutDirection.Ltr) 1 else -1

    /** The slide distance in pixels for a screen [widthPx] wide. */
    fun slideOffsetPx(widthPx: Int, direction: LayoutDirection): Int =
        (widthPx * SLIDE_FRACTION * slideSignFor(direction)).roundToInt()

    // ---- committed (non-gesture) push and pop, from the motion scheme ----

    fun pushEnter(motion: MotionScheme, direction: LayoutDirection): EnterTransition =
        scaleIn(
            animationSpec = motion.defaultSpatialSpec<Float>(),
            initialScale = PUSH_ENTER_SCALE,
            transformOrigin = transformOriginFor(direction),
        ) + slideInHorizontally(motion.defaultSpatialSpec()) { slideOffsetPx(it, direction) } +
            fadeIn(motion.defaultEffectsSpec())

    fun pushExit(motion: MotionScheme): ExitTransition =
        scaleOut(
            animationSpec = motion.defaultSpatialSpec<Float>(),
            targetScale = PUSH_EXIT_SCALE,
            transformOrigin = TransformOrigin.Center,
        ) + fadeOut(motion.defaultEffectsSpec(), targetAlpha = SCRIM_START_ALPHA)

    fun popEnter(motion: MotionScheme): EnterTransition =
        scaleIn(
            animationSpec = motion.defaultSpatialSpec<Float>(),
            initialScale = POP_ENTER_SCALE,
            transformOrigin = TransformOrigin.Center,
        ) + fadeIn(motion.defaultEffectsSpec(), initialAlpha = SCRIM_START_ALPHA)

    fun popExit(motion: MotionScheme, direction: LayoutDirection): ExitTransition =
        scaleOut(
            animationSpec = motion.defaultSpatialSpec<Float>(),
            targetScale = POP_EXIT_SCALE,
            transformOrigin = transformOriginFor(direction),
        ) + slideOutHorizontally(motion.defaultSpatialSpec()) { slideOffsetPx(it, direction) } +
            fadeOut(motion.defaultEffectsSpec())

    // ---- predictive back gesture: linear, so it follows the finger ----

    private fun <T> predictiveSpec() = tween<T>(durationMillis = PREDICTIVE_DURATION_MS, easing = PREDICTIVE_EASING)

    fun predictivePopEnter(): EnterTransition =
        scaleIn(
            animationSpec = predictiveSpec(),
            initialScale = POP_ENTER_SCALE,
            transformOrigin = TransformOrigin.Center,
        ) + fadeIn(predictiveSpec(), initialAlpha = SCRIM_START_ALPHA)

    fun predictivePopExit(direction: LayoutDirection): ExitTransition {
        val fadeDelay = (PREDICTIVE_DURATION_MS * PREDICTIVE_FADE_START).roundToInt()
        return scaleOut(
            animationSpec = predictiveSpec(),
            targetScale = POP_EXIT_SCALE,
            transformOrigin = transformOriginFor(direction),
        ) + slideOutHorizontally(predictiveSpec()) { slideOffsetPx(it, direction) } +
            fadeOut(
                tween(
                    durationMillis = PREDICTIVE_DURATION_MS - fadeDelay,
                    delayMillis = fadeDelay,
                    easing = PREDICTIVE_EASING,
                ),
            )
    }

    fun popExitReducedMotion(): ExitTransition = fadeOut(animationSpec = tween(REDUCED_MOTION_DURATION_MS))
    fun popEnterReducedMotion(): EnterTransition = fadeIn(animationSpec = tween(REDUCED_MOTION_DURATION_MS))
}

/**
 * A sub-screen's own look while it is being closed: its corners round and a shadow appears, both computed from the
 * transition progress by [BackTransitions.cornerRadiusFor] and [BackTransitions.shadowDpFor]. The progress is linear
 * so a predictive back gesture drives it together with the scale.
 */
@Composable
fun AnimatedVisibilityScope.PredictiveBackSubScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = BackTransitions.PREDICTIVE_DURATION_MS, easing = BackTransitions.PREDICTIVE_EASING)
        },
        label = "subScreenBackProgress",
    ) { state ->
        if (state == EnterExitState.Visible) 0f else 1f
    }
    val cornerRadius = BackTransitions.cornerRadiusFor(progress)
    val shadowElevation = BackTransitions.shadowDpFor(progress)
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
