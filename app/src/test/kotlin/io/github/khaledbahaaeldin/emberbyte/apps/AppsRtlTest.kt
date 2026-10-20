package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
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
class AppsRtlTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun apps_range_chips_are_ordered_from_right_to_left_in_rtl() {
        val state = AppsUiState(
            range = AppsRange.LAST_7_DAYS,
            network = AppsNetwork.WIFI,
            sort = io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort.NAME,
            loaded = true,
        )
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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

        val sevenDaysBounds = rule.onNodeWithText("7 days").getBoundsInRoot()
        val wifiBounds = rule.onNodeWithText("Wi-Fi").getBoundsInRoot()
        val nameBounds = rule.onNodeWithText("Name").getBoundsInRoot()

        // In RTL layout direction, the first item "7 days" should be on the right (higher X),
        // followed by "Wi-Fi" to its left, and "Name" furthest to the left.
        assertTrue(
            "In RTL, 7 days (${sevenDaysBounds.left}) must be to the right of Wi-Fi (${wifiBounds.left})",
            sevenDaysBounds.left > wifiBounds.left,
        )
        assertTrue(
            "In RTL, Wi-Fi (${wifiBounds.left}) must be to the right of Name (${nameBounds.left})",
            wifiBounds.left > nameBounds.left,
        )
    }
}
