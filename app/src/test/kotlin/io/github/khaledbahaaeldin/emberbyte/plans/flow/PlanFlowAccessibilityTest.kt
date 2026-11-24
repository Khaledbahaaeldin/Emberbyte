package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
class PlanFlowAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun step1_touch_targets_are_at_least_48dp_and_render_at_font_scale_2_and_rtl() {
        rule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, fontScale = 2f),
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanFlowContent(
                        state = PlanFlowState(step = 1, totalSteps = 5),
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        assertNoTouchTargetSmallerThan48dp(rule)
        rule.onNodeWithText("What should we call it?").assertIsDisplayed()
        rule.onNodeWithContentDescription("Step 1 of 5").assertIsDisplayed()
    }

    @Test
    fun step3_day_grid_touch_targets_are_at_least_48dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowContent(
                    state = PlanFlowState(step = 3, totalSteps = 5),
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        assertNoTouchTargetSmallerThan48dp(rule)
        rule.onNodeWithTag("day_grid_1").assertIsDisplayed()
    }
}
