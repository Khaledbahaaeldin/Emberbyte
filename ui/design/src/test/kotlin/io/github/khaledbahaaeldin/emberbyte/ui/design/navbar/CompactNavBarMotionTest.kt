package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import io.github.khaledbahaaeldin.emberbyte.ui.design.layout.contentWidth
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.GeneratedPalettes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * T13: the bar must stay centred under reduced motion (the 1.0.2 audit saw it pushed to the left edge), the Plus button
 * must animate out, and the pill needs a visible edge on surface2 cards.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CompactNavBarMotionTest {
    @get:Rule val rule = createComposeRule()

    private val items = listOf(
        NavBarItem("home", "Home", Icons.Rounded.Home, NavAccent.Primary),
        NavBarItem("apps", "Apps", Icons.Rounded.Apps, NavAccent.Tertiary),
        NavBarItem("plans", "Plans", Icons.Rounded.AccountBalanceWallet, NavAccent.Secondary),
    )

    private var reduce by mutableStateOf(false)
    private var showPlus by mutableStateOf(false)
    private var selected by mutableStateOf("home")

    private fun setUp(reduceMotion: Boolean, plus: Boolean = false, selectedId: String = "home") {
        reduce = reduceMotion
        showPlus = plus
        selected = selectedId
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduce) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        CompactNavBar(
                            items = items,
                            selectedId = selected,
                            onSelect = {},
                            visible = true,
                            trailing = if (showPlus) {
                                { PlusButton(onClick = {}, contentDescription = "New plan") }
                            } else null,
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    /** Distance from the left edge minus distance from the right edge of the root; 0 when centred. */
    private fun centreOffset(): Float {
        val root = rule.onRoot().getBoundsInRoot()
        val bar = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot()
        return (bar.left.value - root.left.value) - (root.right.value - bar.right.value)
    }

    private fun plusExists(): Boolean = rule.onAllNodesWithContentDescription("New plan").fetchSemanticsNodes().isNotEmpty()

    // ---- the app's real layout: EmberbyteApp.kt aligns the bar with `align(BottomCenter).contentWidth()` ------------

    private fun setUpAsInTheApp(
        reduceMotion: Boolean,
        rtl: Boolean = false,
        plus: Boolean = false,
        fontScale: Float = 1f,
    ) {
        reduce = reduceMotion
        showPlus = plus
        rule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalDensity provides Density(density = 1f, fontScale = fontScale),
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    CompositionLocalProvider(LocalReduceMotion provides reduce) {
                        Box(Modifier.fillMaxSize()) {
                            CompactNavBar(
                                items = items,
                                selectedId = "home",
                                onSelect = {},
                                visible = true,
                                modifier = Modifier.align(Alignment.BottomCenter).contentWidth(),
                                trailing = if (showPlus) {
                                    { PlusButton(onClick = {}, contentDescription = "New plan") }
                                } else null,
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun bar_is_centered_in_the_app_layout_with_normal_motion() {
        setUpAsInTheApp(reduceMotion = false)
        assertEquals(0f, centreOffset(), 1f)
    }

    @Test
    fun bar_is_centered_in_the_app_layout_under_reduced_motion() {
        setUpAsInTheApp(reduceMotion = true)
        assertEquals("1.0.2 pinned the bar to the left edge under reduced motion", 0f, centreOffset(), 1f)
    }

    @Test
    fun bar_is_centered_in_the_app_layout_under_reduced_motion_in_rtl() {
        setUpAsInTheApp(reduceMotion = true, rtl = true)
        assertEquals(0f, centreOffset(), 1f)
    }

    @Test
    fun bar_is_centered_in_the_app_layout_under_reduced_motion_with_the_plus_button() {
        setUpAsInTheApp(reduceMotion = true, plus = true)
        // With the Plus the whole row (bar + gap + button) is centred, so the bar sits half the button plus gap to the left.
        val root = rule.onRoot().getBoundsInRoot()
        val bar = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot()
        val plus = rule.onAllNodesWithContentDescription("New plan").fetchSemanticsNodes().first().boundsInRoot
        val left = bar.left.value - root.left.value
        val right = root.right.value - plus.right
        assertEquals("the bar and the Plus together must be centred", 0f, left - right, 1.5f)
    }

    @Test
    fun bar_is_centered_in_the_app_layout_under_reduced_motion_at_font_scale_2() {
        setUpAsInTheApp(reduceMotion = true, fontScale = 2f)
        assertEquals(0f, centreOffset(), 1f)
    }


    @Test
    fun pill_is_centered_with_normal_motion() {
        setUp(reduceMotion = false)
        assertEquals(0f, centreOffset(), 1f)
    }

    @Test
    fun pill_is_centered_when_reduced_motion() {
        setUp(reduceMotion = true)
        assertEquals("bar must be centred under reduced motion", 0f, centreOffset(), 1f)
    }

    @Test
    fun pill_stays_centered_after_reduced_motion_is_toggled_while_open() {
        setUp(reduceMotion = false)
        reduce = true
        rule.waitForIdle()
        assertEquals(0f, centreOffset(), 1f)
        selected = "plans"
        rule.waitForIdle()
        assertEquals("still centred after a selection under reduced motion", 0f, centreOffset(), 1f)
        reduce = false
        rule.waitForIdle()
        assertEquals(0f, centreOffset(), 1f)
    }

    @Test
    fun plus_button_animates_out() {
        setUp(reduceMotion = false, plus = true, selectedId = "plans")
        assertTrue(plusExists())
        rule.mainClock.autoAdvance = false
        showPlus = false
        var framesStillThere = 0
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
            if (plusExists()) framesStillThere++
        }
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        assertTrue("the Plus button should stay for several frames while it animates out, saw $framesStillThere", framesStillThere >= 3)
        assertTrue("and be gone once the exit finishes", !plusExists())
    }

    @Test
    fun plus_exit_snaps_when_reduced() {
        setUp(reduceMotion = true, plus = true, selectedId = "plans")
        rule.mainClock.autoAdvance = false
        showPlus = false
        var framesStillThere = 0
        repeat(10) {
            rule.mainClock.advanceTimeBy(16)
            if (plusExists()) framesStillThere++
        }
        rule.mainClock.autoAdvance = true
        assertTrue("reduced motion must not animate the exit, saw $framesStillThere frames", framesStillThere <= 2)
    }

    @Test
    fun pill_has_a_visible_edge_on_surface2_cards_in_every_theme() {
        val failures = mutableListOf<String>()
        for ((id, pair) in GeneratedPalettes) {
            for ((mode, palette) in listOf("dark" to pair.first, "light" to pair.second)) {
                val edge = navBarBorderColor(palette).compositeOver(palette.surface2)
                val ratio = contrastRatio(edge, palette.surface2)
                if (ratio < 1.3) failures += "$id/$mode edge %.2f".format(ratio)
            }
        }
        assertTrue("the pill edge must separate it from surface2 cards:\n${failures.joinToString("\n")}", failures.isEmpty())
    }
}
