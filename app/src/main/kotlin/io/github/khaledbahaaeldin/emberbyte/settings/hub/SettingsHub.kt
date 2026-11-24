@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.settings.HealthAction
import io.github.khaledbahaaeldin.emberbyte.settings.HubSwitchId
import io.github.khaledbahaaeldin.emberbyte.settings.SettingsArea
import io.github.khaledbahaaeldin.emberbyte.settings.SettingsHubState
import io.github.khaledbahaaeldin.emberbyte.settings.StorageRowCounts
import io.github.khaledbahaaeldin.emberbyte.settings.storageBreakdown
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.revealOnce
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import java.time.Clock
import java.time.format.DateTimeFormatter

@Composable
internal fun SettingsHub(
    state: SettingsHubState,
    listState: LazyListState,
    clock: Clock,
    versionName: String,
    updateStatus: String,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenArea: (SettingsArea) -> Unit,
    onHealthAction: (HealthAction) -> Unit,
    onToggleSwitch: (HubSwitchId) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0]
    val byteUnits = if (state.settings.unitSystem == UnitSystem.BINARY) ByteUnits.BINARY else ByteUnits.DECIMAL
    val todayFormatted = plainBytes(state.todayBytes, byteUnits)
    val needsFix = state.health.primary != null
    val subtitleText = if (needsFix) "Needs a quick fix" else "Counting · $todayFormatted today"

    val themeName = (ThemePresets.firstOrNull { it.id == state.settings.themeId } ?: ThemePresets.first()).name
    val modeLabel = when (state.settings.themeMode) {
        ThemeMode.SYSTEM -> "Auto"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

    val storage = state.storage
    val totalBytes = storage?.fileBytes ?: 0L
    val breakdown = storageBreakdown(
        fileBytes = totalBytes,
        rowCounts = StorageRowCounts(
            hourly = storage?.hourlyRows ?: 0L,
            daily = storage?.dailyRows ?: 0L,
            plans = storage?.planRows ?: 0L,
            other = storage?.otherRows ?: 0L,
        ),
    )
    val storageText = if (totalBytes > 0L) "about ${plainBytes(totalBytes, byteUnits)}" else "0 B"
    val sinceText = storage?.oldestAt?.let {
        "since ${DateTimeFormatter.ofPattern("MMM yyyy", locale).format(it.atZone(clock.zone))}"
    } ?: "this phone"
    val backupLabel = state.settings.lastBackupAt?.let {
        "Backed up ${DateTimeFormatter.ofPattern("d MMM", locale).format(it.atZone(clock.zone))}"
    } ?: "No backup yet"

    val revealed = remember { mutableSetOf<String>() }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.text,
                        )
                    }
                    Column {
                        Text(
                            text = "Settings",
                            style = EmberbyteType.title(SettingsType.HubTitle),
                            color = palette.text,
                            modifier = Modifier.semantics { heading() },
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (needsFix) palette.warn else palette.primary),
                            )
                            Text(
                                text = subtitleText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.PulseBody),
                                color = if (needsFix) palette.warn else palette.muted,
                            )
                        }
                    }
                }
                val searchInteraction = remember { MutableInteractionSource() }
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("settings_search_button")
                        .pressMorph(
                            shape = MaterialTheme.shapes.medium,
                            pressedCorner = 24.dp,
                            interactionSource = searchInteraction,
                        )
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            interactionSource = searchInteraction,
                            indication = null,
                            role = Role.Button,
                            onClick = onOpenSearch,
                        ),
                    shape = MaterialTheme.shapes.medium,
                    color = palette.surface2,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search settings",
                            tint = palette.text,
                        )
                    }
                }
            }
        }

        item(key = "pulse") {
            MeasuringPulseCard(
                health = state.health,
                strip = state.strip,
                lastSampleAt = state.lastSampleAt,
                now = clock.instant(),
                zone = clock.zone,
                amoled = state.settings.amoledBlack && state.isDark,
                onOpenKeepMeasuring = { onOpenArea(SettingsArea.KeepMeasuring) },
                onAction = onHealthAction,
                modifier = if (RevealGate.enabled) Modifier.revealOnce("pulse", revealed, 0) else Modifier,
            )
        }

        item(key = "switches") {
            ShapeSwitchesRow(
                switches = state.switches,
                onToggle = onToggleSwitch,
                modifier = if (RevealGate.enabled) Modifier.revealOnce("switches", revealed, 1) else Modifier,
            )
        }

        item(key = "mode_bar") {
            ModeBar(
                selected = state.settings.themeMode,
                onSelect = onSelectThemeMode,
                modifier = if (RevealGate.enabled) Modifier.revealOnce("mode_bar", revealed, 2) else Modifier,
            )
        }

        item(key = "cards") {
            val fontScale = LocalDensity.current.fontScale
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val singleColumn = maxWidth < 340.dp || fontScale >= 1.85f
                val amoledDark = state.settings.amoledBlack && state.isDark
                val c0 = if (RevealGate.enabled) Modifier.revealOnce("card_0", revealed, 3) else Modifier
                val c1 = if (RevealGate.enabled) Modifier.revealOnce("card_1", revealed, 4) else Modifier
                val c2 = if (RevealGate.enabled) Modifier.revealOnce("card_2", revealed, 5) else Modifier
                val c3 = if (RevealGate.enabled) Modifier.revealOnce("card_3", revealed, 6) else Modifier
                val c4 = if (RevealGate.enabled) Modifier.revealOnce("card_4", revealed, 7) else Modifier
                val c5 = if (RevealGate.enabled) Modifier.revealOnce("card_5", revealed, 8) else Modifier

                if (singleColumn) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LookAndFeelCard(themeName, modeLabel, amoledDark, { onOpenArea(SettingsArea.LookAndFeel) }, c0)
                        NotificationsCard(
                            state.settings.liveNotificationEnabled,
                            state.settings.notificationShowsSpeed,
                            todayFormatted,
                            amoledDark,
                            { onOpenArea(SettingsArea.Notifications) },
                            c1,
                        )
                        AlertsCard(state.settings.spikeAlertsEnabled, amoledDark, { onOpenArea(SettingsArea.Alerts) }, c2)
                        UnitsCard(state.settings.unitSystem, amoledDark, { onOpenArea(SettingsArea.Units) }, c3)
                        DataAndBackupCard(
                            storageText,
                            sinceText,
                            breakdown,
                            backupLabel,
                            state.settings.lastBackupAt != null,
                            amoledDark,
                            { onOpenArea(SettingsArea.DataAndBackup) },
                            c4,
                        )
                        AboutCard(versionName, updateStatus, amoledDark, { onOpenArea(SettingsArea.About) }, c5)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LookAndFeelCard(
                                themeName,
                                modeLabel,
                                amoledDark,
                                { onOpenArea(SettingsArea.LookAndFeel) },
                                c0.weight(1f),
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                NotificationsCard(
                                    state.settings.liveNotificationEnabled,
                                    state.settings.notificationShowsSpeed,
                                    todayFormatted,
                                    amoledDark,
                                    { onOpenArea(SettingsArea.Notifications) },
                                    c1,
                                )
                                AlertsCard(
                                    state.settings.spikeAlertsEnabled,
                                    amoledDark,
                                    { onOpenArea(SettingsArea.Alerts) },
                                    c2,
                                )
                            }
                        }
                        DataAndBackupCard(
                            storageText,
                            sinceText,
                            breakdown,
                            backupLabel,
                            state.settings.lastBackupAt != null,
                            amoledDark,
                            { onOpenArea(SettingsArea.DataAndBackup) },
                            c3,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            UnitsCard(state.settings.unitSystem, amoledDark, { onOpenArea(SettingsArea.Units) }, c4.weight(1f))
                            AboutCard(versionName, updateStatus, amoledDark, { onOpenArea(SettingsArea.About) }, c5.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeBar(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val items: List<Triple<ThemeMode, String, ImageVector>> = listOf(
        Triple(ThemeMode.SYSTEM, "Auto", Icons.Rounded.AutoMode),
        Triple(ThemeMode.LIGHT, "Light", Icons.Rounded.LightMode),
        Triple(ThemeMode.DARK, "Dark", Icons.Rounded.DarkMode),
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { (mode, label, icon) ->
            val isSelected = mode == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("mode_bar_${label.lowercase()}")
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.RadioButton) { onSelect(mode) },
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) palette.primary.copy(alpha = 0.18f) else palette.surface2,
                border = if (isSelected) BorderStroke(1.5.dp, palette.primary) else null,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) palette.primary else palette.muted,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = label,
                        style = EmberbyteType.heading(SettingsType.PulseBody),
                        color = if (isSelected) palette.text else palette.muted,
                    )
                }
            }
        }
    }
}
