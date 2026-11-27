package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnitBadgeSpinTest {
    @get:Rule
    val rule = createComposeRule()

    private fun rotation(tag: String): Float =
        rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config[UnitBadgeRotationKey]

    private fun labelRotation(tag: String): Float =
        rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config[UnitBadgeLabelRotationKey]

    private fun show(reduced: Boolean = false, vararg speeds: Pair<String, Float>) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    Column {
                        speeds.forEach { (tag, speed) ->
                            UnitBadge("GB", modifier = Modifier.testTag(tag), spinDegPerSec = speed)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun badge_rotates_over_time() {
        show(false, "badge" to 90f)
        assertEquals(0f, rotation("badge"), 0.01f)
        repeat(65) { rule.mainClock.advanceTimeBy(16) }
        val turned = rotation("badge")
        assertTrue("turned about 90 degrees in a second (was $turned)", turned in 60f..120f)
    }

    @Test
    fun badge_label_stays_upright() {
        show(false, "badge" to 120f)
        repeat(40) { rule.mainClock.advanceTimeBy(16) }
        assertEquals(0f, rotation("badge") + labelRotation("badge"), 0.001f)
        assertTrue(rotation("badge") > 10f)
    }

    @Test
    fun badge_spins_faster_with_higher_speed() {
        show(false, "slow" to 30f, "fast" to 200f)
        repeat(40) { rule.mainClock.advanceTimeBy(16) }
        assertTrue(rotation("fast") > rotation("slow") * 3f)
    }

    @Test
    fun badge_idles_slowly_when_speed_is_unknown() {
        show(false, "idle" to 8f)
        repeat(63) { rule.mainClock.advanceTimeBy(16) }
        val turned = rotation("idle")
        assertTrue("idles at about 8 degrees per second (was $turned)", turned in 4f..14f)
    }

    @Test
    fun badge_is_still_when_reduced() {
        show(true, "badge" to 200f)
        repeat(65) { rule.mainClock.advanceTimeBy(16) }
        assertEquals(0f, rotation("badge"), 0.001f)
    }

    @Test
    fun badge_without_speed_never_turns() {
        show(false, "badge" to 0f)
        repeat(65) { rule.mainClock.advanceTimeBy(16) }
        assertEquals(0f, rotation("badge"), 0.001f)
    }
}
