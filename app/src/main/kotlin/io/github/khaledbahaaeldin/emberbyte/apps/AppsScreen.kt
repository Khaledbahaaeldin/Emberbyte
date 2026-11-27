package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.appicons.AppIconProvider
import io.github.khaledbahaaeldin.emberbyte.common.EmberbyteScreenScaffold
import io.github.khaledbahaaeldin.emberbyte.home.emberbyteAnimateItem
import io.github.khaledbahaaeldin.emberbyte.home.rememberRetainedWhen
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PermissionPromptUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.PermissionPrompt
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance

private val USAGE_ACCESS_PROMPT = PermissionPromptUi(
    id = "usage_access",
    title = "Allow usage access",
    body = "Per-app numbers need usage access. Totals keep working without it.",
    actionLabel = "Open settings",
)

/** The Apps page entrance plays once per app start, not every time the tab is opened. */
internal object AppsEntranceGate {
    @Volatile
    var played: Boolean = false

    fun reset() {
        played = false
    }
}

@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
    onOpenApp: (String) -> Unit,
    onPromptAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    iconProvider: AppIconProvider? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val packageNames = (listOfNotNull(state.featured) + state.apps).map { it.packageName }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val motion = rememberEmberbyteMotion()
    val reduce = LocalReduceMotion.current
    var showFiltersSheet by remember { mutableStateOf(false) }
    val playEntrance = remember { !reduce && !AppsEntranceGate.played }
    LaunchedEffect(Unit) { AppsEntranceGate.played = true }

    if (showFiltersSheet) {
        FiltersSheet(
            initialNetwork = state.network,
            initialSort = state.sort,
            initialShowSystemApps = state.showSystemApps,
            onApply = { network, sort, system -> onEvent(AppsEvent.ApplyFilters(network, sort, system)) },
            onDismiss = { showFiltersSheet = false },
        )
    }

    val searching = state.query.isNotBlank()
    val hasApps = state.apps.isNotEmpty() || state.featured != null
    val filters = activeFilters(state.network, state.sort, state.showSystemApps)

    EmberbyteScreenScaffold(
        title = "Apps",
        titleContent = {
            Text(
                text = "Apps",
                style = EmberbyteType.grotesk(AppsType.Title, 700).copy(letterSpacing = (-0.02).em),
                color = palette.text,
                modifier = Modifier.semantics { heading() },
            )
        },
        actions = {
            if (state.loaded && (hasApps || !searching)) {
                Text(
                    text = if (state.appCount == 1) "1 app" else "${state.appCount} apps",
                    style = EmberbyteType.grotesk(AppsType.Pill, 700),
                    color = palette.linkText,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clip(AppsShapes.Chip)
                        .background(palette.primary.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("apps_count_pill"),
                )
            }
        },
        modifier = modifier,
    ) { padding ->
        EmberbyteCrossfade(
            targetState = state.isLoading,
            modifier = Modifier.fillMaxSize(),
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingState(label = "Loading apps")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.spike?.let { spike ->
                        item(key = "spike_banner") {
                            SpikeBanner(
                                spike = spike,
                                onOpen = { onOpenApp(spike.packageName) },
                                onDismiss = { onEvent(AppsEvent.DismissSpike(spike.id)) },
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    }

                    item(key = "search_row") {
                        AppsSearchRow(
                            query = state.query,
                            onQueryChange = { onEvent(AppsEvent.SetQuery(it)) },
                            activeFilterCount = state.activeFilterCount,
                            onOpenFilters = { showFiltersSheet = true },
                            modifier = emberbyteAnimateItem(motion).staggerEntrance(0, playEntrance),
                        )
                    }

                    item(key = "period_buttons") {
                        PeriodButtons(
                            selected = state.range,
                            onSelect = { onEvent(AppsEvent.SetRange(it)) },
                            modifier = emberbyteAnimateItem(motion).staggerEntrance(1, playEntrance),
                        )
                    }

                    item(key = "filter_chips") {
                        val lastFilters = rememberRetainedWhen(filters) { it.isNotEmpty() }
                        EmberbyteAnimatedVisibility(visible = filters.isNotEmpty(), modifier = emberbyteAnimateItem(motion)) {
                            AppsFilterChips(
                                filters = filters.ifEmpty { lastFilters ?: emptyList() },
                                onRemove = { onEvent(AppsEvent.RemoveFilter(it)) },
                                onReset = { onEvent(AppsEvent.ResetFilters) },
                            )
                        }
                    }

                    if (state.needsUsageAccess) {
                        item(key = "usage_prompt") {
                            PermissionPrompt(
                                USAGE_ACCESS_PROMPT,
                                onAction = { onPromptAction(USAGE_ACCESS_PROMPT.id) },
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    }

                    if (hasApps) {
                        item(key = "summary_card") {
                            AppsSummaryCard(
                                total = state.totalBytes,
                                wifi = state.wifiBytes,
                                mobile = state.mobileBytes,
                                periodLabel = periodLabel(state.range),
                                units = state.units,
                                modifier = emberbyteAnimateItem(motion).staggerEntrance(2, playEntrance),
                            )
                        }

                        if (state.featured != null) {
                            item(key = "featured") {
                                FeaturedAppCard(
                                    app = state.featured,
                                    totalBytes = state.totalBytes,
                                    units = state.units,
                                    icon = icons(state.featured.packageName),
                                    onClick = { onOpenApp(state.featured.packageName) },
                                    network = state.network,
                                    modifier = emberbyteAnimateItem(motion).staggerEntrance(3, playEntrance),
                                )
                            }
                        }

                        item(key = "list_header") {
                            Row(
                                modifier = emberbyteAnimateItem(motion).staggerEntrance(4, playEntrance).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "All apps",
                                    style = EmberbyteType.grotesk(AppsType.ListHeader, 700),
                                    color = palette.text,
                                    modifier = Modifier.semantics { heading() },
                                )
                                Text(
                                    sortLabel(state.sort),
                                    style = EmberbyteType.grotesk(AppsType.ListHeader, 600),
                                    color = palette.muted,
                                    modifier = Modifier.testTag("apps_sort_label"),
                                )
                            }
                        }

                        val first = (state.featured ?: state.apps.firstOrNull())?.let { networkAmount(it, state.network) } ?: 0L
                        items(state.apps, key = { it.packageName }) { app ->
                            AppsListRow(
                                app = app,
                                firstAmount = first,
                                totalBytes = state.totalBytes,
                                network = state.network,
                                units = state.units,
                                icon = icons(app.packageName),
                                onClick = { onOpenApp(app.packageName) },
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    } else {
                        item(key = "empty_state") {
                            val lastQuery = rememberRetainedWhen(state.query) { it.isNotBlank() }
                            EmberbyteCrossfade(
                                targetState = if (searching) 0 else if (filters.isNotEmpty()) 1 else 2,
                                modifier = emberbyteAnimateItem(motion, Modifier.fillMaxWidth()),
                            ) { kind ->
                                when (kind) {
                                    0 -> {
                                        val displayQuery = state.query.ifBlank { lastQuery ?: "" }.trim()
                                        EmptyState(
                                            icon = Icons.Rounded.Search,
                                            title = "No apps match",
                                            body = "No apps match \"$displayQuery\".",
                                            action = {
                                                TextButton(
                                                    onClick = { onEvent(AppsEvent.SetQuery("")) },
                                                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                                                ) { Text("Clear search", color = palette.linkText) }
                                            },
                                        )
                                    }
                                    1 -> EmptyState(
                                        icon = Icons.Rounded.Apps,
                                        title = "Nothing to show",
                                        body = "Nothing to show for these filters.",
                                        action = {
                                            TextButton(
                                                onClick = { onEvent(AppsEvent.ResetFilters) },
                                                modifier = Modifier.sizeIn(minHeight = 48.dp),
                                            ) { Text("Reset filters", color = palette.linkText) }
                                        },
                                    )
                                    else -> EmptyState(
                                        icon = Icons.Rounded.Apps,
                                        title = "No app data yet",
                                        body = "Per-app numbers appear within about 5 minutes once usage access is allowed.",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
