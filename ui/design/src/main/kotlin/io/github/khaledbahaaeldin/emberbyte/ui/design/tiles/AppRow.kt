package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import androidx.compose.ui.graphics.ImageBitmap
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.spokenUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor

private fun spoken(bytes: Long, units: ByteUnits): String {
    val f = formatBytes(bytes, units)
    return "${f.value} ${spokenUnit(f.unit)}"
}

@Composable
fun AppRow(
    model: AppRowUi,
    units: ByteUnits,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageBitmap? = null,
    shape: AppShape = shapeFor(model.packageName),
) {
    val total = formatBytes(model.totalBytes, units)
    val mobileShare = if (model.totalBytes > 0) model.mobileBytes.toFloat() / model.totalBytes else 0f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .semantics {
                stateDescription = "Mobile ${spoken(model.mobileBytes, units)}, Wi-Fi ${spoken(model.wifiBytes, units)}"
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(
            label = model.label,
            icon = icon,
            shape = shape,
            size = 40.dp,
        )
        Column(Modifier.weight(1f)) {
            Text(model.label, style = MaterialTheme.typography.bodyLarge)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp)) {
                if (mobileShare > 0f) {
                    Box(Modifier.weight(mobileShare).height(4.dp).background(MaterialTheme.colorScheme.primary))
                }
                if (mobileShare < 1f) {
                    Box(Modifier.weight(1f - mobileShare).height(4.dp).background(MaterialTheme.colorScheme.tertiary))
                }
            }
        }
        ForceLtr {
            Text("${total.value} ${total.unit}", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun AppRow(
    icon: ImageBitmap?,
    shape: AppShape,
    label: String,
    bytes: Long,
    fraction: Float,
    mobileBytes: Long,
    wifiBytes: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    units: ByteUnits = ByteUnits.DECIMAL,
) {
    val palette = LocalEmberbytePalette.current
    val total = formatBytes(bytes, units)
    val rowDescription = "$label, ${total.value} ${spokenUnit(total.unit)}, Mobile ${spoken(mobileBytes, units)}, Wi-Fi ${spoken(wifiBytes, units)}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                this.contentDescription = rowDescription
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(
            label = label,
            icon = icon,
            shape = shape,
            size = 40.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(palette.track),
            ) {
                val clampedFraction = fraction.coerceIn(0f, 1f)
                val minStub = 8.dp
                val barWidth = (maxWidth * clampedFraction).coerceIn(minStub, maxWidth)
                Box(
                    modifier = Modifier
                        .testTag("app_row_bar_stub")
                        .width(barWidth)
                        .height(6.dp)
                        .clip(CircleShape),
                ) {
                    NetworkBar(
                        mobileBytes = mobileBytes,
                        wifiBytes = wifiBytes,
                        mobileColor = palette.primary,
                        wifiColor = palette.accent,
                        height = 6.dp,
                        units = units,
                    )
                }
            }
        }
        // Value bubble
        Box(
            modifier = Modifier
                .testTag("app_row_bubble")
                .clip(CircleShape)
                .background(palette.surface2)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            ForceLtr {
                Text(
                    text = "${total.value} ${total.unit}",
                    style = EmberbyteType.value(12.sp),
                    color = palette.text,
                    maxLines = 1,
                )
            }
        }
    }
}


