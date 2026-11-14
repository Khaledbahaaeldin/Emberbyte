package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppsAccessibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private val sampleAppsState = AppsUiState(
        range = AppsRange.TODAY,
        network = AppsNetwork.ALL,
        apps = listOf(
            AppRowUi("com.video", "Video", 600_000_000L, 400_000_000L),
            AppRowUi("com.chat", "Chat", 100_000_000L, 0L),
        ),
        totalBytes = 1_100_000_000L,
        loaded = true,
    )

    private fun show(state: AppsUiState = sampleAppsState) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = state,
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }
    }

    @Test
    fun apps_icon_and_filter_buttons_have_content_descriptions() {
        show(sampleAppsState.copy(range = AppsRange.LAST_7_DAYS, network = AppsNetwork.WIFI, query = "test"))
        // Screen headline, search field, and Filters button have clear text labels/names
        rule.onNodeWithText("Apps").assertIsDisplayed()
        rule.onNodeWithText("Search apps").assertIsDisplayed()
        rule.onNodeWithText("Filters").assertIsDisplayed()
        rule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
        // Removable filter chips have accessible remove descriptions
        rule.onNodeWithContentDescription("Remove 7 days filter").assertIsDisplayed()
        rule.onNodeWithContentDescription("Remove Wi-Fi filter").assertIsDisplayed()
        // App items have clear text
        rule.onNodeWithText("Video").assertIsDisplayed()
        rule.onNodeWithText("Chat").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun apps_touch_targets_are_at_least_48dp() {
        show(sampleAppsState.copy(query = "test"))
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun icon_buttons_keep_48dp_and_their_content_description() {
        show(sampleAppsState.copy(query = "test"))
        val clearButton = rule.onNodeWithContentDescription("Clear search")
        clearButton
            .assertIsDisplayed()
            .performTouchInput { down(center); up() }
        assertNoTouchTargetSmallerThan48dp(rule)
    }
}
