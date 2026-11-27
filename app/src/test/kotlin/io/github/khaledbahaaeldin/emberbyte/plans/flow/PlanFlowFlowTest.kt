package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class PlanFlowFlowTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun full_five_step_pass_creates_plan_once_and_step_count_is_fixed_while_typing() = runBlocking {
        val midMonthClock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC)
        val repo = FakePlanRepository()
        val initialCount = repo.observePlans().first().size
        val vm = PlanFlowViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), midMonthClock)
        var createdId: Long? = null

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScreen(
                    viewModel = vm,
                    onPlanCreated = { createdId = it },
                    onCancel = {},
                    clock = midMonthClock,
                )
            }
        }

        rule.onNodeWithTag("plan_flow_name_field").performTextInput("Home SIM")
        rule.onNodeWithContentDescription("Step 1 of 5").assertIsDisplayed()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()

        rule.onNodeWithText("+5").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()

        // Day 15 matches today -> 4 steps when passing step 3 if today == dayOfMonth, or 5 if day 1
        rule.onNodeWithTag("day_grid_1").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithContentDescription("Step 4 of 5").assertIsDisplayed()

        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithContentDescription("Step 5 of 5").assertIsDisplayed()

        // Double tap Create saves once
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.waitForIdle()

        assertEquals(initialCount + 1, repo.observePlans().first().size)
        assertEquals(repo.observePlans().first().last().id, createdId)
    }

    @Test
    fun full_four_step_pass_when_today_is_renewal_day() = runBlocking {
        val firstDayClock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)
        val repo = FakePlanRepository()
        val initialCount = repo.observePlans().first().size
        val vm = PlanFlowViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), firstDayClock)
        var createdId: Long? = null

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScreen(
                    viewModel = vm,
                    onPlanCreated = { createdId = it },
                    onCancel = {},
                    clock = firstDayClock,
                )
            }
        }

        rule.onNodeWithText("Work").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()

        rule.onNodeWithText("+10").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()

        rule.onNodeWithTag("day_grid_1").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithContentDescription("Step 4 of 4").assertIsDisplayed()

        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.waitForIdle()

        assertEquals(initialCount + 1, repo.observePlans().first().size)
        assertEquals("Work", repo.observePlans().first().last().name)
        assertEquals(repo.observePlans().first().last().id, createdId)
    }

    @Test
    fun failure_keeps_the_user_on_the_last_step_with_error_and_retry() {
        val firstDayClock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)
        val failingRepo = object : PlanRepository by FakePlanRepository() {
            override suspend fun savePlan(
                plan: Plan,
                addOns: List<io.github.khaledbahaaeldin.emberbyte.engine.model.AddOn>,
                freeRules: List<io.github.khaledbahaaeldin.emberbyte.engine.model.FreeRule>,
                alertThresholds: Set<Int>,
            ): Outcome<Long> = Outcome.Failure(EmberbyteError.Storage("disk full"))
        }
        val vm = PlanFlowViewModel(failingRepo, FakeSettingsRepository(), FakeSimInfoSource(), firstDayClock)
        var created = 0

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScreen(
                    viewModel = vm,
                    onPlanCreated = { created++ },
                    onCancel = {},
                    clock = firstDayClock,
                )
            }
        }

        rule.onNodeWithText("Work").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithText("+10").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.onNodeWithTag("plan_flow_primary_button").performClick()
        rule.waitForIdle()

        assertEquals(0, created)
        rule.onNodeWithContentDescription("Step 4 of 4").assertIsDisplayed()
        rule.onNodeWithText("Could not save plan. Please try again.").assertIsDisplayed()
    }
}
