package io.github.khaledbahaaeldin.emberbyte.data.fake

import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.PlanSnapshot
import io.github.khaledbahaaeldin.emberbyte.engine.model.AddOn
import io.github.khaledbahaaeldin.emberbyte.engine.model.Confidence
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.FreeRule
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanPreview
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** One seeded plan ("Main SIM", 10 GB, 3.8 GB left) with canned state and forecast. */
class FakePlanRepository(private val clock: Clock = Clock.systemUTC()) : PlanRepository {

    private val plans = MutableStateFlow(
        listOf(
            Plan(
                id = 1L,
                name = "Main SIM",
                subscriptionId = null,
                capBytes = 10_000_000_000L,
                cycle = Cycle.MonthlyOnDay(12, LocalTime.MIDNIGHT, clock.zone),
                rollover = Rollover.None,
            ),
        ),
    )
    private val addOns = MutableStateFlow<List<AddOn>>(emptyList())
    private val freeRules = MutableStateFlow<List<FreeRule>>(emptyList())
    private var nextId = 2L

    override fun observePlans(): Flow<List<Plan>> = plans

    override fun observePlanState(planId: Long): Flow<PlanState?> =
        plans.map { list -> list.firstOrNull { it.id == planId }?.let(::stateFor) }

    override fun observeActivePlanStates(): Flow<List<PlanState>> =
        plans.map { list -> list.map(::stateFor) }

    override fun observeForecast(planId: Long): Flow<Forecast?> =
        plans.map { list -> if (list.any { it.id == planId }) forecastFor(planId) else null }

    override fun observeAddOns(planId: Long): Flow<List<AddOn>> =
        addOns.map { list -> list.filter { it.planId == planId } }

    override fun observeFreeRules(planId: Long): Flow<List<FreeRule>> =
        freeRules.map { list -> list.filter { it.planId == planId } }

    override suspend fun upsertPlan(plan: Plan): Outcome<Long> {
        val id = if (plan.id == 0L) nextId++ else plan.id
        plans.update { list -> list.filter { it.id != id } + plan.copy(id = id) }
        return Outcome.Success(id)
    }

    override suspend fun restoreArchivedPlans(): Outcome<Int> {
        var count = 0
        plans.update { list ->
            list.map { if (it.archived) { count++; it.copy(archived = false) } else it }
        }
        return Outcome.Success(count)
    }

    override suspend fun deletePlan(planId: Long): Outcome<PlanSnapshot> {
        val target = plans.value.firstOrNull { it.id == planId } ?: return Outcome.Failure(EmberbyteError.NotFound)
        val snap = PlanSnapshot(
            plan = target,
            addOns = addOns.value.filter { it.planId == planId },
            freeRules = freeRules.value.filter { it.planId == planId },
            alertThresholds = emptySet(),
            budgetIdsWithPlan = emptyList(),
        )
        plans.update { list -> list.filter { it.id != planId } }
        addOns.update { list -> list.filter { it.planId != planId } }
        freeRules.update { list -> list.filter { it.planId != planId } }
        return Outcome.Success(snap)
    }

    override suspend fun restorePlan(snapshot: PlanSnapshot): Outcome<Unit> {
        plans.update { list -> list.filter { it.id != snapshot.plan.id } + snapshot.plan.copy(archived = false) }
        addOns.update { list -> list.filter { it.planId != snapshot.plan.id } + snapshot.addOns }
        freeRules.update { list -> list.filter { it.planId != snapshot.plan.id } + snapshot.freeRules }
        return Outcome.Success(Unit)
    }

    override suspend fun setPinned(id: Long, pinned: Boolean): Outcome<Unit> {
        if (plans.value.none { it.id == id }) return Outcome.Failure(EmberbyteError.NotFound)
        plans.update { list ->
            list.map {
                when {
                    it.id == id -> it.copy(pinned = pinned)
                    pinned -> it.copy(pinned = false)
                    else -> it
                }
            }
        }
        return Outcome.Success(Unit)
    }

    override suspend fun upsertAddOn(addOn: AddOn): Outcome<Long> {
        val id = if (addOn.id == 0L) nextId++ else addOn.id
        addOns.update { list -> list.filter { it.id != id } + addOn.copy(id = id) }
        return Outcome.Success(id)
    }

    override suspend fun deleteAddOn(id: Long): Outcome<Unit> {
        addOns.update { list -> list.filter { it.id != id } }
        return Outcome.Success(Unit)
    }

    override suspend fun upsertFreeRule(rule: FreeRule): Outcome<Long> {
        val id = if (rule.id == 0L) nextId++ else rule.id
        freeRules.update { list -> list.filter { it.id != id } + rule.copy(id = id) }
        return Outcome.Success(id)
    }

    override suspend fun deleteFreeRule(id: Long): Outcome<Unit> {
        freeRules.update { list -> list.filter { it.id != id } }
        return Outcome.Success(Unit)
    }

    override suspend fun whatIf(planId: Long, extraBytesPerDay: Long, from: Instant): Outcome<Forecast> =
        if (plans.value.any { it.id == planId }) Outcome.Success(forecastFor(planId))
        else Outcome.Failure(EmberbyteError.NotFound)

    override suspend fun forecastWithExtraCap(planId: Long, extraBytes: Long): Outcome<Forecast> =
        if (plans.value.any { it.id == planId }) Outcome.Success(forecastFor(planId))
        else Outcome.Failure(EmberbyteError.NotFound)

    override suspend fun previewState(plan: Plan, addOns: List<AddOn>, freeRules: List<FreeRule>): Outcome<PlanPreview> =
        Outcome.Success(PlanPreview(stateFor(plan), forecastFor(plan.id)))

    override fun validateDraft(plan: Plan, addOns: List<AddOn>, freeRules: List<FreeRule>): Map<String, String> =
        emptyMap()

    override suspend fun savePlan(plan: Plan, addOns: List<AddOn>, freeRules: List<FreeRule>, alertThresholds: Set<Int>): Outcome<Long> {
        val id = if (plan.id == 0L) nextId++ else plan.id
        plans.update { list -> list.filter { it.id != id } + plan.copy(id = id) }
        this.addOns.update { list ->
            list.filter { it.planId != id } + addOns.map { a ->
                val aid = if (a.id == 0L) nextId++ else a.id
                a.copy(id = aid, planId = id)
            }
        }
        this.freeRules.update { list ->
            list.filter { it.planId != id } + freeRules.map { r ->
                val rid = if (r.id == 0L) nextId++ else r.id
                r.copy(id = rid, planId = id)
            }
        }
        return Outcome.Success(id)
    }

    private fun stateFor(plan: Plan): PlanState {
        val now = clock.instant()
        val used = 6_200_000_000L
        return PlanState(
            plan = plan,
            window = CycleWindow(now.minus(21, ChronoUnit.DAYS), now.plus(9, ChronoUnit.DAYS)),
            effectiveCapBytes = plan.capBytes,
            usedBytes = used,
            remainingBytes = plan.capBytes - used,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            daysLeft = 9,
            fractionUsed = used.toFloat() / plan.capBytes,
            isApproximate = false,
        )
    }

    private fun forecastFor(planId: Long): Forecast {
        val now = clock.instant()
        return Forecast(
            planId = planId,
            runOutEarliest = now.plus(2, ChronoUnit.DAYS),
            runOutExpected = now.plus(3, ChronoUnit.DAYS),
            runOutLatest = now.plus(4, ChronoUnit.DAYS),
            projectedCycleEndBytes = 13_100_000_000L,
            confidence = Confidence.MEDIUM,
            historyDays = 14,
        )
    }
}
