package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.MorphingNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.BentoType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.liveTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.uploadTile
import kotlin.math.PI
import kotlin.math.sin

/** Which rate the Live tile shows. Session state only: Home keeps it with `rememberSaveable`, never in settings. */
enum class LiveDirection { Download, Upload }

/** Idle floors of the trace scale (bytes per second), so a quiet connection draws a flat, low line. */
internal const val LIVE_TRACE_FLOOR_DOWNLOAD_BPS = 256_000L
internal const val LIVE_TRACE_FLOOR_UPLOAD_BPS = 32_000L

/** Test-only readouts. */
internal val LiveArrowRotationKey = SemanticsPropertyKey<Float>("LiveArrowRotation")
internal val LiveTraceMorphKey = SemanticsPropertyKey<Float>("LiveTraceMorph")
internal val LiveContainerColorKey = SemanticsPropertyKey<Color>("LiveContainerColor")

/**
 * The Live bento tile: speed, a 60-second trace and the other direction as a chip. Pressing it rolls between
 * download and upload (the number rolls, the arrow flips, the trace morphs, the tint changes). All motion uses the
 * motion scheme and switches at once under reduced motion.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiveBentoTile(
    rxBps: Long,
    txBps: Long,
    rxHistory: List<Long>,
    txHistory: List<Long>,
    networkLabel: String,
    units: ByteUnits,
    direction: LiveDirection,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    playEntrance: Boolean = false,
) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val upload = direction == LiveDirection.Upload

    val live = palette.liveTile()
    val uploadColours = palette.uploadTile()
    val tintSpec: FiniteAnimationSpec<Color> = if (reduce) snap() else tween(500)
    val container by animateColorAsState(if (upload) uploadColours.container else live.container, tintSpec, label = "liveContainer")
    val onContainer by animateColorAsState(if (upload) uploadColours.onContainer else live.onContainer, tintSpec, label = "liveOn")
    val secondary by animateColorAsState(
        if (upload) uploadColours.onContainerSecondary else live.onContainerSecondary, tintSpec, label = "liveSecondary",
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (upload) 180f else 0f,
        animationSpec = if (reduce) snap() else spring(dampingRatio = 0.5f, stiffness = 330f),
        label = "liveArrow",
    )
    val morph by animateFloatAsState(
        targetValue = if (upload) 1f else 0f,
        animationSpec = if (reduce) snap() else spring(dampingRatio = 0.62f, stiffness = 240f),
        label = "liveTraceMorph",
    )

    val shape = remember { mixedCorners(large = 28.dp, small = 8.dp, smallCorner = Corner.BottomEnd) }
    val interactionSource = remember { MutableInteractionSource() }
    val ambient = rememberAmbientSeconds(enabled = true)
    val motion = rememberEmberbyteMotion()

    // The speed number is display type: it follows the user's font scale up to 1.3x, then stays the same physical size so
    // it never overflows the tile (the unit and labels keep scaling).
    val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
    val numberSp = (44f * minOf(fontScale, 1.3f) / fontScale).sp

    val actionLabel = if (upload) "Show download speed" else "Show upload speed"
    val stateLabel = if (upload) "Showing upload speed" else "Showing download speed"

    Box(
        modifier = modifier
            .bentoEntrance(order = 0, play = playEntrance)
            .heightIn(min = 212.dp)
            .pressMorph(shape, pressedScale = 0.96f, pressedCorner = 16.dp, interactionSource = interactionSource)
            .clip(shape)
            .background(container)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = actionLabel,
                role = Role.Button,
                onClick = onToggle,
            )
            .semantics(mergeDescendants = true) {
                stateDescription = stateLabel
                set(LiveArrowRotationKey, arrowRotation)
                set(LiveTraceMorphKey, morph)
                set(LiveContainerColorKey, container)
            }
            .testTag("live_bento_tile"),
    ) {
        // A faint corner shape that turns slowly (9 degrees per second); still under reduced motion.
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 26.dp, y = 26.dp)
                .size(96.dp)
                .graphicsLayer { rotationZ = ambient.value * 9f; alpha = 0.13f }
                .background(onContainer, MaterialShapes.Cookie9Sided.toShape()),
        )

        Column(
            // Fills a bounded parent (the side-by-side row) and otherwise grows with its content (the stacked
            // layout at large font scales); the 180 dp minimum plus padding gives the 212 dp tile.
            Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp).heightIn(min = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ForceLtr {
                Column {
                    Text(
                        text = "Live · $networkLabel",
                        style = EmberbyteType.grotesk(BentoType.Label, 600),
                        color = onContainer,
                        maxLines = 1,
                    )
                    AnimatedContent(
                        targetState = direction,
                        transitionSpec = rollSpec(reduce, motion.spatial(), motion.effects(), 0.6f),
                        label = "liveDirectionLabel",
                    ) { dir ->
                        Text(
                            text = if (dir == LiveDirection.Upload) "Upload" else "Download",
                            style = EmberbyteType.grotesk(BentoType.Sub, 500),
                            color = secondary,
                            maxLines = 1,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArrowDisc(rotationDeg = arrowRotation, disc = onContainer, glyph = container)
                    AnimatedContent(
                        targetState = direction,
                        transitionSpec = numberRollSpec(reduce),
                        label = "liveNumber",
                    ) { dir ->
                        val bps = if (dir == LiveDirection.Upload) txBps else rxBps
                        val formatted = formatBytes(bps, units)
                        MorphingNumber(
                            value = formatted.value,
                            unit = "${formatted.unit}/s",
                            style = EmberbyteType.grotesk(numberSp, 600).copy(letterSpacing = (-0.03).em),
                            unitStyle = EmberbyteType.grotesk(BentoType.Unit, 500),
                            color = onContainer,
                            contentDescription = "${if (dir == LiveDirection.Upload) "Upload" else "Download"} " +
                                "${formatted.value} ${formatted.unit} per second",
                        )
                    }
                }
            }

            // The trace fills the free space between the number and the chip.
            val rxNorm = remember(rxHistory) { normalise(rxHistory, LIVE_TRACE_FLOOR_DOWNLOAD_BPS) }
            val txNorm = remember(txHistory) { normalise(txHistory, LIVE_TRACE_FLOOR_UPLOAD_BPS) }
            Canvas(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .clearAndSetSemantics { },
            ) {
                val series = blend(rxNorm, txNorm, morph)
                drawTrace(series, onContainer, ambient.value)
            }

            ForceLtr {
                AnimatedContent(
                    targetState = direction,
                    transitionSpec = rollSpec(reduce, motion.spatial(), motion.effects(), 0.7f),
                    label = "liveChip",
                ) { dir ->
                    val otherBps = if (dir == LiveDirection.Upload) rxBps else txBps
                    val other = formatBytes(otherBps, units)
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(onContainer.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("live_other_direction_chip"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("⇅", style = EmberbyteType.grotesk(BentoType.Sub, 600), color = secondary)
                        Text(
                            text = "${if (dir == LiveDirection.Upload) "↓" else "↑"} ${other.value} ${other.unit}/s",
                            style = EmberbyteType.grotesk(BentoType.Sub, 600),
                            color = onContainer,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** A 24 dp filled disc with a 13 dp arrow cut out of it, turning with [rotationDeg]. */
