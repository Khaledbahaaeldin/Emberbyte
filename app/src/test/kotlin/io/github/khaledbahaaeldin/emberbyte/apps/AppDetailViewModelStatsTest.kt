package io.github.khaledbahaaeldin.emberbyte.apps

import io.github.khaledbahaaeldin.emberbyte.apps.detail.DetailRange
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemorySpikeStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetRepository
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.spike.RoomSpikeRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailViewModelStatsTest {
    private val now = Instant.parse("2026-10-05T12:00:00Z")
    private val date = LocalDate.of(2026, 10, 5)
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()
    private val requested = mutableListOf<Pair<Granularity, DateRange>>()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    /** Daily points carry as many MB of Wi-Fi as the day of the month; hourly points put 1 MB in hour 19 of each day. */
    private inner class DatedUsage(base: UsageRepository) : UsageRepository by base {
        override fun observeApps(range: DateRange, filter: io.github.khaledbahaaeldin.emberbyte.engine.model.UsageFilter, sort: io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort): Flow<List<AppUsage>> =
            flowOf(
                listOf(
                    AppUsage("com.video", "Video", 1, 0L, 600_000_000L, null),
                    AppUsage("com.chat", "Chat", 2, 0L, 400_000_000L, null),
                ),
            )

        override fun observeAppSeries(packageName: String, range: DateRange, granularity: Granularity): Flow<List<UsagePoint>> {
            requested += granularity to range
            val zone = ZoneOffset.UTC
            val points = when (granularity) {
                Granularity.HOUR -> generateSequence(range.from) { it.plusSeconds(3600) }.takeWhile { it < range.to }
                    .map { UsagePoint(it, 0L, if (it.atZone(zone).hour == 19) 1_000_000L else 0L) }.toList()
                else -> generateSequence(range.from) { it.plusSeconds(86_400) }.takeWhile { it < range.to }
                    .map { UsagePoint(it, 0L, it.atZone(zone).dayOfMonth * 1_000_000L) }.toList()
            }
            return flowOf(points)
        }
    }

    private fun vm(zone: ZoneId = ZoneOffset.UTC): AppDetailViewModel {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val cl = Clock.fixed(now, zone)
        return AppDetailViewModel(
            packageName = "com.video",
            usage = DatedUsage(FakeUsageRepository(cl, MutableSharedFlow())),
            settings = FakeSettingsRepository(),
            budgets = RoomBudgetRepository(InMemoryBudgetStore(), planStore, usageStore, cl, ticks = flowOf(now)),
            spikes = RoomSpikeRepository(InMemorySpikeStore()),
            plans = RoomPlanRepository(planStore, usageStore, cl, ticks = flowOf(now)),
            clock = cl,
            locale = Locale.ENGLISH,
            dates = flowOf(date),
        )
    }

    @Test fun loading_state_until_first_emission() {
        val vm = vm()
        assertTrue(vm.uiState.value.isLoading)
        assertEquals("", vm.uiState.value.label)
    }

    @Test fun range_defaults_to_7_days() = runTest(dispatcher) {
        val vm = vm()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(DetailRange.Week, vm.uiState.value.range)
        assertEquals(7, vm.uiState.value.dailySeries.size)
        job.cancel()
    }

    @Test fun set_range_requeries_series_and_previous_period() = runTest(dispatcher) {
        val vm = vm()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onEvent(AppDetailEvent.SetRange(DetailRange.Month))
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(DetailRange.Month, state.range)
        assertEquals(30, state.dailySeries.size)
        // The previous period is the equal-length range right before: days 1..30 back, summed in MB of day-of-month.
        assertTrue(state.previousTotalBytes > 0L)
        val dayOfMonth = { back: Int -> date.minusDays(back.toLong()).dayOfMonth.toLong() * 1_000_000L }
        assertEquals((30..59).sumOf { dayOfMonth(it) }, state.previousTotalBytes)
        // The series was requested for twice the range in one query.
        assertTrue(requested.any { it.first == Granularity.DAY && (it.second.to.epochSecond - it.second.from.epochSecond) == 60L * 86_400L })
        job.cancel()
    }

    @Test fun rank_and_share_come_from_the_apps_list() = runTest(dispatcher) {
        val vm = vm()
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(1, state.rank)
        assertEquals(2, state.appCount)
        assertEquals(0.6f, state.shareOfAll, 1e-4f)
        job.cancel()
    }

    @Test fun hour_totals_use_the_users_zone() = runTest(dispatcher) {
        // A UTC+2 user: the UTC 19:00 spike lands at 21:00 local time.
        val vm = vm(zone = ZoneOffset.ofHours(2))
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val hours = vm.uiState.value.hourTotals
        assertEquals(24, hours.size)
        assertTrue(hours[21] > 0L)
        assertEquals(0L, hours[19])
        job.cancel()
    }

    @Test fun foreground_and_background_are_gone() {
        val names = AppDetailUiState::class.java.declaredFields.map { it.name.lowercase() }
        assertTrue(names.none { it.contains("foreground") || it.contains("background") })
    }
}
