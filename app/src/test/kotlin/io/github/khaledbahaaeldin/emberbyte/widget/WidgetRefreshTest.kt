package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.data.alert.RoomAlertRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.plans.PlanEditorEvent
import io.github.khaledbahaaeldin.emberbyte.plans.PlanEditorViewModel
import io.github.khaledbahaaeldin.emberbyte.settings.AppearanceViewModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetRefreshTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theme_change_triggers_a_refresh_of_all_widget_types() = runTest(dispatcher) {
        var refreshCount = 0
        val settings = FakeSettingsRepository()
        val viewModel = AppearanceViewModel(
            settings = settings,
            widgetRefresh = { refreshCount++ }
        )

        viewModel.setTheme("ember")
        advanceUntilIdle()

        assertTrue("Changing theme must trigger widget refresh", refreshCount > 0)
    }

    @Test
    fun plan_change_triggers_a_refresh() = runTest(dispatcher) {
        var refreshCount = 0
        val now = Instant.parse("2026-10-15T12:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val planStore = InMemoryPlanStore()
        val usage = InMemoryUsageStore()
        val plans = RoomPlanRepository(planStore, usage, clock, ticks = flowOf(now))
        val alerts = RoomAlertRepository(planStore)
        val settings = FakeSettingsRepository()
        val permissions = FakePermissionRepository()
        val sims = FakeSimInfoSource(emptyList())

        val vm = PlanEditorViewModel(
            planId = 0L,
            plans = plans,
            alerts = alerts,
            settings = settings,
            permissions = permissions,
            sims = sims,
            clock = clock,
            locale = Locale.ENGLISH,
            previewDebounceMillis = 0L,
            savedStateHandle = SavedStateHandle(),
            widgetRefresh = { refreshCount++ }
        )
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(PlanEditorEvent.SetName("Primary"))
        vm.onEvent(PlanEditorEvent.SetCap("10"))
        advanceUntilIdle()

        vm.onEvent(PlanEditorEvent.Save)
        advanceUntilIdle()

        job.cancel()
        assertTrue("Saving plan must trigger widget refresh", refreshCount > 0)
    }

    @Test
    fun refresh_is_rate_limited_to_the_existing_minimum_interval() = runTest(dispatcher) {
        WidgetUpdater.lastUpdateMs = 0L
        WidgetUpdater.minIntervalMs = 5_000L

        var fakeTime = 10_000L
        val firstCall = WidgetUpdater.request(context, clock = { fakeTime })
        assertTrue("First update should succeed", firstCall)

        // Rapid second call within 5,000ms
        fakeTime += 1_000L
        val secondCall = WidgetUpdater.request(context, clock = { fakeTime })
        assertFalse("Second rapid call within minimum interval must be rate-limited", secondCall)

        // Third call after 5,000ms has elapsed
        fakeTime += 5_000L
        val thirdCall = WidgetUpdater.request(context, clock = { fakeTime })
        assertTrue("Call after minimum interval elapsed should succeed", thirdCall)
    }

    @Test
    fun every_provider_info_has_a_description_and_a_preview() {
        val xmlIds = listOf(
            R.xml.today_widget_info,
            R.xml.hero_widget_info,
            R.xml.plan_widget_info,
            R.xml.bento_widget_info,
            R.xml.dashboard_widget_info,
        )

        for (resId in xmlIds) {
            val parser = context.resources.getXml(resId)
            var eventType = parser.eventType
            var foundProvider = false
            var descriptionAttr: String? = null
            var previewLayoutAttr: Int = 0
            var previewImageAttr: Int = 0

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG && parser.name == "appwidget-provider") {
                    foundProvider = true
                    descriptionAttr = parser.getAttributeValue("http://schemas.android.com/apk/res/android", "description")
                    previewLayoutAttr = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "previewLayout", 0)
                    previewImageAttr = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "previewImage", 0)
                    break
                }
                eventType = parser.next()
            }

            assertTrue("Resource $resId must have appwidget-provider root", foundProvider)
            assertNotNull("Resource $resId must define android:description", descriptionAttr)
            assertTrue("Resource $resId must have previewLayout or previewImage", previewLayoutAttr != 0 || previewImageAttr != 0)
        }
    }
}
