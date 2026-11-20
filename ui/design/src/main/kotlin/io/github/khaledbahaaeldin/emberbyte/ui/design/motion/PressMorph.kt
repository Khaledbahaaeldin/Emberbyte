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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteShapeRadii
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

val PressMorphShapeKey = SemanticsPropertyKey<Shape>("PressMorphShape")
val PressMorphScaleKey = SemanticsPropertyKey<() -> Float>("PressMorphScale")

/**
 * Material 3 Expressive corner-morphing and scale press feedback.
 *
 * Morphs the provided [shape] corners to [pressedCorner] and scales the element to [pressedScale]
 * when pressed, using the motion scheme's fast spatial spring.
 * Snaps immediately when [LocalReduceMotion] is active.
 *
 * All animations execute in the graphics layer/draw phase to prevent per-frame recomposition.
 */
fun Modifier.pressMorph(
    shape: CornerBasedShape,
    pressedScale: Float = 0.97f,
    pressedCorner: Dp = EmberbyteShapeRadii.Small,
    interactionSource: InteractionSource? = null,
): Modifier = composed {
    var pointerPressed by remember { mutableStateOf(false) }
    val sourcePressed = if (interactionSource != null) {
        val pressed by interactionSource.collectIsPressedAsState()
        pressed
    } else {
        false
    }
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

    val dynamicShape = remember(shape, pressedCorner) {
        DynamicCornerBasedShape(shape, pressedCorner) { animatable.value }
    }

    val pointerMod = if (interactionSource == null) {
        Modifier.pointerInput(Unit) {
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
    } else {
        Modifier
    }

    this
        .then(pointerMod)
        .semantics {
            set(PressMorphShapeKey, dynamicShape)
            set(PressMorphScaleKey) { 1f - (1f - pressedScale) * animatable.value }
        }
        .graphicsLayer {
            val progress = animatable.value
            scaleX = 1f - (1f - pressedScale) * progress
            scaleY = 1f - (1f - pressedScale) * progress
            this.shape = dynamicShape
            this.clip = true
        }
}

private class DynamicCornerBasedShape(
    private val baseShape: CornerBasedShape,
    private val pressedCorner: Dp,
    private val progress: () -> Float,
) : CornerBasedShape(
    topStart = DynamicCornerSize(baseShape.topStart, pressedCorner, progress),
    topEnd = DynamicCornerSize(baseShape.topEnd, pressedCorner, progress),
    bottomEnd = DynamicCornerSize(baseShape.bottomEnd, pressedCorner, progress),
    bottomStart = DynamicCornerSize(baseShape.bottomStart, pressedCorner, progress),
) {
    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): CornerBasedShape = baseShape.copy(topStart, topEnd, bottomEnd, bottomStart)

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline = baseShape.createOutline(size, topStart, topEnd, bottomEnd, bottomStart, layoutDirection)
}

private class DynamicCornerSize(
    private val base: CornerSize,
    private val pressed: Dp,
    private val progress: () -> Float,
) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float {
        val basePx = base.toPx(shapeSize, density)
        val pressedPx = with(density) { pressed.toPx() }
        val p = progress()
        return basePx + (pressedPx - basePx) * p
    }
}
