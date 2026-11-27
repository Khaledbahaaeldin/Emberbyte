package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class AlertsSectionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun notify_switch_chips_and_local_caption_render() {
        var toggled: Int? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AlertsSection(
                    thresholds = setOf(80, 100),
                    onToggleThreshold = { toggled = it },
                )
            }
        }
        rule.onNodeWithText("Notify me").assertIsDisplayed()
        rule.onNodeWithText("Sent from this phone, never from a server").assertIsDisplayed()
        rule.onNodeWithText("50%").performClick()
        assertEquals(50, toggled)
    }
}
