package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
class AppsRtlTest {
    @get:Rule val rule = createComposeRule()

    @org.junit.Before fun settleEntrance() { AppsEntranceGate.played = true }

    @Test
    fun apps_period_buttons_are_ordered_from_right_to_left_in_rtl() {
        val state = AppsUiState(loaded = true)
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppsContent(state = state, onEvent = {}, onOpenApp = {}, onPromptAction = {})
                }
            }
        }
        val today = rule.onNodeWithTag("period_today").getBoundsInRoot()
        val cycle = rule.onNodeWithTag("period_cycle").getBoundsInRoot()
        // In RTL the first period (Today) is on the right and the last (Cycle) on the left.
        assertTrue("Today (${today.left}) must be right of Cycle (${cycle.left})", today.left > cycle.left)
    }
}
