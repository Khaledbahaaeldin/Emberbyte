package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import io.github.khaledbahaaeldin.emberbyte.data.Settings
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
class SettingsNavLogTest {
    @get:Rule val rule = createComposeRule()

    @Test fun long_pressing_the_version_title_asks_to_copy_the_navigation_log() {
        var copies = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsContent(
                    settings = Settings(),
                    onUpdate = {},
                    onBack = {},
                    versionName = "0.2.0",
                    onOpenSource = {},
                    onOpenBatterySettings = {},
                    onCopyNavLog = { copies++ },
                )
            }
        }
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Emberbyte 0.2.0"))
        rule.onNodeWithText("Emberbyte 0.2.0").performTouchInput { longClick() }
        assertEquals(1, copies)
    }

    @Test fun a_plain_tap_on_the_version_title_does_nothing() {
        var copies = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsContent(
                    settings = Settings(),
                    onUpdate = {},
                    onBack = {},
                    versionName = "0.2.0",
                    onOpenSource = {},
                    onOpenBatterySettings = {},
                    onCopyNavLog = { copies++ },
                )
            }
        }
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Emberbyte 0.2.0"))
        rule.onNodeWithText("Emberbyte 0.2.0").performTouchInput { click() }
        assertEquals(0, copies)
    }
}
