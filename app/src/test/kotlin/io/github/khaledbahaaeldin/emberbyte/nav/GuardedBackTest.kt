package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class GuardedBackTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clean_form_does_not_intercept_back() {
        var onConfirmCalled = false
        val dirty = mutableStateOf(false)

        composeTestRule.setContent {
            EmberbyteTheme {
                GuardedBack(
                    dirty = dirty.value,
                    onConfirmLeave = { onConfirmCalled = true }
                ) { shrink ->
                    Text("Content with shrink $shrink")
                }
            }
        }

        // Verify dispatcher has no enabled callback from GuardedBack
        val dispatcher = composeTestRule.activity.onBackPressedDispatcher
        assertFalse(
            "When form is clean (dirty = false), GuardedBack must not enable an interception callback",
            dispatcher.hasEnabledCallbacks()
        )
        assertFalse(onConfirmCalled)
    }

    @Test
    fun dirty_form_previews_the_shrink_while_swiping() {
        val observedShrink = mutableFloatStateOf(1f)
        val dirty = mutableStateOf(true)

        composeTestRule.setContent {
            EmberbyteTheme {
                GuardedBack(
                    dirty = dirty.value,
                    onConfirmLeave = {}
                ) { shrink ->
                    observedShrink.floatValue = shrink
                    Text("Shrink: $shrink")
                }
            }
        }

        val dispatcher = composeTestRule.activity.onBackPressedDispatcher
        assertTrue("When form is dirty, GuardedBack must register an enabled callback", dispatcher.hasEnabledCallbacks())

        val eventStart = BackEventCompat(0f, 0f, 0.0f, BackEventCompat.EDGE_LEFT)
        val eventProgress = BackEventCompat(100f, 100f, 0.5f, BackEventCompat.EDGE_LEFT)

        composeTestRule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(eventStart)
            dispatcher.dispatchOnBackProgressed(eventProgress)
        }
        composeTestRule.waitForIdle()

        // At progress 0.5, shrink should preview towards BackTransitions.SHRINK (0.90f)
        // Expected shrink = 1.0 - (1.0 - 0.90) * 0.5 = 0.95f
        assertEquals(0.95f, observedShrink.floatValue, 0.02f)

        // Cancel
        composeTestRule.runOnUiThread {
            dispatcher.dispatchOnBackCancelled()
        }
        composeTestRule.waitForIdle()
        assertEquals(1.0f, observedShrink.floatValue, 0.01f)
    }

    @Test
    fun dirty_form_commit_shows_the_discard_dialog_and_does_not_leave() {
        var onConfirmCalled = false
        val dirty = mutableStateOf(true)

        composeTestRule.setContent {
            EmberbyteTheme {
                GuardedBack(
                    dirty = dirty.value,
                    onConfirmLeave = { onConfirmCalled = true }
                ) { shrink ->
                    Text("Editor Content")
                }
            }
        }

        val dispatcher = composeTestRule.activity.onBackPressedDispatcher
        composeTestRule.runOnUiThread {
            dispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Discard dialog should appear, onConfirmLeave not called yet
        composeTestRule.onNodeWithText("Discard changes?").assertExists()
        assertFalse("Commit must not leave before user confirms", onConfirmCalled)
    }

    @Test
    fun dirty_form_cancel_resets_the_shrink_and_does_nothing_else() {
        val observedShrink = mutableFloatStateOf(1f)
        val dirty = mutableStateOf(true)

        composeTestRule.setContent {
            EmberbyteTheme {
                GuardedBack(
                    dirty = dirty.value,
                    onConfirmLeave = {}
                ) { shrink ->
                    observedShrink.floatValue = shrink
                    Text("Editor Content")
                }
            }
        }

        val dispatcher = composeTestRule.activity.onBackPressedDispatcher
        val eventProgress = BackEventCompat(100f, 100f, 0.6f, BackEventCompat.EDGE_LEFT)
        composeTestRule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(eventProgress)
            dispatcher.dispatchOnBackProgressed(eventProgress)
        }
        composeTestRule.waitForIdle()
        assertTrue(observedShrink.floatValue < 1.0f)

        composeTestRule.runOnUiThread {
            dispatcher.dispatchOnBackCancelled()
        }
        composeTestRule.waitForIdle()
        assertEquals(1.0f, observedShrink.floatValue, 0.01f)
    }

    @Test
    fun confirming_the_dialog_calls_onConfirmLeave_once() {
        var confirmCalls = 0
        val dirty = mutableStateOf(true)

        composeTestRule.setContent {
            EmberbyteTheme {
                GuardedBack(
                    dirty = dirty.value,
                    onConfirmLeave = { confirmCalls++ }
                ) { shrink ->
                    Text("Editor Content")
                }
            }
        }

        val dispatcher = composeTestRule.activity.onBackPressedDispatcher
        composeTestRule.runOnUiThread {
            dispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Click confirm on dialog
        composeTestRule.onNodeWithText("Discard").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, confirmCalls)
    }

    @Test
    fun plan_editor_and_plan_flow_use_the_guard_with_their_dirty_state() {
        val editorFile = File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/plans/PlanEditorScreen.kt")
        val flowFile = File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/plans/flow/PlanFlowScreen.kt")
        assertTrue("PlanEditorScreen.kt should exist", editorFile.exists())
        assertTrue("PlanFlowScreen.kt should exist", flowFile.exists())

        val editorText = editorFile.readText()
        val flowText = flowFile.readText()

        assertTrue("PlanEditorScreen must use GuardedBack", editorText.contains("GuardedBack"))
        assertTrue("PlanFlowScreen must use GuardedBack", flowText.contains("GuardedBack"))
        assertFalse("PlanEditorScreen must not use unconditional BackHandler(enabled = true)", editorText.contains("BackHandler(enabled = true)"))
        assertFalse("PlanFlowScreen must not use bare BackHandler {", editorText.contains("BackHandler {"))
    }

    @Test
    fun back_callback_is_enabled_only_when_current_page_is_above_0() {
        val onboardingFile = File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/onboarding/OnboardingScreen.kt")
        assertTrue("OnboardingScreen.kt should exist", onboardingFile.exists())
        val text = onboardingFile.readText()
        assertTrue(
            "OnboardingScreen must only enable BackHandler when currentPage > 0",
            text.contains("pagerState.currentPage > 0")
        )
    }
}
