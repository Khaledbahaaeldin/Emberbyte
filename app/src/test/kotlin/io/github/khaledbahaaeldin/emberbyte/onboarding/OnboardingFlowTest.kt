package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
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
@Config(sdk = [35], qualifiers = "w411dp-h2400dp")
class OnboardingFlowTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private var finished = 0
    private var setUpPlan = 0
    private val requested = mutableListOf<String>()

    private fun show(
        state: OnboardingUiState = OnboardingUiState(),
        reduceMotion: Boolean = true,
        fontScale: Float = 1f,
        direction: LayoutDirection = LayoutDirection.Ltr,
    ) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalReduceMotion provides reduceMotion,
                    LocalLayoutDirection provides direction,
                    LocalDensity provides Density(LocalDensity.current.density, fontScale),
                ) {
                    OnboardingContent(
                        state = state,
                        onOpenUsageAccess = { requested += "usage" },
                        onRequestNotifications = { requested += "notifications" },
                        onRequestPhone = { requested += "phone" },
                        onRequestBattery = { requested += "battery" },
                        onSetUpPlan = { setUpPlan++ },
                        onFinish = { finished++ },
                    )
                }
            }
        }
    }

    private val allGranted = OnboardingUiState(
        usageAccess = true,
        notifications = true,
        phoneState = true,
        batteryUnrestricted = true,
    )

    private fun goToAccess() = rule.onNodeWithText("Get started").performClick()

    // ---- Step 1

    @Test
    fun welcome_shows_the_mark_one_sentence_and_three_facts() {
        show()
        rule.onNodeWithTag("welcome_mark").assertIsDisplayed()
        rule.onNodeWithText("Your data stays on your phone.").assertIsDisplayed()
        rule.onNodeWithText("No account").assertIsDisplayed()
        rule.onNodeWithText("No cloud").assertIsDisplayed()
        rule.onNodeWithText("Open source").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Get started")
    }

    @Test
    fun welcome_has_skip_but_no_quiet_button() {
        show()
        rule.onNodeWithTag("onboarding_skip_button").assertIsDisplayed()
        rule.onAllNodesWithText("Not now").assertCountEquals(0)
    }

    @Test
    fun skip_leaves_onboarding_exactly_once() {
        show()
        rule.onNodeWithTag("onboarding_skip_button").performClick()
        rule.onNodeWithTag("onboarding_skip_button").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun get_started_moves_to_the_access_step() {
        show()
        goToAccess()
        rule.waitForIdle()
        rule.onNodeWithText("Let it measure in the background.").assertIsDisplayed()
        rule.onNodeWithContentDescription("Step 2 of 3").assertExists()
    }

    // ---- Step 2

    @Test
    fun access_lists_four_rows_and_only_usage_is_needed() {
        show()
        goToAccess()
        listOf("Usage access", "Notifications", "Phone", "Battery").forEach {
            rule.onNodeWithText(it).assertIsDisplayed()
        }
        rule.onAllNodesWithText("Needed", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("Optional", useUnmergedTree = true).assertCountEquals(3)
    }

    @Test
    fun the_big_button_walks_the_permissions_in_order() {
        show(OnboardingUiState())
        goToAccess()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Allow usage access").performClick()
        assertEquals(listOf("usage"), requested)
    }

    @Test
    fun the_big_button_names_the_next_missing_permission() {
        show(OnboardingUiState(usageAccess = true))
        goToAccess()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Allow notifications")
    }

    @Test
    fun phone_then_battery_follow_notifications() {
        show(OnboardingUiState(usageAccess = true, notifications = true))
        goToAccess()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Allow phone access").performClick()
        assertEquals(listOf("phone"), requested)
    }

    @Test
    fun battery_is_last() {
        show(OnboardingUiState(usageAccess = true, notifications = true, phoneState = true))
        goToAccess()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Stop battery limits").performClick()
        assertEquals(listOf("battery"), requested)
    }

    @Test
    fun tapping_a_row_asks_for_that_permission() {
        show()
        goToAccess()
        rule.onNodeWithTag("permission_row_phone").performClick()
        rule.onNodeWithTag("permission_row_battery").performClick()
        assertEquals(listOf("phone", "battery"), requested)
    }

    @Test
    fun allowed_rows_show_allowed_and_cannot_be_tapped() {
        show(OnboardingUiState(usageAccess = true))
        goToAccess()
        rule.onAllNodesWithText("Allowed", useUnmergedTree = true).assertCountEquals(1)
        rule.onNodeWithTag("permission_row_usage").performClick()
        assertEquals(emptyList<String>(), requested)
    }

    @Test
    fun a_row_announces_its_state() {
        show(OnboardingUiState(usageAccess = true))
        goToAccess()
        rule.onNodeWithContentDescription("Usage access, allowed").assertExists()
        rule.onNodeWithContentDescription("Notifications, Warns you before a plan runs out, not allowed yet").assertExists()
    }

    @Test
    fun not_now_on_access_goes_to_the_plan_step_without_finishing() {
        show()
        goToAccess()
        rule.onNodeWithTag("onboarding_quiet_button").assertTextEquals("Not now").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Add your data plan.").assertIsDisplayed()
        assertEquals(0, finished)
    }

    @Test
    fun with_everything_allowed_the_big_button_continues_and_there_is_no_quiet_button() {
        show(allGranted)
        goToAccess()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Continue")
        rule.onAllNodesWithText("Not now").assertCountEquals(0)
        rule.onNodeWithTag("onboarding_primary_button").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Add your data plan.").assertIsDisplayed()
    }

    // ---- Step 3

    private fun goToPlan(state: OnboardingUiState) {
        show(state)
        goToAccess()
        rule.waitForIdle()
        if (state.usageAccess && state.notifications && state.phoneState && state.batteryUnrestricted) {
            rule.onNodeWithTag("onboarding_primary_button").performClick()
        } else {
            rule.onNodeWithTag("onboarding_quiet_button").performClick()
        }
        rule.waitForIdle()
    }

    @Test
    fun plan_step_invites_the_user_to_set_up_a_plan() {
        goToPlan(OnboardingUiState())
        rule.onNodeWithText("Add your data plan.").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Set up plan")
        rule.onNodeWithTag("onboarding_quiet_button").assertTextEquals("Not now, take me to Home")
        rule.onAllNodesWithText("Skip").assertCountEquals(0)
        rule.onNodeWithContentDescription("Step 3 of 3").assertExists()
    }

    @Test
    fun set_up_plan_opens_the_plan_flow_and_does_not_finish() {
        goToPlan(OnboardingUiState())
        rule.onNodeWithTag("onboarding_primary_button").performClick()
        assertEquals(1, setUpPlan)
        assertEquals(0, finished)
    }

    @Test
    fun not_now_take_me_home_finishes_once() {
        goToPlan(OnboardingUiState())
        rule.onNodeWithTag("onboarding_quiet_button").performClick()
        rule.onNodeWithTag("onboarding_quiet_button").performClick()
        assertEquals(1, finished)
        assertEquals(0, setUpPlan)
    }

    @Test
    fun once_a_plan_exists_the_step_says_so_and_done_finishes() {
        goToPlan(allGranted.copy(hasPlan = true))
        rule.onNodeWithText("Your plan is set.").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Done")
        rule.onAllNodesWithText("Not now, take me to Home").assertCountEquals(0)
        rule.onNodeWithTag("onboarding_primary_button").performClick()
        assertEquals(1, finished)
    }

    // ---- Back, rotation, motion, accessibility

    @Test
    fun system_back_goes_one_page_back() {
        show()
        goToAccess()
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithText("Your data stays on your phone.").assertIsDisplayed()
    }

    @Test
    fun the_page_survives_rotation_with_reduced_motion() {
        val tester = StateRestorationTester(rule)
        tester.setContent {
            CompositionLocalProvider(LocalReduceMotion provides true) {
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
        }
        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithTag("onboarding_quiet_button").performClick()
        rule.onNodeWithText("Add your data plan.").assertIsDisplayed()
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Add your data plan.").assertIsDisplayed()
    }

    @Test
    fun the_animated_pager_also_reaches_the_last_page() {
        show(reduceMotion = false)
        goToAccess()
        rule.waitForIdle()
        rule.onNodeWithTag("onboarding_quiet_button").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("onboarding_primary_button").assertTextEquals("Set up plan")
    }

    @Test
    fun every_target_is_at_least_48dp_on_every_page() {
        show(OnboardingUiState())
        assertNoTouchTargetSmallerThan48dp(rule)
        goToAccess()
        rule.waitForIdle()
        assertNoTouchTargetSmallerThan48dp(rule)
        rule.onNodeWithTag("onboarding_quiet_button").performClick()
        rule.waitForIdle()
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun at_font_scale_2_the_big_button_stays_reachable_on_every_page() {
        show(OnboardingUiState(), fontScale = 2f)
        rule.onNodeWithTag("onboarding_primary_button").assertIsDisplayed()
        goToAccess()
        rule.waitForIdle()
        rule.onNodeWithTag("onboarding_primary_button").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_quiet_button").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("onboarding_primary_button").assertIsDisplayed()
    }

    @Test
    fun right_to_left_renders_the_same_steps() {
        show(direction = LayoutDirection.Rtl)
        rule.onNodeWithText("Your data stays on your phone.").assertIsDisplayed()
        goToAccess()
        rule.waitForIdle()
        rule.onNodeWithText("Usage access").assertIsDisplayed()
    }

    @Test
    fun each_page_has_exactly_one_heading() {
        show()
        rule.onAllNodes(androidx.compose.ui.test.SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.Heading))
            .assertCountEquals(1)
    }
}
