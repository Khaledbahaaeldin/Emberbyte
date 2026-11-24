package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
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
        isDormant = isDormant,
        startsAtText = startsAtText,
        pinned = pinned,
        statusKind = if (isDormant) PlanStatusKind.Upcoming else PlanStatusKind.OnTrack,
        renewsText = "Renews 28 Nov",
        capText = "10 GB",
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

        show(PlansUiState(front = p1, tiles = listOf(p2, p3), loaded = true))

        rule.onNodeWithText("Front Plan").assertIsDisplayed()
        rule.onNodeWithText("Second Plan").assertIsDisplayed()
        rule.onNodeWithText("Third Plan").assertIsDisplayed()
    }

    @Test
    fun upcoming_card_is_shown_with_start_date() {
        val p1 = sampleCard(1, "Active Front")
        val p2 = sampleCard(2, "Dormant Plan", isDormant = true, startsAtText = "Starts 1 Nov")

        show(PlansUiState(front = p1, upcoming = listOf(p2), loaded = true))

        rule.onNodeWithText("Dormant Plan").assertIsDisplayed()
        rule.onNodeWithText("10 GB, Starts 1 Nov").assertIsDisplayed()
    }

    @Test
    fun empty_state_has_the_new_plan_action() {
        var newPlanClicked = false
        show(
            state = PlansUiState(loaded = true),
            onNewPlan = { newPlanClicked = true },
        )

        rule.onNodeWithText("Set up your first plan").assertIsDisplayed().performClick()
        assertTrue(newPlanClicked)
    }

    @Test
    fun tile_exposes_bring_to_front_action() {
        val events = mutableListOf<PlansEvent>()
        val p1 = sampleCard(1, "Front Plan", pinned = true)
        val p2 = sampleCard(2, "Tile Plan", pinned = false)

        show(
            state = PlansUiState(front = p1, tiles = listOf(p2), loaded = true),
            onEvent = { events += it },
        )

        val tileNode = rule.onNodeWithContentDescription("Bring to the front", substring = true).fetchSemanticsNode()
        val actions = tileNode.config.getOrNull(SemanticsActions.CustomActions)
        val pinAction = actions?.firstOrNull { it.label == "Bring to the front" }
        assertNotNull(pinAction)
        pinAction?.action?.invoke()

        assertEquals(listOf<PlansEvent>(PlansEvent.SetPinned(2L, true)), events)
    }

    @Test
    fun font_scale_2_stacks_tiles_in_one_column() {
        val p1 = sampleCard(1, "Front Plan")
        val p2 = sampleCard(2, "Second Plan")
        val p3 = sampleCard(3, "Third Plan")

        rule.setContent {
            val config = LocalConfiguration.current
            config.fontScale = 2.0f
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(front = p1, tiles = listOf(p2, p3), loaded = true),
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlansContent(
                        state = PlansUiState(front = p1, tiles = listOf(p2), loaded = true),
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
