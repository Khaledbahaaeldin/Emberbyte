package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
class NavBarRecompositionTest {
    @get:Rule val rule = createComposeRule()

    private val items = listOf(
        NavBarItem("home", "Home", Icons.Rounded.Home, NavAccent.Primary),
        NavBarItem("apps", "Apps", Icons.Rounded.Apps, NavAccent.Tertiary),
        NavBarItem("plans", "Plans", Icons.Rounded.AccountBalanceWallet, NavAccent.Secondary),
    )

    @Test
    fun switching_tabs_recomposes_the_bar_content_at_most_3_times() {
        var selectedId by mutableStateOf("home")
        var barRecompositions = 0

        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompactNavBar(
                    items = items,
                    selectedId = selectedId,
                    onSelect = { selectedId = it },
                    visible = true,
                )
            }
        }
        rule.mainClock.advanceTimeBy(16)
        val initialInternalCount = CompactNavBarRecompositionTracker.count

        // Switch tab from Home to Plans and step through 40 frames (640 ms) of spring animation
        rule.onNodeWithTag("nav_plans").performClick()
        rule.runOnIdle { }
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
        }

        val deltaInternal = CompactNavBarRecompositionTracker.count - initialInternalCount
        assertTrue(
            "Bar content recomposed $deltaInternal times during 40 frames of spring motion; expected in 1..3",
            deltaInternal in 1..3,
        )
    }
}
