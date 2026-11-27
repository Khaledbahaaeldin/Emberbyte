package io.github.khaledbahaaeldin.emberbyte.apps

import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsageFilter
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
class AppsViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()

    private class SortingUsageRepository(
        private val delegate: FakeUsageRepository,
    ) : UsageRepository by delegate {
        override fun observeApps(range: DateRange, filter: UsageFilter, sort: AppSort): Flow<List<AppUsage>> =
            delegate.observeApps(range, filter, sort).map { list ->
                when (sort) {
                    AppSort.NAME -> list.sortedBy { it.label.lowercase() }
                    AppSort.BYTES_ASC -> list.sortedBy { it.totalBytes }
                    AppSort.BYTES_DESC -> list.sortedByDescending { it.totalBytes }
                    else -> list
                }
            }
    }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(permissions: FakePermissionRepository = FakePermissionRepository()) = AppsViewModel(
        usage = SortingUsageRepository(FakeUsageRepository(clock, MutableSharedFlow())),
        settings = FakeSettingsRepository(),
        permissions = permissions,
        clock = clock,
        dates = flowOf(LocalDate.of(2026, 10, 5)),
    )

    @Test fun lists_the_apps_with_their_total() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals("YouTube", state.featured?.label)
        assertEquals(4, state.apps.size)
        assertEquals("Chrome", state.apps.first().label)
        assertEquals(1_022_000_000L, state.totalBytes)
        job.cancel()
    }

    @Test fun events_change_the_selection_and_the_search() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(AppsEvent.SetRange(AppsRange.LAST_7_DAYS))
        vm.onEvent(AppsEvent.SetNetwork(AppsNetwork.WIFI))
        vm.onEvent(AppsEvent.SetSort(AppSort.NAME))
        vm.onEvent(AppsEvent.SetQuery("chr"))
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(AppsRange.LAST_7_DAYS, state.range)
        assertEquals(AppsNetwork.WIFI, state.network)
        assertEquals(AppSort.NAME, state.sort)
        assertEquals("chr", state.query)
        assertEquals(listOf("Chrome"), state.apps.map { it.label })
        job.cancel()
    }

    @Test fun missing_usage_access_is_reported() = runTest(dispatcher) {
        val permissions = FakePermissionRepository(PermissionState(false, true, true))
        val vm = viewModel(permissions)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(vm.uiState.value.needsUsageAccess)
        permissions.set(PermissionState(true, true, true))
        advanceUntilIdle()
        assertEquals(false, vm.uiState.value.needsUsageAccess)
        job.cancel()
    }

    @Test fun featured_is_the_top_app_and_absent_during_search() = runTest(dispatcher) {
        val vm = viewModel()
        val jobUi = launch { vm.uiState.collect {} }
        val jobFeat = launch { vm.featured.collect {} }
        advanceUntilIdle()
        assertEquals("YouTube", vm.featured.value?.label)
        assertEquals("YouTube", vm.uiState.value.featured?.label)

        vm.onEvent(AppsEvent.SetQuery("you"))
        advanceUntilIdle()
        assertEquals(null, vm.featured.value)
        assertEquals(null, vm.uiState.value.featured)
        jobUi.cancel()
        jobFeat.cancel()
    }

    @Test fun featured_app_is_the_largest_even_when_sorted_by_name() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(AppsEvent.SetSort(AppSort.NAME))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("YouTube", state.featured?.label)
        // When sorted by name, the list below contains all apps alphabetically
        assertEquals(listOf("Chrome", "Maps", "Spotify", "Telegram", "YouTube"), state.apps.map { it.label })
        job.cancel()
    }

    @Test fun featured_app_is_not_repeated_when_sorted_by_usage() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // Default sort is BYTES_DESC
        assertEquals(AppSort.BYTES_DESC, vm.uiState.value.sort)
        assertEquals("YouTube", vm.uiState.value.featured?.label)
        // Featured app (YouTube) must NOT be repeated in the list below
        assertEquals(listOf("Chrome", "Maps", "Spotify", "Telegram"), vm.uiState.value.apps.map { it.label })
        job.cancel()
    }

    @Test fun removing_a_chip_resets_only_that_filter() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(AppsEvent.SetRange(AppsRange.LAST_7_DAYS))
        vm.onEvent(AppsEvent.SetNetwork(AppsNetwork.WIFI))
        advanceUntilIdle()
        assertEquals(AppsRange.LAST_7_DAYS, vm.uiState.value.range)
        assertEquals(AppsNetwork.WIFI, vm.uiState.value.network)

        vm.onEvent(AppsEvent.RemoveFilter(AppFilterKind.NETWORK))
        advanceUntilIdle()
        // The period is not a filter any more: removing a chip never touches it.
        assertEquals(AppsRange.LAST_7_DAYS, vm.uiState.value.range)
        assertEquals(AppsNetwork.ALL, vm.uiState.value.network)
        job.cancel()
    }

    @Test fun reset_returns_all_defaults() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(AppsEvent.SetRange(AppsRange.LAST_7_DAYS))
        vm.onEvent(AppsEvent.SetNetwork(AppsNetwork.WIFI))
        vm.onEvent(AppsEvent.SetSort(AppSort.NAME))
        vm.onEvent(AppsEvent.SetShowSystemApps(true))
        advanceUntilIdle()

        vm.onEvent(AppsEvent.ResetFilters)
        advanceUntilIdle()
        // Reset clears the filters but keeps the period the user picked on the page.
        assertEquals(AppsRange.LAST_7_DAYS, vm.uiState.value.range)
        assertEquals(AppsNetwork.ALL, vm.uiState.value.network)
        assertEquals(AppSort.BYTES_DESC, vm.uiState.value.sort)
        assertEquals(false, vm.uiState.value.showSystemApps)
        job.cancel()
    }

    @Test fun set_range_changes_the_rows_and_totals_immediately() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(AppsEvent.SetRange(AppsRange.LAST_30_DAYS))
        advanceUntilIdle()
        assertEquals(AppsRange.LAST_30_DAYS, vm.uiState.value.range)
        assertTrue(vm.uiState.value.loaded)
        job.cancel()
    }

    @Test fun apply_filters_does_not_change_the_range() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(AppsEvent.SetRange(AppsRange.LAST_7_DAYS))
        vm.onEvent(AppsEvent.ApplyFilters(AppsNetwork.MOBILE, AppSort.NAME, true))
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(AppsRange.LAST_7_DAYS, state.range)
        assertEquals(AppsNetwork.MOBILE, state.network)
        assertEquals(AppSort.NAME, state.sort)
        assertEquals(true, state.showSystemApps)
        assertEquals(3, state.activeFilterCount)
        job.cancel()
    }

    @Test fun totals_split_into_wifi_and_mobile() = runTest(dispatcher) {
        val vm = viewModel()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(state.totalBytes, state.wifiBytes + state.mobileBytes)
        assertTrue(state.mobileBytes > 0L)
        assertEquals(state.apps.size + (if (state.featured != null) 1 else 0), state.appCount)
        job.cancel()
    }

    @Test fun filters_are_independent_of_homes() = runTest(dispatcher) {
        // Each screen owns its own selection: a view model starts with defaults and shares nothing.
        val first = viewModel()
        val second = viewModel()
        val jobs = listOf(launch { first.uiState.collect {} }, launch { second.uiState.collect {} })
        advanceUntilIdle()
        first.onEvent(AppsEvent.ApplyFilters(AppsNetwork.WIFI, AppSort.NAME, true))
        advanceUntilIdle()
        assertEquals(0, second.uiState.value.activeFilterCount)
        assertEquals(3, first.uiState.value.activeFilterCount)
        jobs.forEach { it.cancel() }
    }
}
