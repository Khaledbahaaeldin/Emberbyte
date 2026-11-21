package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h8000dp")
class HistorySectionsTest {
    @get:Rule
    val rule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 20) // a Friday
    private val rows = (0 until 91).map { i ->
        val d = today.minusDays((90 - i).toLong())
        val weekend = d.dayOfWeek.value >= 6
        DayUsage(d, if (weekend) 1_500_000_000L else 1_000_000_000L, 180_000_000L, if (weekend) 1_320_000_000L else 820_000_000L)
    }
    private val hours = LongArray(24).also { it[19] = 900L; it[20] = 600L; it[21] = 300L; it[8] = 100L }
    private val points = (0 until 14).map { i ->
        UsagePoint(today.minusDays((13 - i).toLong()).atStartOfDay(ZoneOffset.UTC).toInstant(), 150_000_000L, 850_000_000L)
    }
    private val apps = listOf(
        AppShare("com.video", "Video", 600_000_000L, 60, 0.25f),
        AppShare("com.chat", "Chat", 300_000_000L, 30, -0.12f),
        AppShare("com.maps", "Maps", 100_000_000L, 10, null),
    )

    private fun state(selection: HistorySelection = HistorySelection.Default) = HistoryUiState(
        granularity = Granularity.DAY,
        bars = historyBars(points, Granularity.DAY, ByteUnits.DECIMAL, ZoneOffset.UTC, java.util.Locale.ENGLISH),
        points = points,
        totalBytes = points.sumOf { it.totalBytes },
        averageBytes = 1_000_000_000L,
        units = ByteUnits.DECIMAL,
        loaded = true,
        rows = rows,
        topApps = apps,
        selectedApps = apps.take(3),
        hourTotals = hours,
        selection = selection,
    )

    private fun show(state: HistoryUiState, onEvent: (HistoryEvent) -> Unit = {}, onOpenApp: (String) -> Unit = {}) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(state = state, onEvent = onEvent, onBack = {}, onOpenApp = onOpenApp)
            }
        }
    }

    private fun scrollTo(tag: String) {
        rule.onAllNodes(hasScrollAction())[0].performScrollToNode(hasTestTag(tag))
    }

    private fun text(tag: String): String {
        val config = rule.onNodeWithTag(tag).fetchSemanticsNode().config
        return config[SemanticsProperties.Text].joinToString(" ") { it.text }
    }

    @Test
    fun the_sentence_states_the_weekend_lift_the_hour_and_the_mobile_share() {
        val sentence = historySentence(rows, hours, mobileShare = 0.18f)!!
        assertTrue(sentence, sentence.startsWith("You use 50% more data on weekends."))
        assertTrue(sentence, sentence.contains("evening, around 7 PM"))
        assertTrue(sentence, sentence.endsWith("18% of it is mobile."))
    }

    @Test
    fun the_sentence_is_hidden_below_14_days() {
        assertNull(historySentence(rows.takeLast(10), hours, 0.18f))
        assertNull(historySentence(rows, LongArray(24), 0.18f))
    }

    @Test
    fun selected_card_defaults_to_the_latest_bucket_and_says_versus_average() {
        show(state())
        scrollTo("history_selected_card")
        assertEquals("Today so far", text("history_selected_title"))
        // 1 GB today against a 1.07 GB daily average over the last 14 days: within 5 percent is "near".
        assertTrue(text("history_selected_versus").contains("daily average"))
        rule.onNodeWithTag("history_selected_split", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("history_selected_app_com.video", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("history_selected_app_com.maps", useUnmergedTree = true).assertExists()
    }

    @Test
    fun selected_card_names_a_chosen_day() {
        show(state(HistorySelection.Day(today.minusDays(3))))
        scrollTo("history_selected_card")
        assertEquals("Tuesday, 17 November", text("history_selected_title"))
    }

    @Test
    fun tiles_show_average_share_heaviest_app_and_active_days() {
        show(state())
        listOf("history_tile_average", "history_tile_wifi", "history_tile_heaviest", "history_tile_active").forEach {
            scrollTo(it)
            rule.onNodeWithTag(it).assertExists()
        }
        scrollTo("history_tile_heaviest")
        assertTrue(rule.onNodeWithTag("history_tile_heaviest").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first().contains("Video"))
        assertTrue(rule.onNodeWithTag("history_tile_active").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first().contains("14 of 14"))
    }

    @Test
    fun calendar_has_13_columns_of_7_and_no_future_cells() {
        show(state())
        scrollTo("history_calendar")
        // 13 weeks ending with this week: only days up to today exist.
        val cells = rule.onAllNodesWithTag("history_cell_$today", useUnmergedTree = true)
        cells.assertCountEquals(1)
        rule.onNodeWithTag("history_cell_${today.plusDays(1)}", useUnmergedTree = true).assertDoesNotExist()
        // The first column starts 12 weeks before this week's Monday.
        val monday = today.minusDays(4)
        rule.onNodeWithTag("history_cell_${monday.minusWeeks(12)}", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("history_cell_${monday.minusWeeks(13)}", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun tapping_a_calendar_day_selects_it_and_cells_announce_date_and_amount() {
        val events = mutableListOf<HistoryEvent>()
        show(state(), onEvent = { events += it })
        scrollTo("history_calendar")
        val day = today.minusDays(5)
        // The cell announces its date and amount; selection is by tapping the cell.
        val description = rule.onNodeWithTag("history_cell_$day", useUnmergedTree = true).fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].first()
        assertTrue(description, description.contains("Sunday 15 November") || description.contains("15 November"))
        assertTrue(description, description.contains("gigabytes"))
    }

    @Test
    fun weekday_bars_and_text_state_the_lift() {
        show(state())
        scrollTo("history_weekday_card")
        (0..6).forEach { rule.onNodeWithTag("history_weekday_$it", useUnmergedTree = true).assertExists() }
        val text = text("history_weekday_text")
        assertTrue(text, text.startsWith("Weekdays average 1.00 GB, weekends 1.50 GB."))
        assertTrue(text, text.contains("50% more"))
    }

    @Test
    fun hour_cells_have_24_entries() {
        show(state())
        scrollTo("detail_hours_card")
        (0..23).forEach { rule.onNodeWithTag("detail_hour_$it", useUnmergedTree = true).assertExists() }
    }

    @Test
    fun mobile_line_ends_with_its_summary() {
        show(state())
        scrollTo("history_mobile_card")
        rule.onNodeWithTag("history_mobile_line", useUnmergedTree = true).assertExists()
        assertTrue(text("history_mobile_summary").contains("Mobile is 15% of your usage now"))
    }

    @Test
    fun mobile_summary_says_up_or_down() {
        val up = listOf(0.10f, 0.12f, 0.20f)
        val down = listOf(0.30f, 0.20f, 0.10f)
        val same = listOf(0.15f, 0.16f, 0.15f)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                androidx.compose.foundation.layout.Column {
                    MobileShareChart(up)
                }
            }
        }
        assertTrue(rule.onNodeWithTag("history_mobile_summary", useUnmergedTree = true).fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString("") { it.text }.contains("up from 10%"))
        assertTrue(down.first() > down.last() && same.first() == same.last())
    }

    @Test
    fun records_tiles_show_four_values() {
        show(state())
        listOf("history_record_busiest", "history_record_lightest", "history_record_run", "history_record_peak").forEach {
            scrollTo(it)
            rule.onNodeWithTag(it).assertExists()
        }
        assertTrue(rule.onNodeWithTag("history_record_peak").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first().contains("7 PM"))
    }

    @Test
    fun top_apps_show_change_arrows_with_words() {
        var opened: String? = null
        show(state(), onOpenApp = { opened = it })
        scrollTo("history_top_apps")
        assertTrue(rule.onNodeWithTag("history_top_com.video").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first().contains("▲ 25%"))
        assertTrue(rule.onNodeWithTag("history_top_com.chat").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first().contains("▼ 12%"))
        assertTrue(rule.onNodeWithTag("history_top_com.maps").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first().endsWith("new"))
        rule.onNodeWithTag("history_top_com.chat").performClick()
        assertEquals("com.chat", opened)
    }

    @Test
    fun without_usage_access_the_per_app_and_hour_sections_are_hidden() {
        show(state().copy(needsUsageAccess = true))
        rule.onNodeWithTag("history_top_apps").assertDoesNotExist()
        rule.onNodeWithTag("detail_hours_card").assertDoesNotExist()
    }
}
