package io.github.khaledbahaaeldin.emberbyte.history

import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsageFilter
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import org.junit.Before
import org.junit.Test

/** The Home card opens History on its selected day and takes back the day chosen there. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryHandOffTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    /** Fourteen daily points ending today, as the Day chart shows. */
    private class FourteenDays(base: UsageRepository, private val today: LocalDate) : UsageRepository by base {
        override fun observeSeries(range: DateRange, granularity: Granularity, filter: UsageFilter): Flow<List<UsagePoint>> =
            flowOf(
                (13 downTo 0).map { back ->
                    UsagePoint(today.minusDays(back.toLong()).atStartOfDay(ZoneOffset.UTC).toInstant(), 50_000_000L, 50_000_000L)
                },
            )
    }

    private fun viewModel(): HistoryViewModel {
        val today = LocalDate.of(2026, 11, 20)
        return HistoryViewModel(
            usage = FourteenDays(FakeUsageRepository(clock, flowOf()), today),
            settings = FakeSettingsRepository(),
            permissions = FakePermissionRepository(PermissionState(usageAccess = true, notifications = true, phoneState = true)),
            clock = clock,
            locale = Locale.ENGLISH,
            dates = flowOf(today),
        )
    }

    @Test fun history_receives_the_selected_day() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.showHomeDay(3) // the card's fourth day = the 11th of 14 bars
        advanceUntilIdle()
        assertEquals(10, vm.uiState.value.selectedIndex)
        job.cancel()
    }

    @Test fun history_returns_the_changed_day_to_the_card() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.showHomeDay(3)
        advanceUntilIdle()
        assertEquals(3, vm.homeStripDay())

        vm.onEvent(HistoryEvent.SelectBar(12))
        advanceUntilIdle()
        assertEquals(5, vm.homeStripDay())

        // A day older than the card's week, or no selection, hands nothing back.
        vm.onEvent(HistoryEvent.SelectBar(2))
        advanceUntilIdle()
        assertNull(vm.homeStripDay())
        job.cancel()
    }

    @Test fun other_granularities_hand_nothing_back() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.showHomeDay(6)
        vm.onEvent(HistoryEvent.SetGranularity(Granularity.WEEK))
        advanceUntilIdle()
        assertNull(vm.homeStripDay())
        job.cancel()
    }

    @Test fun days_outside_the_strip_are_ignored() = runTest(dispatcher) {
        val vm = viewModel()
        vm.showHomeDay(9)
        vm.showHomeDay(-1)
        assertNull(vm.homeStripDay())
    }
}
