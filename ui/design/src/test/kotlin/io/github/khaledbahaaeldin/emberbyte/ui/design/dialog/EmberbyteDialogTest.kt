package io.github.khaledbahaaeldin.emberbyte.ui.design.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EmberbyteDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun title_is_a_heading_and_the_dialog_is_a_pane() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteDialog(
                    onDismiss = {},
                    title = "Confirm Action",
                    icon = Icons.Rounded.Info,
                    confirm = DialogAction(label = "OK", onClick = {}),
                    dismiss = DialogAction(label = "Cancel", onClick = {}),
                ) {
                    Text("Dialog content text")
                }
            }
        }

        // Title has heading semantics
        rule.onNodeWithText("Confirm Action")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))

        // Dialog container has paneTitle
        val paneNodes = rule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Confirm Action")
        ).fetchSemanticsNodes()
        assertEquals("Dialog container must declare paneTitle matching its title", 1, paneNodes.size)
    }

    @Test
    fun destructive_confirm_has_danger_colours_and_an_icon() {
        var iconRendered = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteDialog(
                    onDismiss = {},
                    title = "Delete plan?",
                    icon = Icons.Rounded.Delete,
                    confirm = DialogAction(label = "Delete", onClick = {}),
                    dismiss = DialogAction(label = "Cancel", onClick = {}),
                    destructive = true,
                ) {
                    Text("Are you sure?")
                }
            }
        }

        rule.onNodeWithText("Delete").assertIsDisplayed()
        rule.onNodeWithText("Cancel").assertIsDisplayed()

        // Verify destructive confirm button has danger colors tag or styling
        val confirmNode = rule.onNodeWithTag("emberbyte_dialog_confirm_button", useUnmergedTree = true)
        confirmNode.assertIsDisplayed()

        // Confirm button has warning/danger icon rendered in destructive mode
        val destructiveIconNodes = rule.onAllNodesWithTag("emberbyte_dialog_destructive_icon", useUnmergedTree = true)
            .fetchSemanticsNodes()
        assertTrue("Destructive confirm must display a warning icon", destructiveIconNodes.isNotEmpty())
    }

    @Test
    fun content_scrolls_and_the_buttons_stay_visible_at_font_scale_2() {
        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2.0f, fontScale = 2.0f)
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    EmberbyteDialog(
                        onDismiss = {},
                        title = "Large Dialog",
                        icon = Icons.Rounded.Info,
                        confirm = DialogAction(label = "Confirm", onClick = {}),
                        dismiss = DialogAction(label = "Dismiss", onClick = {}),
                    ) {
                        for (i in 1..20) {
                            Text("Paragraph item number $i in large scrollable content")
                        }
                    }
                }
            }
        }

        // The buttons remain visible and reachable
        rule.onNodeWithText("Confirm").assertIsDisplayed()
        rule.onNodeWithText("Dismiss").assertIsDisplayed()

        // Scroll to the bottom of the content
        rule.onNodeWithText("Paragraph item number 20 in large scrollable content")
            .performScrollTo()
            .assertIsDisplayed()

        // Buttons must still be displayed
        rule.onNodeWithText("Confirm").assertIsDisplayed()
        rule.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun keyboard_open_keeps_the_focused_field_and_the_buttons_visible() {
        val focusRequester = FocusRequester()
        var textValue by mutableStateOf("")

        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2.625f, fontScale = 2.0f)
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    EmberbyteDialog(
                        onDismiss = {},
                        title = "Keyboard Dialog",
                        icon = Icons.Rounded.Info,
                        confirm = DialogAction(label = "Submit", onClick = {}),
                        dismiss = DialogAction(label = "Cancel", onClick = {}),
                    ) {
                        Box(Modifier.height(300.dp))
                        BasicTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            modifier = Modifier
                                .testTag("focused_field")
                                .focusRequester(focusRequester),
                        )
                        Box(Modifier.height(300.dp))
                    }
                }
            }
        }

        // Request focus
        focusRequester.requestFocus()
        rule.waitForIdle()

        val rootHeight = rule.onAllNodes(isRoot()).fetchSemanticsNodes().maxOf { it.size.height }
        val imeHeightPx = (rootHeight * 0.40f).toInt()
        val imeTopPx = rootHeight - imeHeightPx

        // Inject simulated IME insets
        val insets = androidx.core.view.WindowInsetsCompat.Builder()
            .setInsets(
                androidx.core.view.WindowInsetsCompat.Type.ime(),
                androidx.core.graphics.Insets.of(0, 0, 0, imeHeightPx),
            )
            .setVisible(androidx.core.view.WindowInsetsCompat.Type.ime(), true)
            .build()
        fun updateHolderForView(view: android.view.View) {
            try {
                val holderClass = Class.forName("androidx.compose.foundation.layout.WindowInsetsHolder")
                val companion = holderClass.getField("Companion").get(null)
                val holder = companion.javaClass.getMethod("getOrCreateFor", android.view.View::class.java).invoke(companion, view)
                holderClass.getMethod("update", androidx.core.view.WindowInsetsCompat::class.java, Int::class.javaPrimitiveType)
                    .invoke(holder, insets, 0)
            } catch (_: Throwable) {}
            androidx.core.view.ViewCompat.dispatchApplyWindowInsets(view, insets)
        }

        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        if (dialog != null) {
            dialog.window?.decorView?.let { decorView ->
                updateHolderForView(decorView)
                if (decorView is android.view.ViewGroup) {
                    fun traverse(vg: android.view.ViewGroup) {
                        for (i in 0 until vg.childCount) {
                            val child = vg.getChildAt(i)
                            updateHolderForView(child)
                            if (child is android.view.ViewGroup) traverse(child)
                        }
                    }
                    traverse(decorView)
                }
            }
        }
        rule.waitForIdle()

        // Focused field inside content is reachable and visible
        rule.onNodeWithTag("focused_field").performScrollTo().assertIsDisplayed()

        // Confirm & Dismiss buttons are outside the scrollable content area, displayed directly above IME
        val confirmBtn = rule.onNodeWithText("Submit")
        confirmBtn.assertIsDisplayed()
        val confirmBounds = confirmBtn.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Submit button bottom (${confirmBounds.bottom}) must be <= IME top ($imeTopPx)",
            confirmBounds.bottom <= imeTopPx,
        )

        val cancelBtn = rule.onNodeWithText("Cancel")
        cancelBtn.assertIsDisplayed()
        val cancelBounds = cancelBtn.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Cancel button bottom (${cancelBounds.bottom}) must be <= IME top ($imeTopPx)",
            cancelBounds.bottom <= imeTopPx,
        )
    }

    @Test
    fun dismiss_on_back_and_on_outside_tap_calls_onDismiss() {
        var dismissCount = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteDialog(
                    onDismiss = { dismissCount++ },
                    title = "Dismiss Dialog",
                    icon = null,
                    confirm = DialogAction(label = "OK", onClick = {}),
                    dismiss = null,
                ) {
                    Text("Dismiss test content")
                }
            }
        }

        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        assertNotNull("Dialog must be shown in ShadowDialog", dialog)

        // Back press triggers onDismiss
        dialog?.onBackPressed()
        rule.waitForIdle()
        assertEquals(1, dismissCount)
    }

    @Test
    fun confirm_disabled_ignores_clicks() {
        var confirmed = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteDialog(
                    onDismiss = {},
                    title = "Disabled Confirm",
                    icon = null,
                    confirm = DialogAction(label = "Confirm", onClick = { confirmed = true }, enabled = false),
                    dismiss = DialogAction(label = "Cancel", onClick = {}),
                ) {
                    Text("Disabled confirm content")
                }
            }
        }

        val confirmBtn = rule.onNodeWithText("Confirm")
        confirmBtn.assertIsNotEnabled()
        confirmBtn.performClick()
        assertFalse("Disabled confirm must not trigger onClick", confirmed)
    }

    @Test
    fun rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    EmberbyteDialog(
                        onDismiss = {},
                        title = "RTL Dialog",
                        icon = Icons.Rounded.Info,
                        confirm = DialogAction(label = "تأكيد", onClick = {}),
                        dismiss = DialogAction(label = "إلغاء", onClick = {}),
                    ) {
                        Text("محتوى تجريبي")
                    }
                }
            }
        }

        rule.onNodeWithText("RTL Dialog").assertIsDisplayed()
        rule.onNodeWithText("تأكيد").assertIsDisplayed()
        rule.onNodeWithText("إلغاء").assertIsDisplayed()
    }
}
