package io.github.khaledbahaaeldin.emberbyte.history

import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelStatsTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 20)
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    /** One point per day/hour/week/month in the range; the app list depends on the range length so selections differ. */
    private class Dated(base: UsageRepository) : UsageRepository by base {
        override fun observeSeries(range: DateRange, granularity: Granularity, filter: UsageFilter): Flow<List<UsagePoint>> {
            val step = when (granularity) {
                Granularity.HOUR -> 3_600L
                Granularity.WEEK -> 7 * 86_400L
                Granularity.MONTH -> 30 * 86_400L
                else -> 86_400L
            }
            val points = generateSequence(range.from) { it.plusSeconds(step) }.takeWhile { it < range.to }
                .map { UsagePoint(it, 10_000_000L, 40_000_000L) }.toList()
            return flowOf(points)
        }

        override fun observeApps(range: DateRange, filter: UsageFilter, sort: AppSort): Flow<List<AppUsage>> {
            val days = ((range.to.epochSecond - range.from.epochSecond) / 86_400L).coerceAtLeast(1L)
            return flowOf(
                listOf(
                    AppUsage("com.video", "Video", 1, 0L, days * 30_000_000L, null),
                    AppUsage("com.chat", "Chat", 2, 0L, days * 20_000_000L, null),
                ),
            )
        }
    }

    private fun viewModel() = HistoryViewModel(
        usage = Dated(FakeUsageRepository(clock, kotlinx.coroutines.flow.MutableSharedFlow())),
        settings = FakeSettingsRepository(),
        permissions = FakePermissionRepository(PermissionState(usageAccess = true, notifications = true, phoneState = true)),
        clock = clock,
        locale = Locale.ENGLISH,
        dates = flowOf(today),
    )

    @Test fun rows_cover_91_days() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val rows = vm.uiState.value.rows
        assertEquals(91, rows.size)
        assertEquals(today, rows.last().date)
        assertEquals(today.minusDays(90), rows.first().date)
        job.cancel()
    }

    @Test fun default_selection_is_the_latest_bucket() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(null, state.selectedIndex)
        assertEquals(HistorySelection.Default, state.selection)
        // The card lists the top apps of the latest day.
        assertEquals(listOf("Video", "Chat"), state.selectedApps.map { it.label })
        job.cancel()
    }

    @Test fun selecting_a_bar_selects_its_bucket_and_clears_the_day() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(HistoryEvent.SelectDay(today.minusDays(3)))
        advanceUntilIdle()
        assertEquals(HistorySelection.Day(today.minusDays(3)), vm.uiState.value.selection)
        vm.onEvent(HistoryEvent.SelectBar(5))
        advanceUntilIdle()
        assertEquals(HistorySelection.Bucket(5), vm.uiState.value.selection)
        assertEquals(5, vm.uiState.value.selectedIndex)
        job.cancel()
    }

    @Test fun selecting_a_day_overrides_the_bucket_and_tapping_again_clears() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(HistoryEvent.SelectBar(5))
        vm.onEvent(HistoryEvent.SelectDay(today.minusDays(2)))
        advanceUntilIdle()
        assertEquals(HistorySelection.Day(today.minusDays(2)), vm.uiState.value.selection)
        vm.onEvent(HistoryEvent.SelectDay(today.minusDays(2)))
        advanceUntilIdle()
        assertEquals(HistorySelection.Default, vm.uiState.value.selection)
        job.cancel()
    }

    @Test fun switching_granularity_clears_the_selection() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(HistoryEvent.SelectDay(today.minusDays(2)))
        vm.onEvent(HistoryEvent.SetGranularity(Granularity.WEEK))
        advanceUntilIdle()
        assertEquals(HistorySelection.Default, vm.uiState.value.selection)
        job.cancel()
    }

    @Test fun top_apps_come_with_change_and_hours_have_24_entries() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(24, state.hourTotals.size)
        assertEquals("Video", state.topApps.first().label)
        assertTrue(state.topApps.first().percentOfAll in 1..100)
        assertEquals(0f, state.topApps.first().change!!, 1e-4f)
        job.cancel()
    }

    @Test fun loading_state_until_first_emission() {
        assertEquals(false, viewModel().uiState.value.loaded)
    }
}
