package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/**
 * Material 3 Expressive corner-morphing and scale press feedback.
 *
 * Morphs the provided [shape] corners to [pressedCorner] and scales the element to [pressedScale]
 * when pressed, using the motion scheme's fast spatial spring.
 * Snaps immediately when [LocalReduceMotion] is active.
 */
fun Modifier.pressMorph(
    shape: CornerBasedShape,
    pressedScale: Float = 0.97f,
    pressedCorner: Dp = 12.dp,
    interactionSource: InteractionSource? = null,
): Modifier = composed {
    var pointerPressed by remember { mutableStateOf(false) }
    val sourcePressed by (interactionSource?.collectIsPressedAsState() ?: remember { mutableStateOf(false) })
    val isPressed = pointerPressed || sourcePressed

    val animatable = remember { Animatable(0f) }
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current

    LaunchedEffect(isPressed, reduceMotion) {
        val target = if (isPressed) 1f else 0f
        if (reduceMotion) {
            animatable.snapTo(target)
        } else {
            animatable.animateTo(target, motion.fastSpatial())
        }
    }

    val progress = animatable.value
    val scale = 1f - (1f - pressedScale) * progress

    val currentShape = remember(shape, pressedCorner, progress) {
        shape.copy(
            topStart = MorphedCornerSize(shape.topStart, pressedCorner, progress),
            topEnd = MorphedCornerSize(shape.topEnd, pressedCorner, progress),
            bottomEnd = MorphedCornerSize(shape.bottomEnd, pressedCorner, progress),
            bottomStart = MorphedCornerSize(shape.bottomStart, pressedCorner, progress),
        )
    }

    this
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pointerPressed = true
                try {
                    waitForUpOrCancellation()
                } finally {
                    pointerPressed = false
                }
            }
        }
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            shape = currentShape,
            clip = true,
        )
}

data class MorphedCornerSize(
    val base: CornerSize,
    val pressed: Dp,
    val progress: Float,
) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float {
        val basePx = base.toPx(shapeSize, density)
        val pressedPx = with(density) { pressed.toPx() }
        return basePx + (pressedPx - basePx) * progress
    }
}
