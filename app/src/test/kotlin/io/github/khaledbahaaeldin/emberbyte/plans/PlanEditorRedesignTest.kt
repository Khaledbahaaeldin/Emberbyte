package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasText
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.Confidence
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.plans.flow.AmountStep
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import io.github.khaledbahaaeldin.emberbyte.plans.flow.NameStep
import io.github.khaledbahaaeldin.emberbyte.plans.flow.PlanFlowState
import io.github.khaledbahaaeldin.emberbyte.plans.flow.ResetStep
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class PlanEditorRedesignTest {
    @get:Rule val rule = createComposeRule()

    private val fullState = PlanEditorUiState(
        isNew = false,
        planId = 1L,
        loading = false,
        sims = listOf(SimOptionUi(null, "Not tied to a SIM"), SimOptionUi(5, "SIM 1 · Alpha")),
        availableApps = listOf(
            AppChoiceUi("com.whatsapp", "WhatsApp"),
            AppChoiceUi("org.telegram.messenger", "Telegram"),
        ),
        whatIf = WhatIfUi(
            amountText = "500",
            unit = CapUnit.MB,
            currentText = "Won't run out this cycle",
            whatIfText = "runs out Sat 22 Nov",
            error = null,
        ),
        form = PlanEditorForm(
            name = "Main SIM",
            capText = "10",
            capUnit = CapUnit.GB,
            cycleKind = CycleKind.EveryNDays,
            everyDays = 30,
            rolloverKind = RolloverKind.Capped,
            rolloverCapText = "5",
            addOns = listOf(
                AddOnDraft(
                    label = "Boost",
                    amountText = "2",
                    unit = CapUnit.GB,
                    validFrom = LocalDate.of(2026, 10, 1),
                    validUntil = LocalDate.of(2026, 10, 31),
                ),
            ),
            freeRules = listOf(
                FreeRuleDraft(
                    label = "Night",
                    days = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                ),
            ),
            firstCycleChoice = FirstCycleChoice.Entered,
            enteredAmount = "1",
        ),
    )

    private fun hasAncestorWithTag(node: SemanticsNode, tag: String): Boolean {
        var cur = node.parent
        while (cur != null) {
            if (cur.config.getOrNull(SemanticsProperties.TestTag) == tag) return true
            cur = cur.parent
        }
        return false
    }

    private fun hasSelectableGroupAncestor(node: SemanticsNode): Boolean {
        var cur = node.parent
        while (cur != null) {
            if (cur.config.getOrNull(SemanticsProperties.SelectableGroup) != null) return true
            cur = cur.parent
        }
        return false
    }

    @Test
    fun editor_has_no_radio_buttons_or_loose_chips() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = fullState,
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }

        // Sections must be grouped into BentoTiles
        val bentoTiles = rule.onAllNodesWithTag("bento_tile_standard", useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Expected BentoTile sections in PlanEditor, found ${bentoTiles.size}", bentoTiles.size >= 6)

        // Every Role.RadioButton outside the SIM list must belong to a ConnectedButtonGroup (SelectableGroup), never a stock RadioButton row
        val radioNodes = rule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
        ).fetchSemanticsNodes()
        assertTrue(radioNodes.isNotEmpty())
        for (node in radioNodes) {
            val inSimList = hasAncestorWithTag(node, "sim_radio_list")
            val inConnectedGroup = hasSelectableGroupAncestor(node)
            assertTrue(
                "Found stock RadioButton outside SIM list and ConnectedButtonGroup: ${node.config}",
                inSimList || inConnectedGroup,
            )
        }

        // Single-choice units/rollover must not be Role.Checkbox FilterChips; only Alerts (4) + FreeWindow weekdays (8 incl All days) may use Checkbox chips
        val checkboxChips = rule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox),
        ).fetchSemanticsNodes()
        for (chip in checkboxChips) {
            val inMultiSelect = hasAncestorWithTag(chip, "alerts_multi_select") ||
                hasAncestorWithTag(chip, "free_window_days_multi_select")
            assertTrue(
                "Found loose single-choice FilterChip outside multi-select groups: ${chip.config}",
                inMultiSelect,
            )
        }
    }

    @Test
    fun labels_match_the_new_plan_flow() {
        var showEditor by mutableStateOf(false)
        var flowStep by mutableStateOf(1)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                if (!showEditor) {
                    val flowState = PlanFlowState(cycleKind = CycleKind.EveryNDays)
                    if (flowStep == 1) {
                        NameStep(
                            state = flowState,
                            hasPhonePermission = true,
                            reveal = { 1f },
                            onSetName = {},
                            onSetSim = {},
                            onRequestPhonePermission = {},
                        )
                    } else {
                        ResetStep(
                            state = flowState,
                            clock = java.time.Clock.systemUTC(),
                            reveal = { 1f },
                            onSetCycleKind = {},
                            onSetDayOfMonth = {},
                            onSetEveryDaysText = {},
                            onSetEveryDays = {},
                        )
                    }
                } else {
                    PlanEditorContent(
                        state = fullState,
                        onEvent = {},
                        onBack = {},
                        onPrompt = {},
                    )
                }
            }
        }

        // Verify shared vocabulary exists in PlanFlow
        rule.onAllNodesWithText("Plan name").onFirst().assertIsDisplayed()
        flowStep = 3
        rule.waitForIdle()
        rule.onAllNodesWithText("Monthly").onFirst().assertIsDisplayed()

        showEditor = true
        rule.waitForIdle()

        // Verify exact same vocabulary is used in PlanEditor (never "Number of days")
        for (shared in listOf("Plan name", "Cap", "Monthly", "Every N days", "Days per cycle")) {
            rule.onAllNodesWithText(shared).onFirst().assertIsDisplayed()
        }
        rule.onNodeWithText("Number of days").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w411dp-h640dp")
    fun save_is_visible_after_scrolling_to_the_bottom() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Box(Modifier.size(411.dp, 640.dp)) {
                    PlanEditorContent(
                        state = fullState,
                        onEvent = {},
                        onBack = {},
                        onPrompt = {},
                    )
                }
            }
        }

        // Save and Discard changes are visible immediately at the top without scrolling
        val saveInitial = rule.onNodeWithText("Save")
        saveInitial.assertIsDisplayed()
        val initialTop = saveInitial.fetchSemanticsNode().boundsInRoot.top

        // Scroll to the very bottom ("Delete plan" in Danger zone)
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Delete plan"))
        rule.waitForIdle()

        // Sticky Save stays pinned at the exact same bottomBar Y coordinate
        val saveAfter = rule.onNodeWithText("Save")
        saveAfter.assertIsDisplayed()
        assertEquals(initialTop, saveAfter.fetchSemanticsNode().boundsInRoot.top, 1f)
    }

    @Test
    fun new_plan_defaults_to_80_and_100_alerts() {
        val newState = PlanEditorUiState(
            isNew = true,
            loading = false,
            form = PlanEditorForm(),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = newState,
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }

        rule.onNodeWithText("Notify me when the plan reaches…").assertIsDisplayed()
        rule.onNodeWithText("Selecting none turns plan alerts off").assertIsDisplayed()
        rule.onNodeWithText("80%").assertIsSelected()
        rule.onNodeWithText("100%").assertIsSelected()
    }

    @Test
    fun free_window_picks_an_app_without_typing_a_package() {
        var latestDraft: FreeRuleDraft? = null
        val initialRule = FreeRuleDraft(label = "Social", days = setOf(DayOfWeek.MONDAY))
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = fullState.copy(form = fullState.form.copy(freeRules = listOf(initialRule))),
                    onEvent = { ev ->
                        if (ev is PlanEditorEvent.UpdateFreeRule) latestDraft = ev.draft
                    },
                    onBack = {},
                    onPrompt = {},
                )
            }
        }

        // No raw "App package" text field
        rule.onNodeWithText("App package").assertDoesNotExist()
        // "All days" toggle is present
        rule.onNodeWithText("All days").assertIsDisplayed()

        // Tap app picker and choose WhatsApp without typing a package name
        rule.onNodeWithText("All apps", substring = true).performClick()
        rule.onNodeWithText("WhatsApp").assertIsDisplayed().performClick()
        assertEquals("com.whatsapp", latestDraft?.packageName)
    }

    @Test
    fun what_if_does_not_mark_the_plan_dirty() {
        var backCalled = false
        val events = mutableListOf<PlanEditorEvent>()
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = fullState.copy(isDirty = false),
                    onEvent = { events += it },
                    onBack = { backCalled = true },
                    onPrompt = {},
                )
            }
        }

        // What-if result renders inside an inline result chip
        rule.onNodeWithTag("what_if_result_chip", useUnmergedTree = true).assertIsDisplayed()

        // Typing into Extra per day emits SetWhatIfAmount and keeps isDirty false so Back leaves without Discard dialog
        rule.onNodeWithText("Extra per day").performTextInput("1")
        assertTrue(events.any { it is PlanEditorEvent.SetWhatIfAmount })
        assertFalse(fullState.isDirty)
    }

    @Test
    fun what_if_result_shows_a_full_date() {
        // Thursday 19 Nov 2026 -> Saturday 21 Nov / Sunday 22 Nov (within 6 days must STILL show "Sun 22 Nov", never "Sun" alone!)
        val now = Instant.parse("2026-11-19T12:00:00Z")
        val sat21Nov = Instant.parse("2026-11-21T12:00:00Z")
        val forecast = Forecast(
            planId = 1L,
            runOutEarliest = sat21Nov,
            runOutExpected = sat21Nov,
            runOutLatest = sat21Nov,
            projectedCycleEndBytes = 12_000_000_000L,
            confidence = Confidence.HIGH,
            historyDays = 14,
        )
        val sentence = whatIfSentence(forecast, remainingBytes = 2_000_000_000L, now = now, zone = ZoneOffset.UTC, locale = Locale.ENGLISH)
        assertEquals("runs out Sat 21 Nov", sentence)
    }

    @Test
    fun switching_cycle_kind_animates_height() {
        var kind by mutableStateOf(CycleKind.Monthly)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = fullState.copy(form = fullState.form.copy(cycleKind = kind)),
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        rule.mainClock.advanceTimeBy(1000L)

        val startHeight = rule.onNodeWithTag("cap_and_cycle_card", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
        kind = CycleKind.EveryNDays
        Snapshot.sendApplyNotifications()

        val seen = mutableSetOf<Float>()
        repeat(35) {
            rule.mainClock.advanceTimeBy(16L)
            val h = rule.onNodeWithTag("cap_and_cycle_card", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
            seen.add(h)
        }
        val endHeight = rule.onNodeWithTag("cap_and_cycle_card", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
        val inBetween = seen.filter { it > minOf(startHeight, endHeight) + 0.5f && it < maxOf(startHeight, endHeight) - 0.5f }
        assertTrue("Expected > 1 intermediate heights when switching cycle kind, got $inBetween (start=$startHeight, end=$endHeight)", inBetween.size > 1)
    }

    @Test
    fun reduced_motion_snaps() {
        var kind by mutableStateOf(CycleKind.Monthly)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    PlanEditorContent(
                        state = fullState.copy(form = fullState.form.copy(cycleKind = kind)),
                        onEvent = {},
                        onBack = {},
                        onPrompt = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(200L)

        val startHeight = rule.onNodeWithTag("cap_and_cycle_card", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
        kind = CycleKind.EveryNDays
        Snapshot.sendApplyNotifications()

        val seen = mutableSetOf<Float>()
        repeat(10) {
            rule.mainClock.advanceTimeBy(16L)
            val h = rule.onNodeWithTag("cap_and_cycle_card", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
            seen.add(h)
        }
        val settledHeight = rule.onNodeWithTag("cap_and_cycle_card", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
        val inBetween = seen.filter { it > minOf(startHeight, settledHeight) + 0.5f && it < maxOf(startHeight, settledHeight) - 0.5f }

        assertTrue("Height should change between Monthly ($startHeight) and EveryNDays ($settledHeight)", settledHeight != startHeight)
        assertTrue("Reduced motion must snap without intermediate heights, got $inBetween", inBetween.isEmpty())
    }
}
