package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.data.PermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.util.DayClock
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBarValue
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UsageBarRow
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class TrendDirection {
    UP,
    DOWN,
    FLAT,
    NONE,
}

data class ComparisonResult(
    val text: String,
    val direction: TrendDirection,
)

data class HistoryUiState(
    val granularity: Granularity = Granularity.DAY,
    val bars: List<BarUi> = emptyList(),
    val points: List<UsagePoint> = emptyList(),
    val totalBytes: Long = 0L,
    val averageBytes: Long = 0L,
    val units: ByteUnits = ByteUnits.DECIMAL,
    val needsUsageAccess: Boolean = false,
    val loaded: Boolean = false,
    val selectedIndex: Int? = null,
    val comparisonText: String? = null,
    val trendDirection: TrendDirection = TrendDirection.NONE,
    val heroDescription: String? = null,
    val rangeLabel: String? = null,
    val heroLabel: String? = null,
)

sealed interface HistoryEvent {
    data class SetGranularity(val granularity: Granularity) : HistoryEvent
    data class SelectBar(val index: Int) : HistoryEvent
}

/** Day: last 14 days. Week: last 12 weeks (Monday start). Month: last 12 months. Ends at the end of [today]. */
internal fun rangeForHistory(granularity: Granularity, today: LocalDate, zone: ZoneId): DateRange {
    val end = today.plusDays(1).atStartOfDay(zone).toInstant()
    val start = when (granularity) {
        Granularity.DAY, Granularity.HOUR -> today.minusDays(13)
        Granularity.WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(11)
        Granularity.MONTH -> today.withDayOfMonth(1).minusMonths(11)
    }.atStartOfDay(zone).toInstant()
    return DateRange(start, end)
}

internal fun historyBars(
    points: List<UsagePoint>,
    granularity: Granularity,
    units: ByteUnits,
    zone: ZoneId,
    locale: Locale,
): List<BarUi> {
    var prevDate: LocalDate? = null
    return points.map { point ->
        val date = point.start.atZone(zone).toLocalDate()
        val formatted = HistoryLabels.format(date, prevDate, granularity, point.totalBytes, units, locale)
        prevDate = date
        BarUi(
            label = formatted.label,
            bytes = point.totalBytes,
            description = formatted.description,
            valueText = formatBarValue(point.totalBytes, units),
            title = formatted.title,
            shortLabel = formatted.shortLabel,
            isBoundary = formatted.isBoundary,
        )
    }
}

internal fun computeRangeLabel(points: List<UsagePoint>, granularity: Granularity, zone: ZoneId, locale: Locale): String? {
    if (points.isEmpty()) return null
    val firstDate = points.first().start.atZone(zone).toLocalDate()
    val lastDate = points.last().start.atZone(zone).toLocalDate()
    return when (granularity) {
        Granularity.DAY, Granularity.HOUR -> formatDateRange(firstDate, lastDate, locale)
        Granularity.WEEK -> {
            val endOfWeek = lastDate.plusDays(6)
            formatDateRange(firstDate, endOfWeek, locale)
        }
        Granularity.MONTH -> {
            val firstMonth = firstDate.month.getDisplayName(TextStyle.SHORT, locale)
            val lastMonth = lastDate.month.getDisplayName(TextStyle.SHORT, locale)
            if (firstDate.year == lastDate.year) {
                "$firstMonth to $lastMonth ${firstDate.year}"
            } else {
                "$firstMonth ${firstDate.year} to $lastMonth ${lastDate.year}"
            }
        }
    }
}

internal fun computeHeroLabel(
    granularity: Granularity,
    selectedIndex: Int?,
    points: List<UsagePoint>,
    zone: ZoneId,
    locale: Locale,
): String? {
    if (selectedIndex != null && selectedIndex in points.indices) {
        val date = points[selectedIndex].start.atZone(zone).toLocalDate()
        return when (granularity) {
            Granularity.DAY, Granularity.HOUR -> {
                date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
            }
            Granularity.WEEK -> {
                val endOfWeek = date.plusDays(6)
                formatDateRange(date, endOfWeek, locale)
            }
            Granularity.MONTH -> {
                "${date.month.getDisplayName(TextStyle.FULL, locale)} ${date.year}"
            }
        }
    }
    return null
}

