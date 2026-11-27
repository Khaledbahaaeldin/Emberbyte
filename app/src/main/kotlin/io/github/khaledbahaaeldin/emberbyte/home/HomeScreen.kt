package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageFiltersSheet
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageFilters
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageCard
import androidx.compose.runtime.remember
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.rememberPlayHomeEntrance
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanSetupCard
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.LiveDirection
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.appicons.AppIconProvider
import io.github.khaledbahaaeldin.emberbyte.common.EmberbyteScreenScaffold
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.GapBanner
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.PermissionPrompt
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: (dayIndex: Int, origin: androidx.compose.ui.geometry.Rect?) -> Unit,
    onPromptAction: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToApps: () -> Unit = {},
    iconProvider: AppIconProvider? = null,
    returnedDay: Int? = null,
    onReturnedDayConsumed: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // History hands back the day last chosen there; the card keeps it.
    androidx.compose.runtime.LaunchedEffect(returnedDay) {
        if (returnedDay != null) {
            viewModel.onEvent(HomeEvent.ShowDay(returnedDay))
            onReturnedDayConsumed()
        }
    }
    var historyOrigin by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val packageNames = (state.usage?.rows?.map { it.row.packageName } ?: emptyList()) + state.topApps.map { it.packageName }
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
    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenSettings = onOpenSettings,
        onOpenHistory = { day -> onOpenHistory(day, historyOrigin) },
        onPromptAction = onPromptAction,
        onOpenApp = onOpenApp,
        onSetUpPlan = onSetUpPlan,
        onNavigateToApps = onNavigateToApps,
        modifier = modifier,
        icons = { iconMap[it] },
        onHistoryBounds = { historyOrigin = it },
    )
}

/** The Emberbyte mark beside the day title: 44 dp and still (no motion). */
internal val HOME_BRAND_MARK_SIZE = 44.dp

@Composable
internal fun HomeContent(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: (dayIndex: Int) -> Unit,
    onPromptAction: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToApps: () -> Unit = {},
    icons: (String) -> ImageBitmap? = { null },
    onHistoryBounds: (androidx.compose.ui.geometry.Rect) -> Unit = {},
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val palette = LocalEmberbytePalette.current
    // Session only: survives rotation and process recreation of the screen, is never written to settings.
    var direction by rememberSaveable { mutableStateOf(LiveDirection.Download) }
    val title = if (state.isToday || state.heroLabel.isEmpty()) {
        stringResource(R.string.home_hero_label_today)
    } else {
        state.heroLabel
    }
    EmberbyteScreenScaffold(
        title = title,
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = HOME_BRAND_MARK_SIZE, modifier = Modifier.testTag("home_brand_mark"))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenSettings,
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.iconButtonColors(contentColor = palette.text),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                )
            }
        },
        modifier = modifier,
    ) { padding ->
        if (showFilters) {
            UsageFiltersSheet(
                filters = state.usage?.filters ?: UsageFilters(),
                onApply = {
                    onEvent(HomeEvent.SetUsageFilters(it))
                    showFilters = false
                },
                onReset = {
                    onEvent(HomeEvent.SetUsageFilters(UsageFilters()))
                    showFilters = false
                },
                onDismiss = { showFilters = false },
            )
        }
        EmberbyteCrossfade(
            targetState = state.isLoading,
            modifier = Modifier.fillMaxSize(),
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding(),
                        )
                        .testTag("home_loading_indicator"),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingState(label = "Loading today's usage")
                }
            } else {
                val motion = rememberEmberbyteMotion()
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
                    // When usage access permission is missing, show prompt first
                    if (state.topAppsLocked) {
                        val usagePrompt = state.prompts.firstOrNull { it.id == "usage_access" }
                        if (usagePrompt != null) {
                            item(key = "prompt_usage_access") {
                                PermissionPrompt(
                                    usagePrompt,
                                    onAction = { onPromptAction(usagePrompt.id) },
                                    modifier = emberbyteAnimateItem(motion),
                                )
                            }
                        }
                    }

                    // Header
                    if (state.planChips.size >= 2) {
                        item(key = "header") {
                            HomeHeader(
                                state = state,
                                onSelectPlan = { onEvent(HomeEvent.SelectPlan(it)) },
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    }

                    // Hero Section
                    if (!state.topAppsLocked) {
                        item(key = "hero") {
                            HomeHeroSection(
                                state = state,
                                onSetUpPlan = onSetUpPlan,
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    }

                    // Gap banner
                    state.gap?.let { gap ->
                        item(key = "gap") {
                            GapBanner(
                                model = gap,
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    }

                    // Remaining prompts if not locked (or notification prompts)
                    val remainingPrompts = if (state.topAppsLocked) {
                        state.prompts.filter { it.id != "usage_access" }
                    } else {
                        state.prompts
                    }
                    items(remainingPrompts, key = { it.id }) { prompt ->
                        PermissionPrompt(
                            prompt,
                            onAction = { onPromptAction(prompt.id) },
                            modifier = emberbyteAnimateItem(motion),
                        )
                    }

                    // One merged setup card while there is no plan (replaces the old link and the old "Data plan" card).
                    if (!state.hasPlan) {
                        item(key = "plan_setup") {
                            PlanSetupCard(
                                onSetUp = onSetUpPlan,
                                playEntrance = rememberPlayHomeEntrance(),
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    }

                    // Forecast on its own full-width row (plan state), then Live and Today by network side by side.
                    item(key = "forecast_row") {
                        HomeForecastRow(state = state, modifier = emberbyteAnimateItem(motion))
                    }
                    item(key = "bento_pair") {
                        HomeBentoPair(
                            state = state,
                            direction = direction,
                            onToggleDirection = {
                                direction = if (direction == LiveDirection.Download) LiveDirection.Upload else LiveDirection.Download
                            },
                            modifier = emberbyteAnimateItem(motion),
                        )
                    }

                    // One usage card (week strip, ranking, filters, History). While usage access is missing the
                    // existing explanation is shown instead of an empty card.
                    if (state.topAppsLocked) {
                        item(key = "top_apps") {
                            HomeTopAppsSection(
                                state = state,
                                onOpenApp = onOpenApp,
                                onNavigateToApps = onNavigateToApps,
                                icons = icons,
                                modifier = emberbyteAnimateItem(motion),
                            )
                        }
                    } else {
                        state.usage?.let { usage ->
                            item(key = "usage_card") {
                                UsageCard(
                                    state = usage,
                                    onSelectDay = { onEvent(HomeEvent.SelectDay(it)) },
                                    onOpenApp = onOpenApp,
                                    onAllApps = onNavigateToApps,
                                    onOpenFilters = { showFilters = true },
                                    onRemoveFilter = { onEvent(HomeEvent.RemoveUsageFilter(it)) },
                                    onOpenHistory = onOpenHistory,
                                    icons = icons,
                                    onResetFilters = { onEvent(HomeEvent.SetUsageFilters(UsageFilters())) },
                                    playEntrance = rememberPlayHomeEntrance(),
                                    onHistoryBounds = onHistoryBounds,
                                    modifier = emberbyteAnimateItem(motion),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
