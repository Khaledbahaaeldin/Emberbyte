package io.github.khaledbahaaeldin.emberbyte.common

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
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
class ScreenHeaderTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun icon_buttons_keep_48dp_and_their_content_description() {
        var backClicked = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ScreenHeader(
                    title = "Test Screen",
                    onBack = { backClicked = true },
                )
            }
        }

        val backButton = rule.onNodeWithContentDescription("Back")
        backButton
            .assertIsDisplayed()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        // Exercise press interaction (pressed shape morph)
        backButton.performTouchInput {
            down(center)
            up()
        }
        assertTrue("Back button clicked", backClicked)

        assertNoTouchTargetSmallerThan48dp(rule)
    }
}
