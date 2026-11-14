package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A tap that drifts a little must still select the tab it ends on (task T3). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CompactNavBarTapDriftTest {
    @get:Rule val rule = createComposeRule()

    private val items = listOf(
        NavBarItem("home", "Home", Icons.Rounded.Home, NavAccent.Primary),
        NavBarItem("apps", "Apps", Icons.Rounded.Apps, NavAccent.Tertiary),
        NavBarItem("plans", "Plans", Icons.Rounded.AccountBalanceWallet, NavAccent.Secondary),
    )

    private fun DpRect.centerOffset() = Offset((left.value + right.value) / 2f, (top.value + bottom.value) / 2f)

    private val log = mutableListOf<String>()

    private fun show(selectedId: String, picked: MutableList<String>) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompactNavBar(
                    items = items,
                    selectedId = selectedId,
                    onSelect = { picked += it },
                    visible = true,
                    onDiagnostic = { kind, detail -> log += "$kind $detail" },
                )
            }
        }
    }

    @Test
    fun slightly_slidey_tap_inside_the_cell_selects_it() {
        val picked = mutableListOf<String>()
        show("plans", picked)
        val apps = rule.onNodeWithTag("nav_apps").getBoundsInRoot().centerOffset()
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            down(apps)
            moveBy(Offset(12.dp.toPx(), 0f))
            up()
        }
        rule.waitForIdle()
        assertEquals(listOf("apps"), picked)
    }

    @Test
    fun a_tap_near_the_cell_edge_that_drifts_out_of_the_cell_still_selects_the_pressed_tab() {
        val picked = mutableListOf<String>()
        show("plans", picked)
        val bounds = rule.onNodeWithTag("nav_apps").getBoundsInRoot()
        // Press 4 dp inside the left edge of Apps, then slide 14 dp to the left: the finger lifts outside the Apps cell,
        // which cancels the cell's own click.
        val start = Offset(bounds.left.value + 4f, (bounds.top.value + bounds.bottom.value) / 2f)
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            down(start)
            moveBy(Offset(-14.dp.toPx(), 0f))
            up()
        }
        rule.waitForIdle()
        assertEquals("gesture log: $log", listOf("apps"), picked)
    }

    @Test
    fun a_normal_tap_selects_exactly_once() {
        val picked = mutableListOf<String>()
        show("home", picked)
        val apps = rule.onNodeWithTag("nav_apps").getBoundsInRoot().centerOffset()
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            down(apps)
            up()
        }
        rule.waitForIdle()
        assertEquals("gesture log: $log", listOf("apps"), picked)
    }

    @Test
    fun a_real_drag_across_the_bar_still_drags() {
        val picked = mutableListOf<String>()
        show("home", picked)
        val home = rule.onNodeWithTag("nav_home").getBoundsInRoot().centerOffset()
        val plans = rule.onNodeWithTag("nav_plans").getBoundsInRoot().centerOffset()
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            swipe(start = home, end = plans, durationMillis = 300)
        }
        rule.waitForIdle()
        assertEquals(listOf("plans"), picked)
    }
}
