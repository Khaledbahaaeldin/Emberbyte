package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanSectionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun name_field_and_error_render_and_sim_group_shows_for_multiple_sims() {
        var lastName = ""
        var selectedSim: Int? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanSection(
                    name = "Home SIM",
                    nameError = "Name is required",
                    subscriptionId = null,
                    sims = listOf(
                        SimOptionUi(null, "Not tied to a SIM"),
                        SimOptionUi(1, "SIM 1 · Vodafone"),
                        SimOptionUi(2, "SIM 2 · Orange"),
                    ),
                    needsPhonePermission = false,
                    onSetName = { lastName = it },
                    onSetSim = { selectedSim = it },
                    onRequestPhonePermission = {},
                )
            }
        }
        rule.onAllNodesWithText("Plan name").onFirst().assertIsDisplayed()
        rule.onNodeWithText("Name is required").assertIsDisplayed()
        rule.onNodeWithText("SIM 1 · Vodafone").performClick()
        assertEquals(1, selectedSim)

        rule.onNodeWithText("Home SIM").performTextInput("2")
        assertEquals(true, lastName.contains("2"))
    }

    @Test
    fun missing_phone_permission_shows_permission_chip() {
        var requested = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanSection(
                    name = "Work",
                    nameError = null,
                    subscriptionId = null,
                    sims = listOf(SimOptionUi(null, "Not tied to a SIM")),
                    needsPhonePermission = true,
                    onSetName = {},
                    onSetSim = {},
                    onRequestPhonePermission = { requested = true },
                )
            }
        }
        rule.onNodeWithText("Allow phone access to tell your SIMs apart").performClick()
        assertEquals(true, requested)
    }
}
