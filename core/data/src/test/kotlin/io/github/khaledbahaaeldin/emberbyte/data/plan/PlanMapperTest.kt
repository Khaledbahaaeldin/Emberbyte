package io.github.khaledbahaaeldin.emberbyte.data.plan

import io.github.khaledbahaaeldin.emberbyte.data.db.PlanEntity
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanMapperTest {

    private val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
    private val cycleJson = PlanJson.cycleToJson(cycle)
    private val rolloverJson = PlanJson.rolloverToJson(Rollover.None)

    @Test
    fun mapper_roundtrips_each_first_cycle_mode() {
        val basePlan = Plan(
            id = 42L,
            name = "Test Plan",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = cycle,
            rollover = Rollover.None,
            archived = false,
        )

        val plans = listOf(
            basePlan.copy(firstCycle = FirstCycle.PhoneRecord, pinned = false),
            basePlan.copy(firstCycle = FirstCycle.PhoneRecord, pinned = true),
            basePlan.copy(
                firstCycle = FirstCycle.Entered(
                    usedBytes = 500_000_000L,
                    enteredAt = Instant.ofEpochMilli(1700000000000L),
                    cycleStart = Instant.ofEpochMilli(1699900000000L),
                ),
                pinned = false,
            ),
            basePlan.copy(
                firstCycle = FirstCycle.StartsNextCycle(
                    startsAt = Instant.ofEpochMilli(1700100000000L),
                ),
                pinned = true,
            ),
        )

        for (plan in plans) {
            val entity = plan.toEntity()
            val roundtripped = entity.toDomainOrNull()
            assertNotNull("Expected roundtripped plan not to be null for $plan", roundtripped)
            assertEquals("Roundtripped plan must equal original", plan, roundtripped)
        }
    }

    @Test
    fun unknown_mode_maps_to_phone_record() {
        val entity = PlanEntity(
            id = 1L,
            name = "Unknown Mode",
            subscriptionId = null,
            capBytes = 1000L,
            cycleJson = cycleJson,
            rolloverJson = rolloverJson,
            archived = false,
            firstCycleMode = "future_unsupported_mode",
            firstCycleBytes = 500L,
            firstCycleAt = 1000L,
            firstCycleStart = 500L,
            pinned = false,
        )

        val plan = entity.toDomainOrNull()
        assertNotNull(plan)
        assertEquals(FirstCycle.PhoneRecord, plan!!.firstCycle)
    }

    @Test
    fun entered_row_without_bytes_maps_to_phone_record() {
        val entityWithoutBytes = PlanEntity(
            id = 2L,
            name = "Entered without bytes",
            subscriptionId = null,
            capBytes = 1000L,
            cycleJson = cycleJson,
            rolloverJson = rolloverJson,
            archived = false,
            firstCycleMode = "entered",
            firstCycleBytes = null,
            firstCycleAt = 1000L,
            firstCycleStart = 500L,
            pinned = false,
        )

        val plan = entityWithoutBytes.toDomainOrNull()
        assertNotNull(plan)
        assertEquals(FirstCycle.PhoneRecord, plan!!.firstCycle)

        val entityWithoutAt = entityWithoutBytes.copy(firstCycleBytes = 100L, firstCycleAt = null)
        assertEquals(FirstCycle.PhoneRecord, entityWithoutAt.toDomainOrNull()!!.firstCycle)

        val entityWithoutStart = entityWithoutBytes.copy(firstCycleBytes = 100L, firstCycleStart = null)
        assertEquals(FirstCycle.PhoneRecord, entityWithoutStart.toDomainOrNull()!!.firstCycle)
    }
}
