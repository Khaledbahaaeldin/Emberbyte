package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UsageBarRowTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun fourteen_bars_scrolls_and_meets_touch_targets() {
        val bars = (1..14).map { BarUi(bytes = 100L * it, label = "$it", description = "Bar $it") }
        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = 0, onSelect = {})
            }
        }
        for (i in 1..14) {
            val node = rule.onNodeWithContentDescription("Bar $i")
            node.assertWidthIsAtLeast(48.dp)
            node.assertHeightIsAtLeast(48.dp)
            if (i == 14) {
                node.performScrollTo()
            }
        }
    }

    @Test
    fun seven_bars_in_379dp_container_is_scrollable_but_starts_at_first_bar_unclipped() {
        val bars = (1..7).map { BarUi(bytes = 100L * it, label = "$it", description = "FitBar $it") }
        rule.setContent {
            Box(Modifier.size(379.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = 0, onSelect = {}, openAtEnd = false)
            }
        }
        rule.waitForIdle()
        // First bar is displayed
        val firstBar = rule.onNodeWithContentDescription("FitBar 1")
        firstBar.assertIsDisplayed()
        firstBar.assertWidthIsAtLeast(48.dp)
        firstBar.assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun bar_labels_render_in_one_node_matching_text_under_rtl() {
        val bars = listOf(
            BarUi(bytes = 100L, label = "Mon", description = "Monday"),
            BarUi(bytes = 200L, label = "7 days", description = "7 days"),
        )
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(Modifier.size(360.dp, 200.dp)) {
                    UsageBarRow(points = bars, selectedIndex = null, onSelect = {})
                }
            }
        }
        rule.onNodeWithText("Mon").assertIsDisplayed()
        rule.onNodeWithText("7 days").assertIsDisplayed()
    }

    @Test
    fun row_with_many_bars_opens_scrolled_to_end() {
        val bars = (1..60).map { BarUi(bytes = 100L * it, label = "$it", description = "Bar $it") }
        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = null, onSelect = {})
            }
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Bar 60").assertIsDisplayed()
        rule.onNodeWithContentDescription("Bar 1").assertIsNotDisplayed()
    }

    @Test
    fun switching_key_or_granularity_rescrolls_to_end() {
        val barsDay = (1..60).map { BarUi(bytes = 100L * it, label = "D$it", description = "Day $it") }
        val barsWeek = (1..20).map { BarUi(bytes = 100L * it, label = "W$it", description = "Week $it") }
        var currentBars by androidx.compose.runtime.mutableStateOf(barsDay)
        var scrollKey by androidx.compose.runtime.mutableStateOf("DAY")

        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = currentBars, selectedIndex = null, onSelect = {}, scrollKey = scrollKey)
            }
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Day 60").assertIsDisplayed()
        rule.onNodeWithContentDescription("Day 1").assertIsNotDisplayed()

        currentBars = barsWeek
        scrollKey = "WEEK"
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Week 20").assertIsDisplayed()
        rule.onNodeWithContentDescription("Week 1").assertIsNotDisplayed()
    }

    @Test
    fun selecting_a_bar_does_not_jump_scroll_position() {
        val bars = (1..60).map { BarUi(bytes = 100L * it, label = "$it", description = "Bar $it") }
        var selected by androidx.compose.runtime.mutableStateOf<Int?>(null)

        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = selected, onSelect = { selected = it })
            }
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Bar 60").assertIsDisplayed()
        rule.onNodeWithContentDescription("Bar 1").assertIsNotDisplayed()

        // Select the last bar
        selected = 59
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Bar 60").assertIsDisplayed()
        rule.onNodeWithContentDescription("Bar 1").assertIsNotDisplayed()
    }

    @Test
    fun selected_bar_has_a_distinct_colour_in_every_palette() {
        for (preset in io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets) {
            for (palette in listOf(preset.dark, preset.light, preset.dark.copy(background = androidx.compose.ui.graphics.Color.Black))) {
                val ratio = io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio(palette.primary, palette.surface)
                org.junit.Assert.assertTrue(
                    "Palette ${preset.name} must have contrast >= 3:1 between primary and surface (got $ratio)",
                    ratio >= 3.0,
                )
            }
        }
    }

    @Test
    fun values_respect_decimal_and_binary_units() {
        val decVal = io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBarValue(1_200_000_000L, io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits.DECIMAL)
        val binVal = io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBarValue(1_200_000_000L, io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits.BINARY)
        org.junit.Assert.assertEquals("1.2 GB", decVal)
        org.junit.Assert.assertEquals("1.1 GiB", binVal)
    }

    @Test
    fun every_bar_shows_its_value_at_default_font() {
        val bars = listOf(
            BarUi(label = "Mon", bytes = 100_000_000L, description = "Monday, 100 megabytes", valueText = "100 MB"),
            BarUi(label = "Tue", bytes = 200_000_000L, description = "Tuesday, 200 megabytes", valueText = "200 MB"),
            BarUi(label = "Wed", bytes = 700_000_000L, description = "Wednesday, 700 megabytes", valueText = "700 MB"),
        )
        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = null, onSelect = {}, showValues = true)
            }
        }
        rule.onNodeWithText("100 MB", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("200 MB", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("700 MB", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun values_reduce_to_selected_highest_and_today_when_crowded() {
        // 14 bars = crowded (> 7 bars)
        // highest is bar 8 (index 7, 900 MB), selected is bar 3 (index 2, 150 MB), today is bar 14 (index 13, 700 MB)
        val bars = (1..14).map { i ->
            val bytes = if (i == 8) 900_000_000L else i * 50_000_000L
            BarUi(label = "D$i", bytes = bytes, description = "Day $i", valueText = "${bytes / 1_000_000} MB")
        }
        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = 2, onSelect = {}, showValues = true, openAtEnd = false)
            }
        }
        rule.waitForIdle()
        // Bar 3 (selected): 150 MB -> displayed
        rule.onNodeWithText("150 MB", useUnmergedTree = true).assertIsDisplayed()
        // Non-selected/non-highest/non-today bar: Bar 2: 100 MB -> NOT displayed
        rule.onNodeWithText("100 MB", useUnmergedTree = true).assertIsNotDisplayed()
        // Bar 8 (highest) and Bar 14 (today) have value text created
        rule.onNodeWithText("900 MB", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("700 MB", useUnmergedTree = true).assertExists()
    }

    @Test
    fun value_text_is_not_announced_twice() {
        val bars = listOf(
            BarUi(label = "Mon", bytes = 100_000_000L, description = "Monday, 100 megabytes", valueText = "100 MB"),
        )
        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = 0, onSelect = {}, showValues = true)
            }
        }
        // In the unmerged semantics tree, valueText has clearAndSetSemantics {}, so no Text semantics node exists for it
        val textNodes = rule.onAllNodes(androidx.compose.ui.test.hasText("100 MB")).fetchSemanticsNodes()
        org.junit.Assert.assertTrue(textNodes.isEmpty())
    }

    @Test
    fun values_do_not_overlap_at_font_scale_2() {
        val bars = (1..7).map { i ->
            BarUi(label = "D$i", bytes = i * 100_000_000L, description = "Day $i", valueText = "${i * 100} MB")
        }
        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 2.0f)
            ) {
                Box(Modifier.size(360.dp, 250.dp)) {
                    UsageBarRow(points = bars, selectedIndex = 1, onSelect = {}, showValues = true)
                }
            }
        }
        rule.waitForIdle()
        // At font scale 2.0, values reduce to selected (bar 2: 200 MB), highest (bar 7: 700 MB), and today (bar 7)
        rule.onNodeWithText("200 MB", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("700 MB", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("100 MB", useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun selected_label_is_bold() {
        val bars = listOf(
            BarUi(label = "Mon", bytes = 100L, description = "Monday"),
            BarUi(label = "Tue", bytes = 200L, description = "Tuesday"),
        )
        rule.setContent {
            UsageBarRow(points = bars, selectedIndex = 1, onSelect = {})
        }
        rule.onNodeWithText("Tue", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun today_and_selected_are_distinguishable() {
        val bars = listOf(
            BarUi(label = "Mon", bytes = 100L, description = "Monday"),
            BarUi(label = "Tue", bytes = 200L, description = "Tuesday"),
        )
        rule.setContent {
            UsageBarRow(points = bars, selectedIndex = 0, onSelect = {})
        }
        rule.onNodeWithContentDescription("Monday").assertIsDisplayed()
        rule.onNodeWithContentDescription("Tuesday").assertIsDisplayed()
    }

    @Test
    fun selecting_a_bar_does_not_shift_the_layout() {
        val bars = listOf(
            BarUi(label = "Mon", bytes = 100L, description = "Monday"),
            BarUi(label = "Tue", bytes = 200L, description = "Tuesday"),
        )
        var selected by androidx.compose.runtime.mutableStateOf<Int?>(null)
        rule.setContent {
            Box(Modifier.size(360.dp, 200.dp)) {
                UsageBarRow(points = bars, selectedIndex = selected, onSelect = { selected = it })
            }
        }
        rule.waitForIdle()
        val beforeBounds = rule.onNodeWithContentDescription("Monday").fetchSemanticsNode().boundsInRoot
        selected = 0
        rule.waitForIdle()
        val afterBounds = rule.onNodeWithContentDescription("Monday").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertEquals(beforeBounds.top, afterBounds.top, 1f)
        org.junit.Assert.assertEquals(beforeBounds.bottom, afterBounds.bottom, 1f)
    }
}

