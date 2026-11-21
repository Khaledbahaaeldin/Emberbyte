package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.common.EmberbyteScreenScaffold
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
import io.github.khaledbahaaeldin.emberbyte.apps.NetworkLegendItem
import io.github.khaledbahaaeldin.emberbyte.home.emberbyteAnimateItem
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBarValue
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.MorphingNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
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
import kotlin.math.abs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import kotlinx.coroutines.flow.flatMapLatest
import androidx.compose.runtime.remember
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.revealOnce
import kotlinx.coroutines.flow.onStart
import androidx.compose.ui.platform.testTag
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.HistoryType
import io.github.khaledbahaaeldin.emberbyte.apps.detail.HourPattern
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import kotlinx.coroutines.flow.launchIn
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
    /** The last 91 days, oldest first, for the calendar, the weekday pattern and the records. */
    val rows: List<DayUsage> = emptyList(),
    /** The fair like-for-like comparison shown under the total (null without previous data). */
    val comparison: PeriodComparison? = null,
    val selection: HistorySelection = HistorySelection.Default,
    /** The heaviest apps of the whole window, with their change against the window before. */
    val topApps: List<AppShare> = emptyList(),
    /** The top apps of the selected bucket or day (the latest bucket by default). */
    val selectedApps: List<AppShare> = emptyList(),
    /** Total per local hour of the day over the last 12 weeks, for all apps. */
    val hourTotals: LongArray = LongArray(24),
    /** The plan cycle so far against its allowance; null without an active plan. */
    val planPace: PlanPace? = null,
)

/** What the selected card shows: the latest bucket, a chart bar, or a calendar day. */
sealed interface HistorySelection {
    data object Default : HistorySelection
    data class Bucket(val index: Int) : HistorySelection
    data class Day(val date: LocalDate) : HistorySelection
}

