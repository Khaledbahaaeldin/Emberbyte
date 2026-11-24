package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.SavedStateHandle
import io.github.khaledbahaaeldin.emberbyte.data.alert.RoomAlertRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h900dp")
class EditorFlowTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val now = Instant.parse("2026-11-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private fun createVm(planId: Long, repo: RoomPlanRepository, store: InMemoryPlanStore): PlanEditorViewModel =
        PlanEditorViewModel(
            planId = planId,
            plans = repo,
            alerts = RoomAlertRepository(store),
            settings = FakeSettingsRepository(),
            permissions = FakePermissionRepository(),
            sims = FakeSimInfoSource(),
            clock = clock,
            locale = Locale.ENGLISH,
            previewDebounceMillis = 0L,
            savedStateHandle = SavedStateHandle(),
        )

    @Test
    fun edit_updates_save_enabled_only_when_valid_and_changed_and_save_returns() {
        val store = InMemoryPlanStore()
        val repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
        val id = runBlocking {
            (repo.savePlan(
                Plan(0L, "Home SIM", null, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, false),
                emptyList(),
                emptyList(),
                setOf(80, 100),
            ) as Outcome.Success).value
        }
        val vm = createVm(id, repo, store)
        var backed = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorScreen(viewModel = vm, onBack = { backed = true }, onPrompt = {})
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("editor_save_pill").assertIsNotEnabled()
        rule.onNodeWithTag("editor_name_field").performTextReplacement("Home 5G")
        rule.waitForIdle()
        rule.onNodeWithTag("editor_save_pill").assertIsEnabled()

        rule.onNodeWithTag("editor_save_pill").performClick()
        rule.waitForIdle()

        assertTrue(backed)
        val savedName = runBlocking { repo.observePlans().first().first { it.id == id }.name }
        assertEquals("Home 5G", savedName)
    }

    @Test
    fun back_with_unsaved_changes_asks_discard() {
        val store = InMemoryPlanStore()
        val repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
        val id = runBlocking {
            (repo.savePlan(
                Plan(0L, "Home SIM", null, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, false),
                emptyList(),
                emptyList(),
                setOf(80, 100),
            ) as Outcome.Success).value
        }
        val vm = createVm(id, repo, store)
        var backed = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorScreen(viewModel = vm, onBack = { backed = true }, onPrompt = {})
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("editor_name_field").performTextReplacement("Changed")
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Discard changes?").assertIsDisplayed()
        rule.onNodeWithText("Discard").performClick()
        rule.waitForIdle()
        assertTrue(backed)
    }

    @Test
    fun delete_opens_dialog_and_deletes_plan() {
        val store = InMemoryPlanStore()
        val repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
        val id = runBlocking {
            (repo.savePlan(
                Plan(0L, "Home SIM", null, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, false),
                emptyList(),
                emptyList(),
                setOf(80, 100),
            ) as Outcome.Success).value
        }
        val vm = createVm(id, repo, store)
        var backed = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorScreen(viewModel = vm, onBack = { backed = true }, onPrompt = {})
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("editor_lazy_list").performScrollToIndex(9)
        rule.waitForIdle()
        rule.onNodeWithText("Delete plan").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Delete Home SIM?").assertIsDisplayed()
        rule.onAllNodesWithText("Delete").onLast().performClick()
        rule.waitForIdle()

        assertTrue(backed)
        assertTrue(runBlocking { repo.observePlans().first().isEmpty() })
    }
}
