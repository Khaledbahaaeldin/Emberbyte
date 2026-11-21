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
    onOpenHistory: () -> Unit,
    onPromptAction: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToApps: () -> Unit = {},
    iconProvider: AppIconProvider? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val packageNames = state.topApps.map { it.packageName }
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
        onOpenHistory = onOpenHistory,
        onPromptAction = onPromptAction,
        onOpenApp = onOpenApp,
        onSetUpPlan = onSetUpPlan,
        onNavigateToApps = onNavigateToApps,
        modifier = modifier,
        icons = { iconMap[it] },
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onPromptAction: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToApps: () -> Unit = {},
    icons: (String) -> ImageBitmap? = { null },
) {
    val palette = LocalEmberbytePalette.current
    EmberbyteScreenScaffold(
        title = if (state.isToday || state.heroLabel.isEmpty()) {
            stringResource(R.string.home_hero_label_today)
        } else {
            state.heroLabel
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
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = motion.effects(),
                                        placementSpec = motion.spatial(),
                                        fadeOutSpec = motion.effects(),
                                    ),
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
                                modifier = Modifier.animateItem(
                                    fadeInSpec = motion.effects(),
                                    placementSpec = motion.spatial(),
                                    fadeOutSpec = motion.effects(),
                                ),
                            )
                        }
                    }

                    // Hero Section
                    if (!state.topAppsLocked) {
                        item(key = "hero") {
                            HomeHeroSection(
                                state = state,
                                onSetUpPlan = onSetUpPlan,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = motion.effects(),
                                    placementSpec = motion.spatial(),
                                    fadeOutSpec = motion.effects(),
                                ),
                            )
                        }
                    }

                    // Gap banner
                    state.gap?.let { gap ->
                        item(key = "gap") {
                            GapBanner(
                                model = gap,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = motion.effects(),
                                    placementSpec = motion.spatial(),
                                    fadeOutSpec = motion.effects(),
                                ),
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
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }

                    // Bento row: Forecast & Speed tiles
                    item(key = "bento_row") {
                        HomeBentoRow(
                            state = state,
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }

                    // Spike tile
                    state.spike?.let { spike ->
                        item(key = "spike") {
                            BentoTile(
                                title = "Spike detected",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = motion.effects(),
                                        placementSpec = motion.spatial(),
                                        fadeOutSpec = motion.effects(),
                                    ),
                                container = TileContainer.Tertiary,
                                onClick = { onOpenApp(spike.packageName) },
                            ) {
                                Text(spike.title, style = MaterialTheme.typography.titleMedium)
                                ForceLtr {
                                    Text(
                                        spike.detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.onAccent,
                                    )
                                }
                                TextButton(
                                    onClick = { onEvent(HomeEvent.DismissSpike(spike.id)) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = palette.onAccent,
                                    ),
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }

                    // Network tile
                    item(key = "network") {
                        HomeNetworkTile(
                            state = state,
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }

                    if (!state.hasPlan) {
                        item(key = "data_plan") {
                            BentoTile(
                                title = "Data plan",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = motion.effects(),
                                        placementSpec = motion.spatial(),
                                        fadeOutSpec = motion.effects(),
                                    ),
                                onClick = onSetUpPlan,
                            ) {
                                Text(
                                    "Set up your data plan to see what is left and when it runs out.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                TextButton(
                                    onClick = onSetUpPlan,
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                                ) {
                                    Text("Set up your data plan", color = palette.linkText)
                                }
                            }
                        }
                    }

                    // Top apps
                    item(key = "top_apps") {
                        HomeTopAppsSection(
                            state = state,
                            onOpenApp = onOpenApp,
                            onNavigateToApps = onNavigateToApps,
                            icons = icons,
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }

                    // History and chart
                    item(key = "history") {
                        HomeHistorySection(
                            state = state,
                            onSelectDay = { onEvent(HomeEvent.SelectDay(it)) },
                            onOpenHistory = onOpenHistory,
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }
                }
            }
        }
    }
}
