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
class AppDetailLoadingTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun app_detail_shows_loading_not_the_package_name_before_the_first_emission() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppDetailContent(
                    state = AppDetailUiState(packageName = "com.example.unloaded", loaded = false),
                    onEvent = {},
                    onBack = {},
                )
            }
        }

        rule.onNodeWithContentDescription("Loading app details").assertIsDisplayed()
        rule.onNodeWithText("com.example.unloaded").assertDoesNotExist()
        rule.onNodeWithText("No daily usage").assertDoesNotExist()
    }
}
