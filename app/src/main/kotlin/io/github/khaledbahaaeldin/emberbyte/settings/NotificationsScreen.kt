package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
fun NotificationsScreen(
    viewModel: SettingsViewModel,
    isDark: Boolean,
    highlightKey: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationsContent(
        settings = settings,
        isDark = isDark,
        highlightKey = highlightKey,
        onBack = onBack,
        onToggleLive = { enabled -> viewModel.update { it.copy(liveNotificationEnabled = enabled) } },
        onToggleSpeed = { enabled -> viewModel.update { it.copy(notificationShowsSpeed = enabled) } },
        modifier = modifier,
    )
}

@Composable
internal fun NotificationsContent(
    settings: Settings,
    isDark: Boolean,
    highlightKey: String? = null,
    onBack: () -> Unit,
    onToggleLive: (Boolean) -> Unit,
    onToggleSpeed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val context = LocalContext.current
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
                title = "Notifications",
                subtitle = if (settings.liveNotificationEnabled) "Live notification active" else "Live notification off",
                icon = Icons.Rounded.NotificationsActive,
                tint = palette.accent,
                amoled = amoledDark,
                onBack = onBack,
            )
        }

        item(key = "preview") {
            NotificationShadePreview(
                liveOn = settings.liveNotificationEnabled,
                speedOn = settings.notificationShowsSpeed,
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(0, play = true),
            )
        }

        item(key = "controls") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(1, play = true),
            ) {
                SettingsUniformRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Live notification",
                    subtitle = "Shows today's data in your notification shade",
                    highlighted = highlightKey == "live_notification",
                    onClick = { onToggleLive(!settings.liveNotificationEnabled) },
                    trailing = {
                        Switch(
                            checked = settings.liveNotificationEnabled,
                            onCheckedChange = onToggleLive,
                            modifier = Modifier.testTag("notif_live_switch"),
                        )
                    },
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.Speed,
                    title = "Show live speed",
                    subtitle = if (settings.liveNotificationEnabled) {
                        "Displays current download/upload rate"
                    } else {
                        "Turn on live notification to show speed"
                    },
                    highlighted = highlightKey == "show_speed",
                    onClick = if (settings.liveNotificationEnabled) {
                        { onToggleSpeed(!settings.notificationShowsSpeed) }
                    } else {
                        null
                    },
                    trailing = {
                        Switch(
                            checked = settings.notificationShowsSpeed,
                            onCheckedChange = onToggleSpeed,
                            enabled = settings.liveNotificationEnabled,
                            modifier = Modifier.testTag("notif_speed_switch"),
                        )
                    },
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.Update,
                    title = "Live updates",
                    subtitle = "Manage Android promoted live notifications",
                    highlighted = highlightKey == "live_updates",
                    onClick = {
                        val action = if (Build.VERSION.SDK_INT >= 36) {
                            AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS
                        } else {
                            AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS
                        }
                        val intent = Intent(action).apply {
                            putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = palette.muted,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun NotificationShadePreview(
    liveOn: Boolean,
    speedOn: Boolean,
    amoled: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notification_shade_preview")
            .alpha(if (liveOn) 1f else 0.5f),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (amoled) palette.surface else palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (liveOn) "Emberbyte · 3.42 GB today" else "Live notification is off",
                    style = EmberbyteType.heading(SettingsType.RowTitle),
                    color = palette.text,
                )
                if (liveOn && speedOn) {
                    Text(
                        text = "1.4 MB/s",
                        style = EmberbyteType.heading(SettingsType.Caption),
                        color = palette.primary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(palette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (liveOn) 0.62f else 0.0f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(palette.primary),
                )
            }
        }
    }
}
