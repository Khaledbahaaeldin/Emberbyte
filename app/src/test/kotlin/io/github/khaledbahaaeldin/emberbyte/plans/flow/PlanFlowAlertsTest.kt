package io.github.khaledbahaaeldin.emberbyte.plans.flow

import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import java.time.Clock
import java.time.Instant
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanFlowAlertsTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun makeVm(): Pair<PlanFlowViewModel, InMemoryPlanStore> {
        val store = InMemoryPlanStore()
        val repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(clock.instant()))
        val vm = PlanFlowViewModel(
            plans = repo,
            settings = FakeSettingsRepository(),
            sims = FakeSimInfoSource(),
            clock = clock,
        )
        return vm to store
    }

    @Test
    fun alerts_default_to_80_and_100() = runTest(dispatcher) {
        val (vm, _) = makeVm()
        advanceUntilIdle()
        assertEquals(setOf(80, 100), vm.uiState.value.alertThresholds)
    }

    @Test
    fun toggling_a_level_adds_and_removes_it() = runTest(dispatcher) {
        val (vm, _) = makeVm()
        advanceUntilIdle()
        vm.onEvent(PlanFlowEvent.ToggleAlert(50))
        assertEquals(setOf(50, 80, 100), vm.uiState.value.alertThresholds)
        vm.onEvent(PlanFlowEvent.ToggleAlert(80))
        assertEquals(setOf(50, 100), vm.uiState.value.alertThresholds)
    }

    @Test
    fun no_levels_means_alerts_off() = runTest(dispatcher) {
        val (vm, _) = makeVm()
        advanceUntilIdle()
        vm.onEvent(PlanFlowEvent.ToggleAlert(80))
        vm.onEvent(PlanFlowEvent.ToggleAlert(100))
        assertTrue(vm.uiState.value.alertThresholds.isEmpty())
    }

    @Test
    fun the_chosen_levels_are_saved_with_the_plan() = runTest(dispatcher) {
        val (vm, store) = makeVm()
        advanceUntilIdle()

        vm.onEvent(PlanFlowEvent.SetName("Home SIM"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("20"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.ToggleAlert(50))
        vm.onEvent(PlanFlowEvent.ToggleAlert(100))
        vm.onEvent(PlanFlowEvent.CreatePlan)
        advanceUntilIdle()

        val id = vm.uiState.value.createdPlanId!!
        val alerts = store.observeAlertRules(id).first().map { it.thresholdPercent }.toSet()
        assertEquals(setOf(50, 80), alerts)
    }
}
