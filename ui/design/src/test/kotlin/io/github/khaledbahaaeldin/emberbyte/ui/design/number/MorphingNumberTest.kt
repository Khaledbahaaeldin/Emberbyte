package io.github.khaledbahaaeldin.emberbyte.ui.design.number

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphingNumberTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun exposes_one_node_with_the_plain_language_description() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                MorphingNumber(
                    bytes = 1_240_000_000L,
                    throughputBps = 0L,
                    units = ByteUnits.DECIMAL,
                    contentDescription = "1.24 gigabytes used today",
                    animate = false,
                )
            }
        }
        rule.onNodeWithContentDescription("1.24 gigabytes used today").assertIsDisplayed()
    }

    @Test
    fun digits_are_not_separate_semantics_nodes() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                MorphingNumber(
                    bytes = 213_000_000L,
                    throughputBps = 0L,
                    units = ByteUnits.DECIMAL,
                    contentDescription = "213 megabytes used today",
                    animate = false,
                )
            }
        }
        rule.onNodeWithContentDescription("213 megabytes used today").assertIsDisplayed()
        rule.onAllNodesWithText("2", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun morphing_number_tween_and_live_state_snap_and_disabled_when_reduce_motion_is_true() {
        var capturedLive: Boolean? = null
        var capturedSpec: androidx.compose.animation.core.AnimationSpec<Float>? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                androidx.compose.runtime.CompositionLocalProvider(
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
                ) {
                    MorphingNumber(
                        bytes = 1_240_000_000L,
                        throughputBps = 20_000_000L,
                        units = ByteUnits.DECIMAL,
                        contentDescription = "1.24 gigabytes used today",
                        animate = true,
                        onMotionResolved = { live, spec ->
                            capturedLive = live
                            capturedSpec = spec
                        },
                    )
                }
            }
        }
        org.junit.Assert.assertEquals(false, capturedLive)
        org.junit.Assert.assertTrue(
            "Intensity animation spec must be SnapSpec when reduce motion is enabled",
            capturedSpec is androidx.compose.animation.core.SnapSpec,
        )
    }

    @Test
    fun general_morphing_number_exposes_semantics_and_content_description() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                MorphingNumber(
                    value = "1.4",
                    unit = "MB/s",
                    prefix = "↓ ",
                    contentDescription = "Download 1.4 megabytes per second",
                )
            }
        }
        rule.onNodeWithContentDescription("Download 1.4 megabytes per second").assertIsDisplayed()
    }

    @Test
    fun general_morphing_number_snaps_when_reduced_motion() {
        rule.mainClock.autoAdvance = false
        var value by androidx.compose.runtime.mutableStateOf("1.4")

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                androidx.compose.runtime.CompositionLocalProvider(
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
                ) {
                    MorphingNumber(
                        value = value,
                        unit = "GB",
                        contentDescription = "Usage $value GB",
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onAllNodesWithTag("morphing_digit_1", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithTag("morphing_digit_2", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithTag("morphing_digit_4", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithTag("morphing_digit_8", useUnmergedTree = true).assertCountEquals(0)

        rule.runOnUiThread {
            value = "2.8"
            androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        }
        rule.mainClock.advanceTimeByFrame()

        // Reduced motion snaps immediately on frame 1 without animation/mixed states
        rule.onAllNodesWithTag("morphing_digit_1", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithTag("morphing_digit_2", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithTag("morphing_digit_4", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithTag("morphing_digit_8", useUnmergedTree = true).assertCountEquals(1)
    }
}
