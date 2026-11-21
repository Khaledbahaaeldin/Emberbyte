package io.github.khaledbahaaeldin.emberbyte.ui.design.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import kotlinx.coroutines.delay
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
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

private fun buildWavyCirclePathTo(
    path: Path,
    cx: Float,
    cy: Float,
    baseRadius: Float,
    amplitude: Float,
    waves: Int,
    startAngleRad: Float,
    sweepAngleRad: Float,
    phaseOffset: Float = 0f,
) {
    val steps = maxOf(4, (16 * waves * (sweepAngleRad / (2f * Math.PI.toFloat()))).toInt())
    val dTheta = sweepAngleRad / steps
    for (i in 0..steps) {
        val currentAngle = startAngleRad + i * dTheta
        val phase = i * dTheta
        val r = baseRadius + amplitude * kotlin.math.cos(waves * phase + phaseOffset)
        val x = cx + r * kotlin.math.cos(currentAngle)
        val y = cy + r * kotlin.math.sin(currentAngle)
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
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
    buildWavyCirclePathTo(path, cx, cy, baseRadius, amplitude, waves, startAngleRad, sweepAngleRad)
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
    delayMillis: Int = 0,
    onAnimationSpecResolved: ((AnimationSpec<Float>) -> Unit)? = null,
    onAnimatedSweepChanged: ((Float) -> Unit)? = null,
    ghost: GhostRing? = null,
) {
    if (ghost != null) {
        GhostWavyRing(size = size, ghost = ghost, modifier = modifier)
        return
    }
    val palette = LocalEmberbytePalette.current
    val (arcColor, trackColor) = ringColors(state, palette)
    val outlineColor = ringOutlineColor(palette)

    val motion = rememberEmberbyteMotion()
    val animSpec: AnimationSpec<Float> = motion.spatial()
    if (onAnimationSpecResolved != null) {
        SideEffect { onAnimationSpecResolved(animSpec) }
    }

    val targetSweep = effectiveSweep(fraction)
    val reduceMotion = LocalReduceMotion.current
    val anim = remember(reduceMotion) { Animatable(if (reduceMotion) targetSweep else 0f) }
    if (onAnimatedSweepChanged != null) {
        LaunchedEffect(anim) {
            snapshotFlow { anim.value }.collect { onAnimatedSweepChanged(it) }
        }
    }
    var hasEntered by remember(reduceMotion) { mutableStateOf(reduceMotion) }
    LaunchedEffect(targetSweep, reduceMotion) {
        if (reduceMotion) {
            anim.snapTo(targetSweep)
        } else {
            if (!hasEntered && delayMillis > 0) {
                delay(delayMillis.toLong())
            }
            hasEntered = true
            anim.animateTo(targetSweep, animSpec)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = targetSweep,
                    range = 0f..1f,
                    steps = 0,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val strokeWidth = this.size.minDimension * 0.12f
                    val amplitude = strokeWidth * 0.25f
                    val baseRadius = (this.size.minDimension - strokeWidth) / 2f - amplitude
                    val cx = this.size.width / 2f
                    val cy = this.size.height / 2f
                    val waves = 12
                    val startAngle = -Math.PI.toFloat() / 2f

                    // Precompute Stroke instances once per cache pass
                    val trackStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    val outlineStroke = Stroke(width = 1.5.dp.toPx())

                    // 1. Wavy 360-degree track precomputed once per size
                    val trackPath = buildWavyCirclePath(
                        cx = cx,
                        cy = cy,
                        baseRadius = baseRadius,
                        amplitude = amplitude,
                        waves = waves,
                        startAngleRad = startAngle,
                        sweepAngleRad = 2f * Math.PI.toFloat(),
                    )
                    val arcPath = Path()

                    onDrawBehind {
                        val sweep = if (reduceMotion) targetSweep else anim.value

                        // 1. Wavy 360-degree track
                        drawPath(
                            path = trackPath,
                            color = trackColor,
                            style = trackStroke,
                        )

                        // 2. At 0 percent: thin muted outline ring
                        if (sweep == 0f) {
                            drawCircle(
                                color = outlineColor,
                                radius = baseRadius,
                                style = outlineStroke,
                            )
                        }

                        // 3. Progress arc in state color
                        if (sweep > 0f) {
                            arcPath.reset()
                            buildWavyCirclePathTo(
                                path = arcPath,
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
                                style = trackStroke,
                            )
                        }
                    }
                }
        ) { }

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
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = markFg,
                )
            }
        }
    }
}

/**
 * Ghost mode of [WavyRing] for the "no plan yet" card: a decorative ring whose arc draws itself to [fraction] once and
 * whose wave then drifts. Not a progress indicator, so it publishes no progress or description.
 */
data class GhostRing(val fraction: Float = 0.7f, val drawDurationMs: Int = 1300)

/** The ghost arc as it draws: linear from 0 to [target] over progress 0..1, clamped. */
internal fun ghostArcFraction(progress: Float, target: Float): Float = progress.coerceIn(0f, 1f) * target

/** Test-only readouts of the ghost ring. */
internal val GhostRingFractionKey = androidx.compose.ui.semantics.SemanticsPropertyKey<Float>("GhostRingFraction")
internal val GhostRingPhaseKey = androidx.compose.ui.semantics.SemanticsPropertyKey<Float>("GhostRingPhase")

/** Wave drift of the ghost ring, in radians per second. */
private const val GHOST_WAVE_RAD_PER_SEC = 2f

@Composable
private fun GhostWavyRing(size: Dp, ghost: GhostRing, modifier: Modifier) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val progress = remember(reduceMotion) { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            progress.animateTo(1f, androidx.compose.animation.core.tween(ghost.drawDurationMs, easing = androidx.compose.animation.core.LinearEasing))
        } else {
            progress.snapTo(1f)
        }
    }
    val seconds = io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.rememberAmbientSeconds(enabled = true)
    val arc = palette.primary
    val track = palette.primary.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                set(GhostRingFractionKey, ghostArcFraction(progress.value, ghost.fraction))
                set(GhostRingPhaseKey, seconds.value * GHOST_WAVE_RAD_PER_SEC)
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * 0.07f
            val radiusBase = (this.size.minDimension - stroke) / 2f
            // Flat (no wave) when animations are reduced.
            val amplitude = if (reduceMotion) 0f else radiusBase * 0.026f
            val baseRadius = radiusBase - amplitude
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val waves = 14
            val start = -Math.PI.toFloat() / 2f
            val phase = if (reduceMotion) 0f else seconds.value * GHOST_WAVE_RAD_PER_SEC
            val style = Stroke(width = stroke, cap = StrokeCap.Round)

            val trackPath = Path()
            buildWavyCirclePathTo(trackPath, cx, cy, baseRadius, amplitude, waves, start, 2f * Math.PI.toFloat(), phase)
            drawPath(trackPath, track, style = style)

            val fraction = ghostArcFraction(progress.value, ghost.fraction)
            if (fraction > 0f) {
                val arcPath = Path()
                buildWavyCirclePathTo(arcPath, cx, cy, baseRadius, amplitude, waves, start, fraction * 2f * Math.PI.toFloat(), phase)
                drawPath(arcPath, arc, style = style)
            }
        }
    }
}
