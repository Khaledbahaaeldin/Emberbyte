package io.github.khaledbahaaeldin.emberbyte.home

import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.SpikeRepository
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemorySpikeStore
import io.github.khaledbahaaeldin.emberbyte.data.spike.RoomSpikeRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        tick: MutableSharedFlow<Unit> = MutableSharedFlow(),
        settings: FakeSettingsRepository = FakeSettingsRepository(),
        permissions: FakePermissionRepository = FakePermissionRepository(),
        spikes: SpikeRepository = RoomSpikeRepository(InMemorySpikeStore()),
    ) = HomeViewModel(
        usage = FakeUsageRepository(clock, tick),
        plans = FakePlanRepository(clock),
        settings = settings,
        permissions = permissions,
        spikes = spikes,
        clock = clock,
        locale = Locale.ENGLISH,
        dates = flowOf(LocalDate.of(2026, 10, 5)),
    )

    @Test fun emits_today_total_plan_and_forecast() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(1_240_000_000L, state.heroBytes)
        assertEquals("3.80 GB left · runs out Thu (±1 day)", state.subtitle)
        assertEquals(7, state.week.size)
        assertEquals(3, state.topApps.size)
        job.cancel()
    }

    @Test fun live_speed_updates_throughput() = runTest(dispatcher) {
        val tick = MutableSharedFlow<Unit>()
        val vm = viewModel(tick)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(0L, vm.uiState.value.throughputBps)
        tick.emit(Unit)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.throughputBps > 0L)
        job.cancel()
    }

    @Test fun live_history_keeps_receive_and_transmit_separately() = runTest(dispatcher) {
        val tick = MutableSharedFlow<Unit>()
        val vm = viewModel(tick)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        repeat(3) {
            tick.emit(Unit)
            advanceUntilIdle()
        }
        val state = vm.uiState.value
        assertEquals(3, state.liveRxHistory.size)
        assertEquals(3, state.liveTxHistory.size)
        assertEquals(setOf(io.github.khaledbahaaeldin.emberbyte.data.fake.FakeTraffic.rxBpsAt(0L)), state.liveRxHistory.toSet())
        assertEquals(setOf(io.github.khaledbahaaeldin.emberbyte.data.fake.FakeTraffic.txBpsAt(0L)), state.liveTxHistory.toSet())
        assertTrue(state.liveRxHistory.first() != state.liveTxHistory.first())
        job.cancel()
    }

    @Test fun live_history_is_capped_at_60() = runTest(dispatcher) {
        val tick = MutableSharedFlow<Unit>()
        val vm = viewModel(tick)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        repeat(75) {
            tick.emit(Unit)
            advanceUntilIdle()
        }
        assertEquals(60, vm.uiState.value.liveRxHistory.size)
        assertEquals(60, vm.uiState.value.liveTxHistory.size)
        job.cancel()
    }

    @Test fun history_is_not_persisted() = runTest(dispatcher) {
        val tick = MutableSharedFlow<Unit>()
        val first = viewModel(tick)
        val firstJob = launch { first.uiState.collect {} }
        advanceUntilIdle()
        tick.emit(Unit)
        advanceUntilIdle()
        assertEquals(1, first.uiState.value.liveRxHistory.size)
        firstJob.cancel()

        // A new view model (a new process in practice) starts with nothing: samples live only in memory.
        val second = viewModel()
        val secondJob = launch { second.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(second.uiState.value.liveRxHistory.isEmpty())
        assertTrue(second.uiState.value.liveTxHistory.isEmpty())
        secondJob.cancel()
    }

    @Test fun selecting_a_day_switches_the_hero_and_selecting_it_again_returns_to_today() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(HomeEvent.SelectDay(1))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isToday)
        assertEquals(1, vm.uiState.value.selectedDay)

        vm.onEvent(HomeEvent.SelectDay(1))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isToday)
        job.cancel()
    }

    @Test fun unit_setting_is_applied() = runTest(dispatcher) {
        val settings = FakeSettingsRepository()
        val vm = viewModel(settings = settings)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        settings.update { it.copy(unitSystem = UnitSystem.BINARY) }
        advanceUntilIdle()
        assertEquals(io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits.BINARY, vm.uiState.value.units)
        job.cancel()
    }

    @Test fun missing_permissions_become_prompts() = runTest(dispatcher) {
        val permissions = FakePermissionRepository(PermissionState(usageAccess = false, notifications = true, phoneState = true))
        val vm = HomeViewModel(
            usage = FakeUsageRepository(clock, MutableSharedFlow()),
            plans = FakePlanRepository(clock),
            settings = FakeSettingsRepository(),
            permissions = permissions,
            spikes = RoomSpikeRepository(InMemorySpikeStore()),
            clock = clock,
            locale = Locale.ENGLISH,
            dates = flowOf(LocalDate.of(2026, 10, 5)),
        )
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(listOf("usage_access"), vm.uiState.value.prompts.map { it.id })
        permissions.set(PermissionState(true, true, true))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.prompts.isEmpty())
        job.cancel()
    }
}
