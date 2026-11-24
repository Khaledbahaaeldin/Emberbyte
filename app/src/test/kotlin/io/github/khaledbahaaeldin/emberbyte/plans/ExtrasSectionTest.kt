package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1200dp")
class ExtrasSectionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun empty_extras_starts_collapsed_and_expands_on_tap() {
        var addedAddOn = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ExtrasSection(
                    addOns = emptyList(),
                    freeRules = emptyList(),
                    availableApps = emptyList(),
                    unitSystem = UnitSystem.DECIMAL,
                    errors = emptyMap(),
                    onAddAddOn = { addedAddOn = true },
                    onUpdateAddOn = { _, _ -> },
                    onRemoveAddOn = {},
                    onAddFreeRule = {},
                    onUpdateFreeRule = { _, _ -> },
                    onRemoveFreeRule = {},
                )
            }
        }
        rule.onNodeWithText("Add-ons & free windows (0)").performClick()
        rule.onNodeWithText("Add add-on").performClick()
        assertTrue(addedAddOn)
    }

    @Test
    fun filled_extras_shows_add_on_and_free_window_items() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ExtrasSection(
                    addOns = listOf(
                        AddOnDraft(
                            label = "Weekend boost",
                            amountText = "2",
                            validFrom = LocalDate.of(2026, 11, 1),
                            validUntil = LocalDate.of(2026, 11, 30),
                        ),
                    ),
                    freeRules = listOf(FreeRuleDraft(label = "Night owl")),
                    availableApps = emptyList(),
                    unitSystem = UnitSystem.DECIMAL,
                    errors = emptyMap(),
                    onAddAddOn = {},
                    onUpdateAddOn = { _, _ -> },
                    onRemoveAddOn = {},
                    onAddFreeRule = {},
                    onUpdateFreeRule = { _, _ -> },
                    onRemoveFreeRule = {},
                )
            }
        }
        rule.onNodeWithText("Weekend boost").assertIsDisplayed()
        rule.onNodeWithText("Night owl").assertIsDisplayed()
    }
}
