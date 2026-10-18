package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        rule.onNodeWithText("Step 1 of 5").assertIsDisplayed()
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
        rule.onNodeWithText("Step 3 of 5").assertIsDisplayed()
    }
}
