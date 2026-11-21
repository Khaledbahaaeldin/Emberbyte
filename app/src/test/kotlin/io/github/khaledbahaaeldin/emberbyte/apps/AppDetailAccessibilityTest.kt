package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.Density
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
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
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
                SideEffect {
                    expectedTextColor = palette.text
                    expectedBgColor = palette.background
                }
                AppDetailContent(
                    state = sampleAppDetailState,
                    onEvent = {},
                    onBack = {},
                    onOpenSettings = {},
                )
            }
        }
        val variants = listOf(
            false to false,
            true to false,
            true to true,
        )
        for (preset in ThemePresets) {
            for ((dark, amoled) in variants) {
                currentSpec = EmberbyteThemeSpec(presetId = preset.id, darkTheme = dark, amoled = amoled)
                rule.waitForIdle()

                val mobileResults = mutableListOf<TextLayoutResult>()
                rule.onNodeWithTag("app_detail_mobile_label", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(mobileResults) }
                val mobileColor = mobileResults.first().layoutInput.style.color
                assertEquals(
                    "Mobile text color must bind to palette.text on ${preset.id} dark=$dark amoled=$amoled",
                    expectedTextColor,
                    mobileColor,
                )

                val wifiResults = mutableListOf<TextLayoutResult>()
                rule.onNodeWithTag("app_detail_wifi_label", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(wifiResults) }
                val wifiColor = wifiResults.first().layoutInput.style.color
                assertEquals(
                    "Wi-Fi text color must bind to palette.text on ${preset.id} dark=$dark amoled=$amoled",
                    expectedTextColor,
                    wifiColor,
                )

                for ((label, color) in listOf("Mobile" to mobileColor, "Wi-Fi" to wifiColor)) {
                    val fg = color.compositeOver(expectedBgColor)
                    val l1 = fg.luminance() + 0.05
                    val l2 = expectedBgColor.luminance() + 0.05
                    val ratio = maxOf(l1, l2) / minOf(l1, l2)
                    assertTrue(
                        "$label legend text on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1, was $ratio",
                        ratio >= 4.5,
                    )
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun set_a_budget_link_meets_wcag_aa_contrast_across_all_palettes() {
        var currentSpec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
        var expectedLinkColor = Color.Unspecified
        var expectedSurface2 = Color.Unspecified
        var expectedBackground = Color.Unspecified
        rule.setContent {
            EmberbyteTheme(currentSpec) {
                val palette = LocalEmberbytePalette.current
                SideEffect {
                    expectedLinkColor = palette.linkText
                    expectedSurface2 = palette.surface2
                    expectedBackground = palette.background
                }
                AppDetailContent(
                    state = sampleAppDetailState.copy(budget = null),
                    onEvent = {},
                    onBack = {},
                    onOpenSettings = {},
                )
            }
        }
        val variants = listOf(false to false, true to false, true to true)
        for (preset in ThemePresets) {
            for ((dark, amoled) in variants) {
                currentSpec = EmberbyteThemeSpec(presetId = preset.id, darkTheme = dark, amoled = amoled)
                rule.waitForIdle()
                for ((label, bg) in listOf("Set a budget" to expectedSurface2, "App settings" to expectedBackground)) {
                    val results = mutableListOf<TextLayoutResult>()
                    rule.onNodeWithText(label, useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    val actualColor = results.first().layoutInput.style.color
                    assertEquals(
                        "$label must bind to palette.linkText on ${preset.id} dark=$dark amoled=$amoled",
                        expectedLinkColor,
                        actualColor,
                    )
                    val fg = actualColor.compositeOver(bg)
                    val l1 = fg.luminance() + 0.05
                    val l2 = bg.luminance() + 0.05
                    val ratio = maxOf(l1, l2) / minOf(l1, l2)
                    assertTrue(
                        "$label on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1, was $ratio",
                        ratio >= 4.5,
                    )
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun spikes_warn_tile_and_edit_link_meet_wcag_aa_contrast_across_all_palettes() {
        val sampleBudget = BudgetUi(
            id = 1L,
            fraction = 0.5f,
            ringState = BudgetStateUi.Ok,
            percentText = "50%",
            usedText = "500 MB of 1 GB",
            periodText = "Cycle",
            enabled = true,
        )
        val sampleSpike = SpikeRowUi(
            id = 1L,
            whenText = "Yesterday 14:00",
            bytesText = "350 MB",
            multipleText = "4.2x normal",
        )
        var currentSpec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
        var expectedOnWarn = Color.Unspecified
        var expectedWarn = Color.Unspecified
        var expectedLinkColor = Color.Unspecified
        var expectedSurface2 = Color.Unspecified
        var expectedDanger = Color.Unspecified
        rule.setContent {
            EmberbyteTheme(currentSpec) {
                val palette = LocalEmberbytePalette.current
                SideEffect {
                    expectedOnWarn = palette.onWarn
                    expectedWarn = palette.warn
                    expectedLinkColor = palette.linkText
                    expectedSurface2 = palette.surface2
                    expectedDanger = palette.danger
                }
                AppDetailContent(
                    state = sampleAppDetailState.copy(
                        budget = sampleBudget,
                        spikes = listOf(sampleSpike),
                    ),
                    onEvent = {},
                    onBack = {},
                    onOpenSettings = {},
                )
            }
        }
        val variants = listOf(false to false, true to false, true to true)
        for (preset in ThemePresets) {
            for ((dark, amoled) in variants) {
                currentSpec = EmberbyteThemeSpec(presetId = preset.id, darkTheme = dark, amoled = amoled)
                rule.waitForIdle()

                for (warnLabel in listOf("Yesterday 14:00", "350 MB", "·", "4.2x normal", "Dismiss")) {
                    val results = mutableListOf<TextLayoutResult>()
                    rule.onNodeWithText(warnLabel, useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    val actualColor = results.first().layoutInput.style.color
                    assertEquals(
                        "$warnLabel must bind to palette.onWarn on ${preset.id} dark=$dark amoled=$amoled",
                        expectedOnWarn,
                        actualColor,
                    )
                    val fg = actualColor.compositeOver(expectedWarn)
                    val l1 = fg.luminance() + 0.05
                    val l2 = expectedWarn.luminance() + 0.05
                    val ratio = maxOf(l1, l2) / minOf(l1, l2)
                    assertTrue(
                        "$warnLabel on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1 on warn, was $ratio",
                        ratio >= 4.5,
                    )
                }

                val editResults = mutableListOf<TextLayoutResult>()
                rule.onNodeWithText("Edit", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(editResults) }
                val editColor = editResults.first().layoutInput.style.color
                assertEquals(
                    "Edit must bind to palette.linkText on ${preset.id} dark=$dark amoled=$amoled",
                    expectedLinkColor,
                    editColor,
                )
                val fg = editColor.compositeOver(expectedSurface2)
                val l1 = fg.luminance() + 0.05
                val l2 = expectedSurface2.luminance() + 0.05
                val ratio = maxOf(l1, l2) / minOf(l1, l2)
                assertTrue(
                    "Edit on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1 on surface2, was $ratio",
                    ratio >= 4.5,
                )

                val removeResults = mutableListOf<TextLayoutResult>()
                rule.onNodeWithText("Remove", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(removeResults) }
                val removeColor = removeResults.first().layoutInput.style.color
                assertEquals(
                    "Remove must bind to palette.danger on ${preset.id} dark=$dark amoled=$amoled",
                    expectedDanger,
                    removeColor,
                )
                val removeFg = removeColor.compositeOver(expectedSurface2)
                val removeL1 = removeFg.luminance() + 0.05
                val removeL2 = expectedSurface2.luminance() + 0.05
                val removeRatio = maxOf(removeL1, removeL2) / minOf(removeL1, removeL2)
                assertTrue(
                    "Remove on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1 on surface2, was $removeRatio",
                    removeRatio >= 4.5,
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun budget_update_and_removal_crossfade_safely() {
        val initialBudget = BudgetUi(
            id = 1L,
            fraction = 0.5f,
            ringState = BudgetStateUi.Ok,
            percentText = "50%",
            usedText = "500 MB of 1 GB",
            periodText = "Cycle",
            enabled = true,
        )
        val updatedBudget = initialBudget.copy(
            fraction = 0.7f,
            percentText = "70%",
            usedText = "700 MB of 1 GB",
        )
        var state by mutableStateOf(sampleAppDetailState.copy(budget = initialBudget))
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
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("500 MB of 1 GB"))
        rule.onNodeWithText("500 MB of 1 GB").assertIsDisplayed()

        // Updating live budget values must update text without crashing
        state = state.copy(budget = updatedBudget)
        rule.waitForIdle()
        rule.onNodeWithText("700 MB of 1 GB").assertIsDisplayed()

        // Removing budget must crossfade out safely without throwing checkNotNull
        state = state.copy(budget = null)
        rule.waitForIdle()
        rule.onNodeWithText("Set a budget").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun budget_and_loading_snap_under_reduced_motion() {
        val initialBudget = BudgetUi(
            id = 1L,
            fraction = 0.5f,
            ringState = BudgetStateUi.Ok,
            percentText = "50%",
            usedText = "500 MB of 1 GB",
            periodText = "Cycle",
            enabled = true,
        )
        var state by mutableStateOf(sampleAppDetailState.copy(loaded = false, budget = initialBudget))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    AppDetailContent(
                        state = state,
                        onEvent = {},
                        onBack = {},
                        onOpenSettings = {},
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Loading app details").assertIsDisplayed()

        // Snaps out of loading to content within 2 frames (32ms) under reduced motion
        state = state.copy(loaded = true)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithContentDescription("Loading app details").assertDoesNotExist()
        rule.onNodeWithText("Last 7 days").assertIsDisplayed()

        // Scroll to Data budget card and verify budget removal snaps within 2 frames (32ms) under reduced motion
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("500 MB of 1 GB"))
        rule.onNodeWithText("500 MB of 1 GB").assertIsDisplayed()

        state = state.copy(budget = null)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithText("500 MB of 1 GB").assertDoesNotExist()
        rule.onNodeWithText("Set a budget").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun font_scale_2_no_clip_on_narrow_screen() {
        var maxWidthPx = 0f
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2.0f)) {
                maxWidthPx = with(LocalDensity.current) { 360.dp.toPx() }
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppDetailContent(
                        state = sampleAppDetailState.copy(budget = null),
                        onEvent = {},
                        onBack = {},
                        onOpenSettings = {},
                    )
                }
            }
        }
        for (label in listOf("No budget set", "Set a budget")) {
            val node = rule.onNodeWithText(label, useUnmergedTree = true)
            node.assertIsDisplayed()
            val bounds = node.getBoundsInRoot()
            assertTrue("$label must have positive size", (bounds.right - bounds.left).value > 0f && (bounds.bottom - bounds.top).value > 0f)
            assertTrue("$label must fit within 360dp", bounds.right.value <= 360f + 1f)
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("$label must not overflow visually", !layouts.first().hasVisualOverflow)
        }
        val noBudgetBounds = rule.onNodeWithText("No budget set", useUnmergedTree = true).getBoundsInRoot()
        val setBudgetBounds = rule.onNodeWithText("Set a budget", useUnmergedTree = true).getBoundsInRoot()
        assertTrue(
            "No budget set and Set a budget must not overlap horizontally",
            noBudgetBounds.right <= setBudgetBounds.left + 1.dp,
        )
        assertTrue(maxWidthPx > 0f)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun font_scale_2_active_budget_and_split_tiles_no_clip_on_narrow_screen() {
        val activeBudget = BudgetUi(
            id = 1L,
            fraction = 0.5f,
            ringState = BudgetStateUi.Ok,
            percentText = "50%",
            usedText = "500 MB of 1 GB",
            periodText = "Cycle",
            enabled = true,
        )
        val spike = SpikeRowUi(
            id = 1L,
            whenText = "Yesterday 14:00",
            bytesText = "350 MB",
            multipleText = "4.2x normal",
        )
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppDetailContent(
                        state = sampleAppDetailState.copy(
                            foregroundBytes = 700_000_000L,
                            backgroundBytes = 300_000_000L,
                            budget = activeBudget,
                            spikes = listOf(spike),
                        ),
                        onEvent = {},
                        onBack = {},
                        onOpenSettings = {},
                    )
                }
            }
        }
        for (label in listOf("Foreground", "Background", "Edit", "Remove", "Dismiss")) {
            val node = rule.onNodeWithText(label, useUnmergedTree = true)
            node.assertIsDisplayed()
            val bounds = node.getBoundsInRoot()
            assertTrue("$label must have positive size", (bounds.right - bounds.left).value > 0f && (bounds.bottom - bounds.top).value > 0f)
            assertTrue("$label must fit within 360dp", bounds.right.value <= 360f + 1f)
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("$label must not overflow visually", !layouts.first().hasVisualOverflow)
            assertEquals("$label must not wrap mid-word on narrow screen", 1, layouts.first().lineCount)
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
