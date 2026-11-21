package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/** Test-only readout of the reveal progress (0 = the button bounds, 1 = the full screen). */
internal val ContainerRevealFractionKey = SemanticsPropertyKey<Float>("ContainerRevealFraction")

/** Closing takes about 380 ms; opening is a spring of about 620 ms. */
private const val CLOSE_MS = 380

/**
 * A hand-built container transform: the screen is clipped to [origin] (the bounds of the button it grows out of) and
 * the clip grows to the full window, rounded corners easing to square. [closeRequested] plays it backwards and then
 * calls [onClosed]. Without an [origin], or with reduced motion, the screen is simply there and closing is immediate.
 */
@Composable
fun ContainerReveal(
    origin: Rect?,
    closeRequested: Boolean,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduce = LocalReduceMotion.current
    val animated = origin != null && !reduce
    val progress = remember { Animatable(if (animated) 0f else 1f) }
    val onClosedNow by rememberUpdatedState(onClosed)

    LaunchedEffect(Unit) {
        if (animated) progress.animateTo(1f, spring(dampingRatio = 0.9f, stiffness = 130f))
    }
    LaunchedEffect(closeRequested) {
        if (closeRequested) {
            if (animated) progress.animateTo(0f, tween(CLOSE_MS))
            onClosedNow()
        }
    }

    val shape = remember(origin) { RevealShape(origin) { progress.value } }
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { set(ContainerRevealFractionKey, progress.value) }
            .testTag("reveal_root")
            .graphicsLayer {
                if (animated) {
                    this.shape = shape
                    clip = true
                }
            },
    ) {
        content()
    }
}

private class RevealShape(private val origin: Rect?, private val progress: () -> Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val full = Rect(0f, 0f, size.width, size.height)
        val from = origin ?: return Outline.Rectangle(full)
        val p = progress().coerceIn(0f, 1f)
        fun lerp(a: Float, b: Float) = a + (b - a) * p
        val rect = Rect(lerp(from.left, 0f), lerp(from.top, 0f), lerp(from.right, size.width), lerp(from.bottom, size.height))
        val radius = with(density) { lerp(27.dp.toPx(), 0f) }
        return Outline.Rounded(RoundRect(rect, CornerRadius(radius, radius)))
    }

    override fun equals(other: Any?): Boolean = other is RevealShape && other.origin == origin
    override fun hashCode(): Int = origin.hashCode()
}
