package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.chips.EmberbyteFilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.common.ScreenHeader
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.PermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.util.DayClock
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UsageBarRow
import java.time.Clock
import java.time.DayOfWeek
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
    val heroDescription: String? = null,
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
): List<BarUi> = points.map { point ->
    val date = point.start.atZone(zone).toLocalDate()
    val month = date.month.getDisplayName(TextStyle.FULL, locale)
    val spoken = spokenBytes(point.totalBytes, units)
    val (label, description) = when (granularity) {
        Granularity.DAY, Granularity.HOUR ->
            date.dayOfMonth.toString() to
                "${date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)}, ${date.dayOfMonth} $month, $spoken"
        Granularity.WEEK ->
            DateTimeFormatter.ofPattern("d MMM", locale).format(date) to "Week of ${date.dayOfMonth} $month, $spoken"
        Granularity.MONTH ->
            date.month.getDisplayName(TextStyle.SHORT, locale) to "$month ${date.year}, $spoken"
    }
    BarUi(label, point.totalBytes, description)
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
    val heroDesc = when (granularity) {
        Granularity.WEEK -> "$spokenTotal this week"
        Granularity.MONTH -> "$spokenTotal this month"
        else -> "$spokenTotal total"
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
        comparisonText = comparison,
        heroDescription = heroDesc,
    )
}

internal fun computePeriodComparison(granularity: Granularity, points: List<UsagePoint>): String? {
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
    if (percent == 0) return null

    val periodLabel = when (granularity) {
        Granularity.DAY, Granularity.HOUR -> "last week"
        Granularity.WEEK -> "last week"
        Granularity.MONTH -> "last month"
    }

    return if (diff > 0) {
        "$percent% more than $periodLabel"
    } else {
        "$percent% less than $periodLabel"
    }
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
            var lastKeys: List<java.time.Instant>? = null
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
    val palette = io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette.current
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
                io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile(
                    title = "No history yet",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Usage data will appear here once recorded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
            }
        } else {
            // Selected bar readout row if any
            val selectedIndex = state.selectedIndex
            if (selectedIndex != null && selectedIndex in state.bars.indices) {
                val bar = state.bars[selectedIndex]
                val point = state.points.getOrNull(selectedIndex)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = bar.description.substringBefore(","),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.text,
                            )
                            androidx.compose.material3.TextButton(
                                onClick = { onEvent(HistoryEvent.SelectBar(selectedIndex)) },
                                modifier = Modifier.sizeIn(minHeight = 48.dp),
                            ) {
                                Text("Show total")
                            }
                        }

                        if (point != null) {
                            io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar(
                                mobileBytes = point.mobileBytes,
                                wifiBytes = point.wifiBytes,
                                mobileColor = palette.primary,
                                wifiColor = palette.accent,
                                units = state.units,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

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
                )
            }

            // Bento tiles: Total and Average
            item {
                val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
                if (fontScale >= 1.5f) {
                    androidx.compose.foundation.layout.Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BentoTile(title = "Total", modifier = Modifier.fillMaxWidth(), container = TileContainer.Primary) {
                            ForceLtr {
                                Text(plainBytes(state.totalBytes, state.units), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        BentoTile(title = "Average per active $unit", modifier = Modifier.fillMaxWidth()) {
                            ForceLtr {
                                Text(plainBytes(state.averageBytes, state.units), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoTile(title = "Total", modifier = Modifier.weight(1f), container = TileContainer.Primary) {
                            ForceLtr {
                                Text(plainBytes(state.totalBytes, state.units), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        BentoTile(title = "Average per active $unit", modifier = Modifier.weight(1f)) {
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
