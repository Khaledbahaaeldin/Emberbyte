package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.MorphingNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.UsageType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.liveTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon

/** Test-only readout of a row's share fill (0..1). */
internal val RowShareFillKey = SemanticsPropertyKey<Float>("RowShareFill")

/**
 * One ranked app. The first row of the card is the featured one (bigger icon, name and amount). A tap expands the row
 * inline to show the Wi-Fi and mobile split and an "App details" action; the caller keeps one row open at a time.
 */
@Composable
internal fun AppUsageRowItem(
    ranked: RankedApp,
    first: Long,
    featured: Boolean,
    expanded: Boolean,
    dayWord: String,
    units: ByteUnits,
    icon: ImageBitmap?,
    onToggle: () -> Unit,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val shape = RoundedCornerShape(if (featured) 26.dp else 22.dp)
    val interaction = remember { MutableInteractionSource() }
    val fillTarget = shareFillFraction(ranked.amountBytes, first)
    val fill = remember { Animatable(fillTarget) }
    LaunchedEffect(fillTarget, reduce) {
        if (reduce) fill.snapTo(fillTarget) else fill.animateTo(fillTarget, motion.spatial())
    }
    val amount = formatBytes(ranked.amountBytes, units)
    val wifi = formatBytes(ranked.row.wifiBytes, units)
    val mobile = formatBytes(ranked.row.mobileBytes, units)
    val tile = palette.liveTile()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressMorph(shape, pressedScale = 0.97f, pressedCorner = 16.dp, interactionSource = interaction)
            .clip(shape)
            .background(palette.text.copy(alpha = 0.07f))
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onToggle)
            .semantics(mergeDescendants = true) {
                stateDescription = if (expanded) "Expanded" else "Collapsed"
                set(RowShareFillKey, fill.value)
            }
            .testTag("usage_row_${ranked.row.packageName}"),
    ) {
        // The share fill is drawn behind the row, a rounded rectangle growing from the start edge.
        val cornerRadius = if (featured) 26.dp else 22.dp
        Box(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    val width = size.width * fill.value.coerceIn(0f, 1f)
                    val fromStart = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Ltr
                    drawRoundRect(
                        color = tile.container,
                        topLeft = androidx.compose.ui.geometry.Offset(if (fromStart) 0f else size.width - width, 0f),
                        size = androidx.compose.ui.geometry.Size(width, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                    )
                },
        ) {
            ForceLtr {
                Row(
                    modifier = Modifier
                        .heightIn(min = if (featured) 88.dp else 64.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppIcon(
                        label = ranked.row.label,
                        icon = icon,
                        shape = shapeFor(ranked.row.packageName),
                        size = if (featured) 60.dp else 44.dp,
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = ranked.row.label,
                            style = EmberbyteType.grotesk(if (featured) UsageType.FeaturedName else UsageType.RowName, 700),
                            color = palette.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (featured) {
                                "Wi-Fi ${wifi.value} ${wifi.unit} · Mobile ${mobile.value} ${mobile.unit}"
                            } else {
                                "${ranked.percentOfDay}% of $dayWord"
                            },
                            style = EmberbyteType.grotesk(if (featured) UsageType.FeaturedSub else UsageType.RowSub, 500),
                            color = palette.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        MorphingNumber(
                            value = amount.value,
                            unit = amount.unit,
                            style = EmberbyteType.grotesk(if (featured) UsageType.FeaturedAmount else UsageType.RowAmount, 700)
                                .copy(letterSpacing = (-0.02).em),
                            unitStyle = EmberbyteType.grotesk(UsageType.RowSub, 600),
                            color = palette.text,
                            contentDescription = "${ranked.row.label}, ${amount.value} ${amount.unit}",
                        )
                        if (featured) {
                            Text(
                                text = "${ranked.percentOfDay}% of $dayWord",
                                style = EmberbyteType.grotesk(UsageType.RowSub, 500),
                                color = palette.muted,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = if (reduce) androidx.compose.animation.EnterTransition.None else expandVertically(motion.spatial()) + fadeIn(motion.effects()),
            exit = if (reduce) androidx.compose.animation.ExitTransition.None else shrinkVertically(motion.spatial()) + fadeOut(motion.effects()),
        ) {
            ForceLtr {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Wi-Fi ${wifi.value} ${wifi.unit}", style = EmberbyteType.grotesk(UsageType.FeaturedSub, 500), color = palette.text)
                    Text("Mobile ${mobile.value} ${mobile.unit}", style = EmberbyteType.grotesk(UsageType.FeaturedSub, 500), color = palette.text)
                    TextButton(onClick = onOpenApp, modifier = Modifier.heightIn(min = 48.dp).testTag("usage_row_details")) {
                        Text("App details", style = EmberbyteType.grotesk(UsageType.Button, 700), color = palette.primary)
                    }
                }
            }
        }
    }
}
