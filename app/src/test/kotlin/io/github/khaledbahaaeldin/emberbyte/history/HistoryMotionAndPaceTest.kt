package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** History: the plan pace chart, the stacked bars' data and the motion of its sections. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h8000dp")
class HistoryMotionAndPaceTest {
    @get:Rule
    val rule = createComposeRule()

    @Before fun on() { RevealGate.enabled = true }
    @After fun off() { RevealGate.enabled = false }

    private val today = LocalDate.of(2026, 11, 20)
    private val rows = (0 until 91).map { i ->
        DayUsage(today.minusDays((90 - i).toLong()), 1_000_000_000L, 150_000_000L, 850_000_000L)
    }
    private val points = (0 until 14).map { i ->
        UsagePoint(today.minusDays((13 - i).toLong()).atStartOfDay(ZoneOffset.UTC).toInstant(), 150_000_000L, 850_000_000L)
    }

    private fun pace(willPass: Boolean) = PlanPace(
        dayOfCycle = 20, cycleDays = 30, usedBytes = 4_500_000_000L, allowanceBytes = 8_000_000_000L,
        cumulative = (1..20).map { it * 225_000_000L }, projectedEndBytes = if (willPass) 9_100_000_000L else 6_000_000_000L,
        willPass = willPass, passDay = if (willPass) 28 else null,
        text = if (willPass) "You have used 4.5 GB of 8.0 GB (56%) 20 days into the cycle. At this pace you will finish at 9.1 GB and will pass the plan around day 28." else "under the plan",
    )

    private fun state(pace: PlanPace? = null) = HistoryUiState(
        granularity = Granularity.DAY,
        bars = historyBars(points, Granularity.DAY, ByteUnits.DECIMAL, ZoneOffset.UTC, java.util.Locale.ENGLISH),
        points = points, totalBytes = points.sumOf { it.totalBytes }, averageBytes = 1_000_000_000L,
        units = ByteUnits.DECIMAL, loaded = true, rows = rows, planPace = pace,
    )

    private fun show(state: HistoryUiState, reduced: Boolean = false) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    HistoryContent(state = state, onEvent = {}, onBack = {})
                }
            }
        }
    }

    private fun scrollTo(tag: String) {
        rule.onAllNodes(hasScrollAction())[0].performScrollToNode(hasTestTag(tag))
    }

    private fun width(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot().let { (it.right - it.left).value }

    @Test
    fun cycle_chart_shows_with_a_plan_and_its_text_says_pass_or_under() {
        show(state(pace(true)))
        scrollTo("history_pace_card")
        rule.onNodeWithTag("history_pace_chart", useUnmergedTree = true).assertExists()
        val text = rule.onNodeWithTag("history_pace_text").fetchSemanticsNode().config[SemanticsProperties.Text].joinToString("") { it.text }
        assertTrue(text, text.contains("will pass the plan around day 28"))
    }

    @Test
    fun cycle_chart_is_hidden_without_a_plan() {
        show(state(null))
        rule.mainClock.advanceTimeBy(200)
        rule.onNodeWithTag("history_pace_card", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun calendar_cells_pop_in_as_a_wave_and_end_at_full_size() {
        show(state())
        scrollTo("history_calendar")
        rule.mainClock.advanceTimeBy(250)
        val early = width("history_cell_$today")
        val firstCell = width("history_cell_${today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).minusWeeks(12)}")
        assertTrue("the first cell has popped before the last (first=$firstCell, last=$early)", firstCell > early)
        rule.mainClock.advanceTimeBy(5_000)
        assertEquals(width("history_cell_$today"), width("history_cell_${today.minusDays(1)}"), 0.5f)
    }

    @Test
    fun cards_reveal_into_place() {
        show(state())
        val key = io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoEntranceProgressKey
        fun progress() = rule.onNodeWithTag("history_selected_card", useUnmergedTree = true).fetchSemanticsNode().config[key]
        assertTrue("starts hidden", progress() < 0.05f)
        rule.mainClock.advanceTimeBy(4_000)
        assertEquals(1f, progress(), 0.02f)
    }

    @Test
    fun reduced_motion_is_static() {
        show(state(), reduced = true)
        rule.mainClock.advanceTimeBy(16)
        val first = width("history_selected_card")
        rule.mainClock.advanceTimeBy(2_000)
        assertEquals(first, width("history_selected_card"), 0.5f)
    }
}
