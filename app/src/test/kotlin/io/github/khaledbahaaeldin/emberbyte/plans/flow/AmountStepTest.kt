package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class AmountStepTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun amount_step_shows_ring_units_steppers_and_rollover() {
        var cap = "10"
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AmountStep(
                    state = PlanFlowState(capText = cap, unitSystem = UnitSystem.DECIMAL),
                    reveal = { 1f },
                    onSetCap = { cap = it },
                    onSetCapUnit = {},
                    onSetRollover = {},
                )
            }
        }
        rule.onNodeWithText("How much data do you get?").assertIsDisplayed()
        rule.onNodeWithTag("amount_wavy_ring").assertIsDisplayed()
        rule.onNodeWithText("Counted in decimal units (1 GB = 1,000 MB), as set in Settings").assertIsDisplayed()
        rule.onNodeWithText("Unused data: Lost at renewal").assertIsDisplayed()

        rule.onNodeWithText("+5").performClick()
        assertEquals("15", cap)

        rule.onNodeWithText("Unused data: Lost at renewal").performClick()
        rule.onNodeWithText("Carried forward").assertIsDisplayed()
    }

    @Test
    fun binary_caption_and_error_text_render() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AmountStep(
                    state = PlanFlowState(
                        capText = "0",
                        unitSystem = UnitSystem.BINARY,
                        fieldError = PlanFlowError.CapPositive,
                    ),
                    reveal = { 1f },
                    onSetCap = {},
                    onSetCapUnit = {},
                    onSetRollover = {},
                )
            }
        }
        rule.onNodeWithText("Counted in binary units (1 GiB = 1,024 MiB), as set in Settings").assertIsDisplayed()
        rule.onNodeWithText("Enter a number greater than zero").assertIsDisplayed()
    }
}
