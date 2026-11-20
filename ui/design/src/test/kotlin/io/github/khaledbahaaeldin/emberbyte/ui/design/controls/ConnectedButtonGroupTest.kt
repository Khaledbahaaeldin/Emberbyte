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
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
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

    // ---- T3: width-aware layout -------------------------------------------------------------------------

    private val periodOptions = listOf("Today", "7 days", "30 days", "Cycle")

    private fun showGroup(
        options: List<String>,
        widthDp: Int,
        fontScale: Float = 1f,
        selectedIndex: Int = 0,
        rtl: Boolean = false,
    ) {
        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = fontScale),
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.width(widthDp.dp)) {
                        ConnectedButtonGroup(
                            options = options,
                            selectedIndex = selectedIndex,
                            onSelect = {},
                            modifier = Modifier.fillMaxWidth().testTag("group"),
                        )
                    }
                }
            }
        }
    }

    private fun segments() = rule.onAllNodes(
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
    )

    private fun lineCountOf(text: String): Int {
        val results = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode()
            .config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
        return results.firstOrNull()?.lineCount ?: -1
    }

    @Test
    fun labels_are_never_broken_mid_word_at_font_scale_2() {
        showGroup(periodOptions, widthDp = 328, fontScale = 2.0f)
        for (option in periodOptions) {
            assertEquals("'$option' must stay on one line", 1, lineCountOf(option))
        }
    }

    @Test
    fun labels_are_never_broken_mid_word_at_font_scale_1_3_on_a_phone() {
        showGroup(periodOptions, widthDp = 328, fontScale = 1.3f)
        for (option in periodOptions) {
            assertEquals("'$option' must stay on one line", 1, lineCountOf(option))
        }
    }

    @Test
    fun selected_pill_is_never_narrower_than_its_label() {
        showGroup(periodOptions, widthDp = 328, fontScale = 2.0f, selectedIndex = 0)
        val selected = rule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton) and
                SemanticsMatcher.expectValue(SemanticsProperties.Selected, true),
        ).getUnclippedBoundsInRoot()
        val label = rule.onNodeWithText("Today", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(
            "selected width ${selected.right - selected.left} must hold the label ${label.right - label.left}",
            (selected.right - selected.left) >= (label.right - label.left),
        )
        // And it must stay a pill, not collapse into a circle: wider than it is tall.
        assertTrue((selected.right - selected.left) > (selected.bottom - selected.top))
    }

    @Test
    fun segments_in_a_row_have_equal_height() {
        showGroup(periodOptions, widthDp = 328, fontScale = 2.0f)
        val nodes = segments().fetchSemanticsNodes()
        assertEquals(4, nodes.size)
        val byTop = nodes.groupBy { it.boundsInRoot.top.toInt() }
        for ((top, row) in byTop) {
            val heights = row.map { (it.boundsInRoot.bottom - it.boundsInRoot.top).toInt() }.toSet()
            assertEquals("row at $top has uneven heights $heights", 1, heights.size)
        }
    }

    @Test
    fun four_segments_wrap_into_rows_that_fit_when_they_do_not_fit_one_row() {
        // Robolectric measures text narrower than a device, so use a narrow container; the exact 2 + 2 split is
        // pinned by the pure packRows tests.
        showGroup(periodOptions, widthDp = 160, fontScale = 2.0f)
        val nodes = segments().fetchSemanticsNodes()
        val tops = nodes.map { it.boundsInRoot.top.toInt() }.toSet()
        assertTrue("expected more than one row, saw tops $tops", tops.size >= 2)
        nodes.forEach { assertTrue("segment must fit inside 160dp: ${it.boundsInRoot}", it.boundsInRoot.right <= 160.5f) }
    }

    @Test
    fun four_short_segments_stay_on_one_row_at_default_font() {
        showGroup(periodOptions, widthDp = 328, fontScale = 1.0f)
        val tops = segments().fetchSemanticsNodes().map { it.boundsInRoot.top.toInt() }.toSet()
        assertEquals("expected one row, saw tops $tops", 1, tops.size)
    }

    @Test
    fun segments_are_weighted_by_label_width() {
        showGroup(listOf("A", "A much longer label"), widthDp = 320, fontScale = 1.0f)
        val nodes = segments().fetchSemanticsNodes()
        val first = nodes[0].boundsInRoot.let { it.right - it.left }
        val second = nodes[1].boundsInRoot.let { it.right - it.left }
        assertTrue("second ($second) should be wider than first ($first)", second > first * 1.15f)
    }

    @Test
    fun check_icon_is_dropped_above_font_scale_1_3() {
        showGroup(periodOptions, widthDp = 328, fontScale = 1.5f)
        rule.onAllNodes(hasTestTag("connected_button_group_check_icon"), useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun check_icon_is_shown_at_default_font() {
        showGroup(periodOptions, widthDp = 328, fontScale = 1.0f)
        rule.onAllNodes(hasTestTag("connected_button_group_check_icon"), useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun rtl_wrapped_rows_keep_the_first_option_on_the_right() {
        showGroup(periodOptions, widthDp = 328, fontScale = 2.0f, rtl = true)
        val first = rule.onNodeWithText("Today").getUnclippedBoundsInRoot()
        val second = rule.onNodeWithText("7 days").getUnclippedBoundsInRoot()
        assertTrue("In RTL 'Today' (${first.left}) must be right of '7 days' (${second.left})", first.left > second.left)
    }

    @Test
    fun a_label_wider_than_the_whole_group_is_ellipsized_not_wrapped() {
        showGroup(listOf("An extremely long single label that cannot fit", "B"), widthDp = 200, fontScale = 1.0f)
        assertEquals(1, lineCountOf("An extremely long single label that cannot fit"))
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
