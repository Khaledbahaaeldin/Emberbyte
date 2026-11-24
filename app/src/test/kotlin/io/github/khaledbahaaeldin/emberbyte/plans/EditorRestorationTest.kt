package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import io.github.khaledbahaaeldin.emberbyte.data.alert.RoomAlertRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h900dp")
class EditorRestorationTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun unsaved_form_survives_process_death_and_state_restoration() {
        val now = Instant.parse("2026-11-15T12:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val store = InMemoryPlanStore()
        val repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
        val alerts = RoomAlertRepository(store)
        val handle = SavedStateHandle()

        val vm1 = PlanEditorViewModel(
            planId = 0L,
            plans = repo,
            alerts = alerts,
            settings = FakeSettingsRepository(),
            permissions = FakePermissionRepository(),
            sims = FakeSimInfoSource(),
            clock = clock,
            locale = Locale.ENGLISH,
            previewDebounceMillis = 0L,
            savedStateHandle = handle,
        )
        vm1.onEvent(PlanEditorEvent.SetName("Restored Plan"))
        vm1.onEvent(PlanEditorEvent.SetCap("25"))

        val vm2 = PlanEditorViewModel(
            planId = 0L,
            plans = repo,
            alerts = alerts,
            settings = FakeSettingsRepository(),
            permissions = FakePermissionRepository(),
            sims = FakeSimInfoSource(),
            clock = clock,
            locale = Locale.ENGLISH,
            previewDebounceMillis = 0L,
            savedStateHandle = handle,
        )
        assertEquals("Restored Plan", vm2.uiState.value.form.name)
        assertEquals("25", vm2.uiState.value.form.capText)
        assertTrue(vm2.uiState.value.isDirty)

        val restoredState = vm2.uiState.value
        val restorationTester = StateRestorationTester(rule)
        restorationTester.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = restoredState,
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        restorationTester.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Restored Plan").assertIsDisplayed()
        rule.onNodeWithText("25").assertIsDisplayed()
    }
}
