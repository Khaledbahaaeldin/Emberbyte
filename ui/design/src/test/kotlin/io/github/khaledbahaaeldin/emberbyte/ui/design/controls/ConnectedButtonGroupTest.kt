package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
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
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CORNER_FULL_PILL = 50f
private const val CORNER_INNER_FLAT = 8f

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

    @Test
    fun selecting_another_segment_animates_the_shape() {
        var selectedIndex by mutableStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ConnectedButtonGroup(
                    options = listOf("Day", "Week", "Month"),
                    selectedIndex = selectedIndex,
                    onSelect = { selectedIndex = it },
                )
            }
        }
        rule.mainClock.advanceTimeByFrame()

        // Initially "Day" (leading segment) is selected (full pill shape: 50.0)
        val dayNode = rule.onNodeWithText("Day")
        val initialRadius = dayNode.currentCornerRadius()
        assertEquals(CORNER_FULL_PILL, initialRadius, 0.001f)

        // Select "Week"
        selectedIndex = 1

        // Step through paused frames to read the shape at midpoint
        var midRadius: Float? = null
        for (i in 1..30) {
            rule.mainClock.advanceTimeBy(16)
            val r = dayNode.currentCornerRadius()
            if (r != CORNER_FULL_PILL && r != CORNER_INNER_FLAT) {
                midRadius = r
                break
            }
        }

        // Settle the animation
        rule.mainClock.advanceTimeBy(600)
        val finalRadius = dayNode.currentCornerRadius()
        assertEquals(CORNER_INNER_FLAT, finalRadius, 0.001f)

        // Midpoint shape exists and differs from both start and end shape (active morphing)
        assertNotNull("Expected active morphing frame", midRadius)
        assertTrue(midRadius != initialRadius)
        assertTrue(midRadius != finalRadius)
    }

    @Test
    fun reduced_motion_changes_shape_instantly() {
        var selectedIndex by mutableStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    ConnectedButtonGroup(
                        options = listOf("Day", "Week", "Month"),
                        selectedIndex = selectedIndex,
                        onSelect = { selectedIndex = it },
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()

        val dayNode = rule.onNodeWithText("Day")
        val initialRadius = dayNode.currentCornerRadius()
        assertEquals(CORNER_FULL_PILL, initialRadius, 0.001f)

        // Switch selection to "Week"
        selectedIndex = 1

        // Under reduced motion (snap motion scheme), the shape changes to settled 8.0 instantly without intermediate morphing
        var firstChangedRadius: Float? = null
        for (i in 1..10) {
            rule.mainClock.advanceTimeBy(16)
            val r = dayNode.currentCornerRadius()
            if (r != CORNER_FULL_PILL) {
                firstChangedRadius = r
                break
            }
        }
        assertEquals(CORNER_INNER_FLAT, firstChangedRadius ?: -1f, 0.001f)

        // Verify across remaining frames that it stays at settled 8.0 without any intermediate morph values
        repeat(10) {
            rule.mainClock.advanceTimeBy(16)
            assertEquals(CORNER_INNER_FLAT, dayNode.currentCornerRadius(), 0.001f)
        }
    }

    private fun SemanticsNodeInteraction.currentCornerRadius(): Float =
        fetchLayerShape().toRoundRect().topRightCornerRadius.x

    private fun SemanticsNodeInteraction.fetchLayerShape(): Shape {
        val node = fetchSemanticsNode()
        val layoutNodeField = node.javaClass.declaredFields.firstOrNull { it.name.contains("layoutNode") }
            ?.apply { isAccessible = true } ?: error("layoutNode field not found")
        val layoutNode = layoutNodeField.get(node) ?: error("layoutNode not found")
        val getModifierInfo = layoutNode.javaClass.getMethod("getModifierInfo")
        val infos = getModifierInfo.invoke(layoutNode) as List<*>
        for (info in infos) {
            val mod = info!!.javaClass.getMethod("getModifier").invoke(info)
            if (mod != null && mod.javaClass.name.contains("GraphicsLayerElement")) {
                val shapeField = mod.javaClass.declaredFields.firstOrNull { it.name == "shape" }
                    ?.apply { isAccessible = true }
                return shapeField?.get(mod) as? Shape ?: error("Shape field not found on GraphicsLayerElement")
            }
        }
        error("GraphicsLayerElement not found on node")
    }

    private fun Shape.toRoundRect(size: Size = Size(200f, 100f)): RoundRect {
        val outline = createOutline(size, LayoutDirection.Ltr, Density(1f))
        return (outline as Outline.Rounded).roundRect
    }
}
