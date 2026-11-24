package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OnboardingScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun show(
        state: OnboardingUiState = OnboardingUiState(),
        onOpenUsageAccess: () -> Unit = {},
        onRequestNotifications: () -> Unit = {},
        onSetUpPlan: () -> Unit = {},
        onFinish: () -> Unit = {},
        reduceMotion: Boolean = false,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
    ) {
        rule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density, fontScale = fontScale),
                LocalLayoutDirection provides layoutDirection,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                        Box(Modifier.size(360.dp, 1200.dp)) {
                            OnboardingContent(
                                state = state,
                                onOpenUsageAccess = onOpenUsageAccess,
                                onRequestNotifications = onRequestNotifications,
                                onSetUpPlan = onSetUpPlan,
                                onFinish = onFinish,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun four_steps_with_step_n_of_4_descriptions() {
        show()
        rule.onNodeWithContentDescription("Step 1 of 4").assertIsDisplayed()
        rule.onNodeWithText("Your data stays on your phone.").assertIsDisplayed()

        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithContentDescription("Step 2 of 4").assertIsDisplayed()
        rule.onNodeWithText("See your data").assertIsDisplayed()

        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithContentDescription("Step 3 of 4").assertIsDisplayed()
        rule.onNodeWithText("Stay under your cap").assertIsDisplayed()

        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithContentDescription("Step 4 of 4").assertIsDisplayed()
        rule.onNodeWithText("Allow access").assertIsDisplayed()
    }

    @Test
    fun swipe_works_and_buttons_work_without_swipe() {
        show()
        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        rule.onNodeWithContentDescription("Step 2 of 4").assertIsDisplayed()

        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeRight() }
        rule.onNodeWithContentDescription("Step 1 of 4").assertIsDisplayed()

        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithContentDescription("Step 2 of 4").assertIsDisplayed()
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithContentDescription("Step 3 of 4").assertIsDisplayed()
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithContentDescription("Step 4 of 4").assertIsDisplayed()
    }

    @Test
    fun back_goes_to_the_previous_page() {
        show()
        rule.onNodeWithText("Get started").performClick()
        rule.onNodeWithText("Continue").performClick()
        rule.onNodeWithContentDescription("Step 3 of 4").assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Step 2 of 4").assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Step 1 of 4").assertIsDisplayed()
    }

    @Test
    fun granting_a_permission_keeps_the_user_on_the_page_and_continue_finishes_once() {
        var finishCount = 0
        var uiState by mutableStateOf(OnboardingUiState(usageAccess = false))

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                OnboardingContent(
                    state = uiState,
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
        rule.onNodeWithContentDescription("Step 4 of 4").assertIsDisplayed()
        assertEquals(0, finishCount)

        uiState = OnboardingUiState(usageAccess = true)
        rule.waitForIdle()
        assertEquals(0, finishCount)
        rule.onNodeWithContentDescription("Step 4 of 4").assertIsDisplayed()

        uiState = OnboardingUiState(usageAccess = true, notifications = true)
        rule.waitForIdle()
        assertEquals(0, finishCount)

        rule.onNodeWithText("Done").performClick()
        rule.waitForIdle()
        assertEquals(1, finishCount)
    }

    @Test
    @Config(qualifiers = "w360dp-h2000dp")
    fun font_scale_2_no_clip() {
        show(fontScale = 2f)
        val density = Density(rule.density.density, fontScale = 2f)
        val maxWidthPx = with(density) { 360.dp.toPx() }

        for (page in 1..4) {
            rule.onNodeWithContentDescription("Step $page of 4").assertIsDisplayed()
            val allNodes = rule.onAllNodes(androidx.compose.ui.test.SemanticsMatcher("all") { true })
                .fetchSemanticsNodes()
            for (node in allNodes) {
                val textList = node.config.getOrNull(SemanticsProperties.Text)
                if (!textList.isNullOrEmpty()) {
                    val b = node.boundsInRoot
                    assertTrue(
                        "Text '${textList.joinToString()}' on step $page right (${b.right}) exceeds screen width ($maxWidthPx)",
                        b.right <= maxWidthPx + 1f,
                    )
                    assertTrue(
                        "Text '${textList.joinToString()}' on step $page has non-positive size (${b.width} x ${b.height})",
                        b.width > 0f && b.height > 0f,
                    )
                }
            }
            if (page == 1) {
                rule.onNodeWithText("Get started").performClick()
            } else if (page < 4) {
                rule.onNodeWithText("Continue").performClick()
            } else {
                rule.onNodeWithText("Allow access").assertIsDisplayed()
            }
        }
    }

    @Test
    fun rtl_reverses_the_swipe_direction() {
        show(layoutDirection = LayoutDirection.Rtl)
        rule.onNodeWithContentDescription("Step 1 of 4").assertIsDisplayed()

        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeRight() }
        rule.onNodeWithContentDescription("Step 2 of 4").assertIsDisplayed()

        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        rule.onNodeWithContentDescription("Step 1 of 4").assertIsDisplayed()
    }
}
