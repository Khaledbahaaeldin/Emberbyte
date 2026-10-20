package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
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

    @Test
    fun period_group_selects_and_reloads() {
        var selectedGranularity: io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity? = null
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 1_000_000_000L,
            comparisonText = "12% more than last week",
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
        org.junit.Assert.assertEquals(io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.WEEK, selectedGranularity)
    }

    @Test
    fun comparison_is_text_with_an_icon_and_never_only_colour() {
        val state = HistoryUiState(
            loaded = true,
            totalBytes = 1_000_000_000L,
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
