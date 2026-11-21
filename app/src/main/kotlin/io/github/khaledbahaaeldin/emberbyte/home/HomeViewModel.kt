package io.github.khaledbahaaeldin.emberbyte.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.data.PermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.SpikeRepository
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.util.DayClock
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageStatus
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.DayUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.LiveSpeed
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.SpikeEvent
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanChipUi
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.appendSample
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageFilters
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageFilter
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.NetworkFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class HomeData(
    val today: DayUsage,
    val live: LiveSpeed?,
    val planState: PlanState?,
    val forecast: Forecast?,
    val apps: List<AppUsage>,
    val week: List<UsagePoint>,
    val zone: ZoneId,
    val planChips: List<PlanChipUi>,
    val dayApps: List<List<AppUsage>> = emptyList(),
)

private data class HomeBundle(
    val data: HomeData,
    val coverage: CoverageStatus,
    val permissions: PermissionState,
    val spikes: List<SpikeEvent>,
)

/**
 * @param dates emits the current local date and again after every midnight; the ranges below follow it.
 *   Tests MUST pass a finite flow (for example `flowOf(date)`): the default never completes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val usage: UsageRepository,
    plans: PlanRepository,
    settings: SettingsRepository,
    permissions: PermissionRepository,
    private val spikes: SpikeRepository,
    private val labelOf: (String) -> String = { it },
    private val clock: Clock = SystemZoneClock(),
    private val locale: Locale = Locale.getDefault(),
    dates: Flow<LocalDate> = DayClock(clock).dates(),
    private val is24Hour: () -> Boolean = { true },
) : ViewModel() {

    private val selectedDay = MutableStateFlow<Int?>(null)
    private val selectedPlanId = MutableStateFlow<Long?>(null)
    private val usageFilters = MutableStateFlow(UsageFilters()) // session only; independent of the Apps screen

    // Shared so the speed tile, the Live trace and the totals all follow one collection of the live rate.
    private val live: Flow<LiveSpeed?> = usage.observeLiveSpeed().map<LiveSpeed, LiveSpeed?> { it }.onStart { emit(null) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    /** In-memory ring buffers (60 samples) of the receive and transmit rates, kept apart. Never persisted. */
    private val liveHistory: Flow<Pair<List<Long>, List<Long>>> =
        live.scan(emptyList<Long>() to emptyList<Long>()) { (rx, tx), speed ->
            if (speed == null) rx to tx else appendSample(rx, speed.rxBps) to appendSample(tx, speed.txBps)
        }

    private data class PlanBundle(val state: PlanState?, val forecast: Forecast?, val chips: List<PlanChipUi>)

    private val planBundle: Flow<PlanBundle> =
        combine(plans.observeActivePlanStates(), selectedPlanId) { states, selected ->
            val chosen = states.firstOrNull { it.plan.id == selected } ?: states.firstOrNull()
            chosen to states.map { PlanChipUi(it.plan.id, it.plan.name, it.plan.id == chosen?.plan?.id) }
        }.flatMapLatest { (chosen, chips) ->
            if (chosen == null) flowOf(PlanBundle(null, null, chips))
            else plans.observeForecast(chosen.plan.id).map { PlanBundle(chosen, it, chips) }
        }

    private val data: Flow<HomeData> = dates.flatMapLatest { todayDate ->
        val zone = clock.zone
        val endOfDay = todayDate.plusDays(1).atStartOfDay(zone).toInstant()
        val todayRange = DateRange(todayDate.atStartOfDay(zone).toInstant(), endOfDay)
        val weekRange = DateRange(todayDate.minusDays(6).atStartOfDay(zone).toInstant(), endOfDay)
        combine(
            usage.observeToday(),
            live,
            planBundle,
            usage.observeApps(todayRange),
            usage.observeSeries(weekRange, Granularity.DAY),
        ) { today, liveSpeed, pb, apps, week ->
            HomeData(today, liveSpeed, pb.state, pb.forecast, apps, week, zone, pb.chips)
        }.combine(dayApps(todayDate, zone)) { data, perDay -> data.copy(dayApps = perDay) }
    }

    /** One app ranking per day for the last seven days (oldest first), for the usage strip and its filters. */
    private fun dayApps(todayDate: LocalDate, zone: ZoneId): Flow<List<List<AppUsage>>> =
        combine((6 downTo 0).map { back ->
            val day = todayDate.minusDays(back.toLong())
            usage.observeApps(DateRange(day.atStartOfDay(zone).toInstant(), day.plusDays(1).atStartOfDay(zone).toInstant()))
        }) { it.toList() }

    private val bundle: Flow<HomeBundle> =
        combine(
            data,
            usage.observeCoverage(),
            permissions.observe(),
            spikes.observeSpikes(includeDismissed = false),
        ) { d, coverage, perms, spikesList ->
            HomeBundle(d, coverage, perms, spikesList)
        }

    val uiState: StateFlow<HomeUiState> = combine(bundle, settings.observe(), selectedDay, liveHistory, usageFilters) { b, s, selected, history, filters ->
        val newestSpike = b.spikes
            .filter { !it.dismissed }
            .maxWithOrNull(compareBy<SpikeEvent> { it.detectedAt }.thenBy { it.id })
        val spikeUi = newestSpike?.let {
            spikeTile(it, labelOf(it.packageName), s.unitSystem.toByteUnits())
        }
        buildHomeUiState(
            today = b.data.today,
            live = b.data.live,
            planState = b.data.planState,
            forecast = b.data.forecast,
            apps = b.data.apps,
            week = b.data.week,
            units = s.unitSystem,
            selectedDay = selected,
            now = clock.instant(),
            zone = b.data.zone,
            locale = locale,
            coverage = b.coverage,
            permissions = b.permissions,
            planChips = b.data.planChips,
            is24Hour = is24Hour(),
            spike = spikeUi,
            liveRxHistory = history.first,
            liveTxHistory = history.second,
            dayApps = b.data.dayApps,
            usageFilters = filters,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectDay -> selectedDay.update { current -> if (current == event.index) null else event.index }
            is HomeEvent.SelectPlan -> selectedPlanId.value = event.planId
            is HomeEvent.DismissSpike -> viewModelScope.launch { spikes.dismiss(event.id) }
            is HomeEvent.SetUsageFilters -> usageFilters.value = event.filters
            is HomeEvent.RemoveUsageFilter -> usageFilters.update { current ->
                when (event.filter) {
                    UsageFilter.Network -> current.copy(network = NetworkFilter.All)
                    UsageFilter.SystemApps -> current.copy(showSystemApps = false)
                }
            }
        }
    }
}
