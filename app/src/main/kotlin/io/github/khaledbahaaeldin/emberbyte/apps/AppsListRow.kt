package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.liveTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.shareFillFraction
import kotlin.math.roundToInt

/** Test-only readout of a row's share fill (0..1). */
internal val AppsRowFillKey = SemanticsPropertyKey<Float>("AppsRowFill")

/**
 * One row of the Apps list: a share fill behind the row (from the start edge, at least 14 percent, relative to the
 * first row), a 44 dp icon, the name and the amount. The sub-line is the Wi-Fi / mobile split for the All network and
 * the percentage of the total when one network is selected.
 */
@Composable
internal fun AppsListRow(
    app: AppRowUi,
    firstAmount: Long,
    totalBytes: Long,
    network: AppsNetwork,
    units: ByteUnits,
    icon: ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val shape = AppsShapes.Row
    val interaction = remember { MutableInteractionSource() }
    val amountBytes = networkAmount(app, network)
    val fillTarget = shareFillFraction(amountBytes, firstAmount)
    val fill = remember { Animatable(fillTarget) }
    LaunchedEffect(fillTarget, reduce) {
        if (reduce) fill.snapTo(fillTarget) else fill.animateTo(fillTarget, motion.spatial())
    }
    val amount = formatBytes(amountBytes, units)
    val wifi = formatBytes(app.wifiBytes, units)
    val mobile = formatBytes(app.mobileBytes, units)
    val percent = if (totalBytes > 0) (amountBytes * 100f / totalBytes).roundToInt() else 0
    val subLine = if (network == AppsNetwork.ALL) {
        "Wi-Fi ${wifi.value} ${wifi.unit} · Mobile ${mobile.value} ${mobile.unit}"
    } else {
        "$percent% of total"
    }
    val fillColour = palette.liveTile().container

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressMorph(shape, pressedScale = 0.97f, pressedCorner = 16.dp, interactionSource = interaction)
            .clip(shape)
            .background(palette.text.copy(alpha = 0.07f))
            .drawBehind {
                val width = size.width * fill.value.coerceIn(0f, 1f)
                val fromStart = layoutDirection == LayoutDirection.Ltr
                drawRoundRect(
                    color = fillColour,
                    topLeft = Offset(if (fromStart) 0f else size.width - width, 0f),
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(22.dp.toPx()),
                )
            }
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "${app.label}, ${plainBytes(amountBytes, units)}"
                set(AppsRowFillKey, fill.value)
            }
            .heightIn(min = 68.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("apps_row_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(label = app.label, icon = icon, shape = shapeFor(app.packageName), size = 44.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = app.label,
                style = EmberbyteType.grotesk(AppsType.RowName, 700),
                color = palette.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ForceLtr {
                Text(
                    text = subLine,
                    style = EmberbyteType.grotesk(AppsType.RowSub, 500),
                    color = palette.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("apps_row_sub_${app.packageName}"),
                )
            }
        }
        ForceLtr {
            Text(
                text = "${amount.value} ${amount.unit}",
                style = EmberbyteType.grotesk(AppsType.RowAmount, 700),
                color = palette.text,
                maxLines = 1,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}
