package io.github.khaledbahaaeldin.emberbyte.ui.design.progress

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WavyBarTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun fraction_is_clamped() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WavyBar(fraction = 1.5f, color = Color.Green, modifier = Modifier.testTag("bar_over"))
                WavyBar(fraction = -0.3f, color = Color.Green, modifier = Modifier.testTag("bar_under"))
            }
        }
        val nodeOver = rule.onNodeWithTag("bar_over").fetchSemanticsNode()
        val infoOver = nodeOver.config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(1f, infoOver.current, 0.001f)

        val nodeUnder = rule.onNodeWithTag("bar_under").fetchSemanticsNode()
        val infoUnder = nodeUnder.config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0f, infoUnder.current, 0.001f)
    }

    @Test
    fun exposes_progress_semantics() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WavyBar(fraction = 0.65f, color = Color.Green, modifier = Modifier.testTag("bar"))
            }
        }
        val node = rule.onNodeWithTag("bar").fetchSemanticsNode()
        val info = node.config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0.65f, info.current, 0.001f)
        assertEquals(0f..1f, info.range)
    }

    @Test
    fun wavy_bar_grows_from_zero_on_first_show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WavyBar(
                    fraction = 0.8f,
                    color = Color.Green,
                    modifier = Modifier.testTag("bar"),
                )
            }
        }
        rule.mainClock.advanceTimeByFrame()
        val node = rule.onNodeWithTag("bar").fetchSemanticsNode()
        val info = node.config[SemanticsProperties.ProgressBarRangeInfo]
        assertTrue("Wavy bar should start from 0 on first show: ${info.current}", info.current < 0.8f)
    }

    @Test
    fun wavy_bar_respects_delay_millis() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WavyBar(
                    fraction = 0.8f,
                    color = Color.Green,
                    modifier = Modifier.testTag("bar"),
                    delayMillis = 40,
                )
            }
        }
        rule.mainClock.advanceTimeByFrame()
        var info = rule.onNodeWithTag("bar").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0f, info.current, 0.0001f)
        rule.mainClock.advanceTimeBy(30)
        info = rule.onNodeWithTag("bar").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0f, info.current, 0.0001f)
        rule.mainClock.advanceTimeBy(30)
        info = rule.onNodeWithTag("bar").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertTrue("Animated progress should advance after delay: ${info.current}", info.current > 0f)
    }

    @Test
    fun wavy_bar_snaps_when_reduced() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                androidx.compose.runtime.CompositionLocalProvider(
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
                ) {
                    WavyBar(
                        fraction = 0.8f,
                        color = Color.Green,
                        modifier = Modifier.testTag("bar"),
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        val info = rule.onNodeWithTag("bar").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0.8f, info.current, 0.001f)
    }
}
