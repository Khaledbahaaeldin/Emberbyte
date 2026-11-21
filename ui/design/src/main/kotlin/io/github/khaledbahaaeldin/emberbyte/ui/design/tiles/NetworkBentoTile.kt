package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.BentoType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.networkTile
import java.util.Locale


/** Ease-out-back used for the big number's scale (0.9 to 1.0). */
private fun easeOutBack(p: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val t = p - 1f
    return 1f + c3 * t * t * t + c1 * t * t
}

/**
 * The "Today by network" tile: the larger network's bytes as one big number (counted up once per app start), the
 * split bar with a visible mobile segment, and the smaller network underneath.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetworkBentoTile(
    mobileBytes: Long,
    wifiBytes: Long,
    units: ByteUnits,
    playEntrance: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val colours = palette.networkTile()
    val shape = remember { mixedCorners(large = 28.dp, small = 8.dp, smallCorner = Corner.TopStart) }
    val ambient = rememberAmbientSeconds(enabled = true)
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val big = bigNetwork(mobileBytes, wifiBytes)
    val bigFormatted = formatBytes(big.bytes, units)
    val small = if (big.isWifi) "Mobile" to mobileBytes else "Wi-Fi" to wifiBytes
    val smallFormatted = formatBytes(small.second, units)
    val targetValue = bigFormatted.value.toDoubleOrNull() ?: 0.0
    val decimals = bigFormatted.value.substringAfter('.', "").length

    val progress = remember { Animatable(if (playEntrance && !reduce) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (playEntrance && !reduce) {
            progress.animateTo(1f, tween(BENTO_COUNT_UP_MS, delayMillis = BENTO_COUNT_UP_DELAY_MS))
        }
    }
    val shown = countUpValue(progress.value, targetValue)
    val shownText = if (progress.value >= 1f) bigFormatted.value else String.format(Locale.US, "%.${decimals}f", shown)

    Box(
        modifier = modifier
            .bentoEntrance(order = 1, play = playEntrance)
            .heightIn(min = 212.dp)
            .pressMorph(shape, pressedScale = 0.96f, pressedCorner = 16.dp)
            .clip(shape)
            .background(colours.container)
            .semantics(mergeDescendants = true) {
                contentDescription = "Today by network. ${bigFormatted.value} ${bigFormatted.unit} on " +
                    "${if (big.isWifi) "Wi-Fi" else "mobile"}. ${small.first} ${smallFormatted.value} ${smallFormatted.unit}."
            }
            .testTag("network_bento_tile"),
    ) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = (-26).dp, y = (-26).dp)
                .size(96.dp)
                .graphicsLayer { rotationZ = -ambient.value * 7f; alpha = 0.13f }
                .background(colours.onContainer, MaterialShapes.Cookie9Sided.toShape()),
        )
        Column(
            // Fills a bounded parent (the side-by-side row) and otherwise grows with its content (the stacked
            // layout at large font scales); the 180 dp minimum plus padding gives the 212 dp tile.
            Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp).heightIn(min = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Today by network",
                style = EmberbyteType.grotesk(BentoType.Label, 600),
                color = colours.onContainer,
                maxLines = 1,
            )

            ForceLtr {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().heightIn(min = 72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val scale = 0.9f + 0.1f * easeOutBack(progress.value.coerceIn(0f, 1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Auto-sizes between 60 and 36 sp so a long total never clips, even at font scale 2.
                        Text(
                            text = shownText,
                            style = EmberbyteType.grotesk(BentoType.BigMax, 600).copy(letterSpacing = (-0.045).em),
                            color = colours.onContainer,
                            maxLines = 1,
                            softWrap = false,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = BentoType.BigMin,
                                maxFontSize = BentoType.BigMax,
                                stepSize = BentoType.BigStep,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .testTag("network_big_number"),
                        )
                        Text(
                            text = "${bigFormatted.unit} on ${if (big.isWifi) "Wi-Fi" else "mobile"}",
                            style = EmberbyteType.grotesk(BentoType.Caption, 500),
                            color = colours.onContainerSecondary,
                            maxLines = 1,
                            modifier = Modifier.testTag("network_big_caption"),
                        )
                    }
                }
            }

            SplitBar(
                mobileBytes = mobileBytes,
                wifiBytes = wifiBytes,
                mobileColor = palette.primary,
                wifiColor = palette.accent,
                modifier = Modifier.fillMaxWidth().height(14.dp),
            )
            ForceLtr {
                Text(
                    text = "${small.first} ${smallFormatted.value} ${smallFormatted.unit}",
                    style = EmberbyteType.grotesk(BentoType.Small, 500),
                    color = colours.onContainerSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("network_small_line"),
                )
            }
        }
    }
}

/**
 * The split bar of the network tile: a mobile segment (never thinner than 14 dp when there is mobile data) and a Wi-Fi
 * segment, 3 dp apart. A plain layout, because a `BoxWithConstraints` would break the equal-height row of the tile pair
 * (it does not support intrinsic measurements).
 */
@Composable
private fun SplitBar(
    mobileBytes: Long,
    wifiBytes: Long,
    mobileColor: Color,
    wifiColor: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Layout(
        modifier = modifier,
        content = {
            if (mobileBytes > 0L) Box(Modifier.clip(androidx.compose.foundation.shape.CircleShape).background(mobileColor).testTag("network_bar_mobile"))
            if (wifiBytes > 0L) Box(Modifier.clip(androidx.compose.foundation.shape.CircleShape).background(wifiColor).testTag("network_bar_wifi"))
        },
    ) { measurables, constraints ->
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else 0
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else with(density) { 14.dp.roundToPx() }
        val gap = with(density) { 3.dp.roundToPx() }
        val hasBoth = measurables.size == 2
        val available = if (hasBoth) (width - gap).coerceAtLeast(0) else width
        val widthsPx = when {
            hasBoth -> {
                val mobilePx = with(density) { mobileSegmentWidth(mobileBytes, wifiBytes, available / density.density).dp.roundToPx() }
                    .coerceIn(0, available)
                intArrayOf(mobilePx, available - mobilePx)
            }
            measurables.size == 1 -> intArrayOf(available)
            else -> intArrayOf()
        }
        val placeables = measurables.mapIndexed { i, m ->
            m.measure(androidx.compose.ui.unit.Constraints.fixed(widthsPx[i], height))
        }
        layout(width, height) {
            var x = 0
            placeables.forEach {
                it.placeRelative(x, 0)
                x += it.width + gap
            }
        }
    }
}
