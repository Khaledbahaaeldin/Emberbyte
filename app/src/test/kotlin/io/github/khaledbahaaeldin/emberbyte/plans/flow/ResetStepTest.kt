package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class ResetStepTest {
    @get:Rule val rule = createComposeRule()
    private val clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun reset_step_shows_day_grid_last_day_chip_and_summary() {
        var chosenDay = 1
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ResetStep(
                    state = PlanFlowState(cycleKind = CycleKind.Monthly, dayOfMonth = chosenDay),
                    clock = clock,
                    reveal = { 1f },
                    onSetCycleKind = {},
                    onSetDayOfMonth = { chosenDay = it },
                    onSetEveryDaysText = {},
                    onSetEveryDays = {},
                )
            }
        }
        rule.onNodeWithText("When does it reset?").assertIsDisplayed()
        rule.onNodeWithText("Monthly").assertIsDisplayed()
        rule.onNodeWithText("Weekly").assertIsDisplayed()
        rule.onNodeWithText("Custom").assertIsDisplayed()
        rule.onNodeWithText("Last day of the month").assertIsDisplayed()
        rule.onNodeWithText("Resets on the 1st of every month").assertIsDisplayed()
        rule.onNodeWithText("Next reset: Tue 1 December").assertIsDisplayed()

        rule.onNodeWithTag("day_grid_15").performClick()
        assertEquals(15, chosenDay)
    }
}
