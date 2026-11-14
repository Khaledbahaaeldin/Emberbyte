package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Going back between plan steps follows the finger and steps slide in their direction (task T5). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanFlowBackTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private var state by mutableStateOf(PlanFlowState(step = 2, totalSteps = 5, name = "My Plan", capText = "10"))
    private val events = mutableListOf<PlanFlowEvent>()

    private fun show(initial: PlanFlowState) {
        state = initial
        events.clear()
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = state,
                    onEvent = { event ->
                        events += event
                        if (event is PlanFlowEvent.PreviousStep) state = state.copy(step = (state.step - 1).coerceAtLeast(1))
                    },
                    onBack = {},
                )
            }
        }
        rule.waitForIdle()
    }

    private val start = BackEventCompat(0f, 0f, 0.0f, BackEventCompat.EDGE_LEFT)
    private val half = BackEventCompat(100f, 100f, 0.5f, BackEventCompat.EDGE_LEFT)

    @Test fun stepDirection_is_forward_for_increasing_steps_and_mirrors_in_rtl() {
        assertEquals(1, stepDirection(1, 2, rtl = false))
        assertEquals(-1, stepDirection(3, 2, rtl = false))
        assertEquals(0, stepDirection(2, 2, rtl = false))
        assertEquals(-1, stepDirection(1, 2, rtl = true))
        assertEquals(1, stepDirection(3, 2, rtl = true))
    }

    @Test fun a_step_above_one_listens_for_the_back_gesture_and_step_one_does_not() {
        show(PlanFlowState(step = 2, totalSteps = 5, name = "My Plan", capText = "10"))
        assertTrue(rule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
        state = PlanFlowState(step = 1, totalSteps = 5)
        rule.waitForIdle()
        assertFalse(rule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
    }

    @Test fun back_on_step_2_returns_to_step_1_and_keeps_the_entered_data() {
        show(PlanFlowState(step = 2, totalSteps = 5, name = "My Plan", capText = "10"))
        val dispatcher = rule.activity.onBackPressedDispatcher
        rule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(start)
            dispatcher.dispatchOnBackProgressed(half)
            dispatcher.onBackPressed()
        }
        rule.waitForIdle()
        assertEquals(1, events.count { it is PlanFlowEvent.PreviousStep })
        assertEquals(1, state.step)
        assertEquals("My Plan", state.name)
        assertEquals("10", state.capText)
    }

    @Test fun a_cancelled_back_gesture_leaves_the_step_unchanged() {
        show(PlanFlowState(step = 2, totalSteps = 5, name = "My Plan", capText = "10"))
        val dispatcher = rule.activity.onBackPressedDispatcher
        rule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(start)
            dispatcher.dispatchOnBackProgressed(half)
            dispatcher.dispatchOnBackCancelled()
        }
        rule.waitForIdle()
        assertTrue(events.none { it is PlanFlowEvent.PreviousStep })
        assertEquals(2, state.step)
        rule.onNodeWithText("Step 2 of 5").assertIsDisplayed()
    }
}
