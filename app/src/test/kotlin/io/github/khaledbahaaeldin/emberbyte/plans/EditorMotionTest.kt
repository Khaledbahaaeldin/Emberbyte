package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h640dp")
class EditorMotionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun reduced_motion_snaps_slim_preview_immediately() {
        val state = PlanEditorUiState(
            planId = 1L,
            isNew = false,
            loading = false,
            form = PlanEditorForm(name = "Home SIM", capText = "20"),
            preview = PlanCardUi(
                id = 1L,
                name = "Home SIM",
                simLabel = null,
                fraction = 0.5f,
                ringState = BudgetStateUi.Ok,
                percentText = "50%",
                usedText = "10 GB of 20 GB",
                remainingText = "10 GB left",
                daysLeftText = "15 days left",
                forecastText = null,
                isEstimated = false,
                chips = emptyList(),
                isDormant = false,
                startsAtText = null,
                statusKind = PlanStatusKind.OnTrack,
                timeFraction = 0.5f,
            ),
        )
        rule.setContent {
            CompositionLocalProvider(LocalReduceMotion provides true) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanEditorContent(
                        state = state,
                        onEvent = {},
                        onBack = {},
                        onPrompt = {},
                    )
                }
            }
        }
        rule.onNodeWithTag("editor_lazy_list").performScrollToIndex(6)
        rule.waitForIdle()
        rule.onNodeWithTag("editor_slim_preview").assertIsDisplayed()
    }
}
