package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsPageBanner
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformGroup
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType

@Composable
fun AlertsScreen(
    viewModel: SettingsViewModel,
    isDark: Boolean,
    highlightKey: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    AlertsContent(
        settings = settings,
        isDark = isDark,
        highlightKey = highlightKey,
        onBack = onBack,
        onToggleSpikes = { enabled -> viewModel.update { it.copy(spikeAlertsEnabled = enabled) } },
        modifier = modifier,
    )
}

@Composable
internal fun AlertsContent(
    settings: Settings,
    isDark: Boolean,
    highlightKey: String? = null,
    onBack: () -> Unit,
    onToggleSpikes: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val amoledDark = settings.amoledBlack && isDark

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "banner") {
            SettingsPageBanner(
                title = "Alerts",
                subtitle = if (settings.spikeAlertsEnabled) "Spike alerts enabled" else "Spike alerts off",
                icon = Icons.Rounded.TrendingUp,
                tint = palette.warn,
                amoled = amoledDark,
                onBack = onBack,
            )
        }

        item(key = "chart") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(0, play = true)
                    .testTag("alerts_example_chart"),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (amoledDark) palette.surface else palette.surface2,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Example spike detection",
                        style = EmberbyteType.heading(SettingsType.RowTitle),
                        color = palette.text,
                    )
                    Text(
                        text = "Emberbyte compares each hour against your recent baseline and flags sudden jumps.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.RowSubtitle),
                        color = palette.muted,
                    )
                    val lineColor = if (settings.spikeAlertsEnabled) palette.warn else palette.muted
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                    ) {
                        val pts = listOf(0.22f, 0.25f, 0.20f, 0.26f, 0.88f, 0.36f, 0.24f, 0.28f)
                        val stepX = size.width / (pts.size - 1)
                        val path = Path().apply {
                            pts.forEachIndexed { idx, p ->
                                val x = idx * stepX
                                val y = size.height * (1f - p)
                                if (idx == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 5.dp.toPx(),
                            center = Offset(4 * stepX, size.height * (1f - pts[4])),
                        )
                    }
                }
            }
        }

        item(key = "controls") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(1, play = true),
            ) {
                SettingsUniformRow(
                    icon = Icons.Rounded.TrendingUp,
                    title = "Spike alerts",
                    subtitle = "Show a banner on Apps when an app uses far more data than usual",
                    highlighted = highlightKey == "spike_alerts",
                    onClick = { onToggleSpikes(!settings.spikeAlertsEnabled) },
                    trailing = {
                        Switch(
                            checked = settings.spikeAlertsEnabled,
                            onCheckedChange = onToggleSpikes,
                            modifier = Modifier.testTag("alerts_spikes_switch"),
                        )
                    },
                )
            }
        }
    }
}
