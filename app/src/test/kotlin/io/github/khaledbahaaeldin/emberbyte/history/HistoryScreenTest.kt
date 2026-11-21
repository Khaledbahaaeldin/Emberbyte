package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithTag
import io.github.khaledbahaaeldin.emberbyte.common.SCREEN_SCAFFOLD_SCRIM
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HistoryScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val zone = ZoneOffset.UTC
    private val locale = Locale.ENGLISH
    private val baseDate = LocalDate.of(2026, 9, 24)
    private val dayPoints = (0 until 14).map { dayOffset ->
        val d = baseDate.plusDays(dayOffset.toLong())
        val instant = d.atStartOfDay(zone).toInstant()
        UsagePoint(instant, 100_000_000L, 50_000_000L)
    }

    @Test
    fun history_screen_renders_inside_screen_scaffold_with_scrim() {
        val state = buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(state = state, onEvent = {}, onBack = {})
            }
        }
        rule.onNodeWithTag(SCREEN_SCAFFOLD_SCRIM).assertIsDisplayed()
        rule.onNode(hasText("History") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertIsDisplayed()
    }

    @Test
    fun history_labels_the_visible_range() {
        var state by mutableStateOf(buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale))
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(state = state, onEvent = {}, onBack = {})
            }
        }
        rule.onNodeWithText("24 Sep to 7 Oct").assertIsDisplayed()
        rule.onNodeWithText("Last 14 days").assertIsDisplayed()

        // When a bar is selected
        state = buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale, selectedIndex = 13)
        rule.waitForIdle()
        rule.onNodeWithText("Wednesday, 7 October").assertIsDisplayed()
    }

    @Test
    fun selected_bar_is_tinted_and_others_dim() {
        val selectedState = buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale, selectedIndex = 13)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(state = selectedState, onEvent = {}, onBack = {})
            }
        }
        rule.onNode(hasContentDescription("Wednesday, 7 October", substring = true) and isSelected())
            .assertIsDisplayed()
    }

    @Test
    fun split_bar_has_a_legend() {
        val selectedState = buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale, selectedIndex = 13)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(state = selectedState, onEvent = {}, onBack = {})
            }
        }
        rule.onAllNodes(hasScrollAction())[0]
            .performScrollToNode(hasText("Mobile", substring = true))
        rule.onNodeWithText("Mobile", substring = true).assertExists()
        rule.onNodeWithText("Wi-Fi", substring = true).assertExists()
    }

    @Test
    fun selecting_a_bar_does_not_move_the_chart() {
        var state by mutableStateOf(buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale))
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = { event ->
                        if (event is HistoryEvent.SelectBar) {
                            state = buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale, selectedIndex = event.index)
                        }
                    },
                    onBack = {},
                )
            }
        }
        val chartTopBefore = rule.onNodeWithContentDescription("Daily usage", substring = true).getBoundsInRoot().top
        state = buildHistoryUiState(Granularity.DAY, dayPoints, ByteUnits.DECIMAL, zone, locale, selectedIndex = 13)
        rule.waitForIdle()
        val chartTopAfter = rule.onNodeWithContentDescription("Daily usage", substring = true).getBoundsInRoot().top
        assertEquals(chartTopBefore.value, chartTopAfter.value, 1f)
    }

    @Test
    fun month_switch_keeps_chart_top_stable() {
        val monthPoints = (0 until 12).map { offset ->
            val d = LocalDate.of(2025, 11, 1).plusMonths(offset.toLong())
            UsagePoint(d.atStartOfDay(zone).toInstant(), 1_000_000_000L, 0L)
        }
        var currentGranularity by mutableStateOf(Granularity.DAY)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                val activePoints = if (currentGranularity == Granularity.DAY) dayPoints else monthPoints
                val s = buildHistoryUiState(currentGranularity, activePoints, ByteUnits.DECIMAL, zone, locale)
                HistoryContent(
                    state = s,
                    onEvent = { event ->
                        if (event is HistoryEvent.SetGranularity) {
                            currentGranularity = event.granularity
                        }
                    },
                    onBack = {},
                )
            }
        }
        val dayChartTop = rule.onNodeWithContentDescription("Daily usage", substring = true).getBoundsInRoot().top
        currentGranularity = Granularity.MONTH
        rule.waitForIdle()
        val monthChartTop = rule.onNodeWithContentDescription("Monthly usage", substring = true).getBoundsInRoot().top
        assertEquals(dayChartTop.value, monthChartTop.value, 1f)
    }

    @Test
    fun period_group_selects_and_reloads() {
        var selectedGranularity: Granularity? = null
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 1_000_000_000L,
            comparisonText = "12% more than last week",
            trendDirection = TrendDirection.UP,
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = { event ->
                        if (event is HistoryEvent.SetGranularity) {
                            selectedGranularity = event.granularity
                        }
                    },
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("Week").performClick()
        assertEquals(Granularity.WEEK, selectedGranularity)
    }

    @Test
    fun comparison_is_text_with_an_icon_and_never_only_colour() {
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 1_000_000_000L,
            comparisonText = "12% more than last week",
            trendDirection = TrendDirection.UP,
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("12% more than last week").assertIsDisplayed()
        rule.onNodeWithContentDescription("Increased usage").assertIsDisplayed()
    }

    @Test
    fun empty_history_shows_the_empty_state() {
        val state = HistoryUiState(
            loaded = true,
            bars = emptyList(),
            totalBytes = 0L,
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("No history yet"))
        rule.onNodeWithText("No history yet").assertIsDisplayed()
    }

    @Test
    fun total_is_one_node() {
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 640_000_000L,
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithContentDescription("640 megabytes total").assertIsDisplayed()
    }

    @Test
    @Config(fontScale = 2.0f)
    fun font_scale_2_no_clip() {
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 640_000_000L,
            comparisonText = "12% more than last week",
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("12% more than last week").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ar-rEG")
    fun rtl() {
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 640_000_000L,
            comparisonText = "12% more than last week",
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HistoryContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("12% more than last week").assertIsDisplayed()
    }
}
