package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UsageCardTest {
    @get:Rule
    val rule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 20) // a Friday
    private val totals = listOf(900L, 1_400L, 700L, 2_100L, 1_100L, 1_600L, 3_710L).map { it * 1_000_000L }
    private val days = totals.mapIndexed { i, t -> DayUsage(today.minusDays((6 - i).toLong()), t, t / 2, t / 2) }

    private fun app(pkg: String, label: String, amount: Long, system: Boolean = false) =
        RankedApp(AppUsageRow(pkg, label, 0L, amount, system), amount, 0)

    private fun ranked(vararg rows: Pair<String, Long>): List<RankedApp> {
        val total = rows.sumOf { it.second }
        return rows.map { RankedApp(AppUsageRow(it.first, it.first.uppercase(), 0L, it.second), it.second, (it.second * 100 / total).toInt()) }
    }

    private fun defaultState() = state()

    private fun state(
        selected: Int = 6,
        rows: List<RankedApp> = ranked("a" to 1_590_000_000L, "b" to 900_000_000L, "c" to 400_000_000L, "d" to 100_000_000L),
        filters: UsageFilters = UsageFilters(),
    ) = UsageCardState(days, selected, rows, filters, ByteUnits.DECIMAL)

    private fun setCard(
        reduced: Boolean = false,
        state: () -> UsageCardState = { defaultState() },
        onSelectDay: (Int) -> Unit = {},
        onRemoveFilter: (UsageFilter) -> Unit = {},
        onOpenApp: (String) -> Unit = {},
        onResetFilters: () -> Unit = {},
        onOpenHistory: (Int) -> Unit = {},
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    UsageCard(
                        state = state(),
                        onSelectDay = onSelectDay,
                        onOpenApp = onOpenApp,
                        onAllApps = {},
                        onOpenFilters = {},
                        onRemoveFilter = onRemoveFilter,
                        onOpenHistory = onOpenHistory,
                        onResetFilters = onResetFilters,
                        modifier = Modifier.width(380.dp),
                        locale = Locale.ENGLISH,
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(1_200)
    }

    private fun config(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config

    // ------------------------------------------------------------------ strip and summary

    @Test
    fun strip_shows_seven_days_with_values_and_a_marked_selected_day() {
        setCard()
        (0..6).forEach { rule.onNodeWithTag("usage_day_$it").assertExists() }
        assertTrue(config("usage_day_6")[SemanticsProperties.Selected])
        assertFalse(config("usage_day_2")[SemanticsProperties.Selected])
    }

    @Test
    fun day_buttons_announce_the_day_and_value() {
        setCard()
        assertEquals(listOf("Friday, 3.71 gigabytes"), config("usage_day_6")[SemanticsProperties.ContentDescription])
        assertEquals(listOf("Sunday, 1.40 gigabytes"), config("usage_day_1")[SemanticsProperties.ContentDescription].also { })
    }

    @Test
    fun header_pill_and_summary_describe_the_selected_day() {
        setCard()
        assertEquals("Today", config("usage_pill")[SemanticsProperties.Text].joinToString("") { it.text })
        assertEquals("3.71 GB on Friday, so far", config("usage_summary")[SemanticsProperties.Text].joinToString("") { it.text })
    }

    @Test
    fun selecting_a_day_updates_the_summary_and_the_ranking() {
        var selected by mutableStateOf(6)
        val rowsByDay = mapOf(6 to ranked("a" to 1_590_000_000L, "b" to 900_000_000L), 3 to ranked("z" to 2_100_000_000L, "y" to 1L))
        setCard(
            state = { state(selected = selected, rows = rowsByDay.getValue(selected)) },
            onSelectDay = { selected = it },
        )
        rule.onNodeWithTag("usage_day_3").performClick()
        rule.mainClock.advanceTimeBy(1_500)
        assertEquals("Tuesday", config("usage_pill")[SemanticsProperties.Text].joinToString("") { it.text })
        assertEquals("2.10 GB on Tuesday", config("usage_summary")[SemanticsProperties.Text].joinToString("") { it.text })
        rule.onNodeWithTag("usage_row_z").assertExists()
        rule.onNodeWithTag("usage_row_a").assertDoesNotExist()
    }

    // ------------------------------------------------------------------ rows

    @Test
    fun first_row_is_featured_and_the_others_are_compact() {
        setCard()
        val first = rule.onNodeWithTag("usage_row_a", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val second = rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("featured row at least 88 dp (was ${first.bottom - first.top})", (first.bottom - first.top) >= 87.5.dp)
        assertTrue("other rows at least 64 dp", (second.bottom - second.top) >= 63.5.dp)
        assertTrue("other rows are smaller than the featured one", (second.bottom - second.top) < (first.bottom - first.top))
    }

    @Test
    fun rows_expand_one_at_a_time_and_announce_the_state() {
        setCard()
        assertEquals("Collapsed", config("usage_row_a")[SemanticsProperties.StateDescription])
        rule.onNodeWithTag("usage_row_a").performClick()
        rule.mainClock.advanceTimeBy(800)
        assertEquals("Expanded", config("usage_row_a")[SemanticsProperties.StateDescription])
        rule.onNodeWithTag("usage_row_b").performClick()
        rule.mainClock.advanceTimeBy(800)
        assertEquals("Collapsed", config("usage_row_a")[SemanticsProperties.StateDescription])
        assertEquals("Expanded", config("usage_row_b")[SemanticsProperties.StateDescription])
    }

    @Test
    fun an_expanded_row_opens_app_details() {
        var opened: String? = null
        setCard(onOpenApp = { opened = it })
        rule.onNodeWithTag("usage_row_b").performClick()
        rule.mainClock.advanceTimeBy(800)
        rule.onNodeWithTag("usage_row_details", useUnmergedTree = true).performClick()
        assertEquals("b", opened)
    }

    @Test
    fun row_share_fill_follows_usage() {
        setCard()
        assertEquals(1f, config("usage_row_a")[RowShareFillKey], 0.01f)
        assertEquals(900f / 1_590f, config("usage_row_b")[RowShareFillKey], 0.01f)
        assertEquals(0.14f, config("usage_row_d")[RowShareFillKey], 0.01f)
    }

    @Test
    fun rows_glide_to_new_positions_when_the_ranking_changes() {
        var order by mutableStateOf(true)
        setCard(state = {
            state(rows = if (order) ranked("a" to 900_000_000L, "b" to 100_000_000L) else ranked("b" to 900_000_000L, "a" to 100_000_000L))
        })
        val before = rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot().top
        rule.runOnUiThread { order = false }
        rule.mainClock.advanceTimeBy(48)
        rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).fetchSemanticsNode()
        rule.mainClock.advanceTimeBy(48)
        val mid = rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot().top
        rule.mainClock.advanceTimeBy(3_000)
        val after = rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot().top
        assertTrue("b moved up in the end ($before to $after)", after < before)
        assertTrue("b is between the two places mid-glide (mid=$mid before=$before after=$after)", mid < before && mid > after)
    }

    @Test
    fun reduced_motion_ranking_changes_instantly() {
        var order by mutableStateOf(true)
        setCard(reduced = true, state = {
            state(rows = if (order) ranked("a" to 900_000_000L, "b" to 100_000_000L) else ranked("b" to 900_000_000L, "a" to 100_000_000L))
        })
        val before = rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot().top
        rule.runOnUiThread { order = false }
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).fetchSemanticsNode()
        rule.mainClock.advanceTimeBy(100)
        val after = rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot().top
        assertTrue("moved at once ($before to $after)", after < before)
        rule.mainClock.advanceTimeBy(100)
        assertEquals(after, rule.onNodeWithTag("usage_row_b", useUnmergedTree = true).getUnclippedBoundsInRoot().top)
    }

    // ------------------------------------------------------------------ buttons and filters

    @Test
    fun connected_buttons_show_all_apps_filters_history_in_order() {
        setCard()
        val all = rule.onNodeWithTag("usage_btn_all_apps", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val filters = rule.onNodeWithTag("usage_btn_filters", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val history = rule.onNodeWithTag("usage_btn_history", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(all.right <= filters.left && filters.right <= history.left)
        assertEquals("3 dp apart", 3f, (filters.left - all.right).value, 0.6f)
        assertTrue("at least 54 dp tall", (all.bottom - all.top) >= 53.5.dp)
        assertTrue("widths follow 1 : 1.1 : 1.15", (history.right - history.left) > (filters.right - filters.left) &&
            (filters.right - filters.left) > (all.right - all.left))
    }

    @Test
    fun history_button_passes_the_selected_day() {
        var day = -1
        setCard(state = { state(selected = 3) }, onOpenHistory = { day = it })
        rule.onNodeWithTag("usage_btn_history").performClick()
        assertEquals(3, day)
    }

    @Test
    fun filters_segment_shows_a_badge_and_tint_when_active() {
        setCard(state = { state(filters = UsageFilters(NetworkFilter.Mobile, showSystemApps = true)) })
        assertEquals(listOf("Filters, 2 active"), config("usage_btn_filters")[SemanticsProperties.ContentDescription])
        rule.onAllNodesWithText("2", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun filters_segment_has_no_badge_without_filters() {
        setCard()
        assertEquals(listOf("Filters"), config("usage_btn_filters")[SemanticsProperties.ContentDescription])
        rule.onNodeWithTag("usage_filters_badge", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun active_filters_show_as_removable_full_labelled_chips() {
        val removed = mutableListOf<UsageFilter>()
        setCard(
            state = { state(filters = UsageFilters(NetworkFilter.Mobile, showSystemApps = true)) },
            onRemoveFilter = { removed += it },
        )
        rule.onAllNodesWithText("Network: Mobile", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("System apps", useUnmergedTree = true).assertCountEquals(1)
        val chip = rule.onAllNodesWithText("Network: Mobile", useUnmergedTree = true)[0]
        val bounds = chip.getUnclippedBoundsInRoot()
        assertTrue("chip text exists", bounds.right > bounds.left)
        rule.onNodeWithText("Network: Mobile", useUnmergedTree = true).performClick()
        rule.onNodeWithText("System apps", useUnmergedTree = true).performClick()
        assertEquals(listOf(UsageFilter.Network, UsageFilter.SystemApps), removed)
    }

    @Test
    fun a_filter_that_matches_nothing_shows_an_empty_state_with_reset() {
        var resets = 0
        setCard(
            state = { state(rows = emptyList(), filters = UsageFilters(NetworkFilter.Mobile)) },
            onResetFilters = { resets++ },
        )
        rule.onAllNodesWithText("No apps match these filters", useUnmergedTree = true).assertCountEquals(1)
        rule.onNodeWithText("Reset", useUnmergedTree = true).performClick()
        assertEquals(1, resets)
    }

    @Test
    fun filters_sheet_applies_and_resets_a_draft() {
        var applied: UsageFilters? = null
        var resets = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UsageFiltersContent(
                    filters = UsageFilters(),
                    onApply = { applied = it },
                    onReset = { resets++ },
                )
            }
        }
        rule.onNodeWithTag("usage_filters_system").performClick()
        rule.onNodeWithText("Mobile").performClick()
        rule.onNodeWithTag("usage_filters_apply").performClick()
        assertEquals(UsageFilters(NetworkFilter.Mobile, showSystemApps = true), applied)
        rule.onNodeWithTag("usage_filters_reset").performClick()
        assertEquals(1, resets)
        rule.onNodeWithTag("usage_filters_apply").performClick()
        assertEquals(UsageFilters(), applied)
        rule.onAllNodesWithText("Period, sort and more are in Apps").assertCountEquals(1)
    }
}
