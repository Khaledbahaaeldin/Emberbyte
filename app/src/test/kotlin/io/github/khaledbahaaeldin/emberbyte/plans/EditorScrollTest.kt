package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h640dp")
class EditorScrollTest {
    @get:Rule val rule = createComposeRule()

    private val sampleState = PlanEditorUiState(
        planId = 1L,
        isNew = false,
        loading = false,
        form = PlanEditorForm(name = "Home SIM", capText = "20"),
        preview = PlanCardUi(
            id = 1L,
            name = "Home SIM",
            simLabel = null,
            fraction = 0.62f,
            ringState = BudgetStateUi.Ok,
            percentText = "62%",
            usedText = "12.4 GB of 20 GB",
            remainingText = "7.6 GB left",
            daysLeftText = "15 days left",
            forecastText = "At this pace, you will be fine",
            isEstimated = true,
            chips = emptyList(),
            isDormant = false,
            startsAtText = null,
            statusKind = PlanStatusKind.OnTrack,
            timeFraction = 0.5f,
        ),
    )

    @Test
    fun preview_collapses_into_slim_bar_after_scrolling_past_it_and_comes_back_at_top() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = sampleState,
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        rule.onNodeWithTag("editor_preview_card").assertIsDisplayed()
        rule.onNodeWithTag("editor_slim_preview").assertDoesNotExist()

        rule.onNodeWithTag("editor_lazy_list").performScrollToIndex(6)
        rule.waitForIdle()
        rule.onNodeWithTag("editor_slim_preview").assertIsDisplayed()

        rule.onNodeWithTag("editor_lazy_list").performScrollToIndex(0)
        rule.waitForIdle()
        rule.onNodeWithTag("editor_preview_card").assertIsDisplayed()
        rule.onNodeWithTag("editor_slim_preview").assertDoesNotExist()
    }
}