sealed interface HistoryEvent {
    data class SetGranularity(val granularity: Granularity) : HistoryEvent
    data class SelectBar(val index: Int) : HistoryEvent
    data class SelectDay(val date: LocalDate) : HistoryEvent
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
            mobileBytes = point.mobileBytes,
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
    today: LocalDate? = null,
    rows: List<DayUsage> = emptyList(),
    selection: HistorySelection? = null,
    topApps: List<AppShare> = emptyList(),
    selectedApps: List<AppShare> = emptyList(),
    hourTotals: LongArray = LongArray(24),
    planPace: PlanPace? = null,
): HistoryUiState {
    val total = points.sumOf { it.totalBytes }
    val activeBuckets = points.count { it.totalBytes > 0 }
    val average = if (activeBuckets == 0) 0L else total / activeBuckets
    val comparison = computePeriodComparison(granularity, points)
    val fair = if (today != null) fairComparison(granularity, points, today, zone) else null
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
        comparisonText = fair?.text ?: comparison?.text,
        trendDirection = fair?.direction ?: comparison?.direction ?: TrendDirection.NONE,
        comparison = fair,
        rows = rows,
        selection = selection ?: if (selectedIndex != null) HistorySelection.Bucket(selectedIndex) else HistorySelection.Default,
        topApps = topApps,
        selectedApps = selectedApps,
        hourTotals = hourTotals,
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
        Granularity.WEEK, Granularity.MONTH -> {
            val curr = points.last().totalBytes
            val prev = points[points.size - 2].totalBytes
            curr to prev
        }
        else -> return null
    }

    if (previous == 0L) return null
    val diff = current - previous
    val percent = ((abs(diff).toDouble() / previous.toDouble()) * 100).toInt()

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
    plans: PlanRepository? = null,
) : ViewModel() {
    /** The active plan and its forecast (the same numbers Home shows); nothing when there is no plan. */
    private val planAndForecast: Flow<Pair<PlanState?, Forecast?>> =
        (plans?.observeActivePlanStates() ?: flowOf(emptyList()))
            .flatMapLatest { states ->
                val state = states.firstOrNull()
                if (state == null || plans == null) flowOf<Pair<PlanState?, Forecast?>>(null to null)
                else plans.observeForecast(state.plan.id).map { state to it }
            }
            .onStart { emit(null to null) }

    private val granularity = MutableStateFlow(Granularity.DAY)
    private val selectedBar = MutableStateFlow<Int?>(null)
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    init {
        dates.drop(1)
            .onEach {
                selectedBar.value = null
                selectedDate.value = null
            }
            .launchIn(viewModelScope)
    }

    private data class Extras(
        val rows: List<DayUsage>,
        val hours: LongArray,
        val topApps: List<AppShare>,
        val planState: PlanState? = null,
        val forecast: Forecast? = null,
    )

    val uiState: StateFlow<HistoryUiState> = combine(dates, granularity) { date, g -> date to g }
        .flatMapLatest { (date, g) ->
            val zone = clock.zone
            val range = rangeForHistory(g, date, zone)
            val pointsFlow = usage.observeSeries(range, g)
            val rowsRange = DateRange(
                date.minusDays(90).atStartOfDay(zone).toInstant(),
                date.plusDays(1).atStartOfDay(zone).toInstant(),
            )
            val hoursRange = DateRange(
                date.minusDays(83).atStartOfDay(zone).toInstant(),
                date.plusDays(1).atStartOfDay(zone).toInstant(),
            )
            val before = DateRange(
                range.from.minusSeconds(range.to.epochSecond - range.from.epochSecond),
                range.from,
            )
            // Each supporting query starts with "nothing yet" so a slow or empty source never blocks the screen.
            val extras = combine(
                usage.observeSeries(rowsRange, Granularity.DAY).onStart { emit(emptyList()) },
                usage.observeSeries(hoursRange, Granularity.HOUR).onStart { emit(emptyList()) },
                usage.observeApps(range).onStart { emit(emptyList()) },
                usage.observeApps(before).onStart { emit(emptyList()) },
            ) { dayPoints, hourPoints, apps, previous ->
                Extras(
                    rows = dayPoints.map {
                        DayUsage(it.start.atZone(zone).toLocalDate(), it.totalBytes, it.mobileBytes, it.wifiBytes)
                    },
                    hours = hourTotalsOf(hourPoints, zone),
                    topApps = topAppsWithChange(apps, previous),
                )
            }.let { base ->
                combine(base, planAndForecast) { e, pf -> e.copy(planState = pf.first, forecast = pf.second) }
            }
            val selection = combine(selectedBar, selectedDate) { bar, day -> bar to day }
            // The apps of the selected bucket or day (the latest bucket by default).
            val selectedApps = combine(pointsFlow, selection) { points, sel -> points to sel }
                .flatMapLatest { (points, sel) ->
                    val (bar, day) = sel
                    val bucketRange = when {
                        day != null -> DateRange(day.atStartOfDay(zone).toInstant(), day.plusDays(1).atStartOfDay(zone).toInstant())
                        else -> {
                            val index = (bar?.takeIf { it in points.indices }) ?: points.lastIndex
                            if (index < 0) null else DateRange(
                                points[index].start,
                                points.getOrNull(index + 1)?.start ?: range.to,
                            )
                        }
                    }
                    if (bucketRange == null) flowOf(emptyList()) else usage.observeApps(bucketRange).onStart { emit(emptyList()) }.map { topAppsWithChange(it, emptyList(), 3) }
                }
            combine(
                pointsFlow,
                settings.observe(),
                permissions.observe(),
                selection,
                combine(extras, selectedApps) { e, a -> e to a },
            ) { points, s, perm, sel, extraAndApps ->
                val (bar, day) = sel
                val (e, apps) = extraAndApps
                val validSel = if (day == null && bar != null && bar in points.indices) bar else null
                val selection = when {
                    day != null -> HistorySelection.Day(day)
                    validSel != null -> HistorySelection.Bucket(validSel)
                    else -> HistorySelection.Default
                }
                buildHistoryUiState(
                    g, points, s.unitSystem.toByteUnits(), zone, locale,
                    needsUsageAccess = !perm.usageAccess, selectedIndex = validSel,
                    today = date, rows = e.rows, selection = selection,
                    topApps = e.topApps, selectedApps = apps, hourTotals = e.hours,
                    planPace = cyclePace(e.planState, e.forecast, e.rows, date, zone),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    /**
     * Opens on a day of the Home strip (0 = oldest of the last seven, 6 = today): the Day chart shows 14 days ending
     * today, so that is bar `7 + stripIndex`.
     */
    fun showHomeDay(stripIndex: Int) {
        if (stripIndex !in 0..6) return
        granularity.value = Granularity.DAY
        selectedBar.value = 7 + stripIndex
    }

    /** The strip index (0..6) of the bar chosen here when it is one of the last seven days; otherwise null. */
    fun homeStripDay(): Int? {
        if (granularity.value != Granularity.DAY) return null
        val bar = selectedBar.value ?: return null
        return (bar - 7).takeIf { it in 0..6 }
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.SetGranularity -> {
                if (granularity.value != event.granularity) {
                    granularity.value = event.granularity
                    selectedBar.value = null
                    selectedDate.value = null
                }
            }
            is HistoryEvent.SelectBar -> {
                selectedDate.value = null
                selectedBar.value = if (selectedBar.value == event.index) null else event.index
            }
            is HistoryEvent.SelectDay -> {
                selectedBar.value = null
                selectedDate.value = if (selectedDate.value == event.date) null else event.date
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit, modifier: Modifier = Modifier, onOpenApp: (String) -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
        onOpenApp = onOpenApp,
    )
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenApp: (String) -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    val motion = rememberEmberbyteMotion()
    val seen = remember { mutableSetOf<String>() }
    val unit = when (state.granularity) {
        Granularity.WEEK -> "week"
        Granularity.MONTH -> "month"
        else -> "day"
    }

    EmberbyteScreenScaffold(
        title = "History",
        onBack = onBack,
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header with ConnectedButtonGroup period selector, HeroNumber & comparison
            item(key = "header") {
                HistoryHeader(
                    state = state,
                    onEvent = onEvent,
                    modifier = emberbyteAnimateItem(motion),
                )
            }

            // Selected bar detail or chart
            if (state.loaded && state.bars.isEmpty()) {
                item(key = "empty_history") {
                    EmptyState(
                        icon = Icons.Rounded.History,
                        title = "No history yet",
                        body = "Usage data will appear here once recorded.",
                        modifier = emberbyteAnimateItem(motion),
                    )
                }
            } else {
                // Chart
                item(key = "chart") {
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
                        averageBytes = if (state.bars.isEmpty()) null else state.bars.sumOf { it.bytes } / state.bars.size,
                        modifier = emberbyteAnimateItem(motion),
                    )
                }

                item(key = "selected_card") { SelectedCard(state, emberbyteAnimateItem(motion).revealOnce("selected", seen, 0)) }

                item(key = "sentence") {
                    val total = state.rows.sumOf { it.totalBytes }
                    val mobileShare = if (total <= 0L) 0f else state.rows.sumOf { it.mobileBytes }.toFloat() / total
                    historySentence(state.rows, state.hourTotals, mobileShare)?.let {
                        Text(
                            it,
                            style = EmberbyteType.grotesk(HistoryType.Sentence, 500),
                            color = palette.text,
                            modifier = emberbyteAnimateItem(motion).testTag("history_sentence"),
                        )
                    }
                }

                item(key = "tiles") { HistoryTiles(state, emberbyteAnimateItem(motion).revealOnce("tiles", seen, 1)) }

                if (state.rows.isNotEmpty()) {
                    item(key = "calendar") {
                        ActivityCalendar(
                            rows = state.rows,
                            today = state.rows.last().date,
                            selected = (state.selection as? HistorySelection.Day)?.date,
                            units = state.units,
                            onSelect = { onEvent(HistoryEvent.SelectDay(it)) },
                            modifier = emberbyteAnimateItem(motion).revealOnce("calendar", seen, 0),
                        )
                    }
                    item(key = "weekday") { WeekdayPattern(state.rows, state.units, emberbyteAnimateItem(motion).revealOnce("weekday", seen, 0)) }
                }

                if (!state.needsUsageAccess && state.hourTotals.any { it > 0L }) {
                    item(key = "hours") {
                        HourPattern(state.hourTotals, state.units, emberbyteAnimateItem(motion).revealOnce("hours", seen, 0), title = "When you use data, by hour", perDay = 84)
                    }
                }

                state.planPace?.let { pace ->
                    item(key = "cycle_pace") { CyclePaceChart(pace, emberbyteAnimateItem(motion).revealOnce("pace", seen, 0)) }
                }

                item(key = "mobile_share") {
                    MobileShareChart(mobileShareSeries(state.points), emberbyteAnimateItem(motion).revealOnce("mobile", seen, 0))
                }

                if (state.rows.isNotEmpty()) {
                    item(key = "records") { HistoryRecords(state.rows, state.hourTotals, state.units, emberbyteAnimateItem(motion).revealOnce("records", seen, 0)) }
                }

                if (!state.needsUsageAccess && state.topApps.isNotEmpty()) {
                    item(key = "top_apps") { HistoryTopApps(state.topApps, state.units, onOpenApp, emberbyteAnimateItem(motion).revealOnce("topapps", seen, 0)) }
                }
            }

            if (state.needsUsageAccess) {
                item(key = "usage_access_notice") {
                    Text(
                        "Without usage access only the last 7 days are available; older bars stay empty.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                        modifier = emberbyteAnimateItem(motion),
                    )
                }
            }
        }
    }
}
