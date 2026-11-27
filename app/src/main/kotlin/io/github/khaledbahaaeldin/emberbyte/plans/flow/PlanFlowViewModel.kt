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
import io.github.khaledbahaaeldin.emberbyte.plans.RolloverKind
import io.github.khaledbahaaeldin.emberbyte.plans.SimOptionUi
import io.github.khaledbahaaeldin.emberbyte.plans.bytesOf
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val MAX_PLAN_CAP_BYTES = 1_000_000_000_000_000L
internal val DEFAULT_PLAN_ALERT_THRESHOLDS: Set<Int> = setOf(80, 100)

enum class FirstCycleChoice {
    PhoneRecord,
    Entered,
    StartNextCycle,
}

enum class PlanFlowError {
    NameRequired,
    NameTooLong,
    CapPositive,
    CapMax,
    DayRange,
    DaysRange,
    EnteredPositive,
    EnteredMax,
}

/**
 * Accepts only digits and at most one decimal separator (`.`, `,`, or [localeSeparator]).
 * Returns `null` when [incoming] contains any disallowed character or a second separator so the caller keeps the prior text.
 */
internal fun filterDecimalInput(
    incoming: String,
    localeSeparator: Char = '.',
): String? {
    var separatorCount = 0
    for (ch in incoming) {
        when {
            ch.isDigit() -> Unit
            ch == '.' || ch == ',' || ch == localeSeparator -> {
                separatorCount += 1
                if (separatorCount > 1) return null
            }
            else -> return null
        }
    }
    return incoming
}

/** Accepts only digit characters; returns `null` if any non-digit character is present. */
internal fun filterIntegerInput(incoming: String): String? {
    for (ch in incoming) {
        if (!ch.isDigit()) return null
    }
    return incoming
}

internal fun validatePositiveAmount(
    text: String,
    unit: CapUnit,
    unitSystem: UnitSystem,
    positiveError: PlanFlowError,
    maxError: PlanFlowError,
): PlanFlowError? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return positiveError
    val normalized = trimmed.replace(',', '.')
    val parsed = normalized.toBigDecimalOrNull() ?: return positiveError
    if (parsed.signum() <= 0) return positiveError
    val bytes = bytesOf(normalized, unit, unitSystem) ?: return maxError
    return if (bytes in 1L..MAX_PLAN_CAP_BYTES) null else maxError
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
    val dayOfMonthText: String = dayOfMonth.toString(),
    val everyDays: Int = 30,
    val everyDaysText: String = everyDays.toString(),
    val anchor: LocalDate = LocalDate.now(),
    val renewalTime: LocalTime = LocalTime.MIDNIGHT,
    val firstCycleChoice: FirstCycleChoice = FirstCycleChoice.PhoneRecord,
    val enteredAmount: String = "",
    val enteredUnit: CapUnit = CapUnit.GB,
    val unitSystem: UnitSystem = UnitSystem.DECIMAL,
    val isMidCycleApplicable: Boolean = true,
    val currentCycleStartText: String = "",
    val nextCycleStartText: String = "",
    val daysUntilNextRenewal: Int = 30,
    val helperText: String = "",
    val isEnteredOverCap: Boolean = false,
    val isReviewStep: Boolean = false,
    val isSaving: Boolean = false,
    val createdPlanId: Long? = null,
    val fieldError: PlanFlowError? = null,
    val validationAttempt: Int = 0,
    val alertThresholds: Set<Int> = DEFAULT_PLAN_ALERT_THRESHOLDS,
    val rollover: RolloverKind = RolloverKind.None,
    val saveFailed: Boolean = false,
) {
    fun stepValidationError(): PlanFlowError? = when (step) {
        1 -> when {
            name.isBlank() -> PlanFlowError.NameRequired
            name.trim().length > 40 -> PlanFlowError.NameTooLong
            else -> null
        }
        2 -> validatePositiveAmount(
            text = capText,
            unit = capUnit,
            unitSystem = unitSystem,
            positiveError = PlanFlowError.CapPositive,
            maxError = PlanFlowError.CapMax,
        )
        3 -> when (cycleKind) {
            CycleKind.Monthly -> {
                val parsed = dayOfMonthText.toLongOrNull()
                if (dayOfMonth in 1..31 && (dayOfMonthText == dayOfMonth.toString() || parsed in 1L..31L)) null else PlanFlowError.DayRange
            }
            CycleKind.EveryNDays -> {
                val parsed = everyDaysText.toLongOrNull()
                if (everyDays in 1..365 && (everyDaysText == everyDays.toString() || parsed in 1L..365L)) null else PlanFlowError.DaysRange
            }
        }
        4 -> if (!isMidCycleApplicable) {
            null
        } else if (firstCycleChoice == FirstCycleChoice.Entered) {
            validatePositiveAmount(
                text = enteredAmount,
                unit = enteredUnit,
                unitSystem = unitSystem,
                positiveError = PlanFlowError.EnteredPositive,
                maxError = PlanFlowError.EnteredMax,
            )
        } else {
            null
        }
        5 -> null
        else -> PlanFlowError.CapPositive
    }

    fun canContinue(): Boolean = step in 1..5 && stepValidationError() == null
}

