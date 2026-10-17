package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
class NetworkBarTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun network_bar_gives_a_tiny_nonzero_segment_a_visible_width() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Box(Modifier.width(200.dp)) {
                    NetworkBar(
                        mobileBytes = 1L,
                        wifiBytes = 1_000_000_000L,
                        mobileColor = Color.Magenta,
                        wifiColor = Color.Cyan,
                    )
                }
            }
        }
        val bounds = rule.onNodeWithTag("network_bar_mobile", useUnmergedTree = true).getBoundsInRoot()
        val mobileWidth = bounds.right - bounds.left
        assertTrue("Mobile segment should be at least 6dp wide, was $mobileWidth", mobileWidth >= 6.dp)
    }

    @Test
    fun network_bar_all_wifi_shows_no_mobile_segment() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Box(Modifier.width(200.dp)) {
                    NetworkBar(
                        mobileBytes = 0L,
                        wifiBytes = 1_000_000_000L,
                        mobileColor = Color.Magenta,
                        wifiColor = Color.Cyan,
                    )
                }
            }
        }
        rule.onNodeWithTag("network_bar_mobile", useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithTag("network_bar_wifi", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun network_bar_describes_both_values() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Box(Modifier.width(200.dp)) {
                    NetworkBar(
                        mobileBytes = 43_000L,
                        wifiBytes = 1_300_000_000L,
                        mobileColor = Color.Magenta,
                        wifiColor = Color.Cyan,
                    )
                }
            }
        }
        rule.onNodeWithContentDescription("Mobile 43 kilobytes, Wi-Fi 1.3 gigabytes").assertExists()
    }
}
