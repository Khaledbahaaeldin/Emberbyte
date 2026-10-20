package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon
import kotlin.math.roundToInt

@Composable
fun FeaturedAppCard(
    app: AppRowUi,
    totalBytes: Long,
    units: ByteUnits,
    icon: ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val shape = shapeFor(app.packageName)
    val appTotal = app.totalBytes
    val sharePercent = if (totalBytes > 0) {
        (appTotal.toFloat() / totalBytes.toFloat() * 100f).roundToInt()
    } else 0
    val formattedBytes = plainBytes(appTotal, units)
    val description = "${app.label}, $formattedBytes, $sharePercent% of total"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = description
                onClick {
                    onClick()
                    true
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AppIcon(
                    label = app.label,
                    icon = icon,
                    shape = shape,
                    size = 64.dp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Most data used",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.muted,
                    )
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.text,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                ForceLtr {
                    Text(
                        text = formattedBytes,
                        style = EmberbyteType.value(MaterialTheme.typography.headlineMedium.fontSize),
                        color = palette.primary,
                    )
                }
                Text(
                    text = "$sharePercent% of total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }
        }
    }
}