sealed interface PlanFlowEvent {
    data class SetName(val name: String) : PlanFlowEvent
    data class SetSim(val subscriptionId: Int?) : PlanFlowEvent
    data class SetCap(val text: String) : PlanFlowEvent
    data class SetCapUnit(val unit: CapUnit) : PlanFlowEvent
    data class SetCycleKind(val kind: CycleKind) : PlanFlowEvent
    data class SetDayOfMonth(val day: Int) : PlanFlowEvent
    data class SetDayOfMonthText(val text: String) : PlanFlowEvent
    data class SetEveryDays(val days: Int) : PlanFlowEvent
    data class SetEveryDaysText(val text: String) : PlanFlowEvent
    data class SetFirstCycleChoice(val choice: FirstCycleChoice) : PlanFlowEvent
    data class SetEnteredAmount(val amount: String) : PlanFlowEvent
    data class SetEnteredUnit(val unit: CapUnit) : PlanFlowEvent
    data class ToggleAlert(val percent: Int) : PlanFlowEvent
    data class SetRollover(val rollover: RolloverKind) : PlanFlowEvent
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
            dayOfMonth = 1,
            dayOfMonthText = "1",
        ),
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
            recomputeCycleDates(updateStepStructure = true)
        }
    }

    private fun buildValidCycleOrNull(s: PlanFlowState): Cycle? = when (s.cycleKind) {
        CycleKind.Monthly -> if (s.dayOfMonth in 1..31) Cycle.MonthlyOnDay(s.dayOfMonth, s.renewalTime, zone) else null
        CycleKind.EveryNDays -> if (s.everyDays in 1..365) Cycle.EveryNDays(s.everyDays, s.anchor, s.renewalTime, zone) else null
    }

    /**
     * Recomputes renewal dates and over-cap warnings.
     *
     * [updateStepStructure] is `true` only at initialization and when advancing past step 3,
     * so `totalSteps` never changes while the user is typing in a field (D2).
     */
    private fun recomputeCycleDates(updateStepStructure: Boolean) {
        val s = _uiState.value
        val now = clock.instant()
        val capBytes = bytesOf(s.capText, s.capUnit, s.unitSystem)?.takeIf { it <= MAX_PLAN_CAP_BYTES } ?: 0L
        val enteredBytes = bytesOf(s.enteredAmount, s.enteredUnit, s.unitSystem)?.takeIf { it <= MAX_PLAN_CAP_BYTES } ?: 0L
        val isOver = enteredBytes > capBytes && capBytes > 0L

        val validCycle = buildValidCycleOrNull(s)
        if (validCycle == null) {
            _uiState.update { it.copy(isEnteredOverCap = isOver) }
            return
        }

        val window = CycleCalculator.windowAt(validCycle, now)
        val todayLocalDate = now.atZone(zone).toLocalDate()
        val cycleStartLocalDate = window.start.atZone(zone).toLocalDate()
        val cycleEndLocalDate = window.end.atZone(zone).toLocalDate()
        val daysLeft = ChronoUnit.DAYS.between(todayLocalDate, cycleEndLocalDate).coerceAtLeast(0L).toInt()

        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
        val startFormatted = window.start.atZone(zone).format(formatter)
        val nextFormatted = window.end.atZone(zone).format(formatter)

        _uiState.update { current ->
            val midCycle = if (updateStepStructure) todayLocalDate != cycleStartLocalDate else current.isMidCycleApplicable
            val steps = if (updateStepStructure) (if (midCycle) 5 else 4) else current.totalSteps
            current.copy(
                totalSteps = steps,
                isMidCycleApplicable = midCycle,
                currentCycleStartText = startFormatted,
                nextCycleStartText = nextFormatted,
                daysUntilNextRenewal = daysLeft,
                isEnteredOverCap = isOver,
                isReviewStep = (!midCycle && current.step == 4) || (midCycle && current.step == 5),
            )
        }
    }

    fun onEvent(event: PlanFlowEvent) {
        when (event) {
            is PlanFlowEvent.SetName -> {
                _uiState.update { current ->
                    val nextError = when {
                        event.name.trim().length > 40 -> PlanFlowError.NameTooLong
                        event.name.isNotBlank() -> null
                        current.fieldError != null -> PlanFlowError.NameRequired
                        else -> null
                    }
                    current.copy(name = event.name, fieldError = nextError)
                }
            }
            is PlanFlowEvent.SetSim -> _uiState.update { it.copy(subscriptionId = event.subscriptionId) }
            is PlanFlowEvent.SetCap -> {
                _uiState.update { current ->
                    val filtered = filterDecimalInput(event.text)
                    if (filtered == null) {
                        val currentInvalid = validatePositiveAmount(
                            text = current.capText,
                            unit = current.capUnit,
                            unitSystem = current.unitSystem,
                            positiveError = PlanFlowError.CapPositive,
                            maxError = PlanFlowError.CapMax,
                        )
                        current.copy(fieldError = currentInvalid)
                    } else if (filtered.isEmpty()) {
                        current.copy(
                            capText = "",
                            fieldError = if (current.fieldError != null) PlanFlowError.CapPositive else null,
                        )
                    } else {
                        val err = validatePositiveAmount(
                            text = filtered,
                            unit = current.capUnit,
                            unitSystem = current.unitSystem,
                            positiveError = PlanFlowError.CapPositive,
                            maxError = PlanFlowError.CapMax,
                        )
                        current.copy(capText = filtered, fieldError = err)
                    }
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetCapUnit -> {
                _uiState.update { current ->
                    val err = if (current.capText.isEmpty()) {
                        current.fieldError
                    } else {
                        validatePositiveAmount(
                            text = current.capText,
                            unit = event.unit,
                            unitSystem = current.unitSystem,
                            positiveError = PlanFlowError.CapPositive,
                            maxError = PlanFlowError.CapMax,
                        )
                    }
                    current.copy(capUnit = event.unit, fieldError = err)
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetCycleKind -> {
                _uiState.update { current ->
                    val nextState = current.copy(cycleKind = event.kind, fieldError = null)
                    val err = nextState.stepValidationError()
                    nextState.copy(fieldError = err)
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetDayOfMonth -> {
                val valid = event.day in 1..31
                _uiState.update {
                    it.copy(
                        dayOfMonth = event.day,
                        dayOfMonthText = if (event.day > 0) event.day.toString() else "",
                        fieldError = if (valid) null else PlanFlowError.DayRange,
                    )
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetDayOfMonthText -> {
                _uiState.update { current ->
                    val filtered = filterIntegerInput(event.text)
                    if (filtered == null) {
                        current.copy(fieldError = PlanFlowError.DayRange)
                    } else if (filtered.isEmpty()) {
                        current.copy(
                            dayOfMonthText = "",
                            dayOfMonth = 0,
                            fieldError = if (current.fieldError != null) PlanFlowError.DayRange else null,
                        )
                    } else {
                        val parsed = filtered.toLongOrNull()
                        val valid = parsed != null && parsed in 1L..31L
                        current.copy(
                            dayOfMonthText = filtered,
                            dayOfMonth = if (valid) parsed.toInt() else 0,
                            fieldError = if (valid) null else PlanFlowError.DayRange,
                        )
                    }
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetEveryDays -> {
                val valid = event.days in 1..365
                _uiState.update {
                    it.copy(
                        everyDays = event.days,
                        everyDaysText = if (event.days > 0) event.days.toString() else "",
                        fieldError = if (valid) null else PlanFlowError.DaysRange,
                    )
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetEveryDaysText -> {
                _uiState.update { current ->
                    val filtered = filterIntegerInput(event.text)
                    if (filtered == null) {
                        current.copy(fieldError = PlanFlowError.DaysRange)
                    } else if (filtered.isEmpty()) {
                        current.copy(
                            everyDaysText = "",
                            everyDays = 0,
                            fieldError = if (current.fieldError != null) PlanFlowError.DaysRange else null,
                        )
                    } else {
                        val parsed = filtered.toLongOrNull()
                        val valid = parsed != null && parsed in 1L..365L
                        current.copy(
                            everyDaysText = filtered,
                            everyDays = if (valid) parsed.toInt() else 0,
                            fieldError = if (valid) null else PlanFlowError.DaysRange,
                        )
                    }
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetFirstCycleChoice -> {
                _uiState.update { it.copy(firstCycleChoice = event.choice, fieldError = null) }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetEnteredAmount -> {
                _uiState.update { current ->
                    val filtered = filterDecimalInput(event.amount)
                    if (filtered == null) {
                        val currentInvalid = validatePositiveAmount(
                            text = current.enteredAmount,
                            unit = current.enteredUnit,
                            unitSystem = current.unitSystem,
                            positiveError = PlanFlowError.EnteredPositive,
                            maxError = PlanFlowError.EnteredMax,
                        )
                        current.copy(fieldError = currentInvalid)
                    } else if (filtered.isEmpty()) {
                        current.copy(
                            enteredAmount = "",
                            fieldError = if (current.fieldError != null) PlanFlowError.EnteredPositive else null,
                        )
                    } else {
                        val err = validatePositiveAmount(
                            text = filtered,
                            unit = current.enteredUnit,
                            unitSystem = current.unitSystem,
                            positiveError = PlanFlowError.EnteredPositive,
                            maxError = PlanFlowError.EnteredMax,
                        )
                        current.copy(enteredAmount = filtered, fieldError = err)
                    }
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.SetEnteredUnit -> {
                _uiState.update { current ->
                    val err = if (current.enteredAmount.isEmpty()) {
                        current.fieldError
                    } else {
                        validatePositiveAmount(
                            text = current.enteredAmount,
                            unit = event.unit,
                            unitSystem = current.unitSystem,
                            positiveError = PlanFlowError.EnteredPositive,
                            maxError = PlanFlowError.EnteredMax,
                        )
                    }
                    current.copy(enteredUnit = event.unit, fieldError = err)
                }
                recomputeCycleDates(updateStepStructure = false)
            }
            is PlanFlowEvent.ToggleAlert -> {
                _uiState.update { current ->
                    val next = if (event.percent in current.alertThresholds) {
                        current.alertThresholds - event.percent
                    } else {
                        current.alertThresholds + event.percent
                    }
                    current.copy(alertThresholds = next)
                }
            }
            is PlanFlowEvent.SetRollover -> {
                _uiState.update { it.copy(rollover = event.rollover) }
            }
            PlanFlowEvent.NextStep -> {
                val current = _uiState.value
                val error = current.stepValidationError()
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            fieldError = error,
                            validationAttempt = it.validationAttempt + 1,
                        )
                    }
                    return
                }
                if (current.step == 3) {
                    recomputeCycleDates(updateStepStructure = true)
                }
                val updated = _uiState.value
                val nextStep = updated.step + 1
                _uiState.update {
                    it.copy(
                        step = nextStep,
                        fieldError = null,
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
                        fieldError = null,
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
        val capBytes = bytesOf(s.capText, s.capUnit, s.unitSystem)?.takeIf { it in 1L..MAX_PLAN_CAP_BYTES } ?: run {
            _uiState.update { it.copy(isSaving = false) }
            return
        }
        val cycle = buildValidCycleOrNull(s) ?: run {
            _uiState.update { it.copy(isSaving = false) }
            return
        }
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

        val rollover = when (s.rollover) {
            RolloverKind.None -> Rollover.None
            RolloverKind.Full -> Rollover.Full
            RolloverKind.Capped -> Rollover.Capped(capBytes)
        }
        val plan = Plan(
            id = 0L,
            name = s.name.trim(),
            subscriptionId = s.subscriptionId,
            capBytes = capBytes,
            cycle = cycle,
            rollover = rollover,
            archived = false,
            firstCycle = firstCycle,
        )

        val outcome = plans.savePlan(
            plan = plan,
            addOns = emptyList(),
            freeRules = emptyList(),
            alertThresholds = s.alertThresholds,
        )
        if (outcome is Outcome.Success) {
            _uiState.update { it.copy(createdPlanId = outcome.value, isSaving = false, saveFailed = false) }
        } else {
            _uiState.update { it.copy(isSaving = false, saveFailed = true) }
        }
    }
}
