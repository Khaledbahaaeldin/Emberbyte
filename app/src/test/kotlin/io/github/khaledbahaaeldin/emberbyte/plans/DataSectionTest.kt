package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
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
@Config(sdk = [35])
class DataSectionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun cap_field_steppers_unit_group_and_error_render() {
        var cap = "10"
        var unit = CapUnit.GB
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                DataSection(
                    capText = cap,
                    capUnit = unit,
                    unitSystem = UnitSystem.DECIMAL,
                    capError = "Enter a number greater than zero",
                    onSetCap = { cap = it },
                    onSetCapUnit = { unit = it },
                )
            }
        }
        rule.onAllNodesWithText("Cap").onFirst().assertIsDisplayed()
        rule.onNodeWithText("Enter a number greater than zero").assertIsDisplayed()
        rule.onNodeWithText("+1").performClick()
        assertEquals("11", cap)
        rule.onNodeWithText("-1").performClick()
        assertEquals("9", cap)
        rule.onNodeWithText("TB").performClick()
        assertEquals(CapUnit.TB, unit)
    }
}
