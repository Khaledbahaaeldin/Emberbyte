package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class PlanFlowMotionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun reduced_motion_snaps_step_and_is_static() {
        var state by mutableStateOf(PlanFlowState(step = 1, totalSteps = 5))
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    PlanFlowContent(state = state, onEvent = {}, onBack = {})
                }
            }
        }
        rule.onNodeWithText("What should we call it?").assertIsDisplayed()

        state = PlanFlowState(step = 2, totalSteps = 5)
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Step 2 of 5").assertIsDisplayed()
        rule.onNodeWithText("How much data do you get?").assertIsDisplayed()
    }

    @Test
    fun pure_motion_functions_compute_parallax_and_reveal() {
        assertEquals(-30f, flowParallaxOffset(-0.1f, 1000f, isOutgoing = true), 0.01f)
        assertEquals(100f, flowParallaxOffset(0.1f, 1000f, isOutgoing = false), 0.01f)
        assertEquals(1f, flowRevealFraction(1f, 0), 0.001f)
        assertEquals(0f, flowPageAlpha(1f), 0.001f)
    }
}
