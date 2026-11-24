package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OnboardingRedesignTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun welcome_shows_mark_title_sentence_and_three_facts() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                OnboardingContent(
                    state = OnboardingUiState(),
                    onOpenUsageAccess = {},
                    onRequestNotifications = {},
                    onSetUpPlan = {},
                    onFinish = {},
                )
            }
        }
        rule.onNodeWithTag("welcome_mark").assertIsDisplayed()
        rule.onNodeWithText("Your data stays on your phone.").assertIsDisplayed()
        rule.onNodeWithText("No account").assertIsDisplayed()
        rule.onNodeWithText("No cloud").assertIsDisplayed()
        rule.onNodeWithText("Open source").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun see_your_data_shows_the_example_card_split_bar_and_two_tiles() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                OnboardingContent(
                    state = OnboardingUiState(),
                    onOpenUsageAccess = {},
                    onRequestNotifications = {},
                    onSetUpPlan = {},
                    onFinish = {},
                )
            }
        }
        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithTag("see_data_example_card").assertIsDisplayed()
        rule.onNodeWithText("See your data").assertIsDisplayed()
        rule.onNodeWithText("2.4 MB/s").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("23").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun set_up_a_plan_is_tonal_and_continue_is_the_only_filled_action() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                OnboardingContent(
                    state = OnboardingUiState(),
                    onOpenUsageAccess = {},
                    onRequestNotifications = {},
                    onSetUpPlan = {},
                    onFinish = {},
                )
            }
        }
        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithTag("stay_cap_disc").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_set_up_plan_button").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun button_moves_to_notifications_after_usage_is_granted() {
        var state by mutableStateOf(OnboardingUiState(usageAccess = false, notifications = false))
        var finishCount = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                OnboardingContent(
                    state = state,
                    onOpenUsageAccess = {},
                    onRequestNotifications = {},
                    onSetUpPlan = {},
                    onFinish = { finishCount++ },
                )
            }
        }
        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithText("Allow usage access").assertIsDisplayed()

        state = OnboardingUiState(usageAccess = true, notifications = false)
        rule.waitForIdle()
        rule.onNodeWithText("Allow notifications").assertIsDisplayed()
        assertEquals(0, finishCount)

        state = OnboardingUiState(usageAccess = true, notifications = true)
        rule.waitForIdle()
        rule.onNodeWithText("Done").performClick()
        assertEquals(1, finishCount)
    }

    @Test
    fun reduced_motion_cross_fades_and_skips_layer_travel() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    OnboardingContent(
                        state = OnboardingUiState(),
                        onOpenUsageAccess = {},
                        onRequestNotifications = {},
                        onSetUpPlan = {},
                        onFinish = {},
                    )
                }
            }
        }
        rule.onNodeWithContentDescription("Step 1 of 4").assertIsDisplayed()
        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithContentDescription("Step 2 of 4").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_shared_layer").assertDoesNotExist()
    }
}