@Composable
private fun ArrowDisc(rotationDeg: Float, disc: Color, glyph: Color) {
    Box(
        Modifier
            .size(24.dp)
            .graphicsLayer { rotationZ = rotationDeg }
            .background(disc, CircleShape)
            .testTag("live_arrow_disc"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = glyph, modifier = Modifier.size(13.dp))
    }
}

private fun rollSpec(
    reduce: Boolean,
    spatial: FiniteAnimationSpec<IntOffset>,
    effects: FiniteAnimationSpec<Float>,
    fraction: Float,
): androidx.compose.animation.AnimatedContentTransitionScope<LiveDirection>.() -> androidx.compose.animation.ContentTransform =
    {
        if (reduce) {
            EnterTransition.None togetherWith ExitTransition.None
        } else {
            (slideInVertically(spatial) { (it * fraction).toInt() } + fadeIn(effects)) togetherWith
                (slideOutVertically(spatial) { -(it * fraction).toInt() } + fadeOut(effects))
        }
    }

/** The speed number: the old one slides up 105 percent and fades, the new one rolls in from 105 percent below. */
private fun numberRollSpec(
    reduce: Boolean,
): androidx.compose.animation.AnimatedContentTransitionScope<LiveDirection>.() -> androidx.compose.animation.ContentTransform =
    {
        if (reduce) {
            EnterTransition.None togetherWith ExitTransition.None
        } else {
            (
                slideInVertically(spring(dampingRatio = 0.6f, stiffness = 380f)) { (it * 1.05f).toInt() } +
                    fadeIn(tween(240))
                ) togetherWith (
                slideOutVertically(tween(380)) { -(it * 1.05f).toInt() } + fadeOut(tween(380))
                )
        }
    }

/** A series scaled to 0..1 against its own maximum with a floor. */
internal fun normalise(series: List<Long>, floorBps: Long): List<Float> {
    val scale = traceScale(series, floorBps).toFloat()
    return series.map { (it.toFloat() / scale).coerceIn(0f, 1f) }
}

/** Point-by-point blend of two normalised series (aligned at the newest sample); a missing point reads as 0. */
internal fun blend(a: List<Float>, b: List<Float>, t: Float): List<Float> {
    val n = maxOf(a.size, b.size)
    return List(n) { i ->
        val av = a.getOrElse(i - (n - a.size)) { 0f }
        val bv = b.getOrElse(i - (n - b.size)) { 0f }
        av + (bv - av) * t
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrace(series: List<Float>, colour: Color, seconds: Float) {
    if (series.size < 2) {
        // Nothing sampled yet: a flat low line.
        val y = size.height * 0.9f
        drawLine(colour.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
        return
    }
    val step = size.width / 59f
    val frac = (seconds % 1f)
    val top = 4.dp.toPx()
    val bottom = size.height - 4.dp.toPx()
    fun x(i: Int) = size.width - (series.size - 1 - i) * step - frac * step
    fun y(v: Float) = bottom - (bottom - top) * v

    val line = Path()
    line.moveTo(x(0), y(series[0]))
    for (i in 1 until series.size) {
        val mx = (x(i - 1) + x(i)) / 2f
        val my = (y(series[i - 1]) + y(series[i])) / 2f
        line.quadraticTo(x(i - 1), y(series[i - 1]), mx, my)
    }
    line.lineTo(x(series.size - 1), y(series.last()))
    line.lineTo(size.width, y(series.last()))

    val fill = Path().apply {
        addPath(line)
        lineTo(size.width, size.height)
        lineTo(x(0), size.height)
        close()
    }
    drawPath(fill, Brush.verticalGradient(listOf(colour.copy(alpha = 0.28f), colour.copy(alpha = 0f))))
    drawPath(line, colour, style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))

    // The end point pulses around 3.6 dp at about 0.8 Hz.
    val radius = (3.6f + 1.1f * sin(2f * PI.toFloat() * 0.8f * seconds)).dp.toPx()
    drawCircle(colour, radius, Offset(size.width, y(series.last())))
}
