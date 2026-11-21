package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.BudgetPeriod
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDetailAccessibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private val sampleAppDetailState = AppDetailUiState(
        packageName = "com.video",
        label = "Video",
        totalBytes = 1_000_000_000L,
        mobileBytes = 600_000_000L,
        wifiBytes = 400_000_000L,
        bars = listOf(
            BarUi(label = "Mon", bytes = 100_000_000L, description = "Monday, 100 megabytes"),
            BarUi(label = "Tue", bytes = 200_000_000L, description = "Tuesday, 200 megabytes"),
            BarUi(label = "Wed", bytes = 2_100_000_000L, description = "Wednesday, 2.10 gigabytes"),
        ),
        units = ByteUnits.DECIMAL,
        loaded = true,
        budget = BudgetUi(
            id = 1L,
            fraction = 0.8f,
            ringState = BudgetStateUi.Warning,
            percentText = "80%",
            usedText = "800 MB of 1 GB",
            periodText = "today",
            enabled = true,
        ),
        spikes = listOf(
            SpikeRowUi(1L, "Mon 10:00–12:00", "500 MB", "5x normal"),
        ),
    )

    private fun show(state: AppDetailUiState = sampleAppDetailState) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppDetailContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                    onOpenSettings = {},
                )
            }
        }
    }

    @Test
    fun legend_color_meets_wcag_aa_contrast() {
        var currentSpec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
        var expectedTextColor = Color.Unspecified
        var expectedBgColor = Color.Unspecified
        rule.setContent {
            EmberbyteTheme(currentSpec) {
                val palette = LocalEmberbytePalette.current
                expectedTextColor = palette.text
                expectedBgColor = palette.background
                AppDetailContent(
                    state = sampleAppDetailState,
                    onEvent = {},
                    onBack = {},
                    onOpenSettings = {},
                )
            }
        }
        val presets = listOf("dusk", "solarized", "everforest", "ember", "dracula", "nord")
        for (presetId in presets) {
            for (dark in listOf(false, true)) {
                currentSpec = EmberbyteThemeSpec(presetId = presetId, darkTheme = dark)
                rule.waitForIdle()

                val mobileResults = mutableListOf<TextLayoutResult>()
                rule.onNodeWithTag("app_detail_mobile_label", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(mobileResults) }
                val mobileColor = mobileResults.first().layoutInput.style.color
                assertEquals(
                    "Mobile text color must bind to palette.text on $presetId dark=$dark",
                    expectedTextColor,
                    mobileColor,
                )

                val wifiResults = mutableListOf<TextLayoutResult>()
                rule.onNodeWithTag("app_detail_wifi_label", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(wifiResults) }
                val wifiColor = wifiResults.first().layoutInput.style.color
                assertEquals(
                    "Wi-Fi text color must bind to palette.text on $presetId dark=$dark",
                    expectedTextColor,
                    wifiColor,
                )

                val fg = mobileColor.compositeOver(expectedBgColor)
                val l1 = fg.luminance() + 0.05
                val l2 = expectedBgColor.luminance() + 0.05
                val ratio = maxOf(l1, l2) / minOf(l1, l2)
                assertTrue("legend text on $presetId (dark=$dark) must meet 4.5:1, was $ratio", ratio >= 4.5)
            }
        }
    }

    @Test
    fun app_detail_icon_buttons_have_content_descriptions() {
        show()
        val backButton = rule.onNodeWithContentDescription("Back")
        backButton.assertIsDisplayed()
    }

    @Test
    fun icon_buttons_keep_48dp_and_their_content_description() {
        show()
        val backButton = rule.onNodeWithContentDescription("Back")
        backButton
            .assertIsDisplayed()
            .performTouchInput { down(center); up() }
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun app_detail_chart_exposes_summary_node() {
        show()
        val summaryNode = rule.onNodeWithContentDescription(
            "Daily usage, last 7 days, highest Wednesday, 2.10 gigabytes",
        )
        summaryNode.assertExists()
        val summaryBounds = summaryNode.getBoundsInRoot()
        val width = summaryBounds.right - summaryBounds.left
        val height = summaryBounds.bottom - summaryBounds.top
        assertTrue("Summary node width must be >= 48dp but was $width", width >= 48.dp)
        assertTrue("Summary node height must be >= 48dp but was $height", height >= 48.dp)
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun app_detail_touch_targets_are_at_least_48dp() {
        show()
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun budget_editor_dialog_touch_targets_are_at_least_48dp() {
        val editor = BudgetEditorUi(
            limitText = "1",
            unit = CapUnit.GB,
            period = BudgetPeriod.CYCLE,
            planId = 1L,
            warnAtPercent = 80,
            errors = emptyMap(),
            saving = false,
            isEdit = false,
        )
        show(
            sampleAppDetailState.copy(
                editor = editor,
                planOptions = listOf(PlanOptionUi(1L, "Main SIM")),
            ),
        )
        assertNoTouchTargetSmallerThan48dp(rule)
    }
}
