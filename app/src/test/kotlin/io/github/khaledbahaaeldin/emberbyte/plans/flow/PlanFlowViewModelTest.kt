package io.github.khaledbahaaeldin.emberbyte.plans.flow

import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanFlowViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(testClock: Clock = clock): Pair<PlanFlowViewModel, RoomPlanRepository> {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, testClock, ticks = flowOf(testClock.instant()))
        val settings = FakeSettingsRepository()
        val sims = FakeSimInfoSource()
        val vm = PlanFlowViewModel(repo, settings, sims, testClock)
        return vm to repo
    }

    @Test
    fun continue_is_disabled_until_name_and_cap_are_valid() = runTest(dispatcher) {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        // Step 1: Name
        assertEquals(1, vm.uiState.value.step)
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetName("   "))
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetName("Vodafone Main"))
        assertTrue(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.NextStep)
        // Step 2: Cap
        assertEquals(2, vm.uiState.value.step)
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetCap("0"))
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetCap("10"))
        assertTrue(vm.uiState.value.canContinue())
    }

    @Test
    fun cap_zero_or_negative_is_rejected() = runTest(dispatcher) {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("Main"))
        vm.onEvent(PlanFlowEvent.NextStep)

        vm.onEvent(PlanFlowEvent.SetCap("0"))
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetCap("-5"))
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetCap("abc"))
        assertFalse(vm.uiState.value.canContinue())

        vm.onEvent(PlanFlowEvent.SetCap("5.5"))
        assertTrue(vm.uiState.value.canContinue())
    }

    @Test
    fun this_cycle_step_is_skipped_when_the_cycle_starts_today() = runTest(dispatcher) {
        // Today is 1st of month: 2026-10-01
        val startOfMonth = Instant.parse("2026-10-01T10:00:00Z")
        val todayStartClock = Clock.fixed(startOfMonth, ZoneOffset.UTC)
        val (vm, _) = createViewModel(todayStartClock)
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("Main"))
        vm.onEvent(PlanFlowEvent.NextStep) // -> Step 2
        vm.onEvent(PlanFlowEvent.SetCap("10"))
        vm.onEvent(PlanFlowEvent.NextStep) // -> Step 3
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(1)) // Renewal is 1st, which is today!

        assertEquals(3, vm.uiState.value.step)
        vm.onEvent(PlanFlowEvent.NextStep)

        // Mid-cycle step is skipped -> jumps directly to Review (which is step 4 in a 4-step flow or review step)
        assertTrue(vm.uiState.value.isReviewStep)
        assertFalse(vm.uiState.value.isMidCycleApplicable)
    }

    @Test
    fun entered_choice_builds_Entered_with_now_and_the_current_cycle_start() = runTest(dispatcher) {
        // Today is 15 Oct, cycle starts on 1st Oct
        val (vm, repo) = createViewModel()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("My Plan"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("10"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(1))
        vm.onEvent(PlanFlowEvent.NextStep) // -> Step 4 (This cycle)

        assertEquals(4, vm.uiState.value.step)
        assertTrue(vm.uiState.value.isMidCycleApplicable)

        vm.onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.Entered))
        vm.onEvent(PlanFlowEvent.SetEnteredAmount("3.5"))
        vm.onEvent(PlanFlowEvent.SetEnteredUnit(CapUnit.GB))

        vm.onEvent(PlanFlowEvent.NextStep) // -> Review
        assertTrue(vm.uiState.value.isReviewStep)

        vm.onEvent(PlanFlowEvent.CreatePlan)
        advanceUntilIdle()

        val created = vm.uiState.value.createdPlanId?.let { id -> repo.observePlans(true).first().firstOrNull { it.id == id } }
        assertNotNull(created)
        assertTrue(created!!.firstCycle is FirstCycle.Entered)
        val fc = created.firstCycle as FirstCycle.Entered
        assertEquals(now, fc.enteredAt)
        assertEquals(Instant.parse("2026-10-01T00:00:00Z"), fc.cycleStart)
        assertEquals(3_500_000_000L, fc.usedBytes)
    }

    @Test
    fun later_choice_builds_StartsNextCycle_at_the_next_boundary() = runTest(dispatcher) {
        val (vm, repo) = createViewModel()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("My Plan"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("10"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(1))
        vm.onEvent(PlanFlowEvent.NextStep)

        vm.onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.StartNextCycle))
        vm.onEvent(PlanFlowEvent.NextStep)

        vm.onEvent(PlanFlowEvent.CreatePlan)
        advanceUntilIdle()

        val created = vm.uiState.value.createdPlanId?.let { id -> repo.observePlans(true).first().firstOrNull { it.id == id } }
        assertNotNull(created)
        assertTrue(created!!.firstCycle is FirstCycle.StartsNextCycle)
        val fc = created.firstCycle as FirstCycle.StartsNextCycle
        assertEquals(Instant.parse("2026-11-01T00:00:00Z"), fc.startsAt)
    }

    @Test
    fun entered_above_cap_sets_the_over_cap_warning_but_can_continue() = runTest(dispatcher) {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("My Plan"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("10")) // 10 GB
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(1))
        vm.onEvent(PlanFlowEvent.NextStep)

        vm.onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.Entered))
        vm.onEvent(PlanFlowEvent.SetEnteredAmount("15")) // 15 GB > 10 GB
        vm.onEvent(PlanFlowEvent.SetEnteredUnit(CapUnit.GB))

        assertTrue(vm.uiState.value.isEnteredOverCap)
        assertTrue("Entered amount above cap is allowed and does not block continue", vm.uiState.value.canContinue())
    }

    @Test
    fun going_back_keeps_every_value() = runTest(dispatcher) {
        val (vm, _) = createViewModel()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("Saved Name"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("25"))
        vm.onEvent(PlanFlowEvent.SetCapUnit(CapUnit.GB))
        vm.onEvent(PlanFlowEvent.NextStep)

        // Go back to Step 2
        vm.onEvent(PlanFlowEvent.PreviousStep)
        assertEquals("25", vm.uiState.value.capText)
        assertEquals(CapUnit.GB, vm.uiState.value.capUnit)

        // Go back to Step 1
        vm.onEvent(PlanFlowEvent.PreviousStep)
        assertEquals("Saved Name", vm.uiState.value.name)
    }

    @Test
    fun create_calls_upsert_once_even_if_tapped_twice() = runTest(dispatcher) {
        val (vm, repo) = createViewModel()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("Single Create"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("10"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(1))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.NextStep) // Review

        // Double tap CreatePlan
        vm.onEvent(PlanFlowEvent.CreatePlan)
        vm.onEvent(PlanFlowEvent.CreatePlan)
        advanceUntilIdle()

        val created = vm.uiState.value.createdPlanId?.let { id -> repo.observePlans(true).first().firstOrNull { it.id == id } }
        assertNotNull(created)
    }
}
