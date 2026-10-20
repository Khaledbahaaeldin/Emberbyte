package io.github.khaledbahaaeldin.emberbyte.data.plan

import io.github.khaledbahaaeldin.emberbyte.data.db.*
import io.github.khaledbahaaeldin.emberbyte.engine.model.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.room.withTransaction

internal fun maskOf(days: Set<DayOfWeek>): Int = days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
internal fun daysOf(mask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()
internal fun Plan.toEntity(): PlanEntity {
    val mode: String
    val bytes: Long?
    val at: Long?
    val start: Long?
    when (val fc = firstCycle) {
        is FirstCycle.PhoneRecord -> {
            mode = "phone"
            bytes = null
            at = null
            start = null
        }
        is FirstCycle.Entered -> {
            mode = "entered"
            bytes = fc.usedBytes
            at = fc.enteredAt.toEpochMilli()
            start = fc.cycleStart.toEpochMilli()
        }
        is FirstCycle.StartsNextCycle -> {
            mode = "later"
            bytes = null
            at = null
            start = fc.startsAt.toEpochMilli()
        }
    }
    return PlanEntity(
        id = id,
        name = name,
        subscriptionId = subscriptionId,
        capBytes = capBytes,
        cycleJson = PlanJson.cycleToJson(cycle),
        rolloverJson = PlanJson.rolloverToJson(rollover),
        archived = archived,
        firstCycleMode = mode,
        firstCycleBytes = bytes,
        firstCycleAt = at,
        firstCycleStart = start,
        pinned = pinned,
    )
}

internal fun PlanEntity.toDomainOrNull(): Plan? = runCatching {
    val fc = when (firstCycleMode) {
        "entered" -> if (firstCycleBytes != null && firstCycleAt != null && firstCycleStart != null) {
            FirstCycle.Entered(
                usedBytes = firstCycleBytes,
                enteredAt = Instant.ofEpochMilli(firstCycleAt),
                cycleStart = Instant.ofEpochMilli(firstCycleStart),
            )
        } else {
            FirstCycle.PhoneRecord
        }
        "later" -> if (firstCycleStart != null) {
            FirstCycle.StartsNextCycle(startsAt = Instant.ofEpochMilli(firstCycleStart))
        } else {
            FirstCycle.PhoneRecord
        }
        else -> FirstCycle.PhoneRecord
    }
    Plan(
        id = id,
        name = name,
        subscriptionId = subscriptionId,
        capBytes = capBytes,
        cycle = PlanJson.cycleFromJson(cycleJson),
        rollover = PlanJson.rolloverFromJson(rolloverJson),
        archived = archived,
        firstCycle = fc,
        pinned = pinned,
    )
}.getOrNull()
internal fun AddOn.toEntity() = AddOnEntity(id, planId, label, bytes, validFrom.toEpochMilli(), validUntil.toEpochMilli())
internal fun AddOnEntity.toDomain() = AddOn(id, planId, label, bytes, Instant.ofEpochMilli(validFrom), Instant.ofEpochMilli(validUntil))
internal fun FreeRule.toEntity() = FreeRuleEntity(id, planId, label, maskOf(days), start.toSecondOfDay() / 60, end.toSecondOfDay() / 60, packageName)
internal fun FreeRuleEntity.toDomain() = FreeRule(id, planId, label, daysOf(daysMask), LocalTime.ofSecondOfDay(startMinute * 60L), LocalTime.ofSecondOfDay(endMinute * 60L), packageName)
internal fun AlertRule.toEntity() = AlertRuleEntity(id, planId, thresholdPercent, enabled)
internal fun AlertRuleEntity.toDomain() = AlertRule(id, planId, thresholdPercent, enabled)

class RoomPlanStore(private val db: EmberbyteDatabase) : PlanStore {
    private val dao = db.planDao()

    override fun observePlans(): Flow<List<Plan>> = dao.observePlans().map { list -> list.mapNotNull { it.toDomainOrNull() } }
    override fun observePlan(id: Long): Flow<Plan?> = dao.observePlan(id).map { it?.toDomainOrNull() }
    override suspend fun plan(id: Long): Plan? = dao.plan(id)?.toDomainOrNull()
    override suspend fun upsertPlan(plan: Plan): Long = dao.putPlan(plan.toEntity())
    override suspend fun setArchived(id: Long, archived: Boolean): Boolean = dao.setArchived(id, archived) > 0
    override suspend fun setPinned(id: Long, pinned: Boolean): Boolean = db.withTransaction {
        if (dao.plan(id) == null) return@withTransaction false
        if (pinned) {
            dao.clearAllPinned()
            dao.setPinned(id, true) > 0
        } else {
            dao.setPinned(id, false) > 0
        }
    }
    override fun observeAddOns(planId: Long): Flow<List<AddOn>> = dao.observeAddOns(planId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsertAddOn(addOn: AddOn): Long = dao.putAddOn(addOn.toEntity())
    override suspend fun deleteAddOn(id: Long) = dao.deleteAddOn(id)
    override fun observeFreeRules(planId: Long): Flow<List<FreeRule>> = dao.observeFreeRules(planId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsertFreeRule(rule: FreeRule): Long = dao.putFreeRule(rule.toEntity())
    override suspend fun deleteFreeRule(id: Long) = dao.deleteFreeRule(id)
    override fun observeAlertRules(planId: Long): Flow<List<AlertRule>> = dao.observeAlertRules(planId).map { list -> list.map { it.toDomain() } }
    override suspend fun alertRules(planId: Long): List<AlertRule> = dao.alertRules(planId).map { it.toDomain() }
    override suspend fun upsertAlertRule(rule: AlertRule): Long = dao.putAlertRule(rule.toEntity())
    override suspend fun deleteAlertRule(id: Long) = dao.deleteAlertRule(id)
    override suspend fun lastFiredPercent(planId: Long, windowStart: Instant): Int? = dao.firedPercent(planId, windowStart.toEpochMilli())
    override suspend fun setFiredPercent(planId: Long, windowStart: Instant, percent: Int) =
        dao.putFired(AlertFiredEntity(planId, windowStart.toEpochMilli(), percent))

    internal var beforeCommit: (suspend () -> Unit)? = null

    override suspend fun savePlanWithChildren(plan: Plan, addOns: List<AddOn>, freeRules: List<FreeRule>, alertThresholds: Set<Int>): Long =
        db.withTransaction {
            val id = dao.putPlan(plan.toEntity())
            dao.deleteAddOnsExcept(id, addOns.map { it.id }.filter { it != 0L })
            addOns.forEach { dao.putAddOn(it.copy(planId = id).toEntity()) }
            dao.deleteFreeRulesExcept(id, freeRules.map { it.id }.filter { it != 0L })
            freeRules.forEach { dao.putFreeRule(it.copy(planId = id).toEntity()) }
            val wanted = alertThresholds.toList()
            dao.deleteAlertRulesExcept(id, wanted)
            if (wanted.isNotEmpty()) dao.enableAlertRules(id, wanted)
            val existing = dao.thresholds(id).toSet()
            wanted.filter { it !in existing }.forEach { dao.putAlertRule(AlertRuleEntity(planId = id, thresholdPercent = it, enabled = true)) }
            beforeCommit?.invoke()
            id
        }
}
