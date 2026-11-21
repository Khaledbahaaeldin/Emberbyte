package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.chips.EmberbyteFilterChip
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.ForecastTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NoticeLine
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.SpeedTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UsageBarRow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeHeader(
    state: HomeUiState,
    onSelectPlan: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.planChips.forEach { chip ->
            EmberbyteFilterChip(
                selected = chip.selected,
                onClick = { onSelectPlan(chip.id) },
                label = { Text(chip.name) },
            )
        }
    }
}

@Composable
internal fun HomeHeroSection(
    state: HomeUiState,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current

    if (state.topAppsLocked) {
        // Hero figures hidden when permission is missing
        return
    }

    val formatted = formatBytes(state.heroBytes, state.units)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_section"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HeroNumber(
            value = formatted.value,
            unit = formatted.unit,
            contentDescription = state.heroDescription,
            color = palette.primary,
        )

        EmberbyteAnimatedVisibility(visible = state.heroBytes > 0L) {
            Text(
                text = stringResource(R.string.home_includes_system_traffic),
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted,
            )
        }

        val lineText = state.planLine ?: state.subtitle
        if (lineText != null) {
            ForceLtr {
                Text(
                    text = lineText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.text,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            EmberbyteAnimatedVisibility(visible = state.hasPlan) {
                WavyBar(
                    fraction = state.planFraction,
                    color = palette.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else if (!state.hasPlan) {
            TextButton(
                onClick = onSetUpPlan,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(
                    text = "Set up a plan",
                    color = palette.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        EmberbyteAnimatedVisibility(visible = state.isEstimated) {
            NoticeLine(
                text = "Data usage is estimated",
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
internal fun HomeBentoRow(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale

    if (fontScale >= 1.5f) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpeedTile(
                rxBps = state.liveRxBps,
                txBps = state.liveTxBps,
                network = state.network,
                units = state.units,
                modifier = Modifier.fillMaxWidth(),
            )
            var lastForecast by remember { mutableStateOf(state.forecast) }
            if (state.forecast != null) {
                lastForecast = state.forecast
            }
            EmberbyteAnimatedVisibility(visible = state.forecast != null) {
                lastForecast?.let { ForecastTile(it, Modifier.fillMaxWidth()) }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.forecast?.let { forecast ->
                ForecastTile(
                    model = forecast,
                    modifier = Modifier.weight(1f),
                    corner = Corner.TopStart,
                    container = TileContainer.Accent,
                )
            }
            SpeedTile(
                rxBps = state.liveRxBps,
                txBps = state.liveTxBps,
                network = state.network,
                units = state.units,
                modifier = Modifier.weight(1f),
                corner = Corner.BottomStart,
            )
        }
    }
}

@Composable
internal fun HomeNetworkTile(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val total = formatBytes(state.mobileBytes + state.wifiBytes, state.units)
    val mobile = formatBytes(state.mobileBytes, state.units)
    val wifi = formatBytes(state.wifiBytes, state.units)

    BentoTile(
        title = "Today by network",
        modifier = modifier.fillMaxWidth(),
        container = TileContainer.Standard,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NetworkBar(
                mobileBytes = state.mobileBytes,
                wifiBytes = state.wifiBytes,
                mobileColor = palette.primary,
                wifiColor = palette.accent,
                height = 14.dp,
                units = state.units,
            )
            ForceLtr {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Mobile ${mobile.value} ${mobile.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                    )
                    Text(
                        text = "Wi-Fi ${wifi.value} ${wifi.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                    )
                }
            }
        }
    }
}

@Composable
internal fun HomeTopAppsSection(
    state: HomeUiState,
    onOpenApp: (String) -> Unit,
    onNavigateToApps: () -> Unit,
    modifier: Modifier = Modifier,
    icons: (String) -> ImageBitmap? = { null },
) {
    val palette = LocalEmberbytePalette.current

    BentoTile(
        title = "Top apps today",
        modifier = modifier.fillMaxWidth(),
        container = TileContainer.Standard,
        trailing = {
            TextButton(
                onClick = onNavigateToApps,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(
                    text = "See all",
                    color = palette.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) {
        val topAppsState = when {
            state.topAppsLocked -> 0
            state.topApps.isEmpty() -> 1
            else -> 2
        }
        EmberbyteCrossfade(targetState = topAppsState, modifier = Modifier.fillMaxWidth()) { displayMode ->
            when (displayMode) {
                0 -> {
                    Text(
                        text = "Allow usage access to see which apps use your data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                1 -> {
                    Text(
                        text = "No per-app data yet. It appears within about 5 minutes once usage access is allowed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val maxBytes = state.topApps.maxOfOrNull { it.totalBytes } ?: 1L
                        state.topApps.take(5).forEach { app ->
                            val fraction = if (maxBytes > 0L) app.totalBytes.toFloat() / maxBytes else 0f
                            AppRow(
                                icon = icons(app.packageName),
                                shape = shapeFor(app.packageName),
                                label = app.label,
                                bytes = app.totalBytes,
                                fraction = fraction,
                                mobileBytes = app.mobileBytes,
                                wifiBytes = app.wifiBytes,
                                onClick = { onOpenApp(app.packageName) },
                                units = state.units,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeHistorySection(
    state: HomeUiState,
    onSelectDay: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BentoTile(
            title = "History",
            modifier = Modifier.fillMaxWidth(),
            container = TileContainer.Standard,
            onClick = onOpenHistory,
        ) {
            Text(
                text = "Daily, weekly and monthly usage",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        val weekSummary = run {
            val highest = state.week.maxByOrNull { it.bytes }
            if (highest != null) "Daily usage, last 7 days, highest ${highest.description}"
            else "Daily usage, last 7 days"
        }
        UsageBarRow(
            points = state.week,
            selectedIndex = state.selectedDay,
            onSelect = { index ->
                onSelectDay(index)
                onOpenHistory()
            },
            openAtEnd = true,
            summaryDescription = weekSummary,
            todayIndex = state.week.lastIndex,
        )
    }
}
