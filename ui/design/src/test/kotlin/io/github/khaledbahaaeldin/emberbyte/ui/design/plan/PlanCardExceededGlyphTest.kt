package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** State must never be colour-only (Monochrome has no hue to carry it): an exceeded plan shows a warning glyph. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanCardExceededGlyphTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun monochrome_exceeded_shows_error_glyph() {
        val model = PlanCardUi(
            id = 1, name = "Main", simLabel = null, fraction = 1.1f, ringState = BudgetStateUi.Exceeded,
            percentText = "110%", usedText = "11 GB of 10 GB", remainingText = "0 B left", daysLeftText = "9 days left",
            forecastText = "Already over your cap", isEstimated = false, chips = emptyList(), archived = false,
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec(presetId = "monochrome", darkTheme = false)) { PlanCard(model, {}) }
        }
        rule.onNodeWithContentDescription("Data used up").assertIsDisplayed()
    }
}
