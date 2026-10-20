package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
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
class PlanTileTest {
    @get:Rule val rule = createComposeRule()

    private val baseModel = PlanCardUi(
        id = 1L,
        name = "Unlimited Plus",
        simLabel = "SIM 1",
        fraction = 0.63f,
        ringState = BudgetStateUi.Ok,
        percentText = "63%",
        usedText = "6.30 GB of 10.0 GB",
        remainingText = "3.70 GB left",
        daysLeftText = "7 days left",
        forecastText = null,
        isEstimated = false,
        chips = emptyList(),
        archived = false,
    )

    private fun show(model: PlanCardUi = baseModel, onClick: () -> Unit = {}) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanTile(model = model, onClick = onClick)
            }
        }
    }

    @Test
    fun tile_is_one_node_with_the_card_sentence() {
        show(baseModel)
        rule.onNodeWithContentDescription("Unlimited Plus, 63 percent used, 3.70 GB left, 7 days left")
            .assertIsDisplayed()
    }

    @Test
    fun dormant_tile_shows_no_ring_and_the_start_text() {
        val dormant = baseModel.copy(
            isDormant = true,
            startsAtText = "Starts 1 Nov",
        )
        show(dormant)

        rule.onNodeWithText("Starts 1 Nov").assertIsDisplayed()
        rule.onNodeWithContentDescription("Unlimited Plus, Starts 1 Nov").assertIsDisplayed()
        // No percentage text or ring in dormant tile
        rule.onNodeWithText("63%", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun entered_note_is_visible_and_spoken() {
        val withNote = baseModel.copy(enteredNote = "Includes 3 GB you entered")
        show(withNote)

        rule.onNodeWithText("Includes 3 GB you entered").assertIsDisplayed()
        rule.onNodeWithContentDescription("Includes 3 GB you entered", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun pinned_shows_an_icon_with_a_description() {
        val pinned = baseModel.copy(pinned = true)
        show(pinned)

        rule.onNodeWithContentDescription("Pinned", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithContentDescription("pinned", substring = true).assertIsDisplayed()
    }

    @Test
    fun tile_min_height_is_96dp_and_clickable_48dp() {
        var clicked = 0
        show(baseModel, onClick = { clicked++ })

        rule.onNodeWithContentDescription("Unlimited Plus, 63 percent used, 3.70 GB left, 7 days left")
            .assertHeightIsAtLeast(96.dp)
            .assertWidthIsAtLeast(48.dp)
            .performClick()

        assertEquals(1, clicked)
    }

    @Test
    fun exceeded_plan_shows_the_mark_not_only_colour() {
        val exceeded = baseModel.copy(
            fraction = 1.2f,
            ringState = BudgetStateUi.Exceeded,
            percentText = "120%",
        )
        show(exceeded)

        rule.onNodeWithContentDescription("Over the cap", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun font_scale_2_no_clip() {
        rule.setContent {
            val density = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density, fontScale = 2f),
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanTile(model = baseModel, onClick = {})
                }
            }
        }

        rule.onNodeWithText("Unlimited Plus", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("3.70 GB left", useUnmergedTree = true).assertIsDisplayed()
    }
}
