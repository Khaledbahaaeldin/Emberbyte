package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.appicons.AppIconProvider
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // When usage access permission is missing, show prompt first
        if (state.topAppsLocked) {
            val usagePrompt = state.prompts.firstOrNull { it.id == "usage_access" }
            if (usagePrompt != null) {
                item(key = "prompt_usage_access") {
                    PermissionPrompt(usagePrompt, onAction = { onPromptAction(usagePrompt.id) })
                }
            }
        }

        // Header
        item(key = "header") {
            HomeHeader(
                state = state,
                onOpenSettings = onOpenSettings,
                onSelectPlan = { onEvent(HomeEvent.SelectPlan(it)) },
            )
        }

        // Hero Section
        item(key = "hero") {
            HomeHeroSection(
                state = state,
                onSetUpPlan = onSetUpPlan,
            )
        }

        // Gap banner
        state.gap?.let { gap ->
            item(key = "gap") { GapBanner(gap) }
        }

        // Remaining prompts if not locked (or notification prompts)
        val remainingPrompts = if (state.topAppsLocked) {
            state.prompts.filter { it.id != "usage_access" }
        } else {
            state.prompts
        }
        items(remainingPrompts, key = { it.id }) { prompt ->
            PermissionPrompt(prompt, onAction = { onPromptAction(prompt.id) })
        }

        // Bento row: Forecast & Speed tiles
        item(key = "bento_row") {
            HomeBentoRow(state = state)
        }

        // Spike tile
        state.spike?.let { spike ->
            item(key = "spike") {
                BentoTile(
                    title = "Spike detected",
                    modifier = Modifier.fillMaxWidth(),
                    container = TileContainer.Tertiary,
                    onClick = { onOpenApp(spike.packageName) },
                ) {
                    Text(spike.title, style = MaterialTheme.typography.titleMedium)
                    ForceLtr {
                        Text(
                            spike.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { onEvent(HomeEvent.DismissSpike(spike.id)) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
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
            HomeNetworkTile(state = state)
        }

        if (!state.hasPlan) {
            item(key = "data_plan") {
                BentoTile(
                    title = "Data plan",
                    modifier = Modifier.fillMaxWidth(),
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
                        Text("Set up your data plan")
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
            )
        }

        // History and chart
        item(key = "history") {
            HomeHistorySection(
                state = state,
                onSelectDay = { onEvent(HomeEvent.SelectDay(it)) },
                onOpenHistory = onOpenHistory,
            )
        }
    }
}
