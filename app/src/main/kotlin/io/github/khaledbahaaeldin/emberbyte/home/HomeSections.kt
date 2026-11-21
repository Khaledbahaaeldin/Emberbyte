package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroEntrance
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.badgeSpinDegPerSec
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
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

/** The unit badge turns with the live rate shown on the Live tile; an unknown or zero rate idles slowly. */
internal fun homeBadgeSpinDegPerSec(state: HomeUiState): Float =
    badgeSpinDegPerSec(state.throughputBps / 1_000_000f)

@Composable
internal fun HomeHeroSection(
    state: HomeUiState,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
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
            entrance = HeroEntrance.WeightBloom,
            badgeSpinDegPerSec = homeBadgeSpinDegPerSec(state),
        )

        EmberbyteAnimatedVisibility(visible = state.heroBytes > 0L) {
            Text(
                text = stringResource(R.string.home_includes_system_traffic),
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted,
            )
        }

        val lineText = state.planLine ?: state.subtitle
        val lastLineText = rememberRetainedWhen(lineText) { it != null }
        EmberbyteCrossfade(
            targetState = when {
                lineText != null -> HeroStatusKind.Line
                !state.hasPlan -> HeroStatusKind.SetupPrompt
                else -> HeroStatusKind.None
            },
        ) { statusState ->
            when (statusState) {
                HeroStatusKind.Line -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        (lineText ?: lastLineText)?.let { text ->
                            ForceLtr {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.text,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        EmberbyteAnimatedVisibility(visible = state.hasPlan) {
                            WavyBar(
                                fraction = state.planFraction,
                                color = palette.primary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                HeroStatusKind.SetupPrompt -> {
                    TextButton(
                        onClick = onSetUpPlan,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Text(
                            text = "Set up a plan",
                            color = palette.linkText,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                HeroStatusKind.None -> Unit
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
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    val lastForecast = rememberRetainedWhen(state.forecast) { it != null }

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
            EmberbyteAnimatedVisibility(visible = state.forecast != null) {
                (state.forecast ?: lastForecast)?.let { ForecastTile(it, Modifier.fillMaxWidth()) }
            }
        }
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val halfWidth = (maxWidth - 12.dp) / 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                AnimatedVisibility(
                    visible = state.forecast != null,
                    enter = if (reduceMotion) {
                        EnterTransition.None
                    } else {
                        expandHorizontally(animationSpec = motion.spatial()) + fadeIn(animationSpec = motion.effects())
                    },
                    exit = if (reduceMotion) {
                        ExitTransition.None
                    } else {
                        shrinkHorizontally(animationSpec = motion.spatial()) + fadeOut(animationSpec = motion.effects())
                    },
                ) {
                    (state.forecast ?: lastForecast)?.let { forecast ->
                        ForecastTile(
                            model = forecast,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .width(halfWidth),
                            corner = Corner.TopStart,
                            container = TileContainer.Accent,
                        )
                    }
                }
                SpeedTile(
                    rxBps = state.liveRxBps,
                    txBps = state.liveTxBps,
                    network = state.network,
                    units = state.units,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_speed_tile"),
                    corner = Corner.BottomStart,
                )
            }
        }
    }
}

@Composable
internal fun HomeNetworkTile(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
                    color = palette.linkText,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) {
        val topAppsState = when {
            state.topAppsLocked -> TopAppsStateKind.Locked
            state.topApps.isEmpty() -> TopAppsStateKind.Empty
            else -> TopAppsStateKind.Content
        }
        val lastTopApps = rememberRetainedWhen(state.topApps) { it.isNotEmpty() }
        EmberbyteCrossfade(targetState = topAppsState, modifier = Modifier.fillMaxWidth()) { displayMode ->
            when (displayMode) {
                TopAppsStateKind.Locked -> {
                    Text(
                        text = "Allow usage access to see which apps use your data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                TopAppsStateKind.Empty -> {
                    Text(
                        text = "No per-app data yet. It appears within about 5 minutes once usage access is allowed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                TopAppsStateKind.Content -> {
                    val rows = state.topApps.ifEmpty { lastTopApps }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val maxBytes = rows.maxOfOrNull { it.totalBytes } ?: 1L
                        rows.take(5).forEach { app ->
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

private enum class HeroStatusKind {
    Line,
    SetupPrompt,
    None,
}

private enum class TopAppsStateKind {
    Locked,
    Empty,
    Content,
}

@Composable
internal fun <T> rememberRetainedWhen(
    value: T,
    keep: (T) -> Boolean,
): T {
    val ref = remember {
        object {
            var retained: T = value
        }
    }
    if (keep(value)) {
        ref.retained = value
    }
    return ref.retained
}

internal fun LazyItemScope.emberbyteAnimateItem(
    motion: EmberbyteMotion,
    modifier: Modifier = Modifier,
): Modifier = modifier.animateItem(
    fadeInSpec = motion.effects(),
    placementSpec = motion.spatial(),
    fadeOutSpec = motion.effects(),
)
