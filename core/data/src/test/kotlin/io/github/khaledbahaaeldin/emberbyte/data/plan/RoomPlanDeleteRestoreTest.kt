package io.github.khaledbahaaeldin.emberbyte.data.plan

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.AddOn
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppBudget
import io.github.khaledbahaaeldin.emberbyte.engine.model.BudgetPeriod
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FreeRule
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomPlanDeleteRestoreTest {
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private lateinit var db: EmberbyteDatabase
    private lateinit var store: RoomPlanStore
    private lateinit var budgetStore: RoomBudgetStore
    private lateinit var repo: RoomPlanRepository

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
        store = RoomPlanStore(db)
        budgetStore = RoomBudgetStore(db)
        repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun samplePlan(name: String = "Main", archived: Boolean = false, pinned: Boolean = false) = Plan(
        id = 0L,
        name = name,
        subscriptionId = 1,
        capBytes = 10_000_000_000L,
        cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
        rollover = Rollover.None,
        archived = archived,
        pinned = pinned,
    )

    @Test
    fun restore_archived_plans_unarchives_all_and_returns_the_count() = runBlocking {
        repo.upsertPlan(samplePlan("Old 1", archived = true))
        repo.upsertPlan(samplePlan("Old 2", archived = true, pinned = true))
        repo.upsertPlan(samplePlan("Active", archived = false))

        val count = (repo.restoreArchivedPlans() as Outcome.Success).value
        assertEquals(2, count)
        assertTrue(repo.observePlans().first().none { it.archived })
        assertEquals(0, (repo.restoreArchivedPlans() as Outcome.Success).value)
    }

    @Test
    fun delete_plan_removes_addons_rules_and_thresholds_and_unlinks_budgets_then_restores() = runBlocking {
        val planId = (repo.savePlan(
            plan = samplePlan("Main"),
            addOns = listOf(AddOn(0L, 0L, "Boost", 2_000_000_000L, now.minusSeconds(3600), now.plusSeconds(86400))),
            freeRules = listOf(FreeRule(0L, 0L, "Night", setOf(DayOfWeek.MONDAY), LocalTime.MIDNIGHT, LocalTime.of(6, 0), null)),
            alertThresholds = setOf(80, 100),
        ) as Outcome.Success).value

        val budgetId = budgetStore.upsertBudget(
            AppBudget(
                id = 0L,
                packageName = "com.video",
                limitBytes = 1_000_000_000L,
                period = BudgetPeriod.CYCLE,
                planId = planId,
                warnAtPercent = 80,
                enabled = true,
            ),
        )

        val snapshot = (repo.deletePlan(planId) as Outcome.Success).value
        assertNull(store.plan(planId))
        assertTrue(store.observeAddOns(planId).first().isEmpty())
        assertTrue(store.observeFreeRules(planId).first().isEmpty())
        assertTrue(store.alertRules(planId).isEmpty())
        assertEquals(listOf(budgetId), snapshot.budgetIdsWithPlan)
        assertNull(budgetStore.budgets().single { it.id == budgetId }.planId)

        assertTrue(repo.restorePlan(snapshot) is Outcome.Success)
        assertEquals("Main", store.plan(planId)?.name)
        assertEquals(1, store.observeAddOns(planId).first().size)
        assertEquals(1, store.observeFreeRules(planId).first().size)
        assertEquals(listOf(80, 100), store.alertRules(planId).map { it.thresholdPercent })
        assertEquals(planId, budgetStore.budgets().single { it.id == budgetId }.planId)
    }
}
