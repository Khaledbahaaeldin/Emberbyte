package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.plans.SimOptionUi
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
class NameStepTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun name_step_shows_title_suggestions_and_live_preview() {
        var name = ""
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                NameStep(
                    state = PlanFlowState(name = name),
                    hasPhonePermission = true,
                    reveal = { 1f },
                    onSetName = { name = it },
                    onSetSim = {},
                    onRequestPhonePermission = {},
                )
            }
        }
        rule.onNodeWithText("What should we call it?").assertIsDisplayed()
        rule.onNodeWithText("Amount comes next").assertIsDisplayed()
        rule.onNodeWithText("Home SIM").performClick()
        assertEquals("Home SIM", name)
    }

    @Test
    fun sim_group_appears_only_with_two_or_more_sims_and_permission_chip_when_missing() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                NameStep(
                    state = PlanFlowState(sims = listOf(SimOptionUi(1, "SIM 1"))),
                    hasPhonePermission = false,
                    reveal = { 1f },
                    onSetName = {},
                    onSetSim = {},
                    onRequestPhonePermission = {},
                )
            }
        }
        rule.onNodeWithTag("name_sim_group").assertDoesNotExist()
        rule.onNodeWithText("Allow phone access to tell your SIMs apart").assertIsDisplayed()
    }

    @Test
    fun sim_group_appears_with_two_sims_and_shows_validation_error() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                NameStep(
                    state = PlanFlowState(
                        sims = listOf(SimOptionUi(1, "SIM 1"), SimOptionUi(2, "SIM 2")),
                        fieldError = PlanFlowError.NameRequired,
                    ),
                    hasPhonePermission = true,
                    reveal = { 1f },
                    onSetName = {},
                    onSetSim = {},
                    onRequestPhonePermission = {},
                )
            }
        }
        rule.onNodeWithTag("name_sim_group").assertIsDisplayed()
        rule.onNodeWithText("Enter a plan name").assertIsDisplayed()
    }
}
