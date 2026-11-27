package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The App detail motion: icon entrance, hour wave, section reveal and the collapsing header. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class DetailMotionTest {
    @get:Rule
    val rule = createComposeRule()

    @Before fun on() { RevealGate.enabled = true }
    @After fun off() { RevealGate.enabled = false }

    private fun show(reduced: Boolean = false) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    AppDetailContent(state = sampleDetail(), onEvent = {}, onBack = {})
                }
            }
        }
    }

    private fun width(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot().let { (it.right - it.left).value }

    @Test
    fun icon_entrance_has_in_between_frames_and_ends_at_full_size() {
        show()
        rule.mainClock.advanceTimeBy(16)
        val early = width("detail_icon")
        var sawMiddle = false
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
            val w = width("detail_icon")
            if (w in 10f..70f) sawMiddle = true
        }
        rule.mainClock.advanceTimeBy(3_000)
        assertTrue("starts small (was $early)", early < 76f)
        assertTrue("has in-between frames", sawMiddle)
        assertEquals(76f, width("detail_icon"), 1f)
    }

    @Test
    fun reduced_motion_is_static() {
        show(reduced = true)
        rule.mainClock.advanceTimeBy(16)
        assertEquals(76f, width("detail_icon"), 0.5f)
        assertEquals(76f, width("detail_icon"), 0.5f)
    }

    @Test
    fun hour_cells_wave_in_from_midnight() {
        show()
        rule.mainClock.advanceTimeBy(300)
        val first = width("detail_hour_0")
        val last = width("detail_hour_23")
        assertTrue("12 AM has popped in before 11 PM (first=$first, last=$last)", first > last)
        rule.mainClock.advanceTimeBy(4_000)
        assertEquals(width("detail_hour_0"), width("detail_hour_23"), 2f)
    }

    @Test
    fun a_section_reveals_into_place_and_ends_complete() {
        show()
        val key = io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoEntranceProgressKey
        fun progress() = rule.onNodeWithTag("detail_chart_card", useUnmergedTree = true).fetchSemanticsNode().config[key]
        assertTrue("starts hidden", progress() < 0.05f)
        var sawMiddle = false
        repeat(60) {
            rule.mainClock.advanceTimeBy(16)
            if (progress() in 0.1f..0.9f) sawMiddle = true
        }
        assertTrue("has in-between frames", sawMiddle)
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(1f, progress(), 0.02f)
    }

    @Test
    @Config(qualifiers = "w411dp-h640dp")
    fun the_header_collapses_into_a_mini_bar_on_scroll() {
        show(reduced = true) // no entrance animations, so the test can scroll with the normal clock
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithTag("detail_mini_bar", useUnmergedTree = true).assertDoesNotExist()
        rule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("detail_budget_card"))
        rule.mainClock.advanceTimeBy(200)
        rule.onNodeWithTag("detail_mini_bar", useUnmergedTree = true).assertExists()
    }
}
