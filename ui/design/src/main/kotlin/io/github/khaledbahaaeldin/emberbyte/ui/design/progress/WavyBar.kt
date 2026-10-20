package io.github.khaledbahaaeldin.emberbyte.ui.design.progress

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

internal fun clampBarFraction(fraction: Float): Float {
    return if (fraction.isNaN() || fraction < 0f) 0f else minOf(fraction, 1f)
}

/**
 * Expressive wavy bar progress indicator (Contract 13.1, Task A11).
 *
 * Renders a wavy filled segment in [color], a flat dim remainder in [EmberbytePalette.track],
 * with round caps on both ends.
 *
 * Clamps fraction between 0f and 1f and exposes progress semantics via [ProgressBarRangeInfo].
 * Animates with [MaterialTheme.motionScheme.defaultSpatialSpec], snapping when [LocalReduceMotion] is active.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    onAnimationSpecResolved: ((AnimationSpec<Float>) -> Unit)? = null,
) {
    val clamped = clampBarFraction(fraction)
    val reduceMotion = LocalReduceMotion.current
    val animSpec: AnimationSpec<Float> = if (reduceMotion) snap() else MaterialTheme.motionScheme.defaultSpatialSpec()
    onAnimationSpecResolved?.invoke(animSpec)

    val animatedFraction by animateFloatAsState(
        targetValue = clamped,
        animationSpec = animSpec,
        label = "wavy_bar",
    )

    val palette = LocalEmberbytePalette.current
    val trackColor = palette.track

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = clamped,
                    range = 0f..1f,
                    steps = 0,
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val cy = size.height / 2f
            val startX = strokeWidth / 2f
            val endX = size.width - strokeWidth / 2f
            val availableWidth = endX - startX

            // 1. Flat dim remainder with round caps
            drawLine(
                color = trackColor,
                start = Offset(startX, cy),
                end = Offset(endX, cy),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            // 2. Wavy filled part with round caps
            if (animatedFraction > 0f && availableWidth > 0f) {
                val fillEndX = startX + availableWidth * animatedFraction
                val fillPath = Path()
                val wavelengthPx = 16.dp.toPx()
                val amplitude = (size.height / 2f - strokeWidth / 2f).coerceAtLeast(1f)
                val fillWidth = fillEndX - startX
                val steps = maxOf(4, (fillWidth / 2.dp.toPx()).toInt())
                val dx = fillWidth / steps

                for (i in 0..steps) {
                    val x = startX + i * dx
                    val wavePhase = (x - startX) / wavelengthPx * 2f * Math.PI.toFloat()
                    val y = cy + amplitude * kotlin.math.sin(wavePhase)
                    if (i == 0) {
                        fillPath.moveTo(x, y)
                    } else {
                        fillPath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = fillPath,
                    color = color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
    }
}
