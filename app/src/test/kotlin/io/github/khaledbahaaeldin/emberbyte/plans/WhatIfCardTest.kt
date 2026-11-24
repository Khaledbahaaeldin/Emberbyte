package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WhatIfCardTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun displays_now_and_with_extra_forecast_sentences() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WhatIfCard(
                    capGb = 10,
                    extraGb = 5,
                    nowText = "Now: Runs out Dec 3",
                    withExtraText = "With +5 GB: Lasts to Dec 9",
                    unitSystem = UnitSystem.DECIMAL,
                    onChangeExtraGb = {},
                )
            }
        }
        rule.onNodeWithText("What if").assertIsDisplayed()
        rule.onNodeWithText("Now: Runs out Dec 3").assertIsDisplayed()
        rule.onNodeWithText("With +5 GB: Lasts to Dec 9").assertIsDisplayed()
    }
}
