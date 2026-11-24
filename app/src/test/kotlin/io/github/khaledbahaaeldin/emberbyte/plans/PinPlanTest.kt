package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.lifecycle.SavedStateHandle
import io.github.khaledbahaaeldin.emberbyte.data.alert.RoomAlertRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanCalculator
import io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanUrgency
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class PinPlanTest {
    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-11-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makePlan(id: Long, name: String, pinned: Boolean) = Plan(
        id = id,
        name = name,
        subscriptionId = null,
        capBytes = 10_000_000_000L,
        cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
        rollover = Rollover.None,
        archived = false,
        pinned = pinned,
    )

    private fun createEnv(): Triple<InMemoryPlanStore, RoomPlanRepository, RoomAlertRepository> {
        val store = InMemoryPlanStore()
        val repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
        val alerts = RoomAlertRepository(store)
        return Triple(store, repo, alerts)
    }

    private fun createVm(repo: RoomPlanRepository, alerts: RoomAlertRepository, planId: Long): PlanEditorViewModel =
        PlanEditorViewModel(
            planId = planId,
            plans = repo,
            alerts = alerts,
            settings = FakeSettingsRepository(),
            permissions = FakePermissionRepository(),
            sims = FakeSimInfoSource(),
            clock = clock,
            previewDebounceMillis = 0L,
            savedStateHandle = SavedStateHandle(),
        )

    @Test
    fun pinning_sets_the_flag() = runTest(dispatcher) {
        val (_, repo, alerts) = createEnv()
        val id = (repo.savePlan(makePlan(0L, "Home SIM", pinned = false), emptyList(), emptyList(), emptySet()) as Outcome.Success).value
        val vm = createVm(repo, alerts, id)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.form.pinned)
        vm.onEvent(PlanEditorEvent.SetPinned(true))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.form.pinned)
        assertNull(vm.uiState.value.pendingPinReplacement)
        vm.onEvent(PlanEditorEvent.Save)
        advanceUntilIdle()

        val saved = repo.observePlans().first().first { it.id == id }
        assertTrue(saved.pinned)
    }

    @Test
    fun pinning_another_plan_asks_before_unpinning_the_first() = runTest(dispatcher) {
        val (_, repo, alerts) = createEnv()
        val id1 = (repo.savePlan(makePlan(0L, "Work", pinned = true), emptyList(), emptyList(), emptySet()) as Outcome.Success).value
        val id2 = (repo.savePlan(makePlan(0L, "Home SIM", pinned = false), emptyList(), emptyList(), emptySet()) as Outcome.Success).value
        val vm = createVm(repo, alerts, id2)
        advanceUntilIdle()

        vm.onEvent(PlanEditorEvent.SetPinned(true))
        advanceUntilIdle()

        assertFalse(vm.uiState.value.form.pinned)
        assertEquals("Work", vm.uiState.value.pendingPinReplacement)

        vm.onEvent(PlanEditorEvent.ConfirmPinReplacement)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.form.pinned)
        assertNull(vm.uiState.value.pendingPinReplacement)

        vm.onEvent(PlanEditorEvent.Save)
        advanceUntilIdle()

        val plans = repo.observePlans().first()
        assertFalse(plans.first { it.id == id1 }.pinned)
        assertTrue(plans.first { it.id == id2 }.pinned)
    }

    @Test
    fun the_front_card_follows_the_pin() {
        val nearLimit = PlanCalculator.state(
            plan = makePlan(1L, "Work", pinned = false),
            at = now,
            usage = emptyList(),
            addOns = emptyList(),
            freeRules = emptyList(),
            previousCycleUnusedBytes = 0L,
            isApproximate = false,
        ).copy(usedBytes = 9_500_000_000L, remainingBytes = 500_000_000L, fractionUsed = 0.95f)
        val pinnedPlan = PlanCalculator.state(
            plan = makePlan(2L, "Home SIM", pinned = true),
            at = now,
            usage = emptyList(),
            addOns = emptyList(),
            freeRules = emptyList(),
            previousCycleUnusedBytes = 0L,
            isApproximate = false,
        ).copy(usedBytes = 1_000_000_000L, remainingBytes = 9_000_000_000L, fractionUsed = 0.1f)

        val front: PlanState? = PlanUrgency.front(listOf(nearLimit, pinnedPlan), now)
        assertEquals(2L, front?.plan?.id)
    }
}
