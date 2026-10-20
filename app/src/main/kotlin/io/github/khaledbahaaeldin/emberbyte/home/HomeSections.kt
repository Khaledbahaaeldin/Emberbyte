package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.chips.EmberbyteFilterChip
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.ForecastTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NoticeLine
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.SpeedTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UsageBarRow

@Composable
internal fun HomeHeader(
    state: HomeUiState,
    onOpenSettings: () -> Unit,
    onSelectPlan: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BrandMark(size = 32.dp)
                Text(
                    text = state.heroLabel,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.text,
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = palette.text,
                )
            }
        }

        if (state.planChips.size >= 2) {
            Row(
                modifier = Modifier
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

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("home_loading_indicator"),
            contentAlignment = Alignment.Center,
        ) {
            LoadingState(label = "Loading today's usage")
        }
        return
    }

    val formatted = formatBytes(state.heroBytes, state.units)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeroNumber(
            value = formatted.value,
            unit = formatted.unit,
            contentDescription = state.heroDescription,
            color = palette.primary,
        )

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
            if (state.hasPlan) {
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

        if (state.isEstimated) {
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
    val hasForecast = state.forecast != null

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
            state.forecast?.let { ForecastTile(it, Modifier.fillMaxWidth()) }
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                state.topAppsLocked -> {
                    Text(
                        text = "Allow usage access to see which apps use your data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                state.topApps.isEmpty() -> {
                    Text(
                        text = "No per-app data yet. It appears within about 5 minutes once usage access is allowed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                else -> {
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
            onSelect = onSelectDay,
            openAtEnd = true,
            summaryDescription = weekSummary,
        )
    }
}
