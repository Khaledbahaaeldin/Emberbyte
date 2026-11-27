package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
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
class ThisCycleSectionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun visible_only_for_first_cycle_and_enter_an_amount_selects_entered() {
        var choice = FirstCycleChoice.PhoneRecord
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ThisCycleSection(
                    countedSoFarText = "12.4 GB",
                    firstCycleChoice = choice,
                    enteredAmount = "",
                    enteredUnit = CapUnit.GB,
                    unitSystem = UnitSystem.DECIMAL,
                    enteredError = null,
                    onSelectChoice = { choice = it },
                    onSetEnteredAmount = {},
                    onSetEnteredUnit = {},
                )
            }
        }
        rule.onNodeWithText("Counted so far 12.4 GB").assertIsDisplayed()
        rule.onNodeWithText("Estimated").assertIsDisplayed()
        rule.onNodeWithText("Enter an amount").performClick()
        assertEquals(FirstCycleChoice.Entered, choice)
    }
}
