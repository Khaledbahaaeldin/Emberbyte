package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ContainerRevealTest {
    @get:Rule
    val rule = createComposeRule()

    private val origin = Rect(40f, 600f, 200f, 660f)

    private fun fraction() = rule.onNodeWithTag("reveal_root", useUnmergedTree = true)
        .fetchSemanticsNode().config[ContainerRevealFractionKey]

    private fun show(origin: Rect?, reduced: Boolean = false) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    ContainerReveal(origin = origin, closeRequested = false, onClosed = {}) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    @Test
    fun history_grows_out_of_the_button_in_about_620ms() {
        show(origin)
        assertEquals(0f, fraction(), 0.01f)
        var sawMiddle = false
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
            if (fraction() in 0.1f..0.9f) sawMiddle = true
        }
        assertTrue("has in-between frames", sawMiddle)
        rule.mainClock.advanceTimeBy(2_500)
        assertEquals(1f, fraction(), 0.01f)
    }

    @Test
    fun history_open_is_instant_when_reduced() {
        show(origin, reduced = true)
        assertEquals(1f, fraction(), 0.001f)
    }

    @Test
    fun without_an_origin_the_screen_is_simply_there() {
        show(origin = null)
        assertEquals(1f, fraction(), 0.001f)
    }

    @Test
    fun closing_reverses_the_reveal_and_then_reports_closed() {
        var closed = 0
        var close by mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ContainerReveal(origin = origin, closeRequested = close, onClosed = { closed++ }) {
                    Box(Modifier.fillMaxSize())
                }
            }
        }
        rule.mainClock.advanceTimeBy(2_500)
        assertEquals(1f, fraction(), 0.01f)
        rule.runOnUiThread { close = true }
        rule.mainClock.advanceTimeBy(100)
        fraction() // reading the tree applies the pending state change
        rule.mainClock.advanceTimeBy(100)
        val mid = fraction()
        assertTrue("shrinking, not yet closed (was $mid)", mid in 0.05f..0.95f)
        assertEquals(0, closed)
        rule.mainClock.advanceTimeBy(1_000)
        assertEquals(1, closed)
    }

    @Test
    fun closing_is_instant_when_reduced() {
        var closed = 0
        var close by mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    ContainerReveal(origin = origin, closeRequested = close, onClosed = { closed++ }) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }
        rule.runOnUiThread { close = true }
        rule.mainClock.advanceTimeBy(100)
        fraction()
        rule.mainClock.advanceTimeBy(100)
        assertEquals(1, closed)
    }
}
