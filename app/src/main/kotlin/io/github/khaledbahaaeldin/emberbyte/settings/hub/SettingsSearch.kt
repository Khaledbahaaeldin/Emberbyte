package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.settings.SettingEntry
import io.github.khaledbahaaeldin.emberbyte.settings.SettingsArea
import io.github.khaledbahaaeldin.emberbyte.settings.defaultSettingsSearchIndex
import io.github.khaledbahaaeldin.emberbyte.settings.searchSettings
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType

@Composable
internal fun SettingsSearchOverlay(
    onClose: () -> Unit,
    onSelect: (SettingEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var query by rememberSaveable { mutableStateOf("") }
    val index = remember { defaultSettingsSearchIndex() }
    val results = remember(index, query) { searchSettings(index, query) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = palette.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.text,
                    )
                }
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            text = "Search settings",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.RowTitle),
                            color = palette.muted,
                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = palette.muted)
                            }
                        } else {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.muted)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = palette.surface2,
                        unfocusedContainerColor = palette.surface2,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = palette.text,
                        unfocusedTextColor = palette.text,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("settings_search_field"),
                )
            }

            Spacer(Modifier.height(12.dp))

            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                ) {
                    Text(
                        text = "Nothing matches \"$query\". Try battery, backup or theme.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.RowTitle),
                        color = palette.muted,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(results, key = { _, item -> item.id }) { indexPos, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .staggerEntrance(indexPos.coerceAtMost(7), play = true)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onSelect(item) }
                                .padding(horizontal = 12.dp)
                                .testTag("search_result_${item.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(palette.surface2),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = iconForArea(item.area),
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = EmberbyteType.heading(SettingsType.RowTitle),
                                    color = palette.text,
                                )
                                Text(
                                    text = item.areaTitle,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Caption),
                                    color = palette.muted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun iconForArea(area: SettingsArea): ImageVector = when (area) {
    SettingsArea.LookAndFeel -> Icons.Rounded.Palette
    SettingsArea.Notifications -> Icons.Rounded.NotificationsActive
    SettingsArea.Alerts -> Icons.Rounded.TrendingUp
    SettingsArea.Units -> Icons.Rounded.DataUsage
    SettingsArea.DataAndBackup -> Icons.Rounded.Save
    SettingsArea.KeepMeasuring -> Icons.Rounded.Shield
    SettingsArea.About -> Icons.Rounded.Info
}
