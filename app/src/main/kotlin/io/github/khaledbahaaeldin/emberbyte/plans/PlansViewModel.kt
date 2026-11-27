package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.common.forecastSentence
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.PlanSnapshot
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.data.source.SimInfo
import io.github.khaledbahaaeldin.emberbyte.data.source.SimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.AddOn
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.home.forecastToUi
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlansViewModel(
    private val plans: PlanRepository,
    settings: SettingsRepository,
    private val sims: SimInfoSource,
    private val clock: Clock = SystemZoneClock(),
    private val locale: Locale = Locale.getDefault(),
    private val session: PlansSessionCoordinator = PlansSessionCoordinator(),
) : ViewModel() {

    private val localPendingDelete = MutableStateFlow<PlanSnapshot?>(null)
    private val expandedDetails = MutableStateFlow(false)
    private val whatIfExtraGb = MutableStateFlow(0)
    private val whatIfSentence = MutableStateFlow<String?>(null)

    private data class Entry(val plan: Plan, val state: PlanState?, val forecast: Forecast?, val addOns: List<AddOn>)
    private data class LocalControls(val expanded: Boolean, val extraGb: Int, val whatIfText: String?, val pendingDelete: PlanSnapshot?, val restoredCount: Int)

    private val entries: Flow<List<Entry>> = plans.observePlans().flatMapLatest { list ->
        if (list.isEmpty()) flowOf(emptyList())
        else combine(list.map { plan ->
            combine(plans.observePlanState(plan.id), plans.observeForecast(plan.id), plans.observeAddOns(plan.id)) { s, f, a ->
                Entry(plan, s, f, a)
            }
        }) { it.toList() }
    }

    private val controls: Flow<LocalControls> = combine(
        expandedDetails,
        whatIfExtraGb,
        whatIfSentence,
        combine(localPendingDelete, session.pendingDelete) { local, shared -> shared ?: local },
        session.restoredCount,
    ) { expanded, extraGb, whatIfText, pending, restored ->
        LocalControls(expanded, extraGb, whatIfText, pending, restored)
    }

    private var frontPlanId: Long? = null
    private var currentUnitSystem: UnitSystem = UnitSystem.DECIMAL
    private var currentFrontState: PlanState? = null
    private var currentFrontForecast: Forecast? = null

    val uiState: StateFlow<PlansUiState> = combine(
        entries,
        settings.observe(),
        controls,
    ) { list, s, ctrl ->
        currentUnitSystem = s.unitSystem
        val units = s.unitSystem.toByteUnits()
        val installed = sims.activeSims()
        val now = clock.instant()
        val states = list.mapNotNull { it.state }
        val entriesById = list.associateBy { it.plan.id }

        val activeStates = states.filter { !it.isDormant }
        val upcomingStates = states.filter { it.isDormant }
        val orderedActive = io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanUrgency.order(activeStates, now)
        val frontState = frontPlan(orderedActive)
        frontPlanId = frontState?.plan?.id
        currentFrontState = frontState
        currentFrontForecast = frontState?.let { entriesById[it.plan.id]?.forecast }

        val frontCard = frontState?.let { st ->
            val e = entriesById[st.plan.id]
            planCardUi(st, e?.forecast, e?.addOns.orEmpty(), simLabel(st.plan.subscriptionId, installed), units, now, clock.zone, locale)
        }
        val tileCards = orderedActive.filter { it.plan.id != frontState?.plan?.id }.mapNotNull { st ->
            val e = entriesById[st.plan.id] ?: return@mapNotNull null
            planCardUi(st, e.forecast, e.addOns, simLabel(st.plan.subscriptionId, installed), units, now, clock.zone, locale)
        }
        val upcomingCards = upcomingStates.mapNotNull { st ->
            val e = entriesById[st.plan.id] ?: return@mapNotNull null
            planCardUi(st, e.forecast, e.addOns, simLabel(st.plan.subscriptionId, installed), units, now, clock.zone, locale)
        }

        val summary = summarize(states, now, clock.zone)
        val worst = worstPlan(activeStates)
        val insight = if (activeStates.isEmpty()) {
            null
        } else {
            val worstForecast = worst?.let { entriesById[it.plan.id]?.forecast }
            val runsOutEarly = worst != null && worstForecast?.runOutExpected != null &&
                worstForecast.runOutExpected!!.isBefore(worst.window.end)
            if (runsOutEarly && worst != null && worstForecast?.runOutExpected != null) {
                val earlyDays = ChronoUnit.DAYS.between(
                    worstForecast.runOutExpected!!.atZone(clock.zone).toLocalDate(),
                    worst.window.end.atZone(clock.zone).toLocalDate(),
                ).toInt().coerceAtLeast(1)
                PlansInsight(
                    worstPlanId = worst.plan.id,
                    worstPlanName = worst.plan.name,
                    earlyDays = earlyDays,
                    dailyAllowanceBytes = dailyAllowance(worst.remainingBytes, worst.daysLeft),
                    allOnTrack = false,
                )
            } else {
                PlansInsight(
                    worstPlanId = null,
                    worstPlanName = null,
                    earlyDays = 0,
                    dailyAllowanceBytes = null,
                    allOnTrack = true,
                )
            }
        }

        PlansUiState(
            front = frontCard,
            tiles = tileCards,
            upcoming = upcomingCards,
            summary = summary,
            insight = insight,
            renewals = renewalEvents(states, now, clock.zone),
            restoredCount = ctrl.restoredCount,
            deletedPlanName = ctrl.pendingDelete?.plan?.name,
            whatIfExtraGb = ctrl.extraGb,
            whatIfSentence = ctrl.whatIfText,
            expandedDetails = ctrl.expanded,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlansUiState())

    fun onEvent(event: PlansEvent) {
        when (event) {
            is PlansEvent.SetPinned -> {
                viewModelScope.launch {
                    plans.setPinned(event.id, event.pinned)
                }
            }
            is PlansEvent.Delete -> {
                viewModelScope.launch {
                    when (val outcome = plans.deletePlan(event.id)) {
                        is Outcome.Success -> {
                            localPendingDelete.value = outcome.value
                            session.setPendingDelete(outcome.value)
                        }
                        is Outcome.Failure -> Unit
                    }
                }
            }
            PlansEvent.UndoDelete -> {
                val snap = session.clearPendingDelete() ?: localPendingDelete.value ?: return
                localPendingDelete.value = null
                viewModelScope.launch {
                    plans.restorePlan(snap)
                }
            }
            PlansEvent.DismissDeleteUndo -> {
                session.clearPendingDelete()
                localPendingDelete.value = null
            }
            PlansEvent.DismissRestoredNotice -> {
                session.dismissRestoredNotice()
            }
            PlansEvent.ToggleDetails -> {
                expandedDetails.update { !it }
            }
            is PlansEvent.SetWhatIf -> {
                whatIfExtraGb.value = event.extraGb
                if (event.extraGb <= 0) {
                    whatIfSentence.value = null
                    return
                }
                val targetId = frontPlanId ?: return
                val state = currentFrontState ?: return
                val base = if (currentUnitSystem == UnitSystem.BINARY) 1024L else 1000L
                val extraBytes = event.extraGb.toLong() * base * base * base
                viewModelScope.launch {
                    val now = clock.instant()
                    val units = currentUnitSystem.toByteUnits()
                    val nowText = currentFrontForecast?.let {
                        forecastSentence(forecastToUi(it, now, clock.zone, locale, state.remainingBytes))
                    } ?: "Will last the cycle"
                    val outcome = plans.forecastWithExtraCap(targetId, extraBytes)
                    val newForecast = (outcome as? Outcome.Success)?.value
                    val nextText = newForecast?.let {
                        forecastSentence(forecastToUi(it, now, clock.zone, locale, state.remainingBytes + extraBytes))
                    } ?: "will last the cycle"
                    val allowance = dailyAllowance(state.remainingBytes + extraBytes, state.daysLeft)
                        ?.let { ", and you could use ${plainBytes(it, units)} a day" }.orEmpty()
                    whatIfSentence.value = "Now: $nowText. With +${event.extraGb} GB: $nextText$allowance."
                }
            }
        }
    }

    private fun simLabel(subscriptionId: Int?, installed: List<SimInfo>): String? = when {
        subscriptionId == null -> null
        else -> installed.firstOrNull { it.subscriptionId == subscriptionId }
            ?.let { "SIM ${it.slotIndex + 1} · ${it.displayName}" }
            ?: if (subscriptionId == sims.defaultDataSubscriptionId()) "Default data SIM" else "SIM (not found)"
    }
}
