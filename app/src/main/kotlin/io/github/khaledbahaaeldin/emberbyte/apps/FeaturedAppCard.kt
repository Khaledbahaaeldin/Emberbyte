package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.liveTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon
import kotlin.math.roundToInt

/**
 * The "Most used" card: the top app of the current period and network, with its icon, amount and share. Tapping opens
 * the app.
 */
@Composable
fun FeaturedAppCard(
    app: AppRowUi,
    totalBytes: Long,
    units: ByteUnits,
    icon: ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    network: AppsNetwork = AppsNetwork.ALL,
) {
    val palette = LocalEmberbytePalette.current
    val tile = palette.liveTile()
    val shape = remember { mixedCorners(large = 28.dp, small = 10.dp, smallCorner = Corner.TopEnd) }
    val interaction = remember { MutableInteractionSource() }
    val amount = networkAmount(app, network)
    val sharePercent = if (totalBytes > 0) (amount.toFloat() / totalBytes.toFloat() * 100f).roundToInt() else 0
    val formatted = formatBytes(amount, units)
    val description = "Most used: ${app.label}, ${plainBytes(amount, units)}, $sharePercent% of total"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressMorph(shape, pressedScale = 0.97f, pressedCorner = 16.dp, interactionSource = interaction)
            .clip(shape)
            .background(tile.container)
            .testTag("apps_most_used")
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = description
                onClick {
                    onClick()
                    true
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppIcon(label = app.label, icon = icon, shape = shapeFor(app.packageName), size = 64.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "MOST USED",
                style = EmberbyteType.grotesk(AppsType.MostCaption, 700).copy(letterSpacing = 0.12.em),
                color = tile.onContainerSecondary,
            )
            Text(
                text = app.label,
                style = EmberbyteType.grotesk(AppsType.MostName, 700),
                color = tile.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ForceLtr {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatted.value} ${formatted.unit}",
                    style = EmberbyteType.grotesk(AppsType.MostAmount, 700).copy(letterSpacing = (-0.02).em),
                    color = tile.onContainer,
                    maxLines = 1,
                )
                Text(
                    text = "$sharePercent% of total",
                    style = EmberbyteType.grotesk(AppsType.MostSub, 500),
                    color = tile.onContainerSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}
