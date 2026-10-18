package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.data.source.SimInfoSource
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.plan.CycleCalculator
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.plans.SimOptionUi
import io.github.khaledbahaaeldin.emberbyte.plans.bytesOf
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FirstCycleChoice {
    PhoneRecord,
    Entered,
    StartNextCycle,
}

data class PlanFlowState(
    val step: Int = 1,
    val totalSteps: Int = 5,
    val name: String = "",
    val subscriptionId: Int? = null,
    val sims: List<SimOptionUi> = emptyList(),
    val capText: String = "",
    val capUnit: CapUnit = CapUnit.GB,
    val cycleKind: CycleKind = CycleKind.Monthly,
    val dayOfMonth: Int = 1,
    val everyDays: Int = 30,
    val anchor: LocalDate = LocalDate.now(),
    val renewalTime: LocalTime = LocalTime.MIDNIGHT,
    val firstCycleChoice: FirstCycleChoice = FirstCycleChoice.PhoneRecord,
    val enteredAmount: String = "",
    val enteredUnit: CapUnit = CapUnit.GB,
    val unitSystem: UnitSystem = UnitSystem.DECIMAL,
    val isMidCycleApplicable: Boolean = true,
    val currentCycleStartText: String = "",
    val nextCycleStartText: String = "",
    val helperText: String = "",
    val isEnteredOverCap: Boolean = false,
    val isReviewStep: Boolean = false,
    val isSaving: Boolean = false,
    val createdPlanId: Long? = null,
) {
    fun canContinue(): Boolean = when (step) {
        1 -> name.isNotBlank()
        2 -> {
            val bytes = bytesOf(capText, capUnit, unitSystem)
            bytes != null && bytes > 0L
        }
        3 -> when (cycleKind) {
            CycleKind.Monthly -> dayOfMonth in 1..31
            CycleKind.EveryNDays -> everyDays in 1..365
        }
        4 -> if (!isMidCycleApplicable) {
            true // step 4 is review if midcycle is skipped
        } else {
            if (firstCycleChoice == FirstCycleChoice.Entered) {
                val entered = bytesOf(enteredAmount, enteredUnit, unitSystem)
                entered != null && entered > 0L
            } else true
        }
        5 -> true
        else -> false
    }
}

sealed interface PlanFlowEvent {
    data class SetName(val name: String) : PlanFlowEvent
    data class SetSim(val subscriptionId: Int?) : PlanFlowEvent
    data class SetCap(val text: String) : PlanFlowEvent
    data class SetCapUnit(val unit: CapUnit) : PlanFlowEvent
    data class SetCycleKind(val kind: CycleKind) : PlanFlowEvent
    data class SetDayOfMonth(val day: Int) : PlanFlowEvent
    data class SetEveryDays(val days: Int) : PlanFlowEvent
    data class SetFirstCycleChoice(val choice: FirstCycleChoice) : PlanFlowEvent
    data class SetEnteredAmount(val amount: String) : PlanFlowEvent
    data class SetEnteredUnit(val unit: CapUnit) : PlanFlowEvent
    data object NextStep : PlanFlowEvent
    data object PreviousStep : PlanFlowEvent
    data object CreatePlan : PlanFlowEvent
}

