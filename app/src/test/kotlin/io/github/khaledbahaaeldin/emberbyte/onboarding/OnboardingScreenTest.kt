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
    fun onboarding_page_2_does_not_show_stale_step_1_of_3() {
        show()
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithText("STEP 1 OF 3").assertDoesNotExist()
    }

    @Test
    fun four_pages_with_page_n_of_4_descriptions() {
        show()
        rule.onNodeWithContentDescription("Page 1 of 4").assertIsDisplayed()
        rule.onNodeWithText("Your data stays on your phone.").assertIsDisplayed()

        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()
        rule.onNodeWithText("See your data").assertIsDisplayed()

        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 3 of 4").assertIsDisplayed()
        rule.onNodeWithText("Stay under your cap").assertIsDisplayed()

        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 4 of 4").assertIsDisplayed()
        rule.onNodeWithText("Allow usage access").assertIsDisplayed()
    }

    @Test
    fun next_advances_and_the_last_page_shows_allow_access() {
        var openUsageCalled = 0
        show(onOpenUsageAccess = { openUsageCalled++ })

        rule.onNodeWithText("Next").assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()

        rule.onNodeWithText("Next").assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Page 3 of 4").assertIsDisplayed()

        rule.onNodeWithText("Next").assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Page 4 of 4").assertIsDisplayed()

        rule.onNodeWithText("Next").assertDoesNotExist()
        rule.onNodeWithText("Allow access").assertIsDisplayed().performClick()
        assertEquals(1, openUsageCalled)
    }

    @Test
    fun skip_jumps_to_the_permission_page_not_out_of_onboarding() {
        var finished = 0
        show(onFinish = { finished++ })

        rule.onNodeWithContentDescription("Page 1 of 4").assertIsDisplayed()
        rule.onNodeWithText("Skip").assertIsDisplayed().performClick()

        rule.onNodeWithContentDescription("Page 4 of 4").assertIsDisplayed()
        rule.onNodeWithText("Allow usage access").assertIsDisplayed()
        rule.onNodeWithText("Skip").assertDoesNotExist()
        assertEquals(0, finished)
    }

    @Test
    fun swipe_works_and_buttons_work_without_swipe() {
        show()
        // Swipe from page 1 to page 2
        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()

        // Swipe back to page 1
        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeRight() }
        rule.onNodeWithContentDescription("Page 1 of 4").assertIsDisplayed()

        // Advance using buttons only without swipe (for TalkBack users)
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 3 of 4").assertIsDisplayed()
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 4 of 4").assertIsDisplayed()
    }

    @Test
    fun back_goes_to_the_previous_page() {
        show()
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Page 3 of 4").assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Page 1 of 4").assertIsDisplayed()
    }

    @Test
    fun permission_granted_completes_onboarding_exactly_once() {
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

        // Jump to the permission page
        rule.onNodeWithText("Skip").performClick()
        rule.onNodeWithContentDescription("Page 4 of 4").assertIsDisplayed()
        assertEquals(0, finishCount)

        // Grant usage access -> should complete onboarding once
        uiState = OnboardingUiState(usageAccess = true)
        rule.waitForIdle()
        assertEquals(1, finishCount)

        // Subsequent state emissions with usageAccess = true must not re-fire onFinish
        uiState = OnboardingUiState(usageAccess = true, notifications = true)
        rule.waitForIdle()
        assertEquals(1, finishCount)
    }

    @Test
    fun reduce_motion_changes_pages_without_animation() {
        show(reduceMotion = true)
        rule.mainClock.autoAdvance = false

        rule.onNodeWithText("Next").performClick()
        // Advance a single frame: with reduceMotion = true (scrollToPage), page 2 is already settled
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()
        rule.onNodeWithText("See your data").assertIsDisplayed()

        rule.onNodeWithText("Skip").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("Page 4 of 4").assertIsDisplayed()
        rule.onNodeWithText("Allow usage access").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h2000dp")
    fun font_scale_2_no_clip() {
        show(fontScale = 2f)
        val density = Density(rule.density.density, fontScale = 2f)
        val maxWidthPx = with(density) { 360.dp.toPx() }

        for (page in 1..4) {
            rule.onNodeWithContentDescription("Page $page of 4").assertIsDisplayed()
            val allNodes = rule.onAllNodes(androidx.compose.ui.test.SemanticsMatcher("all") { true })
                .fetchSemanticsNodes()
            for (node in allNodes) {
                val textList = node.config.getOrNull(SemanticsProperties.Text)
                if (!textList.isNullOrEmpty()) {
                    val b = node.boundsInRoot
                    assertTrue(
                        "Text '${textList.joinToString()}' on page $page right (${b.right}) exceeds screen width ($maxWidthPx)",
                        b.right <= maxWidthPx + 1f,
                    )
                    assertTrue(
                        "Text '${textList.joinToString()}' on page $page has non-positive size (${b.width} x ${b.height})",
                        b.width > 0f && b.height > 0f,
                    )
                }
            }
            if (page < 4) {
                rule.onNodeWithText("Next").assertIsDisplayed().performClick()
            } else {
                rule.onNodeWithText("Allow access").assertIsDisplayed()
            }
        }
    }

    @Test
    fun rtl_reverses_the_swipe_direction() {
        show(layoutDirection = LayoutDirection.Rtl)
        rule.onNodeWithContentDescription("Page 1 of 4").assertIsDisplayed()

        // In RTL, swiping right advances to the next page (page 2)
        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeRight() }
        rule.onNodeWithContentDescription("Page 2 of 4").assertIsDisplayed()

        // And swiping left goes back to page 1
        rule.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        rule.onNodeWithContentDescription("Page 1 of 4").assertIsDisplayed()
    }
}
