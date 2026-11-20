package io.github.khaledbahaaeldin.emberbyte.home

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
class HomeLoadingTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun home_shows_loading_not_the_set_up_a_plan_card_before_the_first_emission() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HomeContent(
                    state = HomeUiState(isLoading = true, hasPlan = false),
                    onEvent = {},
                    onOpenSettings = {},
                    onOpenHistory = {},
                    onPromptAction = {},
                    onOpenApp = {},
                    onSetUpPlan = {},
                )
            }
        }

        rule.onNodeWithContentDescription("Loading today's usage").assertIsDisplayed()
        rule.onNodeWithText("Set up a plan").assertDoesNotExist()
        rule.onNodeWithText("Top apps today").assertDoesNotExist()
    }
}
