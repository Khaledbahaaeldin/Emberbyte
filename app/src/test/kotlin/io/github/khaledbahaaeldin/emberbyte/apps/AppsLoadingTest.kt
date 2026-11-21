package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppsLoadingTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun apps_shows_loading_not_zero_bytes_before_the_first_emission() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = AppsUiState(loaded = false, totalBytes = 0L),
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }

        rule.onNodeWithContentDescription("Loading apps").assertIsDisplayed()
        rule.onNodeWithText("0 B (apps only)").assertDoesNotExist()
    }
}
