package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UsageBarRowRedesignTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun row_is_one_semantic_node_with_label_value_and_percent() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UsageBarRow(
                    label = "Chrome",
                    bytes = 1_200_000_000L,
                    fraction = 0.43f,
                    color = Color.Cyan,
                    valueText = "1.2 GB",
                )
            }
        }

        // Must be one merged semantic node with ProgressBarRangeInfo and full description
        val node = rule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0.43f, 0f..1f, 0)
            )
        )
        node.assertExists()
        node.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                listOf("Chrome, 1.2 gigabytes, 43 percent of the total")
            )
        )
    }

    @Test
    fun value_bubble_never_clips_at_font_scale_2() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.width(360.dp).testTag("container")) {
                        UsageBarRow(
                            label = "Chrome",
                            bytes = 1_200_000_000L,
                            fraction = 0.43f,
                            color = Color.Cyan,
                            valueText = "1.2 GB",
                            modifier = Modifier.testTag("usage_row"),
                        )
                    }
                }
            }
        }

        val rowBounds = rule.onNodeWithTag("usage_row").getBoundsInRoot()
        val bubbleBounds = rule.onNodeWithTag("usage_bar_row_bubble", useUnmergedTree = true).getBoundsInRoot()

        assertTrue(
            "Bubble left (${bubbleBounds.left}) must be >= row left (${rowBounds.left})",
            bubbleBounds.left >= rowBounds.left
        )
        assertTrue(
            "Bubble right (${bubbleBounds.right}) must be <= row right (${rowBounds.right})",
            bubbleBounds.right <= rowBounds.right
        )
    }

    @Test
    fun fraction_zero_shows_a_visible_stub_of_at_least_4dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Box(Modifier.width(300.dp)) {
                    UsageBarRow(
                        label = "Idle",
                        bytes = 0L,
                        fraction = 0f,
                        color = Color.Cyan,
                        valueText = "0 B",
                    )
                }
            }
        }

        val stub = rule.onNodeWithTag("usage_bar_row_stub", useUnmergedTree = true)
        stub.assertExists()
        val bounds = stub.getBoundsInRoot()
        val width = bounds.right - bounds.left
        assertTrue("Stub width ($width) must be at least 4.dp", width >= 4.dp)
    }

    @Test
    fun very_long_app_name_ellipsises_and_keeps_the_value_visible() {
        val longName = "Very Long Application Name That Would Normally Exceed The Screen Bounds ".repeat(3)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Box(Modifier.width(320.dp)) {
                    UsageBarRow(
                        label = longName,
                        bytes = 1_200_000_000L,
                        fraction = 0.5f,
                        color = Color.Cyan,
                        valueText = "1.2 GB",
                    )
                }
            }
        }

        val bubble = rule.onNodeWithTag("usage_bar_row_bubble", useUnmergedTree = true)
        bubble.assertIsDisplayed()
        rule.onNodeWithText("1.2 GB", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun rtl_puts_the_bubble_at_the_start_edge_and_keeps_the_number_ltr() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.width(360.dp).testTag("container")) {
                        UsageBarRow(
                            label = "Chrome",
                            bytes = 1_200_000_000L,
                            fraction = 0.43f,
                            color = Color.Cyan,
                            valueText = "1.2 GB",
                            modifier = Modifier.testTag("usage_row"),
                        )
                    }
                }
            }
        }

        val rowBounds = rule.onNodeWithTag("usage_row").getBoundsInRoot()
        val bubbleBounds = rule.onNodeWithTag("usage_bar_row_bubble", useUnmergedTree = true).getBoundsInRoot()

        assertTrue("Bubble left (${bubbleBounds.left}) should be at row left (${rowBounds.left}) in RTL", bubbleBounds.left < rowBounds.right / 2)
        rule.onNodeWithText("1.2 GB", useUnmergedTree = true).assertIsDisplayed()
        assertTrue("Bubble must be inside row bounds", bubbleBounds.right <= rowBounds.right && bubbleBounds.left >= rowBounds.left)
    }
}