internal fun buildHistoryUiState(
    granularity: Granularity,
    points: List<UsagePoint>,
    units: ByteUnits,
    zone: ZoneId,
    locale: Locale,
    needsUsageAccess: Boolean = false,
    selectedIndex: Int? = null,
): HistoryUiState {
    val total = points.sumOf { it.totalBytes }
    val activeBuckets = points.count { it.totalBytes > 0 }
    val average = if (activeBuckets == 0) 0L else total / activeBuckets
    val comparison = computePeriodComparison(granularity, points)
    val spokenTotal = spokenBytes(total, units)
    val rangeLbl = computeRangeLabel(points, granularity, zone, locale)
    val heroLbl = computeHeroLabel(granularity, selectedIndex, points, zone, locale)
    val heroDesc = if (selectedIndex != null && selectedIndex in points.indices) {
        val selBytes = points[selectedIndex].totalBytes
        "${spokenBytes(selBytes, units)}, $heroLbl"
    } else {
        when (granularity) {
            Granularity.WEEK -> "$spokenTotal this week"
            Granularity.MONTH -> "$spokenTotal this month"
            else -> "$spokenTotal total"
        }
    }
    return HistoryUiState(
        granularity = granularity,
        bars = historyBars(points, granularity, units, zone, locale),
        points = points,
        totalBytes = total,
        averageBytes = average,
        units = units,
        needsUsageAccess = needsUsageAccess,
        loaded = true,
        selectedIndex = selectedIndex,
        comparisonText = comparison?.text,
        trendDirection = comparison?.direction ?: TrendDirection.NONE,
        heroDescription = heroDesc,
        rangeLabel = rangeLbl,
        heroLabel = heroLbl,
    )
}

internal fun computePeriodComparison(granularity: Granularity, points: List<UsagePoint>): ComparisonResult? {
    if (points.size < 2) return null
    val (current, previous) = when (granularity) {
        Granularity.DAY -> {
            if (points.size < 14) return null
            val half = points.size / 2
            val prevSum = points.take(half).sumOf { it.totalBytes }
            val currSum = points.drop(half).sumOf { it.totalBytes }
            currSum to prevSum
        }
        Granularity.WEEK -> {
            val curr = points.last().totalBytes
            val prev = points[points.size - 2].totalBytes
            curr to prev
        }
        Granularity.MONTH -> {
            val curr = points.last().totalBytes
            val prev = points[points.size - 2].totalBytes
            curr to prev
        }
        else -> return null
    }

    if (previous == 0L) return null
    val diff = current - previous
    val percent = ((kotlin.math.abs(diff).toDouble() / previous.toDouble()) * 100).toInt()

    val periodLabel = when (granularity) {
        Granularity.DAY, Granularity.HOUR -> "last week"
        Granularity.WEEK -> "last week"
        Granularity.MONTH -> "last month"
    }

    val (text, direction) = when {
        percent == 0 -> "About the same as $periodLabel" to TrendDirection.FLAT
        diff > 0 -> "$percent% more than $periodLabel" to TrendDirection.UP
        else -> "$percent% less than $periodLabel" to TrendDirection.DOWN
    }
    return ComparisonResult(text, direction)
}

