package io.github.khaledbahaaeldin.emberbyte.ui.design.progress

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette

internal fun ringColors(state: BudgetStateUi, palette: EmberbytePalette): Pair<Color, Color> {
    val arcColor = when (state) {
        BudgetStateUi.Ok -> palette.primary
        BudgetStateUi.Warning -> palette.warn
        BudgetStateUi.Exceeded -> palette.danger
    }
    return arcColor to palette.track
}

internal fun ringOutlineColor(palette: EmberbytePalette): Color = palette.muted

internal fun effectiveSweep(fraction: Float): Float {
    return if (fraction.isNaN() || fraction < 0f) 0f else minOf(fraction, 1f)
}

private fun buildWavyCirclePath(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    amplitude: Float,
    waves: Int,
    startAngleRad: Float,
    sweepAngleRad: Float,
): Path {
    val path = Path()
    val steps = maxOf(4, (16 * waves * (sweepAngleRad / (2f * Math.PI.toFloat()))).toInt())
    val dTheta = sweepAngleRad / steps
    for (i in 0..steps) {
        val currentAngle = startAngleRad + i * dTheta
        val phase = i * dTheta
        val r = baseRadius + amplitude * kotlin.math.cos(waves * phase)
        val x = cx + r * kotlin.math.cos(currentAngle)
        val y = cy + r * kotlin.math.sin(currentAngle)
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    return path
}

/**
 * Expressive wavy ring progress indicator for budgets and plans (Contract 13.1, Task A11).
 *
 * Draws a 360-degree wavy track in [EmberbytePalette.track], and an arc in the state color:
 * - [BudgetStateUi.Ok]: [EmberbytePalette.primary]
 * - [BudgetStateUi.Warning]: [EmberbytePalette.warn]
 * - [BudgetStateUi.Exceeded]: [EmberbytePalette.danger]
 *
 * 0 percent draws a thin outline ring in [EmberbytePalette.muted].
 * Fractions above 1 draw a full ring.
 *
 * Center label uses [EmberbyteType.value] and is wrapped in [ForceLtr].
 * At font scales > 1.3, the label size is capped to prevent clipping against the ring stroke.
 *
 * Warning and exceeded states add a burst "!" mark in the top-end corner.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyRing(
    fraction: Float,
    state: BudgetStateUi,
    size: Dp,
    label: String,
    modifier: Modifier = Modifier,
    labelSize: TextUnit = 17.sp,
    onAnimationSpecResolved: ((AnimationSpec<Float>) -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    val (arcColor, trackColor) = ringColors(state, palette)
    val outlineColor = ringOutlineColor(palette)

    val reduceMotion = LocalReduceMotion.current
    val animSpec: AnimationSpec<Float> = if (reduceMotion) snap() else MaterialTheme.motionScheme.defaultSpatialSpec()
    onAnimationSpecResolved?.invoke(animSpec)

    val sweep by animateFloatAsState(
        targetValue = effectiveSweep(fraction),
        animationSpec = animSpec,
        label = "wavy_ring",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = this.size.minDimension * 0.12f
            val amplitude = strokeWidth * 0.25f
            val baseRadius = (this.size.minDimension - strokeWidth) / 2f - amplitude
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val waves = 12
            val startAngle = -Math.PI.toFloat() / 2f

            // 1. Wavy 360-degree track
            val trackPath = buildWavyCirclePath(
                cx = cx,
                cy = cy,
                baseRadius = baseRadius,
                amplitude = amplitude,
                waves = waves,
                startAngleRad = startAngle,
                sweepAngleRad = 2f * Math.PI.toFloat(),
            )
            drawPath(
                path = trackPath,
                color = trackColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // 2. At 0 percent: thin muted outline ring
            if (sweep == 0f) {
                drawCircle(
                    color = outlineColor,
                    radius = baseRadius,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }

            // 3. Progress arc in state color
            if (sweep > 0f) {
                val arcPath = buildWavyCirclePath(
                    cx = cx,
                    cy = cy,
                    baseRadius = baseRadius,
                    amplitude = amplitude,
                    waves = waves,
                    startAngleRad = startAngle,
                    sweepAngleRad = sweep * 2f * Math.PI.toFloat(),
                )
                drawPath(
                    path = arcPath,
                    color = arcColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        // Center label: EmberbyteType.value, ForceLtr, capped at font scale > 1.3
        val fontScale = LocalDensity.current.fontScale
        val effectiveLabelSize = if (fontScale > 1.3f) {
            labelSize * (1.3f / fontScale)
        } else if (fontScale > 1.2f && label.length >= 4) {
            labelSize * (1.3f / fontScale)
        } else {
            labelSize
        }

        ForceLtr {
            Text(
                text = label,
                style = EmberbyteType.value(effectiveLabelSize),
                color = palette.text,
                maxLines = 1,
            )
        }

        // Warning or exceeded state adds the burst "!" mark
        if (state != BudgetStateUi.Ok) {
            val markDesc = if (state == BudgetStateUi.Exceeded) "Over the cap" else "Nearly used up"
            val markBg = if (state == BudgetStateUi.Exceeded) palette.danger else palette.warn
            val markFg = if (state == BudgetStateUi.Exceeded) palette.onDanger else palette.onWarn
            val burstShape = MaterialShapes.SoftBurst.toShape()

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.28f)
                    .clip(burstShape)
                    .background(markBg)
                    .semantics { contentDescription = markDesc },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "!",
                    style = EmberbyteType.badge(size.value.sp * 0.20f),
                    color = markFg,
                )
            }
        }
    }
}
