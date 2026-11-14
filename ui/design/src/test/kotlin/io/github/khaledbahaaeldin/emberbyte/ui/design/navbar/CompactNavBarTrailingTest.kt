package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The trailing + button on Plans must slide the bar over instead of making it jump (task T8b). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CompactNavBarTrailingTest {
    @get:Rule val rule = createComposeRule()

    private val items = listOf(
        NavBarItem("home", "Home", Icons.Rounded.Home, NavAccent.Primary),
        NavBarItem("apps", "Apps", Icons.Rounded.Apps, NavAccent.Tertiary),
        NavBarItem("plans", "Plans", Icons.Rounded.AccountBalanceWallet, NavAccent.Secondary),
    )

    private var showPlus by mutableStateOf(false)

    private fun setUp(reduceMotion: Boolean) {
        showPlus = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                    // Centred like the app's scaffold does, so a growing row pushes the bar sideways.
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        CompactNavBar(
                            items = items,
                            selectedId = "plans",
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

    private fun barX(): Float = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot().left.value

    @Test
    fun bar_does_not_jump_when_the_trailing_button_appears() {
        setUp(reduceMotion = false)
        val before = barX()
        rule.mainClock.autoAdvance = false
        showPlus = true
        val seen = mutableListOf(before)
        repeat(90) {
            rule.mainClock.advanceTimeBy(16)
            seen += barX()
        }
        val distinct = seen.map { (it * 10).toInt() }.toSet()
        val biggestStep = seen.zipWithNext { a, b -> kotlin.math.abs(b - a) }.max()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        val after = barX()
        assertTrue("bar must end to the left of where it started: $before -> $after", after < before - 20f)
        assertEquals("bar ends half the button plus gap to the left", before - 34f, after, 1.5f)
        assertTrue("bar moved in too few frames (${distinct.size} positions)", distinct.size >= 4)
        assertTrue("bar jumped $biggestStep dp in one frame", biggestStep < 30f)
    }

    @Test
    fun with_reduced_motion_the_bar_moves_at_once() {
        setUp(reduceMotion = true)
        val before = barX()
        showPlus = true
        rule.waitForIdle()
        assertEquals(before - 34f, barX(), 1.5f)
    }

    @Test
    fun the_button_leaves_with_an_animation_and_is_gone_afterwards() {
        setUp(reduceMotion = false)
        showPlus = true
        rule.waitForIdle()
        rule.onNodeWithTag("plus_button").getBoundsInRoot()
        showPlus = false
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(32)
        // still composed mid-exit
        rule.onNodeWithTag("plus_button").getBoundsInRoot()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithTag("plus_button").assertDoesNotExist()
    }

    @Test
    fun the_plus_button_is_still_56dp_when_settled() {
        setUp(reduceMotion = false)
        showPlus = true
        rule.waitForIdle()
        val bounds = rule.onNodeWithTag("plus_button").getBoundsInRoot()
        assertEquals(56.dp.value, bounds.width.value, 0.5f)
        assertEquals(56.dp.value, bounds.height.value, 0.5f)
    }
}
