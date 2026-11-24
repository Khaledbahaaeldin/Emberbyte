package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
        assertNoTouchTargetSmallerThan48dp(rule)
        rule.onNodeWithText("Get started").performClick()
        assertNoTouchTargetSmallerThan48dp(rule)
        rule.onNodeWithText("Continue").performClick()
        assertNoTouchTargetSmallerThan48dp(rule)
        rule.onNodeWithText("Continue").performClick()
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun onboarding_action_buttons_have_non_empty_accessible_names() {
        show(OnboardingUiState(usageAccess = false, notifications = false, hasPlan = false))

        val skipPage1 = rule.onNodeWithText("Skip intro")
        skipPage1.assertIsDisplayed()
        skipPage1.assert(hasClickAction())

        val getStartedBtn = rule.onNodeWithText("Get started")
        getStartedBtn.assertIsDisplayed()
        getStartedBtn.assert(hasClickAction())

        getStartedBtn.performClick()
        rule.onNodeWithText("Continue").performClick()

        val setUpPlan = rule.onNodeWithText("Set up a plan").performScrollTo()
        setUpPlan.assertIsDisplayed()
        setUpPlan.assert(hasClickAction())

        rule.onNodeWithText("Continue").performClick()

        val openSettings = rule.onNodeWithText("Open settings").performScrollTo()
        openSettings.assertIsDisplayed()
        openSettings.assert(hasClickAction())

        val allowNotifications = rule.onNodeWithText("Allow notifications").performScrollTo()
        allowNotifications.assertIsDisplayed()
        allowNotifications.assert(hasClickAction())

        val skipForNow = rule.onNodeWithText("Skip for now")
        skipForNow.assertIsDisplayed()
        skipForNow.assert(hasClickAction())

        val primaryBtn = rule.onNodeWithTag("onboarding_primary_button")
        primaryBtn.assertIsDisplayed()
        primaryBtn.assert(hasClickAction())
    }
}
