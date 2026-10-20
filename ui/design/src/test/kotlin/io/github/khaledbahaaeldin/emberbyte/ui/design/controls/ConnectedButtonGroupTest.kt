package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
class ConnectedButtonGroupTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun selected_segment_has_selected_state_and_a_check_icon() {
        val options = listOf("Day", "Week", "Month")
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ConnectedButtonGroup(
                    options = options,
                    selectedIndex = 1,
                    onSelect = {},
                    modifier = Modifier.testTag("group"),
                )
            }
        }
        val weekNode = rule.onNodeWithText("Week")
        weekNode.assertIsSelected()
        val semantics = weekNode.fetchSemanticsNode()
        assertEquals(Role.RadioButton, semantics.config.getOrNull(SemanticsProperties.Role))

        val dayNode = rule.onNodeWithText("Day")
        dayNode.assertIsNotSelected()
        val monthNode = rule.onNodeWithText("Month")
        monthNode.assertIsNotSelected()

        // Check icon exists only for the selected segment in unmerged tree
        rule.onNodeWithTag("connected_button_group_check_icon", useUnmergedTree = true).assertExists()
    }

    @Test
    fun clicking_another_segment_calls_onSelect_with_its_index() {
        val options = listOf("Day", "Week", "Month")
        var selected = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ConnectedButtonGroup(
                    options = options,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                )
            }
        }
        rule.onNodeWithText("Month").performClick()
        assertEquals(2, selected)
    }

    @Test
    fun every_segment_is_at_least_48dp_tall() {
        val options = listOf("Day", "Week", "Month")
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ConnectedButtonGroup(
                    options = options,
                    selectedIndex = 0,
                    onSelect = {},
                )
            }
        }
        for (option in options) {
            val bounds = rule.onNodeWithText(option).getUnclippedBoundsInRoot()
            val height = bounds.bottom - bounds.top
            assertTrue(
                "Segment '$option' height must be >= 48dp, was $height",
                height >= 48.dp
            )
        }
    }

    @Test
    fun three_segments_fit_in_a_280dp_container_at_font_scale_2() {
        val options = listOf("Today", "7 days", "This month")
        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 2.0f)
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(modifier = Modifier.width(280.dp).testTag("container")) {
                        ConnectedButtonGroup(
                            options = options,
                            selectedIndex = 0,
                            onSelect = {},
                            modifier = Modifier.fillMaxWidth().testTag("group"),
                        )
                    }
                }
            }
        }
        val containerBounds = rule.onNodeWithTag("container").getUnclippedBoundsInRoot()
        val groupBounds = rule.onNodeWithTag("group").getUnclippedBoundsInRoot()
        val containerWidth = containerBounds.right - containerBounds.left
        val groupWidth = groupBounds.right - groupBounds.left
        assertTrue(
            "Group width ($groupWidth) must fit inside container ($containerWidth)",
            groupWidth <= containerWidth + 1.dp
        )
        for (option in options) {
            val labelBounds = rule.onNodeWithText(option).getUnclippedBoundsInRoot()
            assertTrue(
                "Label '$option' left (${labelBounds.left}) inside container (${containerBounds.left})",
                labelBounds.left >= containerBounds.left - 0.5.dp
            )
            assertTrue(
                "Label '$option' right (${labelBounds.right}) inside container (${containerBounds.right})",
                labelBounds.right <= containerBounds.right + 0.5.dp
            )
        }
    }

    @Test
    fun rtl_reverses_the_segment_order() {
        val options = listOf("First", "Second", "Third")
        rule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    ConnectedButtonGroup(
                        options = options,
                        selectedIndex = 0,
                        onSelect = {},
                        modifier = Modifier.width(300.dp),
                    )
                }
            }
        }
        val firstBounds = rule.onNodeWithText("First").getUnclippedBoundsInRoot()
        val thirdBounds = rule.onNodeWithText("Third").getUnclippedBoundsInRoot()
        assertTrue(
            "In RTL, 'First' (${firstBounds.left}) must be to the right of 'Third' (${thirdBounds.left})",
            firstBounds.left > thirdBounds.left
        )
    }

    @Test
    fun disabled_group_ignores_clicks() {
        val options = listOf("Day", "Week", "Month")
        var selected = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ConnectedButtonGroup(
                    options = options,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                    enabled = false,
                )
            }
        }
        rule.onNodeWithText("Month").performClick()
        assertEquals(0, selected)
    }
}