/** @param dates tests MUST pass a finite flow. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    usage: UsageRepository,
    settings: SettingsRepository,
    permissions: PermissionRepository,
    private val clock: Clock = SystemZoneClock(),
    private val locale: Locale = Locale.getDefault(),
    dates: Flow<LocalDate> = DayClock(clock).dates(),
) : ViewModel() {
    private val granularity = MutableStateFlow(Granularity.DAY)
    private val selectedBar = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(dates, granularity) { date, g -> date to g }
        .flatMapLatest { (date, g) ->
            selectedBar.value = null
            val zone = clock.zone
            var lastKeys: List<Instant>? = null
            val pointsFlow = usage.observeSeries(rangeForHistory(g, date, zone), g)
                .onEach { pts ->
                    val keys = pts.map { it.start }
                    if (lastKeys != null && lastKeys != keys) {
                        selectedBar.value = null
                    }
                    lastKeys = keys
                }
            combine(
                pointsFlow,
                settings.observe(),
                permissions.observe(),
                selectedBar,
            ) { points, s, perm, sel ->
                buildHistoryUiState(g, points, s.unitSystem.toByteUnits(), zone, locale, needsUsageAccess = !perm.usageAccess, selectedIndex = sel)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.SetGranularity -> {
                granularity.value = event.granularity
                selectedBar.value = null
            }
            is HistoryEvent.SelectBar -> {
                selectedBar.value = if (selectedBar.value == event.index) null else event.index
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val unit = when (state.granularity) {
        Granularity.WEEK -> "week"
        Granularity.MONTH -> "month"
        else -> "day"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with Back button, ConnectedButtonGroup period selector, HeroNumber & comparison
        item {
            HistoryHeader(
                state = state,
                onEvent = onEvent,
                onBack = onBack,
            )
        }

        // Selected bar detail or chart
        if (state.loaded && state.bars.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = "No history yet",
                    body = "Usage data will appear here once recorded.",
                )
            }
        } else {
            // Chart
            item {
                val summary = when (state.granularity) {
                    Granularity.DAY -> {
                        val highest = state.bars.maxByOrNull { it.bytes }
                        if (highest != null) "Daily usage, last 14 days, highest ${highest.description}"
                        else "Daily usage, last 14 days"
                    }
                    Granularity.WEEK -> {
                        val highest = state.bars.maxByOrNull { it.bytes }
                        if (highest != null) "Weekly usage, last 12 weeks, highest ${highest.description}"
                        else "Weekly usage, last 12 weeks"
                    }
                    Granularity.MONTH -> {
                        val highest = state.bars.maxByOrNull { it.bytes }
                        if (highest != null) "Monthly usage, last 12 months, highest ${highest.description}"
                        else "Monthly usage, last 12 months"
                    }
                    else -> null
                }
                UsageBarRow(
                    points = state.bars,
                    selectedIndex = state.selectedIndex,
                    onSelect = { onEvent(HistoryEvent.SelectBar(it)) },
                    scrollKey = state.granularity,
                    summaryDescription = summary,
                    todayIndex = if (state.granularity == Granularity.DAY) state.bars.lastIndex else null,
                )
            }

            // Detail slot below chart: reserved height so selecting a bar does not jump the chart or tiles
            val selectedIndex = state.selectedIndex
            val isSelected = selectedIndex != null && selectedIndex in state.bars.indices
            val bar = if (isSelected) state.bars[selectedIndex] else null
            val point = if (isSelected) state.points.getOrNull(selectedIndex) else null

            item(key = "chart_detail_slot") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected && bar != null && point != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = bar.title ?: bar.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.text,
                                )
                                TextButton(
                                    onClick = { onEvent(HistoryEvent.SelectBar(selectedIndex)) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                ) {
                                    Text("Show total", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            NetworkBar(
                                mobileBytes = point.mobileBytes,
                                wifiBytes = point.wifiBytes,
                                mobileColor = palette.primary,
                                wifiColor = palette.accent,
                                units = state.units,
                                modifier = Modifier.fillMaxWidth().height(10.dp),
                            )

                            // Legend row with Mobile and Wi-Fi dots
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(
                                                color = palette.primary,
                                                shape = CircleShape,
                                            )
                                    )
                                    val mobileFormatted = formatBytes(point.mobileBytes, state.units)
                                    Text(
                                        text = "Mobile ${mobileFormatted.value} ${mobileFormatted.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.muted,
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(
                                                color = palette.accent,
                                                shape = CircleShape,
                                            )
                                    )
                                    val wifiFormatted = formatBytes(point.wifiBytes, state.units)
                                    Text(
                                        text = "Wi-Fi ${wifiFormatted.value} ${wifiFormatted.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.muted,
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Tap a bar for mobile and Wi-Fi breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                    }
                }
            }

            // Bento tiles: Total/Active days and Average
            item {
                val fontScale = LocalDensity.current.fontScale
                val isDay = state.granularity == Granularity.DAY
                val firstTileTitle = if (isDay) stringResource(R.string.history_active_days) else "Total"
                val firstTileValue = if (isDay) {
                    "${state.points.count { it.totalBytes > 0 }} of ${state.points.size}"
                } else {
                    plainBytes(state.totalBytes, state.units)
                }
                val avgTitle = "Avg / active $unit"

                if (fontScale >= 1.5f) {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BentoTile(title = firstTileTitle, modifier = Modifier.fillMaxWidth(), container = TileContainer.Primary) {
                            ForceLtr {
                                Text(firstTileValue, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        BentoTile(title = avgTitle, modifier = Modifier.fillMaxWidth()) {
                            ForceLtr {
                                Text(plainBytes(state.averageBytes, state.units), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BentoTile(
                            title = firstTileTitle,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            container = TileContainer.Primary,
                        ) {
                            ForceLtr {
                                Text(firstTileValue, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        BentoTile(
                            title = avgTitle,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        ) {
                            ForceLtr {
                                Text(plainBytes(state.averageBytes, state.units), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }
        }

        if (state.needsUsageAccess) {
            item {
                Text(
                    "Without usage access only the last 7 days are available; older bars stay empty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
