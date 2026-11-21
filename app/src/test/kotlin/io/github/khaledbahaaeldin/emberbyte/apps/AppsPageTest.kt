package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
class AppsPageTest {
    @get:Rule
    val rule = createComposeRule()

    @Before fun settleEntrance() {
        AppsEntranceGate.played = true
    }

    private val video = AppRowUi("com.video", "Video", mobileBytes = 100_000_000L, wifiBytes = 1_490_000_000L)
    private val chat = AppRowUi("com.chat", "Chat", mobileBytes = 800_000_000L, wifiBytes = 100_000_000L)
    private val maps = AppRowUi("com.maps", "Maps", mobileBytes = 9_000_000L, wifiBytes = 0L)

    private fun defaultState() = state()

    private fun state(
        featured: AppRowUi? = video,
        apps: List<AppRowUi> = listOf(chat, maps),
        network: AppsNetwork = AppsNetwork.ALL,
        sort: AppSort = AppSort.BYTES_DESC,
        range: AppsRange = AppsRange.TODAY,
        system: Boolean = false,
        query: String = "",
    ): AppsUiState {
        val shown = listOfNotNull(featured) + apps
        return AppsUiState(
            range = range, network = network, sort = sort, showSystemApps = system, query = query,
            apps = apps, featured = featured,
            totalBytes = shown.sumOf { it.totalBytes },
            wifiBytes = shown.sumOf { it.wifiBytes },
            mobileBytes = shown.sumOf { it.mobileBytes },
            appCount = shown.size,
            units = ByteUnits.DECIMAL, loaded = true,
        )
    }