class PlanFlowViewModel(
    private val plans: PlanRepository,
    private val settings: SettingsRepository,
    private val sims: SimInfoSource,
    private val clock: Clock,
) : ViewModel() {
    private val zone: ZoneId = clock.zone
    private val _uiState = MutableStateFlow(
        PlanFlowState(
            anchor = LocalDate.now(clock),
            dayOfMonth = LocalDate.now(clock).dayOfMonth,
        )
    )
    val uiState: StateFlow<PlanFlowState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val system = settings.observe().first().unitSystem
            val active = sims.activeSims().sortedBy { it.slotIndex }
            val defaultId = sims.defaultDataSubscriptionId()
            val simList = mutableListOf(SimOptionUi(null, "Not tied to a SIM"))
            if (defaultId >= 0 && active.none { it.subscriptionId == defaultId }) {
                simList.add(SimOptionUi(defaultId, "Default data SIM"))
            }
            active.forEach { sim ->
                simList.add(SimOptionUi(sim.subscriptionId, "SIM ${sim.slotIndex + 1} · ${sim.displayName}"))
            }
            _uiState.update { it.copy(unitSystem = system, sims = simList) }
            recomputeCycleDates()
        }
    }

    private fun recomputeCycleDates() {
        val s = _uiState.value
        val now = clock.instant()
        val cycle = buildCycle(s)
        val window = CycleCalculator.windowAt(cycle, now)
        val todayLocalDate = now.atZone(zone).toLocalDate()
        val cycleStartLocalDate = window.start.atZone(zone).toLocalDate()

        val isMidCycle = todayLocalDate != cycleStartLocalDate
        val totalSteps = if (isMidCycle) 5 else 4

        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
        val startFormatted = window.start.atZone(zone).format(formatter)
        val nextFormatted = window.end.atZone(zone).format(formatter)

        val capBytes = bytesOf(s.capText, s.capUnit, s.unitSystem) ?: 0L
        val enteredBytes = bytesOf(s.enteredAmount, s.enteredUnit, s.unitSystem) ?: 0L
        val isOver = enteredBytes > capBytes && capBytes > 0L

        _uiState.update {
            it.copy(
                totalSteps = totalSteps,
                isMidCycleApplicable = isMidCycle,
                currentCycleStartText = startFormatted,
                nextCycleStartText = nextFormatted,
                isEnteredOverCap = isOver,
                isReviewStep = (!isMidCycle && it.step == 4) || (isMidCycle && it.step == 5),
            )
        }
    }

    private fun buildCycle(s: PlanFlowState): Cycle = when (s.cycleKind) {
        CycleKind.Monthly -> Cycle.MonthlyOnDay(s.dayOfMonth, s.renewalTime, zone)
        CycleKind.EveryNDays -> Cycle.EveryNDays(s.everyDays, s.anchor, s.renewalTime, zone)
    }

    fun onEvent(event: PlanFlowEvent) {
        when (event) {
            is PlanFlowEvent.SetName -> _uiState.update { it.copy(name = event.name) }
            is PlanFlowEvent.SetSim -> _uiState.update { it.copy(subscriptionId = event.subscriptionId) }
            is PlanFlowEvent.SetCap -> {
                _uiState.update { it.copy(capText = event.text) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetCapUnit -> {
                _uiState.update { it.copy(capUnit = event.unit) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetCycleKind -> {
                _uiState.update { it.copy(cycleKind = event.kind) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetDayOfMonth -> {
                _uiState.update { it.copy(dayOfMonth = event.day) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetEveryDays -> {
                _uiState.update { it.copy(everyDays = event.days) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetFirstCycleChoice -> {
                _uiState.update { it.copy(firstCycleChoice = event.choice) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetEnteredAmount -> {
                _uiState.update { it.copy(enteredAmount = event.amount) }
                recomputeCycleDates()
            }
            is PlanFlowEvent.SetEnteredUnit -> {
                _uiState.update { it.copy(enteredUnit = event.unit) }
                recomputeCycleDates()
            }
            PlanFlowEvent.NextStep -> {
                val current = _uiState.value
                if (!current.canContinue()) return
                val nextStep = current.step + 1
                _uiState.update {
                    it.copy(
                        step = nextStep,
                        isReviewStep = (!it.isMidCycleApplicable && nextStep == 4) || (it.isMidCycleApplicable && nextStep == 5),
                    )
                }
            }
            PlanFlowEvent.PreviousStep -> {
                _uiState.update {
                    val prev = (it.step - 1).coerceAtLeast(1)
                    it.copy(
                        step = prev,
                        isReviewStep = false,
                    )
                }
            }
            PlanFlowEvent.CreatePlan -> {
                if (_uiState.value.isSaving || _uiState.value.createdPlanId != null) return
                viewModelScope.launch {
                    createPlan()
                }
            }
        }
    }

    private suspend fun createPlan() {
        _uiState.update { it.copy(isSaving = true) }
        val s = _uiState.value
        val now = clock.instant()
        val capBytes = bytesOf(s.capText, s.capUnit, s.unitSystem) ?: return
        val cycle = buildCycle(s)
        val window = CycleCalculator.windowAt(cycle, now)

        val firstCycle: FirstCycle = if (!s.isMidCycleApplicable) {
            FirstCycle.PhoneRecord
        } else {
            when (s.firstCycleChoice) {
                FirstCycleChoice.PhoneRecord -> FirstCycle.PhoneRecord
                FirstCycleChoice.Entered -> {
                    val enteredBytes = bytesOf(s.enteredAmount, s.enteredUnit, s.unitSystem) ?: 0L
                    FirstCycle.Entered(
                        usedBytes = enteredBytes,
                        enteredAt = now,
                        cycleStart = window.start,
                    )
                }
                FirstCycleChoice.StartNextCycle -> FirstCycle.StartsNextCycle(startsAt = window.end)
            }
        }

        val plan = Plan(
            id = 0L,
            name = s.name.trim(),
            subscriptionId = s.subscriptionId,
            capBytes = capBytes,
            cycle = cycle,
            rollover = Rollover.None,
            archived = false,
            firstCycle = firstCycle,
        )

        val outcome = plans.upsertPlan(plan)
        if (outcome is Outcome.Success) {
            _uiState.update { it.copy(createdPlanId = outcome.value, isSaving = false) }
        } else {
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
