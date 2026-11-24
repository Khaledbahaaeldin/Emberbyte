package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DangerZoneTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun delete_opens_confirm_dialog_and_confirms() {
        var deleted = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                DangerZone(
                    planName = "Home SIM",
                    onDeleteConfirmed = { deleted = true },
                )
            }
        }
        rule.onNodeWithText("Delete plan").performClick()
        rule.onNodeWithText("Delete Home SIM?").assertIsDisplayed()
        rule.onNodeWithText("This removes the plan, its add-ons and its alerts. Your recorded usage is kept, and you can undo this for a few seconds.").assertIsDisplayed()
        rule.onAllNodesWithText("Delete").onLast().performClick()
        assertTrue(deleted)
    }
}
