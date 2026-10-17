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
    fun total_width_is_identical_for_every_selected_item() {
        var currentItems by androidx.compose.runtime.mutableStateOf(items)
        var currentSelected by androidx.compose.runtime.mutableStateOf("home")
        val expected3ItemsWidth = (68 * items.size + 20).dp // 224.dp
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompactNavBar(
                    items = currentItems,
                    selectedId = currentSelected,
                    onSelect = {},
                    visible = true,
                )
            }
        }
        for (item in items) {
            currentSelected = item.id
            rule.waitForIdle()
            rule.onNodeWithTag("compact_nav_bar").assertWidthIsEqualTo(expected3ItemsWidth)
        }

        currentItems = fourItems
        rule.waitForIdle()
        val expected4ItemsWidth = (68 * fourItems.size + 20).dp // 292.dp
        rule.onNodeWithTag("compact_nav_bar").assertWidthIsEqualTo(expected4ItemsWidth)
    }

    @Test
    fun each_slot_is_68dp_wide_and_at_least_48dp_tall() {
        show(selectedId = "home")
        for (item in items) {
            val node = rule.onNodeWithTag("nav_${item.id}")
            node.assertWidthIsEqualTo(68.dp)
            val bounds = node.getBoundsInRoot()
            val height = bounds.bottom - bounds.top
            assertTrue("${item.id} height ($height) >= 48.dp", height >= 48.dp)
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
    fun haptic_tick_fires_once_per_snap() {
        val haptics = CountingHaptics()
        show(selectedId = "home", haptics = haptics)
        val homeCenter = rule.onNodeWithTag("nav_home").getBoundsInRoot().centerOffset()
        val appsCenter = rule.onNodeWithTag("nav_apps").getBoundsInRoot().centerOffset()

        rule.onNodeWithTag("compact_nav_bar").performTouchInput {
            swipe(start = homeCenter, end = appsCenter, durationMillis = 300)
        }
        rule.waitForIdle()
        assertEquals(1, haptics.calls)
        assertEquals(HapticFeedbackType.SegmentTick, haptics.history.first())
    }

    @Test
    fun hidden_when_not_visible() {
        show(selectedId = "home", visible = false)
        rule.onNodeWithTag("compact_nav_bar").assertDoesNotExist()
    }

    @Test
    fun at_font_scale_2_labels_stay_one_line_and_the_bar_does_not_overflow_a_360dp_screen() {
        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2f, fontScale = 2f),
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.width(360.dp)) {
                        CompactNavBar(
                            items = items,
                            selectedId = "home",
                            onSelect = {},
                            visible = true,
                        )
                    }
                }
            }
        }
        val barBounds = rule.onNodeWithTag("compact_nav_bar").getBoundsInRoot()
        val barWidth = barBounds.right - barBounds.left
        assertTrue("bar width ($barWidth) <= 360.dp", barWidth <= 360.dp)
        for (item in items) {
            rule.onNodeWithText(item.label).assertIsDisplayed()
            val itemBounds = rule.onNodeWithTag("nav_${item.id}").getBoundsInRoot()
            assertTrue("${item.id} within bar left", itemBounds.left >= barBounds.left)
            assertTrue("${item.id} within bar right", itemBounds.right <= barBounds.right)
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
}
