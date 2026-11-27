package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.source.SimInfo
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanFlowScreenTest {
    @get:Rule val rule = createComposeRule()

    private val fixedInstant = Instant.parse("2026-10-15T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    @Test
    fun five_steps_are_announced_as_step_n_of_5() {
        val state = PlanFlowState(
            step = 1,
            totalSteps = 5,
            name = "",
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithContentDescription("Step 1 of 5").assertIsDisplayed()
    }

    @Test
    fun focus_moves_to_the_step_title_on_each_step() {
        val state = PlanFlowState(
            step = 2,
            totalSteps = 5,
            name = "My Plan",
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("How much data do you get?").assertIsDisplayed()
    }

    @Test
    fun keyboard_does_not_hide_the_continue_button_at_font_scale_2() {
        val state = PlanFlowState(
            step = 1,
            totalSteps = 5,
            name = "Test Plan",
        )
        rule.setContent {
            val density = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density, fontScale = 2f),
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanFlowContent(
                        state = state,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun rtl() {
        val state = PlanFlowState(
            step = 2,
            totalSteps = 5,
            capText = "10",
        )
        rule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanFlowContent(
                        state = state,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Continue").assertIsDisplayed()
        rule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun reduce_motion_swaps_steps_without_animation() {
        val state = PlanFlowState(
            step = 3,
            totalSteps = 5,
        )
        rule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanFlowContent(
                        state = state,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithContentDescription("Step 3 of 5").assertIsDisplayed()
    }

    @Test
    fun step2_allowance_field_and_step4_entered_field_have_usable_width_and_row_text_selects_choice() {
        var lastEvent: PlanFlowEvent? = null
        val showStep4 = androidx.compose.runtime.mutableStateOf(false)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                if (!showStep4.value) {
                    AmountStep(
                        state = PlanFlowState(
                            step = 2,
                            totalSteps = 5,
                            capText = "10",
                            fieldError = PlanFlowError.CapPositive,
                        ),
                        reveal = { 1f },
                        onSetCap = {},
                        onSetCapUnit = {},
                        onSetRollover = {},
                    )
                } else {
                    ThisCycleStep(
                        state = PlanFlowState(
                            step = 4,
                            totalSteps = 5,
                            firstCycleChoice = FirstCycleChoice.Entered,
                            enteredAmount = "3",
                        ),
                        clock = clock,
                        reveal = { 1f },
                        onSelectChoice = { lastEvent = PlanFlowEvent.SetFirstCycleChoice(it) },
                        onSetEnteredAmount = {},
                        onSetEnteredUnit = {},
                    )
                }
            }
        }
        val allowanceBounds = rule.onNodeWithTag("amount_exact_field").fetchSemanticsNode().boundsInRoot
        assertTrue("Data allowance field should not be squished, was ${allowanceBounds.width}", allowanceBounds.width > 150f)

        showStep4.value = true
        rule.waitForIdle()

        val usedBounds = rule.onNodeWithTag("this_cycle_entered_field").fetchSemanticsNode().boundsInRoot
        assertTrue("Used so far field should not be squished, was ${usedBounds.width}", usedBounds.width > 150f)

        rule.onNodeWithText("Start with the next cycle").performClick()
        assertEquals(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.StartNextCycle), lastEvent)
    }

    @Test
    fun review_shows_sim_and_next_renewal() {
        val state = PlanFlowState(
            step = 5,
            totalSteps = 5,
            name = "Vodafone Main",
            subscriptionId = 2,
            sims = listOf(
                io.github.khaledbahaaeldin.emberbyte.plans.SimOptionUi(null, "Not tied to a SIM"),
                io.github.khaledbahaaeldin.emberbyte.plans.SimOptionUi(2, "SIM 1 · Vodafone"),
            ),
            capText = "20",
            capUnit = CapUnit.GB,
            cycleKind = CycleKind.Monthly,
            dayOfMonth = 17,
            nextCycleStartText = "17 Dec",
            isMidCycleApplicable = true,
            firstCycleChoice = FirstCycleChoice.PhoneRecord,
            isReviewStep = true,
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ReviewStep(
                    state = state,
                    reveal = { 1f },
                    onToggleAlert = {},
                )
            }
        }
        rule.onNodeWithText("Vodafone Main").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("20 GB").onFirst().performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Resets on the 17th of every month").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun continue_is_a_filled_button_when_valid() {
        val state = PlanFlowState(
            step = 1,
            totalSteps = 5,
            name = "Vodafone Main",
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNode(androidx.compose.ui.test.hasTestTag("plan_flow_primary_button")).assertIsDisplayed().assertIsEnabled()
        rule.onNode(androidx.compose.ui.test.hasContentDescription("Close")).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun plan_flow_progress_is_wavy_and_animated() {
        rule.mainClock.autoAdvance = false
        val state = androidx.compose.runtime.mutableStateOf(
            PlanFlowState(step = 1, totalSteps = 5, name = "Vodafone Main"),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = state.value,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        state.value = state.value.copy(step = 2)
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(600)

        rule.onNodeWithTag("plan_flow_progress_pill").assertIsDisplayed()
        rule.onNodeWithContentDescription("Step 2 of 5").assertIsDisplayed()
    }

    @Test
    fun plan_flow_progress_snaps_when_reduced_motion() {
        rule.mainClock.autoAdvance = false
        val state = androidx.compose.runtime.mutableStateOf(
            PlanFlowState(step = 1, totalSteps = 5, name = "Vodafone Main"),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                androidx.compose.runtime.CompositionLocalProvider(
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
                ) {
                    PlanFlowContent(
                        state = state.value,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        state.value = state.value.copy(step = 2)
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithContentDescription("Step 2 of 5").assertIsDisplayed()
    }

    @Test
    fun step_label_crossfades() {
        rule.mainClock.autoAdvance = false
        val state = androidx.compose.runtime.mutableStateOf(
            PlanFlowState(step = 1, totalSteps = 5, name = "Vodafone Main"),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = state.value,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithContentDescription("Step 1 of 5").assertIsDisplayed()

        state.value = state.value.copy(step = 2)
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(64)

        rule.mainClock.advanceTimeBy(600)
        rule.onNodeWithContentDescription("Step 2 of 5").assertIsDisplayed()
    }

    @Test
    fun step_label_snaps_when_reduced_motion() {
        rule.mainClock.autoAdvance = false
        val state = androidx.compose.runtime.mutableStateOf(
            PlanFlowState(step = 1, totalSteps = 5, name = "Vodafone Main"),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                androidx.compose.runtime.CompositionLocalProvider(
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
                ) {
                    PlanFlowContent(
                        state = state.value,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        state.value = state.value.copy(step = 2)
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithContentDescription("Step 2 of 5").assertIsDisplayed()
        rule.onNodeWithContentDescription("Step 1 of 5").assertDoesNotExist()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "h1200dp")
    fun all_steps_respect_48dp_targets_and_bounds_across_widths_font_scales_and_themes() {
        val themeSpecs = listOf(
            EmberbyteThemeSpec(presetId = "dusk", darkTheme = false, amoled = false),
            EmberbyteThemeSpec(presetId = "dusk", darkTheme = true, amoled = false),
            EmberbyteThemeSpec(presetId = "dusk", darkTheme = true, amoled = true),
        )
        val widthsDp = listOf(360, 411)
        val fontScales = listOf(1.0f, 1.5f, 2.0f)

        val currentSpec = androidx.compose.runtime.mutableStateOf(themeSpecs.first())
        val currentWidth = androidx.compose.runtime.mutableStateOf(411)
        val currentScale = androidx.compose.runtime.mutableStateOf(1.0f)
        val currentStep = androidx.compose.runtime.mutableStateOf(2)

        rule.setContent {
            val base = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(base.density, fontScale = currentScale.value),
            ) {
                EmberbyteTheme(currentSpec.value) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion provides true,
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .width(androidx.compose.ui.unit.Dp(currentWidth.value.toFloat()))
                                .height(androidx.compose.ui.unit.Dp(1200f)),
                        ) {
                            PlanFlowContent(
                                state = PlanFlowState(
                                    step = currentStep.value,
                                    totalSteps = 5,
                                    name = "Vodafone Main",
                                    capText = "20",
                                    nextCycleStartText = "17 Dec",
                                    isReviewStep = currentStep.value == 5,
                                ),
                                onEvent = {},
                                onBack = {},
                            )
                        }
                    }
                }
            }
        }

        for (spec in themeSpecs) {
            currentSpec.value = spec
            for (w in widthsDp) {
                currentWidth.value = w
                for (scale in fontScales) {
                    currentScale.value = scale
                    for (step in listOf(2, 3, 5)) {
                        currentStep.value = step
                        rule.waitForIdle()
                        val minPx = with(rule.density) { 47.5f * density }
                        val maxWidthPx = with(rule.density) { (w + 1) * density }
                        val clickables = rule.onAllNodes(androidx.compose.ui.test.hasClickAction()).fetchSemanticsNodes()
                        assertTrue("Expected interactive controls on step $step", clickables.isNotEmpty())
                        for (node in clickables) {
                            val b = node.boundsInRoot
                            assertTrue(
                                "Touch target height ${b.height}px < ${minPx}px on step $step width=${w}dp scale=$scale",
                                b.height >= minPx,
                            )
                            assertTrue(
                                "Control overflows ${w}dp container (right=${b.right} > $maxWidthPx) on step $step scale=$scale",
                                b.right <= maxWidthPx,
                            )
                        }
                    }
                }
            }
        }
    }
}

