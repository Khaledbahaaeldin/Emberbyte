package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlansScreenTest {
    @get:Rule val rule = createComposeRule()

    private fun sampleCard(
        id: Long,
        name: String,
        isDormant: Boolean = false,
        startsAtText: String? = null,
        pinned: Boolean = false,
    ) = PlanCardUi(
        id = id,
        name = name,
        simLabel = "SIM 1",
        fraction = 0.4f,
        ringState = BudgetStateUi.Ok,
        percentText = "40%",
        usedText = "4.0 GB of 10.0 GB",
        remainingText = "6.0 GB left",
        daysLeftText = "12 days left",
        forecastText = null,
        isEstimated = false,
        chips = emptyList(),
        archived = false,
        isDormant = isDormant,
        startsAtText = startsAtText,
        pinned = pinned,
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

    @Test
    fun front_plan_is_the_large_card_and_others_are_tiles() {
        val p1 = sampleCard(1, "Front Plan")
        val p2 = sampleCard(2, "Second Plan")
        val p3 = sampleCard(3, "Third Plan")

        show(PlansUiState(cards = listOf(p1, p2, p3), loaded = true))

        rule.onNodeWithText("Front Plan").assertIsDisplayed()
        rule.onNodeWithText("Second Plan").assertIsDisplayed()
        rule.onNodeWithText("Third Plan").assertIsDisplayed()
    }

    @Test
    fun dormant_tile_is_dimmed_and_describes_when_it_starts() {
        val p1 = sampleCard(1, "Active Front")
        val p2 = sampleCard(2, "Dormant Plan", isDormant = true, startsAtText = "Starts 1 Nov")

        show(PlansUiState(cards = listOf(p1, p2), loaded = true))

        rule.onNodeWithText("Dormant Plan").assertIsDisplayed()
        rule.onNodeWithText("Starts 1 Nov").assertIsDisplayed()
        rule.onNodeWithContentDescription("Dormant Plan, Starts 1 Nov").assertIsDisplayed()
    }

    @Test
    fun empty_state_has_the_new_plan_action() {
        var newPlanClicked = false
        show(
            state = PlansUiState(cards = emptyList(), loaded = true),
            onNewPlan = { newPlanClicked = true },
        )

        rule.onNodeWithText("Set up your first plan").assertIsDisplayed()
        rule.onNodeWithText("Set up your first plan").performClick()
        assertTrue(newPlanClicked)
    }

    @Test
    fun pin_is_available_through_a_custom_accessibility_action() {
        var pinnedId: Long? = null
        var isPinned: Boolean? = null
        val p1 = sampleCard(1, "Front Plan", pinned = false)

        show(
            state = PlansUiState(cards = listOf(p1), loaded = true),
            onEvent = { event ->
                if (event is PlansEvent.SetPinned) {
                    pinnedId = event.id
                    isPinned = event.pinned
                }
            },
        )

        // Custom action on PlanCardWithMenu
        val node = rule.onNodeWithContentDescription("Front Plan, 40 percent used, 6.0 GB left, 12 days left")
            .fetchSemanticsNode()
        val actions = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsActions.CustomActions)
        val pinAction = actions?.firstOrNull { it.label == "Pin" }
        org.junit.Assert.assertNotNull(pinAction)
        pinAction?.action?.invoke()

        assertEquals(1L, pinnedId)
        assertEquals(true, isPinned)
    }

    @Test
    fun font_scale_2_stacks_tiles_in_one_column() {
        val p1 = sampleCard(1, "Front Plan")
        val p2 = sampleCard(2, "Second Plan")
        val p3 = sampleCard(3, "Third Plan")

        rule.setContent {
            val config = androidx.compose.ui.platform.LocalConfiguration.current
            config.fontScale = 2.0f
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(cards = listOf(p1, p2, p3), loaded = true),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithText("Front Plan").assertIsDisplayed()
        rule.onNodeWithText("Second Plan").assertIsDisplayed()
        rule.onNodeWithText("Third Plan").assertIsDisplayed()
    }

    @Test
    fun rtl() {
        val p1 = sampleCard(1, "Front Plan")
        val p2 = sampleCard(2, "Second Plan")

        rule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlansContent(
                        state = PlansUiState(cards = listOf(p1, p2), loaded = true),
                        onEvent = {},
                        onOpenPlan = {},
                        onNewPlan = {},
                    )
                }
            }
        }

        rule.onNodeWithText("Front Plan").assertIsDisplayed()
        rule.onNodeWithText("Second Plan").assertIsDisplayed()
    }
}
