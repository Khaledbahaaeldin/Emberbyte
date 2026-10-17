package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.ForecastUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.NetworkKindUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

@Composable
fun SpeedTile(
    rxBps: Long,
    txBps: Long,
    network: NetworkKindUi?,
    units: ByteUnits,
    modifier: Modifier = Modifier,
    corner: Corner? = Corner.BottomStart,
) {
    val palette = LocalEmberbytePalette.current
    val networkLabel = when (network) {
        NetworkKindUi.Wifi -> "Wi-Fi"
        NetworkKindUi.Mobile -> "Mobile"
        null -> "Offline"
    }
    val down = formatBytes(rxBps, units)
    val up = formatBytes(txBps, units)
    BentoTile(
        title = "Live · $networkLabel",
        modifier = modifier.fillMaxHeight(),
        container = TileContainer.Standard,
        corner = corner,
    ) {
        if (network == null) {
            Text("No connection", style = MaterialTheme.typography.headlineSmall, color = palette.text)
        } else {
            ForceLtr {
                Text("↓ ${down.value} ${down.unit}/s", style = MaterialTheme.typography.headlineSmall, color = palette.text)
                Text(
                    "↑ ${up.value} ${up.unit}/s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
fun ForecastTile(
    model: ForecastUi,
    modifier: Modifier = Modifier,
    corner: Corner? = Corner.TopStart,
    container: TileContainer = TileContainer.Accent,
) {
    val palette = LocalEmberbytePalette.current
    BentoTile(
        title = "Forecast",
        modifier = modifier.fillMaxHeight(),
        container = container,
        corner = corner,
    ) {
        ForceLtr {
            Text(model.headline, style = MaterialTheme.typography.headlineLarge, color = palette.onAccent)
            Text(model.detail, style = MaterialTheme.typography.bodyMedium, color = palette.onAccent.copy(alpha = 0.85f))
            Text(
                model.confidence,
                style = MaterialTheme.typography.labelMedium,
                color = palette.onAccent.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun EstimatedBadge(modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = palette.warn,
        contentColor = palette.onWarn,
    ) {
        Text(
            "Estimated",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
