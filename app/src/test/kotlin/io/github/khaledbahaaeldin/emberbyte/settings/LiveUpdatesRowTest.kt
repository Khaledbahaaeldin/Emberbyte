package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LiveUpdatesRowTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun row_shows_allowed_or_off_and_opens_the_system_screen() {
        var opened = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LiveUpdatesRow(
                    mayPost = true,
                    onClick = { opened = true },
                )
            }
        }
        rule.onNodeWithText("Live updates").assertIsDisplayed()
        rule.onNodeWithText("Allowed").assertIsDisplayed()
        rule.onNode(hasText("Live updates") and hasClickAction()).performClick()
        assertTrue(opened)
    }

    @Test
    fun samsung_hint_is_shown_only_on_samsung() {
        var isSamsung by mutableStateOf(true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LiveUpdatesRow(
                    mayPost = true,
                    isSamsung = isSamsung,
                    onClick = {},
                )
            }
        }
        rule.onNodeWithText("Also allow Emberbyte under Now bar in your phone's settings").assertIsDisplayed()

        isSamsung = false
        rule.onNodeWithText("Also allow Emberbyte under Now bar in your phone's settings").assertDoesNotExist()
    }

    @Test
    fun row_is_one_node_with_label_and_state() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LiveUpdatesRow(
                    mayPost = false,
                    onClick = {},
                )
            }
        }
        rule.onNodeWithText("Turned off in system settings").assertIsDisplayed()
    }

    @Test
    fun font_scale_2_no_clip() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    LiveUpdatesRow(
                        mayPost = true,
                        isSamsung = true,
                        onClick = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Live updates").assertIsDisplayed()
        rule.onNodeWithText("Allowed").assertIsDisplayed()
    }

    @Test
    fun rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    LiveUpdatesRow(
                        mayPost = true,
                        onClick = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Live updates").assertIsDisplayed()
    }
}
