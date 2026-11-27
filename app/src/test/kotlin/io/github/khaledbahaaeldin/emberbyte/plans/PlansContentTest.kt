package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class PlansContentTest {
    @get:Rule val rule = createComposeRule()

    private fun sampleCard(id: Long = 1L, name: String = "Main SIM") = PlanCardUi(
        id = id,
        name = name,
        simLabel = "SIM 1 · Alpha",
        fraction = 0.5f,
        ringState = BudgetStateUi.Ok,
        percentText = "50%",
        usedText = "5.00 GB of 10.0 GB",
        remainingText = "5.00 GB left",
        daysLeftText = "15 days left",
        forecastText = null,
        isEstimated = false,
        chips = emptyList(),
    )

    private fun show(
        state: PlansUiState,
        onEvent: (PlansEvent) -> Unit = {},
        onOpenPlan: (Long) -> Unit = {},
        onNewPlan: () -> Unit = {},
    ) = rule.setContent {
        EmberbyteTheme(EmberbyteThemeSpec.Default) {
            PlansContent(
                state = state,
                onEvent = onEvent,
                onOpenPlan = onOpenPlan,
                onNewPlan = onNewPlan,
            )
        }
    }

    @Test fun cards_render_and_tapping_front_opens_plan() {
        var openedId: Long? = null
        val card = sampleCard(id = 42L, name = "Primary SIM")
        show(
            state = PlansUiState(front = card, loaded = true),
            onOpenPlan = { openedId = it },
        )

        rule.onNodeWithText("Primary SIM").assertIsDisplayed()
        rule.onNodeWithText("Primary SIM").performClick()
        assertEquals(42L, openedId)
    }

    @Test fun new_plan_button_calls_on_new_plan() {
        var newPlanCalled = false
        show(
            state = PlansUiState(loaded = true),
            onNewPlan = { newPlanCalled = true },
        )

        rule.onNodeWithText("New plan").assertIsDisplayed()
        rule.onNodeWithText("New plan").performClick()
        assertEquals(true, newPlanCalled)
    }

    @Test fun empty_state_shows_when_no_cards_and_loaded_true() {
        var newPlanCalled = false
        show(
            state = PlansUiState(loaded = true),
            onNewPlan = { newPlanCalled = true },
        )

        rule.onNodeWithText("Nothing to track yet").assertIsDisplayed()
        rule.onNodeWithText("Set up your first plan").assertIsDisplayed().performClick()
        assertEquals(true, newPlanCalled)
    }

    @Test fun empty_state_does_not_show_when_loaded_false() {
        show(state = PlansUiState(loaded = false))
        rule.onNodeWithText("Nothing to track yet").assertDoesNotExist()
        rule.onNodeWithText("Set up your first plan").assertDoesNotExist()
    }

    @Test fun loading_state_shows_when_loaded_false() {
        show(state = PlansUiState(loaded = false))
        rule.onNodeWithContentDescription("Loading plans").assertIsDisplayed()
    }

    @Test fun front_and_tiles_render_together() {
        val card1 = sampleCard(id = 1L, name = "Primary SIM")
        val card2 = sampleCard(id = 2L, name = "Secondary SIM")
        show(state = PlansUiState(front = card1, tiles = listOf(card2), loaded = true))

        rule.onNodeWithText("Primary SIM").assertIsDisplayed()
        rule.onNodeWithText("Secondary SIM").assertIsDisplayed()
    }
}
