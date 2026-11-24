package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class ThisCycleStepTest {
    @get:Rule val rule = createComposeRule()
    private val clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun this_cycle_step_shows_cards_estimated_tag_and_entered_field() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ThisCycleStep(
                    state = PlanFlowState(
                        firstCycleChoice = FirstCycleChoice.Entered,
                        nextCycleStartText = "1 December",
                    ),
                    clock = clock,
                    reveal = { 1f },
                    onSelectChoice = {},
                    onSetEnteredAmount = {},
                    onSetEnteredUnit = {},
                )
            }
        }
        rule.onNodeWithText("Today is the 15th. How do we count this cycle?").assertIsDisplayed()
        rule.onNodeWithText("Use what this phone recorded").assertIsDisplayed()
        rule.onNodeWithText("Estimated").assertIsDisplayed()
        rule.onNodeWithText("I will enter what I have used").assertIsDisplayed()
        rule.onNodeWithTag("this_cycle_entered_field").assertIsDisplayed()
        rule.onNodeWithText("Start with the next cycle").assertIsDisplayed()
        rule.onNodeWithText("Nothing is counted and no warnings are sent until 1 December").assertIsDisplayed()
        rule.onNodeWithText("You can change this later in the plan, for the first cycle only.").assertIsDisplayed()
    }
}
