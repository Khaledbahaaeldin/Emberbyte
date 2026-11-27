package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1200dp")
class CycleSectionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun resets_on_change_opens_day_grid_sheet_and_rollover_updates() {
        var day = 1
        var rollover = RolloverKind.None
        var kind = CycleKind.Monthly
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CycleSection(
                    cycleKind = kind,
                    dayOfMonth = day,
                    everyDays = 30,
                    anchor = LocalDate.of(2026, 11, 1),
                    renewalTime = LocalTime.MIDNIGHT,
                    rolloverKind = rollover,
                    rolloverCapText = "",
                    rolloverCapUnit = CapUnit.GB,
                    unitSystem = UnitSystem.DECIMAL,
                    daysError = null,
                    rolloverError = null,
                    onSetCycleKind = { kind = it },
                    onSetDayOfMonth = { day = it },
                    onSetEveryDays = {},
                    onSetAnchor = {},
                    onSetRolloverKind = { rollover = it },
                    onSetRolloverCap = {},
                    onSetRolloverUnit = {},
                )
            }
        }
        rule.onNodeWithText("Monthly").assertIsDisplayed()
        rule.onNodeWithText("Weekly").assertIsDisplayed()
        rule.onNodeWithText("Custom").assertIsDisplayed()
        rule.onNodeWithText("Unused data").assertIsDisplayed()

        rule.onNodeWithText("Change").performClick()
        rule.onNodeWithTag("day_grid_15").performClick()
        assertEquals(15, day)

        rule.onNodeWithText("Carried forward").performClick()
        assertEquals(RolloverKind.Full, rollover)
    }
}
