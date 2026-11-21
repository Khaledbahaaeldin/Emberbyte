package io.github.khaledbahaaeldin.emberbyte.ui.design.number

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroBloomTest {
    @get:Rule
    val rule = createComposeRule()

    @Before
    fun freshAppStart() {
        HeroEntranceGate.reset()
    }

    @After
    fun resetGate() {
        HeroEntranceGate.reset()
    }

    private fun weightOf(digit: Char): Float =
        rule.onNodeWithTag("hero_digit_$digit", useUnmergedTree = true)
            .fetchSemanticsNode().config[HeroDigitWeightKey]

    private fun show(
        value: () -> String = { "1.23" },
        reduced: Boolean = false,
        entrance: HeroEntrance = HeroEntrance.WeightBloom,
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    HeroNumber(
                        value = value(),
                        unit = "GB",
                        contentDescription = "1.23 gigabytes",
                        entrance = entrance,
                    )
                }
            }
        }
    }

    @Test
    fun hero_starts_thin_and_ends_at_final_weight() {
        show()
        assertEquals(100f, weightOf('1'), 0.5f)
        assertEquals(100f, weightOf('3'), 0.5f)
        rule.mainClock.advanceTimeBy(4_000)
        listOf('1', '2', '3').forEach { assertEquals(800f, weightOf(it), 1f) }
    }

    @Test
    fun hero_digits_start_in_order() {
        show()
        // 150 ms delay for the first digit, then 70 ms per digit.
        rule.mainClock.advanceTimeBy(200)
        assertTrue("first digit has started", weightOf('1') > 110f)
        assertEquals("second digit has not started", 100f, weightOf('2'), 0.5f)
        assertEquals("third digit has not started", 100f, weightOf('3'), 0.5f)
        rule.mainClock.advanceTimeBy(120)
        assertTrue("second digit has started (${weightOf('2')})", weightOf('2') > 105f)
    }

    @Test
    fun hero_weight_overshoots_then_settles_at_800_and_never_exceeds_1000() {
        show()
        var peak = 0f
        repeat(150) {
            rule.mainClock.advanceTimeBy(16)
            peak = maxOf(peak, weightOf('1'))
        }
        assertTrue("overshoots the final weight (peak $peak)", peak > 800f)
        assertTrue("never above the font cap (peak $peak)", peak <= 1000f)
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(800f, weightOf('1'), 1f)
    }

    @Test
    fun entrance_snaps_when_reduced() {
        show(reduced = true)
        // Reduced motion uses the plain hero: no bloom weights are published at all.
        val config = rule.onNodeWithTag("hero_digit_1", useUnmergedTree = true).fetchSemanticsNode().config
        assertFalse(config.contains(HeroDigitWeightKey))
    }

    @Test
    fun entrance_does_not_replay_on_value_change_and_only_the_changed_digit_pulses() {
        var value by mutableStateOf("1.23")
        show(value = { value })
        rule.mainClock.advanceTimeBy(4_000)

        rule.runOnUiThread {
            value = "1.24"
            androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        }
        rule.mainClock.advanceTimeBy(48)
        listOf('1', '2').forEach { assertEquals("unchanged digit $it stays put", 800f, weightOf(it), 0.5f) }
        assertTrue("the changed digit pulses above 800", weightOf('4') > 800.5f)

        rule.mainClock.advanceTimeBy(4_000)
        assertEquals(800f, weightOf('4'), 1f)
    }

    @Test
    fun entrance_plays_once_per_app_start() {
        var visible by mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                if (visible) {
                    HeroNumber("1.23", "GB", "1.23 gigabytes", entrance = HeroEntrance.WeightBloom)
                }
            }
        }
        assertEquals(100f, weightOf('1'), 0.5f)
        rule.mainClock.advanceTimeBy(4_000)

        visible = false
        rule.mainClock.advanceTimeBy(100)
        visible = true
        rule.mainClock.advanceTimeBy(16)
        assertEquals("the second visit shows the final weight at once", 800f, weightOf('1'), 0.5f)
    }

    @Test
    fun other_screens_hero_has_no_entrance() {
        show(entrance = HeroEntrance.None)
        val config = rule.onNodeWithTag("hero_digit_1", useUnmergedTree = true).fetchSemanticsNode().config
        assertFalse(config.contains(HeroDigitWeightKey))
        assertFalse(HeroEntranceGate.played)
    }
}
