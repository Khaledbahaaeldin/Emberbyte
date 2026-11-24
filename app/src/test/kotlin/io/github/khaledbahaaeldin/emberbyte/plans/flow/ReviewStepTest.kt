package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
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
class ReviewStepTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun review_step_shows_alert_chips_and_summary_card() {
        var toggled = -1
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ReviewStep(
                    state = PlanFlowState(
                        name = "Home SIM",
                        capText = "20",
                        capUnit = CapUnit.GB,
                        alertThresholds = setOf(80, 100),
                    ),
                    reveal = { 1f },
                    onToggleAlert = { toggled = it },
                )
            }
        }
        rule.onNodeWithText("Warn me before it runs out").assertIsDisplayed()
        rule.onNodeWithText("Looks right?").assertIsDisplayed()
        rule.onNodeWithTag("review_summary_ring").assertIsDisplayed()
        rule.onNodeWithText("Home SIM").assertIsDisplayed()
        rule.onNodeWithTag("alert_chip_50").performClick()
        assertEquals(50, toggled)
    }
}
