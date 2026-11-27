package io.github.khaledbahaaeldin.emberbyte.nav

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApp
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Back from anything opened inside Settings must land on the Settings hub, not on Home. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class SettingsBackNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun openSettings() {
        val app = ApplicationProvider.getApplicationContext<Application>() as EmberbyteApplication
        val graph = app.graph
        runBlocking {
            graph.onboarding.complete()
            graph.onboardingCompleted.first { it == true }
        }
        rule.setContent {
            val settings by graph.settingsState.collectAsState()
            val current = settings
            if (current != null) {
                EmberbyteTheme(
                    spec = EmberbyteThemeSpec(
                        presetId = current.themeId,
                        darkTheme = true,
                        amoled = current.amoledBlack,
                        wallpaperColors = current.useWallpaperColors,
                    ),
                ) {
                    EmberbyteApp(graph, hapticsEnabled = current.hapticsEnabled)
                }
            }
        }
        rule.waitUntil(10_000) { rule.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("pulse_card").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun back_from_look_and_feel_returns_to_the_settings_hub() {
        openSettings()
        rule.onNodeWithTag("hub_card_look_and_feel").performScrollTo().performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("pulse_card").fetchSemanticsNodes().isEmpty() }

        pressBack()

        rule.waitUntil(10_000) { rule.onAllNodesWithTag("pulse_card").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
    }

    @Test
    fun back_from_an_area_returns_to_the_settings_hub() {
        openSettings()
        rule.onNodeWithTag("hub_card_about").performScrollTo().performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("about_version_card").fetchSemanticsNodes().isNotEmpty() }

        pressBack()

        rule.waitUntil(10_000) { rule.onAllNodesWithTag("pulse_card").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
    }
}
