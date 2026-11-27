package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.NetworkKind
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.usage.MinuteTotal
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlansViewModelTest {
    private val now = Instant.parse("2026-10-10T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loading_until_first_emission() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val settings = FakeSettingsRepository()
        val sims = FakeSimInfoSource()
        val vm = PlansViewModel(repo, settings, sims, clock, Locale.ENGLISH)

        assertEquals(false, vm.uiState.value.loaded)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(vm.uiState.value.loaded)
        assertNull(vm.uiState.value.front)
        assertTrue(vm.uiState.value.tiles.isEmpty())
        job.cancel()
    }

    @Test
    fun state_has_front_tiles_and_upcoming() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val settings = FakeSettingsRepository()
        val sims = FakeSimInfoSource()
        val vm = PlansViewModel(repo, settings, sims, clock, Locale.ENGLISH)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        usageStore.addMinute(MinuteTotal(now.minusSeconds(3600), NetworkKind.MOBILE, 5, 2_000_000_000L, 0L))
        repo.upsertPlan(Plan(0L, "Main SIM", 5, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, pinned = true))
        repo.upsertPlan(Plan(0L, "Second SIM", 6, 5_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None))
        repo.upsertPlan(Plan(0L, "Future SIM", 7, 5_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, firstCycle = io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle.StartsNextCycle(now.plusSeconds(5 * 86_400L))))
        advanceUntilIdle()

        assertEquals("Main SIM", vm.uiState.value.front?.name)
        assertEquals(listOf("Second SIM"), vm.uiState.value.tiles.map { it.name })
        assertEquals(listOf("Future SIM"), vm.uiState.value.upcoming.map { it.name })
        job.cancel()
    }

    @Test
    fun pin_makes_the_tile_the_front_and_unpins_the_previous() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val vm = PlansViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), clock, Locale.ENGLISH)

        val job = launch { vm.uiState.collect {} }
        repo.upsertPlan(Plan(0L, "Plan A", 5, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, pinned = true))
        repo.upsertPlan(Plan(0L, "Plan B", 6, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, pinned = false))
        advanceUntilIdle()

        val idB = vm.uiState.value.tiles.single().id
        vm.onEvent(PlansEvent.SetPinned(idB, true))
        advanceUntilIdle()

        assertEquals("Plan B", vm.uiState.value.front?.name)
        assertEquals(listOf("Plan A"), vm.uiState.value.tiles.map { it.name })
        job.cancel()
    }

    @Test
    fun delete_then_undo_restores_the_plan() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val vm = PlansViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), clock, Locale.ENGLISH)

        val job = launch { vm.uiState.collect {} }
        repo.upsertPlan(Plan(0L, "Main SIM", 5, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None))
        advanceUntilIdle()

        val id = vm.uiState.value.front!!.id
        vm.onEvent(PlansEvent.Delete(id))
        advanceUntilIdle()
        assertNull(vm.uiState.value.front)
        assertEquals("Main SIM", vm.uiState.value.deletedPlanName)

        vm.onEvent(PlansEvent.UndoDelete)
        advanceUntilIdle()
        assertEquals("Main SIM", vm.uiState.value.front?.name)
        assertNull(vm.uiState.value.deletedPlanName)
        job.cancel()
    }

    @Test
    fun delete_without_undo_stands() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val vm = PlansViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), clock, Locale.ENGLISH)

        val job = launch { vm.uiState.collect {} }
        repo.upsertPlan(Plan(0L, "Main SIM", 5, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None))
        advanceUntilIdle()

        val id = vm.uiState.value.front!!.id
        vm.onEvent(PlansEvent.Delete(id))
        advanceUntilIdle()
        vm.onEvent(PlansEvent.DismissDeleteUndo)
        advanceUntilIdle()

        assertTrue(repo.observePlans().first().isEmpty())
        assertNull(vm.uiState.value.deletedPlanName)
        job.cancel()
    }

    @Test
    fun what_if_never_changes_stored_plans() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val vm = PlansViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), clock, Locale.ENGLISH)

        val job = launch { vm.uiState.collect {} }
        repo.upsertPlan(Plan(0L, "Main SIM", 5, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None))
        advanceUntilIdle()
        val before = repo.observePlans().first()

        vm.onEvent(PlansEvent.SetWhatIf(5))
        advanceUntilIdle()
        val after = repo.observePlans().first()
        assertEquals(before, after)
        assertEquals(5, vm.uiState.value.whatIfExtraGb)
        assertTrue(vm.uiState.value.whatIfSentence?.contains("+5 GB") == true)
        job.cancel()
    }
}
