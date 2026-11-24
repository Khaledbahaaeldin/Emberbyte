package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsPageBanner
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformGroup
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutScreen(
    versionName: String,
    updateSource: UpdateSource,
    isDark: Boolean,
    amoled: Boolean,
    hasWhatsNew: Boolean,
    highlightKey: String? = null,
    onOpenWhatsNew: () -> Unit,
    onOpenLicences: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onCopyNavLog: () -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val amoledDark = amoled && isDark
    var updateCheckLabel by remember { mutableStateOf("Tap to check via ${updateSource.displayName}") }

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
                title = "About",
                subtitle = "Version $versionName",
                icon = Icons.Rounded.Info,
                tint = palette.primary,
                amoled = amoledDark,
                onBack = onBack,
            )
        }

        item(key = "version_card") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(0, play = true)
                    .combinedClickable(onClick = {}, onLongClick = onCopyNavLog)
                    .testTag("about_version_card"),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (amoledDark) palette.surface else palette.surface2,
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Emberbyte $versionName",
                            style = EmberbyteType.heading(SettingsType.PulseHeadline),
                            color = palette.text,
                        )
                        Text(
                            text = "Offline · No INTERNET permission",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.PulseBody),
                            color = palette.muted,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = palette.primary.copy(alpha = 0.16f),
                    ) {
                        Text(
                            text = updateSource.displayName,
                            style = EmberbyteType.heading(SettingsType.Micro),
                            color = palette.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }
        }

        item(key = "rows") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(1, play = true),
            ) {
                SettingsUniformRow(
                    icon = Icons.Rounded.SystemUpdate,
                    title = "Check for updates",
                    subtitle = updateCheckLabel,
                    highlighted = highlightKey == "check_updates",
                    onClick = {
                        updateCheckLabel = "Checking..."
                        scope.launch {
                            delay(350)
                            updateCheckLabel = "No network permission — open ${updateSource.displayName}"
                            val url = RELEASES_URL
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    },
                    trailing = {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = palette.muted)
                    },
                )
                if (hasWhatsNew) {
                    SettingsUniformRow(
                        icon = Icons.Rounded.NewReleases,
                        title = "What's new",
                        subtitle = "See what changed in recent releases",
                        onClick = onOpenWhatsNew,
                    )
                }
                SettingsUniformRow(
                    icon = Icons.Rounded.Code,
                    title = "Source code",
                    subtitle = "GitHub · Khaledbahaaeldin/Emberbyte",
                    highlighted = highlightKey == "source_code",
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Khaledbahaaeldin/Emberbyte"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                    trailing = {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = palette.muted)
                    },
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.Description,
                    title = "Open-source licences",
                    subtitle = "Libraries and font notices",
                    highlighted = highlightKey == "licences",
                    onClick = onOpenLicences,
                    modifier = Modifier.testTag("about_licences_row"),
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.ContentCopy,
                    title = "Copy debug log",
                    subtitle = "Copy version and device info to clipboard",
                    highlighted = highlightKey == "copy_debug_log",
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(
                            ClipData.newPlainText("Emberbyte debug", "Emberbyte $versionName (${updateSource.displayName})"),
                        )
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        item(key = "credit") {
            Text(
                text = "Made for offline-first data clarity.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Caption),
                color = palette.muted,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}
