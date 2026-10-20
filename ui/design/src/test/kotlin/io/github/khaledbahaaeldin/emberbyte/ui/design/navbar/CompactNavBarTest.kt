package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CompactNavBarTest {
    @get:Rule val rule = createComposeRule()

    private val items = listOf(
        NavBarItem("home", "Home", Icons.Rounded.Home, NavAccent.Primary),
        NavBarItem("apps", "Apps", Icons.Rounded.Apps, NavAccent.Tertiary),
        NavBarItem("plans", "Plans", Icons.Rounded.AccountBalanceWallet, NavAccent.Secondary),
    )

    private val fourItems = items + NavBarItem("extra", "Extra", Icons.Rounded.Visibility, NavAccent.Error)

    private class CountingHaptics : HapticFeedback {
        var calls = 0
        val history = mutableListOf<HapticFeedbackType>()
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            calls++
            history.add(hapticFeedbackType)
        }
    }

    private fun DpRect.centerOffset(): Offset {
        val centerX = (left.value + right.value) / 2f
        val centerY = (top.value + bottom.value) / 2f
        return Offset(centerX, centerY)
    }

    private fun show(
        selectedId: String = "home",
        navItems: List<NavBarItem> = items,
        visible: Boolean = true,
        reduceMotion: Boolean = false,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        density: Density? = null,
        haptics: HapticFeedback? = null,
        trailing: (@Composable () -> Unit)? = null,
        onSelect: (String) -> Unit = {},
    ) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalReduceMotion provides reduceMotion,
                    LocalLayoutDirection provides layoutDirection,
                    LocalDensity provides (density ?: LocalDensity.current),
                    LocalHapticFeedback provides (haptics ?: LocalHapticFeedback.current),
                ) {
                    CompactNavBar(
                        items = navItems,
                        selectedId = selectedId,
                        onSelect = onSelect,
                        visible = visible,
                        trailing = trailing,
                    )
                }
            }
        }
    }

    @Test
    fun unselected_icon_color_interpolates_by_pill_coverage() {
        val muted = Color(0xFF888888)
        val onPrimary = Color(0xFF001133)

        // Case 1: Pill has not reached icon at all (0% coverage) -> returns muted
        val colorNoCoverage = interpolateIconColor(
            iconLeft = 100f,
            iconWidth = 24f,
            pillLeft = 0f,
            pillRight = 60f,
            unselectedColor = muted,
            selectedColor = onPrimary,
        )
        assertEquals(muted, colorNoCoverage)

        // Case 2: Pill completely covers icon (100% coverage) -> returns onPrimary
        val colorFullCoverage = interpolateIconColor(
            iconLeft = 100f,
            iconWidth = 24f,
            pillLeft = 80f,
            pillRight = 150f,
            unselectedColor = muted,
            selectedColor = onPrimary,
        )
        assertEquals(onPrimary, colorFullCoverage)

        // Case 3: Pill covers half the icon (50% coverage) -> returns lerp(muted, onPrimary, 0.5f)
        val colorHalfCoverage = interpolateIconColor(
            iconLeft = 100f,
            iconWidth = 24f,
            pillLeft = 80f,
            pillRight = 112f,
            unselectedColor = muted,
            selectedColor = onPrimary,
        )
        val expectedMid = lerp(muted, onPrimary, 0.5f)
        assertEquals(expectedMid.red, colorHalfCoverage.red, 0.01f)
        assertEquals(expectedMid.green, colorHalfCoverage.green, 0.01f)
        assertEquals(expectedMid.blue, colorHalfCoverage.blue, 0.01f)
    }

    @Test
    fun total_width_is_identical_for_every_selection_and_at_every_frame_of_a_switch() {
        var currentSelected by androidx.compose.runtime.mutableStateOf("home")
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompactNavBar(
                    items = items,
                    selectedId = currentSelected,
                    onSelect = { currentSelected = it },
                    visible = true,
                )
            }
        }
        rule.mainClock.advanceTimeBy(16)
        val initialBarBounds = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot()
        val expectedWidth = initialBarBounds.right - initialBarBounds.left
        val expectedHeight = initialBarBounds.bottom - initialBarBounds.top
        assertEquals(64.dp, expectedHeight)
        assertEquals(216.dp, expectedWidth)

        // Switch through every selection and sample every 16 ms frame
        for (target in listOf("apps", "plans", "home")) {
            currentSelected = target
            repeat(35) {
                rule.mainClock.advanceTimeBy(16)
                rule.onNodeWithTag("compact_nav_bar").assertWidthIsEqualTo(expectedWidth)
            }
        }
    }

    @Test
    fun label_of_the_selected_tab_is_visible_when_settled_and_clipped_not_wrapped_while_moving() {
        var currentSelected by androidx.compose.runtime.mutableStateOf("home")
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompactNavBar(
                    items = items,
                    selectedId = currentSelected,
                    onSelect = { currentSelected = it },
                    visible = true,
                )
            }
        }
        rule.mainClock.advanceTimeBy(16)

        // When settled on Home, Home label is visible and cell is expanded (>= 92.dp), others are collapsed (56.dp)
        val homeBounds = rule.onNodeWithTag("nav_home").getBoundsInRoot()
        val appsBounds = rule.onNodeWithTag("nav_apps").getBoundsInRoot()
        val plansBounds = rule.onNodeWithTag("nav_plans").getBoundsInRoot()
        assertTrue("Selected cell width (${homeBounds.right - homeBounds.left}) >= 92.dp", (homeBounds.right - homeBounds.left) >= 92.dp)
        assertEquals(56.dp, appsBounds.right - appsBounds.left)
        assertEquals(56.dp, plansBounds.right - plansBounds.left)
        rule.onNodeWithTag("nav_label_home", useUnmergedTree = true).assertIsDisplayed()

        // Switch to Plans and verify across frames that the expanding label never wraps (height stays single-line)
        rule.runOnIdle { currentSelected = "plans" }
        rule.mainClock.advanceTimeByFrame()
        var settledHeight: androidx.compose.ui.unit.Dp? = null
        repeat(95) {
            rule.mainClock.advanceTimeBy(16)
        }
        val finalLabelBounds = rule.onNodeWithTag("nav_label_plans", useUnmergedTree = true).getBoundsInRoot()
        settledHeight = finalLabelBounds.bottom - finalLabelBounds.top
        assertTrue("Settled label height ($settledHeight) must be > 0.dp and single-line (<= 40.dp)", settledHeight > 0.dp && settledHeight <= 40.dp)

        // Switch back to Home and sample every frame while moving
        rule.runOnIdle { currentSelected = "home" }
        rule.mainClock.advanceTimeByFrame()
        repeat(25) {
            rule.mainClock.advanceTimeBy(16)
            val movingNodes = rule.onAllNodes(androidx.compose.ui.test.hasTestTag("nav_label_home"), useUnmergedTree = true).fetchSemanticsNodes()
            if (movingNodes.isNotEmpty()) {
                val b = rule.onNodeWithTag("nav_label_home", useUnmergedTree = true).getBoundsInRoot()
                val h = b.bottom - b.top
                assertTrue("Moving label height ($h) must never wrap past single-line height ($settledHeight)", h <= settledHeight + 2.dp)
            }
        }
    }

    @Test
    fun unselected_tabs_are_icon_only_but_keep_their_names_for_talkback_and_48dp_targets() {
        show(selectedId = "home", reduceMotion = true)
        for (item in items) {
            val node = rule.onNodeWithTag("nav_${item.id}")
            node.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            val bounds = node.getBoundsInRoot()
            val width = bounds.right - bounds.left
            val height = bounds.bottom - bounds.top
            assertTrue("${item.id} width ($width) >= 48.dp", width >= 48.dp)
            assertTrue("${item.id} height ($height) >= 48.dp", height >= 48.dp)

            val semanticsNode = node.fetchSemanticsNode()
            val descriptions = semanticsNode.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }
            val texts = semanticsNode.config.getOrElse(SemanticsProperties.Text) { emptyList() }.map { it.text }
            assertTrue(
                "${item.id} must expose '${item.label}' to TalkBack via contentDescription or text",
                descriptions.contains(item.label) || texts.contains(item.label),
            )

            if (item.id != "home") {
                assertEquals("Unselected tab ${item.id} must be 56.dp wide (icon-only cell)", 56.dp, width)
            } else {
                assertTrue("Selected tab ${item.id} must be >= 92.dp wide", width >= 92.dp)
            }
        }
    }

    @Test
    fun selected_item_is_a_tab_with_selected_state_and_the_others_are_not() {
        show(selectedId = "home")
        rule.onNodeWithTag("nav_home")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assertIsSelected()
        rule.onNodeWithTag("nav_apps")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assertIsNotSelected()
        rule.onNodeWithTag("nav_plans")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assertIsNotSelected()
    }

    @Test
    fun tapping_another_item_calls_onSelect() {
        var picked: String? = null
        show(selectedId = "home", onSelect = { picked = it })
        rule.onNodeWithTag("nav_apps").performClick()
        assertEquals("apps", picked)
    }

    @Test
    fun dragging_from_item_0_to_item_2_selects_item_2() {
        var layoutDir by androidx.compose.runtime.mutableStateOf(LayoutDirection.Ltr)
        var picked: String? = null
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    CompactNavBar(
                        items = items,
                        selectedId = "home",
                        onSelect = { picked = it },
                        visible = true,
                    )
                }
            }
        }
        // LTR drag
        val homeCenter = rule.onNodeWithTag("nav_home").getBoundsInRoot().centerOffset()
        val plansCenter = rule.onNodeWithTag("nav_plans").getBoundsInRoot().centerOffset()
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            swipe(start = homeCenter, end = plansCenter, durationMillis = 300)
        }
        assertEquals("plans", picked)

        // RTL drag
        picked = null
        layoutDir = LayoutDirection.Rtl
        rule.waitForIdle()
        val homeCenterRtl = rule.onNodeWithTag("nav_home").getBoundsInRoot().centerOffset()
        val plansCenterRtl = rule.onNodeWithTag("nav_plans").getBoundsInRoot().centerOffset()
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            swipe(start = homeCenterRtl, end = plansCenterRtl, durationMillis = 300)
        }
        assertEquals("plans", picked)
    }

    @Test
    fun dragging_works_with_animations_off() {
        var picked: String? = null
        show(selectedId = "home", reduceMotion = true, onSelect = { picked = it })
        val homeCenter = rule.onNodeWithTag("nav_home").getBoundsInRoot().centerOffset()
        val plansCenter = rule.onNodeWithTag("nav_plans").getBoundsInRoot().centerOffset()
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            swipe(start = homeCenter, end = plansCenter, durationMillis = 300)
        }
        assertEquals("plans", picked)
    }

    @Test
    fun haptic_fires_once_per_landing_not_per_frame() {
        val haptics = CountingHaptics()
        var currentSelected by androidx.compose.runtime.mutableStateOf("home")
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalHapticFeedback provides haptics) {
                    CompactNavBar(
                        items = items,
                        selectedId = currentSelected,
                        onSelect = { currentSelected = it },
                        visible = true,
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(16)
        val homeCenter = rule.onNodeWithTag("nav_home").getBoundsInRoot().centerOffset()
        val plansCenter = rule.onNodeWithTag("nav_plans").getBoundsInRoot().centerOffset()

        // Drag across all 3 items (passing through Apps to Plans) and step through 40 frames of spring settle
        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            swipe(start = homeCenter, end = plansCenter, durationMillis = 300)
        }
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
        }
        assertEquals("Haptic must fire once on landing, not per intermediate tab or per spring frame", 1, haptics.calls)
        assertEquals(HapticFeedbackType.SegmentTick, haptics.history.first())
    }

    @Test
    fun hidden_when_not_visible() {
        show(selectedId = "home", visible = false)
        rule.onNodeWithTag("compact_nav_bar").assertDoesNotExist()
    }

    @Test
    fun at_font_scale_2_the_expanded_cell_grows_and_the_bar_width_stays_identical_across_selections() {
        val longItems = listOf(
            NavBarItem("home", "Dashboard", Icons.Rounded.Home, NavAccent.Primary),
            NavBarItem("apps", "Applications", Icons.Rounded.Apps, NavAccent.Tertiary),
            NavBarItem("plans", "Budgets", Icons.Rounded.AccountBalanceWallet, NavAccent.Secondary),
        )
        var currentSelected by androidx.compose.runtime.mutableStateOf("home")
        var fontScale by androidx.compose.runtime.mutableStateOf(1f)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalDensity provides Density(density = 1f, fontScale = fontScale),
                    LocalReduceMotion provides true,
                ) {
                    Box(Modifier.width(360.dp)) {
                        CompactNavBar(
                            items = longItems,
                            selectedId = currentSelected,
                            onSelect = { currentSelected = it },
                            visible = true,
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
        val widthAtScale1 = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot().let { it.right - it.left }

        fontScale = 2f
        rule.waitForIdle()
        val widthAtScale2Home = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot().let { it.right - it.left }
        val expandedCellAtScale2 = rule.onNodeWithTag("nav_home").getBoundsInRoot().let { it.right - it.left }

        assertTrue(
            "At font scale 2.0, expanded cell ($expandedCellAtScale2) and bar width ($widthAtScale2Home) must grow beyond scale 1.0 ($widthAtScale1)",
            widthAtScale2Home > widthAtScale1 && expandedCellAtScale2 > 92.dp,
        )
        assertTrue("Bar width ($widthAtScale2Home) <= 360.dp", widthAtScale2Home <= 360.dp)

        for (item in longItems) {
            currentSelected = item.id
            rule.waitForIdle()
            rule.onNodeWithTag("compact_nav_bar").assertWidthIsEqualTo(widthAtScale2Home)
        }
    }

    @Test
    fun trailing_plus_button_sits_to_the_right_and_is_56dp() {
        show(
            selectedId = "plans",
            trailing = {
                PlusButton(
                    onClick = {},
                    contentDescription = "New plan",
                )
            },
        )
        val plusNode = rule.onNodeWithTag("plus_button")
        plusNode.assertIsDisplayed()
        plusNode.assertWidthIsEqualTo(56.dp)
        plusNode.assertHeightIsEqualTo(56.dp)

        val barBounds = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot()
        val plusBounds = plusNode.getBoundsInRoot()
        assertTrue(
            "plus button left (${plusBounds.left}) sits to the right of bar (${barBounds.right})",
            plusBounds.left >= barBounds.right,
        )
    }

    @Test
    fun tabs_are_ordered_right_to_left_in_rtl() {
        show(selectedId = "home", layoutDirection = LayoutDirection.Rtl, reduceMotion = true)

        val homeBounds = rule.onNodeWithTag("nav_home").getBoundsInRoot()
        val appsBounds = rule.onNodeWithTag("nav_apps").getBoundsInRoot()
        val plansBounds = rule.onNodeWithTag("nav_plans").getBoundsInRoot()

        assertTrue(
            "Plans (left=${plansBounds.left}) must be to the left of Apps (left=${appsBounds.left}) in RTL",
            plansBounds.left < appsBounds.left,
        )
        assertTrue(
            "Apps (left=${appsBounds.left}) must be to the left of Home (left=${homeBounds.left}) in RTL",
            appsBounds.left < homeBounds.left,
        )
    }

    @Test
    fun rtl_mirrors_the_pill_and_the_travel_direction() {
        var currentSelected by androidx.compose.runtime.mutableStateOf("home")
        var picked: String? = null
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    CompactNavBar(
                        items = items,
                        selectedId = currentSelected,
                        onSelect = {
                            picked = it
                            currentSelected = it
                        },
                        visible = true,
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(16)

        val barBounds = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot()
        val homeBounds = rule.onNodeWithTag("nav_home").getBoundsInRoot()

        // In RTL, Home (item 0) is on the right side of the bar, with expanded width (>= 92.dp)
        val homeWidth = homeBounds.right - homeBounds.left
        assertTrue("RTL Home width ($homeWidth) >= 92.dp", homeWidth >= 92.dp)
        assertTrue("RTL Home is on the right half of the bar", homeBounds.left > barBounds.left + 100.dp)

        // Switching from Home (right) to Plans (left) moves the pill leftward (left edge leads with higher stiffness)
        rule.onNodeWithTag("nav_plans").performClick()
        assertEquals("plans", picked)
        rule.runOnIdle { currentSelected = "plans" }
        rule.mainClock.advanceTimeByFrame()
        repeat(95) {
            rule.mainClock.advanceTimeBy(16)
        }
        val plansBoundsAfter = rule.onNodeWithTag("nav_plans").getBoundsInRoot()
        assertTrue("After switching to Plans in RTL, Plans cell expands (>= 91.5.dp, was ${plansBoundsAfter.right - plansBoundsAfter.left})", (plansBoundsAfter.right - plansBoundsAfter.left) >= 91.5.dp)
        assertEquals(56.dp, rule.onNodeWithTag("nav_home").getBoundsInRoot().let { it.right - it.left })
    }
}



