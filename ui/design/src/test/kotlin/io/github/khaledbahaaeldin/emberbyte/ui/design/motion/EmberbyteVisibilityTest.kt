package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.DuskDark
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.DuskLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EmberbyteVisibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private fun heightOf(tag: String): Float =
        rule.onNodeWithTag(tag).getUnclippedBoundsInRoot().height.value

    @Test
    fun visibility_has_in_between_frames() {
        var visible by mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteAnimatedVisibility(visible = visible, modifier = Modifier.testTag("holder")) {
                    Box(Modifier.testTag("body").width(100.dp).height(80.dp))
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithTag("body").assertDoesNotExist()

        visible = true
        val heights = mutableListOf<Float>()
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
            if (rule.onAllNodesExist("holder")) heights += heightOf("holder")
        }
        rule.mainClock.advanceTimeBy(1000)
        assertEquals(80f, heightOf("holder"), 0.5f)
        val between = heights.filter { it > 1f && it < 79f }
        assertTrue("expected in-between heights, saw $heights", between.isNotEmpty())
    }

    @Test
    fun visibility_snaps_when_reduced() {
        var visible by mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalReduceMotion provides true) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    CompositionLocalProvider(LocalReduceMotion provides true) {
                        EmberbyteAnimatedVisibility(visible = visible, modifier = Modifier.testTag("holder")) {
                            Box(Modifier.testTag("body").width(100.dp).height(80.dp))
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        visible = true
        val heights = mutableListOf<Float>()
        repeat(10) {
            rule.mainClock.advanceTimeBy(16)
            if (rule.onAllNodesExist("holder")) heights += heightOf("holder")
        }
        assertTrue("content should be visible at full height", heights.isNotEmpty() && heights.last() > 79f)
        assertTrue(
            "reduced motion must not show in-between heights, saw $heights",
            heights.none { it > 1f && it < 79f },
        )
    }

    @Test
    fun crossfade_has_in_between_frames() {
        var target by mutableStateOf("a")
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteCrossfade(targetState = target) { value ->
                    Box(Modifier.testTag("content-$value").width(40.dp).height(40.dp))
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithTag("content-a").assertExists()

        target = "b"
        var sawBoth = false
        repeat(30) {
            rule.mainClock.advanceTimeBy(16)
            if (rule.onAllNodesExist("content-a") && rule.onAllNodesExist("content-b")) sawBoth = true
        }
        assertTrue("old and new content should overlap while fading", sawBoth)
        rule.mainClock.advanceTimeBy(1000)
        rule.onNodeWithTag("content-a").assertDoesNotExist()
        rule.onNodeWithTag("content-b").assertExists()
    }

    @Test
    fun crossfade_snaps_when_reduced() {
        var target by mutableStateOf("a")
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    EmberbyteCrossfade(targetState = target) { value ->
                        Box(Modifier.testTag("content-$value").width(40.dp).height(40.dp))
                    }
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        target = "b"
        var sawBoth = false
        repeat(10) {
            rule.mainClock.advanceTimeBy(16)
            if (rule.onAllNodesExist("content-a") && rule.onAllNodesExist("content-b")) sawBoth = true
        }
        // A 90 ms effects fade is allowed under reduced motion, so the overlap must be over within ~160 ms.
        assertTrue(!sawBoth || rule.onAllNodesExist("content-b"))
        rule.mainClock.advanceTimeBy(200)
        rule.onNodeWithTag("content-a").assertDoesNotExist()
    }

    @Test
    fun palette_change_animates_every_token() {
        var target by mutableStateOf(DuskLight)
        var current: EmberbytePalette? = null
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                current = animatePaletteAsState(target)
            }
        }
        rule.mainClock.advanceTimeByFrame()
        assertEquals(DuskLight, current)

        rule.runOnUiThread {
            target = DuskDark
            Snapshot.sendApplyNotifications()
        }
        val differing = tokens(DuskLight).indices.filter { tokens(DuskLight)[it] != tokens(DuskDark)[it] }
        assertTrue("the two palettes should differ in several tokens", differing.size >= 6)
        var sawMixed = false
        repeat(30) {
            rule.mainClock.advanceTimeBy(16)
            val now = tokens(current ?: error("palette missing"))
            if (differing.all { now[it] != tokens(DuskLight)[it] && now[it] != tokens(DuskDark)[it] }) sawMixed = true
        }
        assertTrue("every differing token should pass through a mixed colour at the same time", sawMixed)
        rule.mainClock.advanceTimeBy(1000)
        assertPaletteClose(DuskDark, current ?: error("palette missing"))
    }

    @Test
    fun palette_change_snaps_when_reduced() {
        var target by mutableStateOf(DuskLight)
        var current: EmberbytePalette? = null
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    current = animatePaletteAsState(target)
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.runOnUiThread {
            target = DuskDark
            Snapshot.sendApplyNotifications()
        }
        // 90 ms is the reduced-motion effects duration; after 3 frames it must be fully switched.
        repeat(8) { rule.mainClock.advanceTimeBy(16) }
        assertPaletteClose(DuskDark, current ?: error("palette missing"))
    }

    private fun tokens(p: EmberbytePalette): List<Color> = listOf(
        p.background, p.surface, p.surface2, p.text, p.muted, p.primary, p.onPrimary,
        p.accent, p.onAccent, p.warn, p.onWarn, p.danger, p.onDanger, p.track,
    )

    /** Colours animate through Oklab, so the settled value can differ from the target by rounding. */
    private fun assertPaletteClose(expected: EmberbytePalette, actual: EmberbytePalette) {
        val e = tokens(expected)
        val a = tokens(actual)
        e.indices.forEach { i ->
            assertEquals("token $i red", e[i].red, a[i].red, 0.01f)
            assertEquals("token $i green", e[i].green, a[i].green, 0.01f)
            assertEquals("token $i blue", e[i].blue, a[i].blue, 0.01f)
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesExist(tag: String): Boolean =
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
