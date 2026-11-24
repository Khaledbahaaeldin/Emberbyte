package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsPageBanner
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformGroup
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance

@Composable
fun KeepMeasuringScreen(
    viewModel: SettingsViewModel,
    isDark: Boolean,
    highlightKey: String? = null,
    onAction: (HealthAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.hubState.collectAsStateWithLifecycle()
    KeepMeasuringContent(
        state = state,
        isDark = isDark,
        highlightKey = highlightKey,
        onAction = onAction,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun KeepMeasuringContent(
    state: SettingsHubState,
    isDark: Boolean,
    highlightKey: String? = null,
    onAction: (HealthAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val amoledDark = state.settings.amoledBlack && isDark
    val isWarning = state.health.primary != null

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
                title = "Keep measuring",
                subtitle = "${state.health.score} of 4 checks passing",
                icon = Icons.Rounded.Shield,
                tint = if (isWarning) palette.warn else palette.primary,
                amoled = amoledDark,
                onBack = onBack,
            )
        }

        item(key = "ring_card") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(0, play = true),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (amoledDark) palette.surface else palette.surface2,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WavyRing(
                        fraction = (state.health.score / 4f).coerceIn(0f, 1f),
                        state = if (isWarning) BudgetStateUi.Warning else BudgetStateUi.Ok,
                        size = 150.dp,
                        label = "${state.health.score}/4",
                    )
                    Text(
                        text = if (isWarning) "Needs attention" else "Counting everything",
                        style = EmberbyteType.heading(SettingsType.PulseHeadline),
                        color = palette.text,
                    )
                }
            }
        }

        item(key = "checks") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(1, play = true),
            ) {
                state.health.checks.forEach { check ->
                    val (icon, title, subtitle, action, key) = when (check.kind) {
                        HealthCheckKind.BatteryUnrestricted -> CheckMeta(
                            Icons.Rounded.BatteryChargingFull,
                            "Battery unrestricted",
                            if (check.passed) "Background counting allowed" else "Android may pause counting",
                            HealthAction.FixBattery,
                            "battery",
                        )
                        HealthCheckKind.UsageAccess -> CheckMeta(
                            Icons.Rounded.DataUsage,
                            "Usage access",
                            if (check.passed) "Permission granted" else "Required to read per-app usage",
                            HealthAction.AllowUsageAccess,
                            "usage_access",
                        )
                        HealthCheckKind.LiveNotification -> CheckMeta(
                            Icons.Rounded.NotificationsActive,
                            "Live notification",
                            if (check.passed) "Active in notification shade" else "Turn on for continuous sampling",
                            HealthAction.TurnOnLive,
                            "live_notification",
                        )
                        HealthCheckKind.SamplingRecently -> CheckMeta(
                            Icons.Rounded.Schedule,
                            "Sampling recently",
                            if (check.passed) "Sample recorded in the last 45 min" else "No recent sample recorded",
                            null,
                            "sampling",
                        )
                    }
                    SettingsUniformRow(
                        icon = icon,
                        title = title,
                        subtitle = subtitle,
                        highlighted = highlightKey == key,
                        trailing = {
                            if (!check.passed && action != null) {
                                FilledTonalButton(
                                    onClick = { onAction(action) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = palette.warn.copy(alpha = 0.22f),
                                        contentColor = palette.text,
                                    ),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("fix_check_${key}"),
                                ) {
                                    Text("Fix")
                                }
                            } else {
                                Text(
                                    text = if (check.passed) "Pass" else "Waiting",
                                    style = EmberbyteType.heading(SettingsType.Caption),
                                    color = if (check.passed) palette.primary else palette.warn,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

private data class CheckMeta(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val action: HealthAction?,
    val key: String,
)
