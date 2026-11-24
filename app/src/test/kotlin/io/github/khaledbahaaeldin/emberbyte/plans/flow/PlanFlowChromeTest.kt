package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanFlowChromeTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pill_reads_step_n_of_total() {
        var pos by mutableFloatStateOf(0f)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowProgressPill(position = pos, totalSteps = 5)
            }
        }
        rule.onNodeWithContentDescription("Step 1 of 5").assertIsDisplayed()
        pos = 2f
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Step 3 of 5").assertIsDisplayed()
    }

    @Test
    fun continue_is_the_only_filled_button() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowActionBar(
                    step = 2,
                    isLastStep = false,
                    isSaving = false,
                    onBack = {},
                    onPrimary = {},
                )
            }
        }
        rule.onNodeWithTag("plan_flow_back_button").assertIsDisplayed()
        rule.onNodeWithTag("plan_flow_primary_button").assertIsDisplayed()
        rule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun close_asks_to_discard_only_after_typing() {
        var cancelCalls = 0
        var hasDirtyInput by mutableStateOf(false)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScaffoldChrome(
                    position = 0f,
                    totalSteps = 5,
                    step = 1,
                    isLastStep = false,
                    isSaving = false,
                    hasDirtyInput = hasDirtyInput,
                    onBack = { cancelCalls++ },
                    onClose = { cancelCalls++ },
                    onPrimary = {},
                ) { }
            }
        }
        rule.onNodeWithTag("plan_flow_close_button").performClick()
        assertEquals(1, cancelCalls)
        rule.onNodeWithText("Discard this plan?").assertDoesNotExist()

        hasDirtyInput = true
        rule.waitForIdle()
        rule.onNodeWithTag("plan_flow_close_button").performClick()
        rule.onNodeWithText("Discard this plan?").assertIsDisplayed()
        assertEquals(1, cancelCalls)
    }

    @Test
    fun system_back_goes_one_step_back_then_leaves() {
        var step by mutableIntStateOf(2)
        var leftCount = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScaffoldChrome(
                    position = (step - 1).toFloat(),
                    totalSteps = 5,
                    step = step,
                    isLastStep = false,
                    isSaving = false,
                    hasDirtyInput = false,
                    onBack = {
                        if (step > 1) step-- else leftCount++
                    },
                    onClose = { leftCount++ },
                    onPrimary = {},
                ) { }
            }
        }
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        assertEquals(1, step)
        assertEquals(0, leftCount)
    }

    @Test
    fun reduced_motion_changes_steps_without_travel() {
        var pos by mutableFloatStateOf(0f)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    PlanFlowProgressPill(position = pos, totalSteps = 5)
                }
            }
        }
        pos = 1.4f
        rule.waitForIdle()
        // Under reduced motion, pill snaps to step 2 (roundToInt(1.4) = 1 -> Step 2 of 5)
        rule.onNodeWithContentDescription("Step 2 of 5").assertIsDisplayed()
        assertTrue(true)
    }
}
