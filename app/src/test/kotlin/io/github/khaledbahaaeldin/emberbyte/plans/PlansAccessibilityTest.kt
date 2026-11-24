package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class PlansAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    private val samplePlanCard = PlanCardUi(
        id = 1L,
        name = "Unlimited Plus",
        simLabel = "SIM 1",
        fraction = 0.5f,
        ringState = BudgetStateUi.Ok,
        percentText = "50%",
        usedText = "5.0 GB of 10.0 GB",
        remainingText = "5.0 GB left",
        daysLeftText = "15 days left",
        forecastText = null,
        isEstimated = false,
        chips = emptyList(),
        renewsText = "Renews 28 Nov",
        capText = "10.0 GB",
    )

    private fun show(state: PlansUiState) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = state,
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }
    }

    @Test
    fun plans_screen_touch_targets_are_at_least_48dp_with_plans() {
        show(
            PlansUiState(
                loaded = true,
                front = samplePlanCard,
                tiles = listOf(samplePlanCard.copy(id = 2L, name = "Second Plan")),
            ),
        )
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun icon_buttons_keep_48dp_and_their_content_description() {
        val secondPlan = samplePlanCard.copy(id = 2L, name = "Second Plan")
        show(
            PlansUiState(
                loaded = true,
                front = samplePlanCard,
                tiles = listOf(secondPlan),
            ),
        )

        val cardMenuButton = rule.onNodeWithContentDescription("Plan options for ${samplePlanCard.name}")
        cardMenuButton.assertIsDisplayed().performTouchInput { down(center); up() }

        val tileMenuButton = rule.onNodeWithContentDescription("Plan options for ${secondPlan.name}")
        tileMenuButton.assertIsDisplayed().performTouchInput { down(center); up() }

        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun plans_screen_touch_targets_are_at_least_48dp_empty_state() {
        show(PlansUiState(loaded = true))
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun plans_action_buttons_have_non_empty_accessible_names() {
        show(
            PlansUiState(
                loaded = true,
                front = samplePlanCard,
            ),
        )

        val newPlanNode = rule.onNodeWithText("New plan")
        newPlanNode.assertIsDisplayed()
        newPlanNode.assert(hasClickAction())
    }
}
