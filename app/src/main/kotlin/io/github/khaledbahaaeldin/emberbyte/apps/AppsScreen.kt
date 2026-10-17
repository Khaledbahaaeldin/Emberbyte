package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.appicons.AppIconProvider
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.chips.EmberbyteFilterChip
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PermissionPromptUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.PermissionPrompt

private val USAGE_ACCESS_PROMPT = PermissionPromptUi(
    id = "usage_access",
    title = "Allow usage access",
    body = "Per-app numbers need usage access. Totals keep working without it.",
    actionLabel = "Open settings",
)

private data class ActiveFilter(val kind: AppFilterKind, val label: String)

@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
    onOpenApp: (String) -> Unit,
    onPromptAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    iconProvider: AppIconProvider? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val packageNames = state.apps.map { it.packageName }
    val iconMap by produceState<Map<String, ImageBitmap>>(
        initialValue = emptyMap(),
        packageNames,
        iconProvider,
    ) {
        if (iconProvider == null) return@produceState
        val loaded = mutableMapOf<String, ImageBitmap>()
        for (pkg in packageNames) {
            val bitmap = iconProvider.icon(pkg)
            if (bitmap != null) {
                loaded[pkg] = bitmap
                value = loaded.toMap()
            }
        }
    }
    AppsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenApp = onOpenApp,
        onPromptAction = onPromptAction,
        modifier = modifier,
        icons = { iconMap[it] },
    )
}

@Composable
internal fun AppsContent(
    state: AppsUiState,
    onEvent: (AppsEvent) -> Unit,
    onOpenApp: (String) -> Unit,
    onPromptAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    icons: (String) -> ImageBitmap? = { null },
) {
    val palette = LocalEmberbytePalette.current
    var showFiltersSheet by remember { mutableStateOf(false) }

    if (showFiltersSheet) {
        FiltersSheet(
            initialRange = state.range,
            initialNetwork = state.network,
            initialSort = state.sort,
            initialShowSystemApps = state.showSystemApps,
            onApply = { r, n, s, sys ->
                onEvent(AppsEvent.ApplyFilters(r, n, s, sys))
            },
            onReset = {
                onEvent(AppsEvent.ResetFilters)
            },
            onDismiss = { showFiltersSheet = false },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Apps",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.text,
            )
        }

        // Search bar with Filters button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onEvent(AppsEvent.SetQuery(it)) },
                    label = { Text("Search apps") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = palette.muted,
                        )
                    },
                    trailingIcon = if (state.query.isNotEmpty()) {
                        {
                            IconButton(onClick = { onEvent(AppsEvent.SetQuery("")) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search",
                                    tint = palette.muted,
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = palette.muted,
                        focusedTextColor = palette.text,
                        unfocusedTextColor = palette.text,
                    ),
                    modifier = Modifier.weight(1f),
                )

                Button(
                    onClick = { showFiltersSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.surface2,
                        contentColor = palette.text,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text("Filters")
                    }
                }
            }
        }

        // Removable filter chips row (for each non-default filter)
        val activeFilters = buildList {
            if (state.range != AppsRange.TODAY) {
                val label = when (state.range) {
                    AppsRange.TODAY -> "Today"
                    AppsRange.LAST_7_DAYS -> "7 days"
                    AppsRange.LAST_30_DAYS -> "30 days"
                    AppsRange.THIS_MONTH, AppsRange.CYCLE -> "Cycle"
                }
                add(ActiveFilter(AppFilterKind.RANGE, label))
            }
            if (state.network != AppsNetwork.ALL) {
                val label = when (state.network) {
                    AppsNetwork.ALL -> "All"
                    AppsNetwork.MOBILE -> "Mobile"
                    AppsNetwork.WIFI -> "Wi-Fi"
                }
                add(ActiveFilter(AppFilterKind.NETWORK, label))
            }
            if (state.sort != AppSort.BYTES_DESC) {
                val label = when (state.sort) {
                    AppSort.NAME -> "Name"
                    AppSort.BYTES_ASC -> "Least data"
                    AppSort.BYTES_DESC, AppSort.SCREEN_TIME_DESC -> "Most data"
                }
                add(ActiveFilter(AppFilterKind.SORT, label))
            }
            if (state.showSystemApps) {
                add(ActiveFilter(AppFilterKind.SYSTEM_APPS, "System apps"))
            }
        }

        if (activeFilters.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    activeFilters.forEach { filter ->
                        EmberbyteFilterChip(
                            selected = true,
                            showCheckIcon = false,
                            onClick = { onEvent(AppsEvent.RemoveFilter(filter.kind)) },
                            label = { Text(filter.label) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove ${filter.label} filter",
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
            }
        }

        // Featured card
        if (state.featured != null) {
            item {
                FeaturedAppCard(
                    app = state.featured,
                    totalBytes = state.totalBytes,
                    units = state.units,
                    icon = icons(state.featured.packageName),
                    onClick = { onOpenApp(state.featured.packageName) },
                )
            }
        }

        // Usage access prompt
        if (state.needsUsageAccess) {
            item {
                PermissionPrompt(
                    USAGE_ACCESS_PROMPT,
                    onAction = { onPromptAction(USAGE_ACCESS_PROMPT.id) },
                )
            }
        }

        // Total bytes readout
        item {
            ForceLtr {
                Text(
                    text = "${plainBytes(state.totalBytes, state.units)} in total",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.muted,
                )
            }
        }

        // Apps list or empty state
        if (state.loaded && state.apps.isEmpty()) {
            item {
                if (state.query.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "No apps match \"${state.query.trim()}\".",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.muted,
                        )
                        TextButton(
                            onClick = { onEvent(AppsEvent.SetQuery("")) },
                        ) {
                            Text("Clear search")
                        }
                    }
                } else {
                    Text(
                        text = "No app data yet. Per-app numbers appear within about 5 minutes once usage access is allowed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
            }
        } else {
            items(state.apps, key = { it.packageName }) { app ->
                AppRow(
                    icon = icons(app.packageName),
                    shape = io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor(app.packageName),
                    label = app.label,
                    bytes = app.totalBytes,
                    fraction = if (state.totalBytes > 0) app.totalBytes.toFloat() / state.totalBytes.toFloat() else 0f,
                    mobileBytes = app.mobileBytes,
                    wifiBytes = app.wifiBytes,
                    onClick = { onOpenApp(app.packageName) },
                    units = state.units,
                )
            }
        }
    }
}