    private fun show(
        state: () -> AppsUiState = { defaultState() },
        reduced: Boolean = false,
        onEvent: (AppsEvent) -> Unit = {},
        onOpenApp: (String) -> Unit = {},
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    AppsContent(state = state(), onEvent = onEvent, onOpenApp = onOpenApp, onPromptAction = {})
                }
            }
        }
        rule.mainClock.advanceTimeBy(600)
    }

    private fun config(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config

    private fun bounds(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    // ------------------------------------------------------------------ header, search, period

    @Test
    fun header_pill_shows_the_app_count() {
        show()
        assertEquals("3 apps", config("apps_count_pill")[SemanticsProperties.Text].joinToString("") { it.text })
    }

    @Test
    fun header_pill_is_singular_for_one_app() {
        show(state = { state(featured = null, apps = listOf(chat)) })
        assertEquals("1 app", config("apps_count_pill")[SemanticsProperties.Text].joinToString("") { it.text })
    }

    @Test
    fun search_and_filters_are_both_56dp() {
        show()
        assertEquals(56f, (bounds("apps_search_field").bottom - bounds("apps_search_field").top).value, 0.5f)
        assertEquals(56f, (bounds("apps_filters_button").bottom - bounds("apps_filters_button").top).value, 0.5f)
        assertEquals(64f, (bounds("apps_filters_button").right - bounds("apps_filters_button").left).value, 0.5f)
        assertEquals("6 dp apart", 6f, (bounds("apps_filters_button").left - bounds("apps_search_field").right).value, 0.6f)
    }

    @Test
    fun filters_button_shows_a_count_badge_when_active() {
        show(state = { state(network = AppsNetwork.MOBILE, sort = AppSort.NAME) })
        rule.onNodeWithText("2", useUnmergedTree = true).assertExists()
        assertEquals(listOf("Filters, 2 active"), config("apps_filters_button")[SemanticsProperties.ContentDescription])
    }

    @Test
    fun filters_button_has_no_badge_without_filters() {
        show()
        rule.onAllNodesWithText("0", useUnmergedTree = true).assertCountEquals(0)
        assertEquals(listOf("Filters"), config("apps_filters_button")[SemanticsProperties.ContentDescription])
    }

    @Test
    fun period_buttons_set_the_range_and_mark_the_selected_one() {
        val events = mutableListOf<AppsEvent>()
        show(state = { state(range = AppsRange.LAST_7_DAYS) }, onEvent = { events += it })
        assertTrue(config("period_last_7_days")[SemanticsProperties.Selected])
        assertFalse(config("period_today")[SemanticsProperties.Selected])
        rule.onNodeWithTag("period_last_30_days").performClick()
        rule.onNodeWithTag("period_cycle").performClick()
        assertEquals(
            listOf<AppsEvent>(AppsEvent.SetRange(AppsRange.LAST_30_DAYS), AppsEvent.SetRange(AppsRange.CYCLE)),
            events,
        )
        val selected = bounds("period_last_7_days")
        val other = bounds("period_today")
        assertEquals("selected is 1.25 times wider", 1.25f, (selected.right - selected.left).value / (other.right - other.left).value, 0.06f)
        assertTrue("at least 48 dp tall", (selected.bottom - selected.top) >= 47.5.dp)
    }

    @Test
    fun period_buttons_announce_their_state() {
        show()
        assertEquals(listOf("Last 7 days"), config("period_last_7_days")[SemanticsProperties.ContentDescription])
        assertTrue(config("period_today")[SemanticsProperties.Selected])
    }

    @Test
    fun clear_button_appears_only_with_text() {
        show(state = { state(query = "") })
        rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Clear search")).assertCountEquals(0)
    }

    @Test
    fun clear_button_is_there_with_text_and_clears() {
        val events = mutableListOf<AppsEvent>()
        show(state = { state(query = "vid") }, onEvent = { events += it })
        rule.onNode(androidx.compose.ui.test.hasContentDescription("Clear search")).performClick()
        assertEquals(AppsEvent.SetQuery(""), events.last())
    }

    @Test
    fun typing_sends_the_query() {
        val events = mutableListOf<AppsEvent>()
        show(onEvent = { events += it })
        rule.onNode(hasSetTextAction()).performTextInput("vid")
        assertEquals(AppsEvent.SetQuery("vid"), events.last())
    }

    // ------------------------------------------------------------------ chips

    @Test
    fun chips_are_labelled_in_full_and_removable() {
        val events = mutableListOf<AppsEvent>()
        show(state = { state(network = AppsNetwork.MOBILE, sort = AppSort.BYTES_ASC, system = true) }, onEvent = { events += it })
        rule.onAllNodesWithText("Network: Mobile", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("Sort: Least data", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("System apps", useUnmergedTree = true).assertCountEquals(1)
        assertTrue("at least 32 dp tall", (bounds("apps_chip_network").bottom - bounds("apps_chip_network").top) >= 31.5.dp)
        rule.onNodeWithTag("apps_chip_sort").performClick()
        assertEquals(AppsEvent.RemoveFilter(AppFilterKind.SORT), events.last())
    }

    @Test
    fun reset_chip_appears_with_two_or_more_filters() {
        show(state = { state(network = AppsNetwork.MOBILE) })
        rule.onNodeWithTag("apps_chip_reset").assertDoesNotExist()
    }

    @Test
    fun reset_chip_is_there_with_two_filters() {
        val events = mutableListOf<AppsEvent>()
        show(state = { state(network = AppsNetwork.MOBILE, sort = AppSort.NAME) }, onEvent = { events += it })
        rule.onNodeWithTag("apps_chip_reset").performClick()
        assertEquals(AppsEvent.ResetFilters, events.last())
    }

    @Test
    fun chips_wrap_instead_of_clipping() {
        show(state = { state(network = AppsNetwork.MOBILE, sort = AppSort.BYTES_ASC, system = true) })
        val row = bounds("apps_filter_chips")
        listOf("apps_chip_network", "apps_chip_sort", "apps_chip_system_apps", "apps_chip_reset").forEach {
            val chip = bounds(it)
            assertTrue("$it inside the row", chip.left >= row.left - 0.5.dp && chip.right <= row.right + 0.5.dp)
        }
    }

    // ------------------------------------------------------------------ cards and rows

    @Test
    fun summary_shows_the_total_split_and_apps_only_label() {
        show()
        rule.onAllNodesWithText("Apps used today (apps only)", useUnmergedTree = true).assertCountEquals(1)
        rule.onNodeWithTag("apps_summary_total", useUnmergedTree = true).assertIsDisplayed()
        rule.onAllNodesWithText("Wi-Fi 1.59 GB", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("Mobile 909 MB", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun summary_label_names_the_period() {
        show(state = { state(range = AppsRange.LAST_30_DAYS) })
        rule.onAllNodesWithText("Apps used in 30 days (apps only)", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun split_bar_has_a_minimum_segment() {
        show(state = { state(featured = video.copy(mobileBytes = 1_000_000L), apps = emptyList()) })
        rule.mainClock.advanceTimeBy(1_500)
        val mobile = bounds("apps_split_mobile")
        assertTrue("mobile keeps a visible width (was ${mobile.right - mobile.left})", (mobile.right - mobile.left) >= 13.dp)
    }

    @Test
    fun most_used_card_shows_icon_name_amount_and_share_and_opens_the_app() {
        var opened: String? = null
        show(onOpenApp = { opened = it })
        rule.onAllNodesWithText("MOST USED", useUnmergedTree = true).assertCountEquals(1)
        val config = config("apps_most_used")
        assertTrue(config[SemanticsProperties.ContentDescription].first().startsWith("Most used: Video"))
        assertTrue(config[SemanticsProperties.ContentDescription].first().contains("% of total"))
        rule.onNodeWithTag("apps_most_used").performClick()
        assertEquals("com.video", opened)
        val card = bounds("apps_most_used")
        assertTrue("icon row is at least 64 dp", (card.bottom - card.top) >= 96.dp)
    }

    @Test
    fun rows_show_split_sub_line_for_all_networks_and_percent_for_one() {
        show()
        assertEquals("Wi-Fi 100 MB · Mobile 800 MB", config("apps_row_sub_com.chat")[SemanticsProperties.Text].joinToString("") { it.text })
    }

    @Test
    fun rows_show_a_percent_when_one_network_is_selected() {
        show(state = { state(network = AppsNetwork.MOBILE) })
        assertTrue(config("apps_row_sub_com.chat")[SemanticsProperties.Text].joinToString("") { it.text }.endsWith("% of total"))
    }

    @Test
    fun rows_have_share_fill_relative_to_the_first_row() {
        show()
        assertEquals(900f / 1_590f, config("apps_row_com.chat")[AppsRowFillKey], 0.02f)
        assertEquals(0.14f, config("apps_row_com.maps")[AppsRowFillKey], 0.02f)
    }

    @Test
    fun rows_glide_when_the_period_changes() {
        var order by mutableStateOf(true)
        show(state = { state(apps = if (order) listOf(chat, maps) else listOf(maps, chat)) })
        val before = bounds("apps_row_com.maps").top
        rule.runOnUiThread { order = false }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithTag("apps_row_com.maps", useUnmergedTree = true).fetchSemanticsNode()
        rule.mainClock.advanceTimeBy(64)
        val mid = bounds("apps_row_com.maps").top
        rule.mainClock.advanceTimeBy(3_000)
        val after = bounds("apps_row_com.maps").top
        assertTrue("Maps moved up ($before to $after)", after < before)
        assertTrue("Maps is between the two places mid-glide (mid=$mid before=$before after=$after)", mid < before && mid > after)
    }

    @Test
    fun reduced_motion_applies_changes_instantly() {
        var order by mutableStateOf(true)
        show(reduced = true, state = { state(apps = if (order) listOf(chat, maps) else listOf(maps, chat)) })
        val before = bounds("apps_row_com.maps").top
        rule.runOnUiThread { order = false }
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag("apps_row_com.maps", useUnmergedTree = true).fetchSemanticsNode()
        rule.mainClock.advanceTimeBy(100)
        val after = bounds("apps_row_com.maps").top
        assertTrue("moved at once ($before to $after)", after < before)
        rule.mainClock.advanceTimeBy(200)
        assertEquals(after, bounds("apps_row_com.maps").top)
    }

    // ------------------------------------------------------------------ empty states

    @Test
    fun search_with_no_match_shows_one_message_and_a_clear_button_and_no_zero_total() {
        val events = mutableListOf<AppsEvent>()
        show(state = { AppsUiState(query = "chr", loaded = true, units = ByteUnits.DECIMAL) }, onEvent = { events += it })
        rule.onAllNodesWithText("No apps match \"chr\".", useUnmergedTree = true).assertCountEquals(1)
        rule.onNodeWithText("Clear search", useUnmergedTree = true).performClick()
        assertEquals(AppsEvent.SetQuery(""), events.last())
        rule.onNodeWithTag("apps_summary_card").assertDoesNotExist()
        rule.onNodeWithTag("apps_most_used").assertDoesNotExist()
        rule.onAllNodesWithText("All apps", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText("0 B", substring = true, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun filters_that_match_nothing_offer_a_reset() {
        val events = mutableListOf<AppsEvent>()
        show(state = { AppsUiState(network = AppsNetwork.MOBILE, loaded = true, units = ByteUnits.DECIMAL) }, onEvent = { events += it })
        rule.onAllNodesWithText("Nothing to show for these filters.", useUnmergedTree = true).assertCountEquals(1)
        rule.onNodeWithText("Reset filters", useUnmergedTree = true).performClick()
        assertEquals(AppsEvent.ResetFilters, events.last())
    }

    @Test
    fun list_header_shows_the_sort() {
        show(state = { state(sort = AppSort.BYTES_ASC) })
        assertEquals("Least data", config("apps_sort_label")[SemanticsProperties.Text].joinToString("") { it.text })
    }
}
