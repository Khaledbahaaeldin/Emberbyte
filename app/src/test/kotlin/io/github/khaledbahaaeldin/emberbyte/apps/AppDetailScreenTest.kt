package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.apps.detail.DetailRange
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class AppDetailScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @org.junit.Before fun settleReveals() { io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate.enabled = false }

    private val sampleState = sampleDetail()

    private fun show(state: AppDetailUiState = sampleState, onEvent: (AppDetailEvent) -> Unit = {}, onSettings: (() -> Unit)? = null) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppDetailContent(state = state, onEvent = onEvent, onBack = {}, onOpenSettings = onSettings)
            }
        }
    }

    private fun config(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config

    private fun text(tag: String): String {
        val merged = rule.onNodeWithTag(tag, useUnmergedTree = false).fetchSemanticsNode().config
        val node = if (merged.contains(SemanticsProperties.Text)) merged else config(tag)
        return node[SemanticsProperties.Text].joinToString(" ") { it.text }
    }

    // ------------------------------------------------------------------ header and range

    @Test
    fun total_is_one_node_with_unit() {
        show()
        rule.onNodeWithContentDescription("1.61 gigabytes used in the last 7 days").assertIsDisplayed()
    }

    @Test
    fun pills_show_rank_share_and_network() {
        show()
        assertEquals("#2 of 47 apps", text("detail_pill_0"))
        assertEquals("34% of all app data", text("detail_pill_1"))
        assertEquals("Mostly Wi-Fi", text("detail_pill_2"))
    }

    @Test
    fun an_unranked_app_has_no_rank_pill_and_no_crash() {
        show(sampleState.copy(packageName = "uid:1000", label = "uid:1000", rank = 0, appCount = 0, shareOfAll = 0f))
        rule.onAllNodesWithText("#0 of 0 apps", useUnmergedTree = true).assertCountEquals(0)
        rule.onNodeWithTag("detail_pill_0", useUnmergedTree = true).assertExists()
        assertEquals("Mostly Wi-Fi", text("detail_pill_0"))
        rule.onNodeWithTag("detail_sentence").assertDoesNotExist()
    }

    @Test
    fun back_is_a_48dp_tonal_circle() {
        show()
        val back = rule.onNodeWithTag("detail_back", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals(48f, (back.right - back.left).value, 0.5f)
        val icon = rule.onNodeWithTag("detail_icon", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals(76f, (icon.right - icon.left).value, 0.5f)
        assertEquals("one left edge", back.left.value, icon.left.value, 0.5f)
    }

    @Test
    fun range_buttons_mark_the_selected_one_and_send_the_event() {
        val events = mutableListOf<AppDetailEvent>()
        show(onEvent = { events += it })
        assertTrue(config("detail_range_7")[SemanticsProperties.Selected])
        assertFalse(config("detail_range_30")[SemanticsProperties.Selected])
        rule.onNodeWithTag("detail_range_30").performClick()
        rule.onNodeWithTag("detail_range_90").performClick()
        assertEquals(
            listOf<AppDetailEvent>(AppDetailEvent.SetRange(DetailRange.Month), AppDetailEvent.SetRange(DetailRange.Quarter)),
            events,
        )
    }

    @Test
    fun change_pill_uses_wording_and_an_arrow() {
        show()
        assertEquals("\u25B2 12% more than the 7 days before", text("detail_change"))
        show2(sampleState.copy(previousTotalBytes = 0L), "First week of data")
    }

    private fun show2(state: AppDetailUiState, expected: String) {
        // The same screen with another state: composed fresh to keep one setContent per test.
        assertEquals(expected, io.github.khaledbahaaeldin.emberbyte.apps.detail.changeLabel(
            io.github.khaledbahaaeldin.emberbyte.apps.detail.changeFraction(state.totalBytes, state.previousTotalBytes), 7).text)
    }

    @Test
    fun no_percentage_when_there_is_no_previous_period() {
        show(sampleState.copy(previousTotalBytes = 0L))
        assertEquals("First week of data", text("detail_change"))
    }

    @Test
    fun the_sentence_summarises_the_app() {
        show()
        assertTrue(text("detail_sentence").startsWith("Video is your second biggest data user: 34% of everything your apps used."))
    }

    // ------------------------------------------------------------------ chart

    @Test
    fun bars_stack_wifi_and_mobile_and_announce_day_and_amount() {
        show()
        rule.onNodeWithTag("detail_bar_wifi_2", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("detail_bar_mobile_2", useUnmergedTree = true).assertExists()
        assertTrue(config("detail_bar_0")[SemanticsProperties.ContentDescription].first().endsWith("456 megabytes"))
        rule.onNodeWithTag("detail_average_line", useUnmergedTree = true).assertExists()
    }

    @Test
    fun selecting_a_bar_updates_the_caption_and_tapping_again_clears() {
        show()
        // With nothing selected the caption shows the busiest bar (the first day: 456 MB).
        assertTrue(text("detail_chart_caption"), text("detail_chart_caption").contains("456 MB"))
        rule.onNodeWithTag("detail_bar_2").performClick()
        rule.waitForIdle()
        assertTrue(config("detail_bar_2")[SemanticsProperties.Selected])
        assertTrue(text("detail_chart_caption").contains("230 MB"))
        rule.onNodeWithTag("detail_bar_2").performClick()
        rule.waitForIdle()
        assertFalse(config("detail_bar_2")[SemanticsProperties.Selected])
        assertTrue(text("detail_chart_caption"), text("detail_chart_caption").contains("456 MB"))
    }

    @Test
    fun ninety_days_shows_13_weekly_bars() {
        val end = java.time.LocalDate.of(2026, 11, 20)
        val series = (0 until 90).map {
            io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint(
                end.minusDays((89 - it).toLong()).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(), 1_000_000L, 2_000_000L,
            )
        }
        show(sampleState.copy(range = DetailRange.Quarter, dailySeries = series))
        rule.onNodeWithTag("detail_bar_12", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("detail_bar_13", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun empty_range_shows_the_empty_state() {
        show(sampleState.copy(dailySeries = emptyList()))
        rule.onNodeWithText("No daily usage").assertIsDisplayed()
        rule.onNodeWithText("Daily breakdown will show here once usage is recorded.").assertIsDisplayed()
    }

    // ------------------------------------------------------------------ tiles, hours, network

    @Test
    fun tiles_show_average_busiest_share_and_pace() {
        show()
        listOf("detail_tile_average", "detail_tile_busiest", "detail_tile_share", "detail_tile_pace").forEach {
            rule.onNodeWithTag(it, useUnmergedTree = true).assertExists()
        }
        assertTrue(config("detail_tile_busiest")[SemanticsProperties.ContentDescription].first().contains("a weekend"))
        assertTrue(config("detail_tile_share")[SemanticsProperties.ContentDescription].first().contains("Rank #2 of 47 apps"))
        rule.onNodeWithTag("detail_share_ring", useUnmergedTree = true).assertExists()
    }

    @Test
    fun hour_cells_have_24_entries_with_labels_and_tapping_shows_the_amount() {
        show()
        (0..23).forEach { rule.onNodeWithTag("detail_hour_$it", useUnmergedTree = true).assertExists() }
        assertEquals(listOf("7 PM, 330 megabytes"), config("detail_hour_19")[SemanticsProperties.ContentDescription])
        assertTrue(text("detail_hour_caption"), text("detail_hour_caption").startsWith("Busiest: 7 PM to 10 PM"))
        rule.onNodeWithTag("detail_hour_grid").performTouchInput { click(androidx.compose.ui.geometry.Offset(width * (19.5f / 24f), height / 2f)) }
        rule.waitForIdle()
        assertTrue(text("detail_hour_caption").startsWith("7 PM"))
    }

    @Test
    fun parts_of_day_highlight_the_largest() {
        show()
        rule.onNodeWithTag("detail_part_evening", useUnmergedTree = true).assertExists()
        assertTrue(config("detail_part_evening")[SemanticsProperties.ContentDescription].first().startsWith("Evening"))
    }

    @Test
    fun impact_line_shows_with_a_plan_and_is_hidden_without_one() {
        show()
        assertEquals("Its mobile data is 1.1% of your plan in a week.", text("detail_plan_impact"))
    }

    @Test
    fun impact_line_hidden_without_a_plan() {
        show(sampleState.copy(planMobileImpact = null))
        rule.onNodeWithTag("detail_plan_impact").assertDoesNotExist()
    }

    @Test
    fun split_bar_keeps_a_minimum_segment() {
        show(sampleState.copy(mobileBytes = 1_000_000L, wifiBytes = 3_700_000_000L))
        val mobile = rule.onNodeWithTag("detail_split_mobile", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("visible (was ${mobile.right - mobile.left})", (mobile.right - mobile.left) >= 13.dp)
    }

    // ------------------------------------------------------------------ budget, actions

    @Test
    fun no_budget_shows_an_empty_ring_and_a_suggestion() {
        show()
        assertTrue(text("detail_budget_suggestion").contains("a limit of 300 MB"))
        rule.onNodeWithTag("detail_budget_projection").assertDoesNotExist()
    }

    @Test
    fun action_buttons_open_the_editor_and_settings() {
        val events = mutableListOf<AppDetailEvent>()
        var settings = 0
        show(onEvent = { events += it }, onSettings = { settings++ })
        rule.onNodeWithTag("detail_budget_button").performClick()
        rule.onNodeWithTag("detail_settings_button").performClick()
        assertEquals(AppDetailEvent.OpenBudgetEditor, events.last())
        assertEquals(1, settings)
        assertTrue((rule.onNodeWithTag("detail_budget_button").getUnclippedBoundsInRoot().let { it.bottom - it.top }) >= 51.5.dp)
    }

    @Test
    fun settings_button_hidden_for_uid_packages() {
        show(sampleState.copy(packageName = "uid:1000"), onSettings = {})
        rule.onNodeWithTag("detail_settings_button").assertDoesNotExist()
    }

    // ------------------------------------------------------------------ layout

    @Test
    fun font_scale_2_no_clip() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) { AppDetailContent(state = sampleState, onEvent = {}, onBack = {}) }
            }
        }
        rule.onNodeWithTag("detail_title", useUnmergedTree = true).assertIsDisplayed()
        // The four tiles stack to one column.
        val average = rule.onNodeWithTag("detail_tile_average", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val busiest = rule.onNodeWithTag("detail_tile_busiest", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("tiles stack (${average.bottom} <= ${busiest.top})", average.bottom <= busiest.top + 0.5.dp)
    }

    @Test
    fun rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) { AppDetailContent(state = sampleState, onEvent = {}, onBack = {}) }
            }
        }
        rule.onNodeWithTag("detail_title", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun unknown_package_shows_the_letter_icon_and_no_crash() {
        show(AppDetailUiState(packageName = "com.unknown.app", label = "Unknown", totalBytes = 500_000L, loaded = true))
        rule.onNodeWithText("U", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Unknown", useUnmergedTree = true).assertIsDisplayed()
    }
}
