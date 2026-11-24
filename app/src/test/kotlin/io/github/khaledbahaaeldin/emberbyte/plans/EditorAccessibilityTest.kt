package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h2400dp")
class EditorAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    private val state = PlanEditorUiState(
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
    fun touch_targets_headings_merged_preview_and_rtl_font_scale_2_pass() {
        rule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, fontScale = 2f),
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
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
        assertNoTouchTargetSmallerThan48dp(rule)
        val headings = rule.onAllNodes(isHeading()).fetchSemanticsNodes()
        assertTrue(headings.isNotEmpty())

        val previewNode = rule.onNodeWithTag("editor_preview_card").fetchSemanticsNode()
        val previewTexts = previewNode.config.getOrElse(SemanticsProperties.Text) { emptyList() }
            .joinToString(" ") { it.text }
        assertTrue(previewTexts.contains("10 GB of 20 GB"))
        rule.onNodeWithText("Edit plan").assertIsDisplayed()
    }
}
