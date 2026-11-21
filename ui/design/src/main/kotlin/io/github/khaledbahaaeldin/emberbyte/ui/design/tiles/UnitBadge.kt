@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/** Test-only readout of the badge's current rotation, in degrees. */
internal val UnitBadgeRotationKey = SemanticsPropertyKey<Float>("UnitBadgeRotation")

/** Test-only readout of the label's counter-rotation, in degrees. */
internal val UnitBadgeLabelRotationKey = SemanticsPropertyKey<Float>("UnitBadgeLabelRotation")

/**
 * Compact cookie-shaped badge displaying a unit of data measurement (Contract 13.1).
 *
 * Rendered with [MaterialShapes.Cookie9Sided], with primary badge color and onPrimary text.
 * Hidden from the accessibility semantics tree as the parent hero number or row provides
 * the full accessible plain-language description.
 *
 * With [spinDegPerSec] above zero the shape turns continuously (Home passes a speed that follows the live rate) while
 * the label is counter-rotated so it always reads upright. The angle lives in a graphics layer, so nothing recomposes
 * per frame, and the loop only runs while the screen is started. Reduced motion keeps the badge still.
 */
@Composable
fun UnitBadge(
    unit: String,
    size: Dp = 62.dp,
    modifier: Modifier = Modifier,
    spinDegPerSec: Float = 0f,
) {
    val palette = LocalEmberbytePalette.current
    val badgeShape = MaterialShapes.Cookie9Sided.toShape()
    val textSize = (size.value * 0.28f).sp

    val reduceMotion = LocalReduceMotion.current
    val speed by rememberUpdatedState(if (reduceMotion) 0f else spinDegPerSec)
    var angle by remember { mutableFloatStateOf(0f) }
    val spins = spinDegPerSec > 0f && !reduceMotion
    if (spins) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var last = 0L
                while (true) {
                    val now = withInfiniteAnimationFrameNanos { it }
                    if (last == 0L) {
                        last = now
                    } else if (now - last >= AMBIENT_FRAME_NANOS) {
                        angle = (angle + speed * (now - last) / 1_000_000_000f) % 360f
                        last = now
                    }
                }
            }
        }
    }

    ForceLtr {
        Box(
            modifier = modifier
                .size(size)
                .graphicsLayer { rotationZ = angle }
                .background(
                    color = palette.primary,
                    shape = badgeShape,
                )
                .clearAndSetSemantics {
                    set(UnitBadgeRotationKey, angle)
                    set(UnitBadgeLabelRotationKey, -angle)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = unit,
                modifier = Modifier
                    .graphicsLayer { rotationZ = -angle }
                    .clearAndSetSemantics { },
                color = palette.onPrimary,
                style = EmberbyteType.badge(textSize),
                maxLines = 1,
            )
        }
    }
}
