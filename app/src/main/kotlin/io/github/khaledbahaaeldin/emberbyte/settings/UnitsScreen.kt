package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsPageBanner
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformGroup
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType

@Composable
fun UnitsScreen(
    viewModel: SettingsViewModel,
    isDark: Boolean,
    highlightKey: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    UnitsContent(
        settings = settings,
        isDark = isDark,
        highlightKey = highlightKey,
        onBack = onBack,
        onSelectUnit = { system -> viewModel.update { it.copy(unitSystem = system) } },
        modifier = modifier,
    )
}

@Composable
internal fun UnitsContent(
    settings: Settings,
    isDark: Boolean,
    highlightKey: String? = null,
    onBack: () -> Unit,
    onSelectUnit: (UnitSystem) -> Unit,
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
                title = "Units",
                subtitle = if (settings.unitSystem == UnitSystem.DECIMAL) "Decimal · 1000" else "Binary · 1024",
                icon = Icons.Rounded.DataUsage,
                tint = palette.primary,
                amoled = amoledDark,
                onBack = onBack,
            )
        }

        item(key = "selector") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(0, play = true),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UnitOptionButton(
                    title = "Decimal (GB)",
                    selected = settings.unitSystem == UnitSystem.DECIMAL,
                    onClick = { onSelectUnit(UnitSystem.DECIMAL) },
                    modifier = Modifier.weight(1f).testTag("unit_decimal_button"),
                )
                UnitOptionButton(
                    title = "Binary (GiB)",
                    selected = settings.unitSystem == UnitSystem.BINARY,
                    onClick = { onSelectUnit(UnitSystem.BINARY) },
                    modifier = Modifier.weight(1f).testTag("unit_binary_button"),
                )
            }
        }

        item(key = "example") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(1, play = true),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (amoledDark) palette.surface else palette.surface2,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.large)
                                .background(palette.surface)
                                .padding(14.dp),
                        ) {
                            Text("1,000,000,000 B", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro), color = palette.muted)
                            Text("1.00 GB", style = EmberbyteType.heading(SettingsType.PulseHeadline), color = palette.primary)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.large)
                                .background(palette.surface)
                                .padding(14.dp),
                        ) {
                            Text("1,000,000,000 B", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro), color = palette.muted)
                            Text("0.93 GiB", style = EmberbyteType.heading(SettingsType.PulseHeadline), color = palette.accent)
                        }
                    }
                    Text(
                        text = "Decimal matches what your carrier bills. Binary matches what Android storage shows.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.PulseBody),
                        color = palette.muted,
                    )
                }
            }
        }

        item(key = "applies") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(2, play = true),
            ) {
                SettingsUniformRow(
                    icon = Icons.Rounded.Home,
                    title = "Home & Apps",
                    subtitle = "Today total, app rows, and charts",
                    highlighted = highlightKey == "units",
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Live notification",
                    subtitle = "Current usage and live speed",
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.Widgets,
                    title = "Home-screen widgets",
                    subtitle = "Today, plan, and dashboard widgets",
                )
            }
        }
    }
}

@Composable
private fun UnitOptionButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) palette.primary.copy(alpha = 0.18f) else palette.surface2,
        border = if (selected) BorderStroke(1.5.dp, palette.primary) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = EmberbyteType.heading(SettingsType.PulseBody),
                color = if (selected) palette.text else palette.muted,
            )
        }
    }
}
