package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OnboardingAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    private fun show(state: OnboardingUiState = OnboardingUiState()) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                OnboardingContent(
                    state = state,
                    onOpenUsageAccess = {},
                    onRequestNotifications = {},
                    onSetUpPlan = {},
                    onFinish = {},
                )
            }
        }
    }

    @Test
    fun onboarding_touch_targets_are_at_least_48dp() {
        show(OnboardingUiState(usageAccess = false, notifications = false, hasPlan = false))
        for (page in 1..4) {
            assertNoTouchTargetSmallerThan48dp(rule)
            if (page < 4) {
                rule.onNodeWithText("Next").performClick()
            }
        }
    }

    @Test
    fun onboarding_action_buttons_have_non_empty_accessible_names() {
        show(OnboardingUiState(usageAccess = false, notifications = false, hasPlan = false))

        // Page 1: Skip and Next
        val skipPage1 = rule.onNodeWithText("Skip")
        skipPage1.assertIsDisplayed()
        skipPage1.assert(hasClickAction())

        val nextBtn = rule.onNodeWithText("Next")
        nextBtn.assertIsDisplayed()
        nextBtn.assert(hasClickAction())

        // Advance to Page 3 ("Stay under your cap")
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithText("Next").performClick()

        val setUpPlan = rule.onNodeWithText("Set up a plan").performScrollTo()
        setUpPlan.assertIsDisplayed()
        setUpPlan.assert(hasClickAction())

        // Advance to Page 4 ("Allow usage access")
        rule.onNodeWithText("Next").performClick()

        val openSettings = rule.onNodeWithText("Open settings").performScrollTo()
        openSettings.assertIsDisplayed()
        openSettings.assert(hasClickAction())

        val allowNotifications = rule.onNodeWithText("Allow notifications").performScrollTo()
        allowNotifications.assertIsDisplayed()
        allowNotifications.assert(hasClickAction())

        val skipForNow = rule.onNodeWithText("Skip for now")
        skipForNow.assertIsDisplayed()
        skipForNow.assert(hasClickAction())

        val allowAccessBtn = rule.onNodeWithText("Allow access")
        allowAccessBtn.assertIsDisplayed()
        allowAccessBtn.assert(hasClickAction())
    }
}
