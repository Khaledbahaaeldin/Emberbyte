package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class PassphraseDialogTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun passphrase_mismatch_outlines_both_fields() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CreateBackupPassphraseDialog(
                    onDismiss = {},
                    onCreate = {},
                )
            }
        }

        // One-line irrecoverable warning and disabled reason are visible before typing
        rule.onNodeWithText("If you lose this passphrase the backup cannot be recovered").assertIsDisplayed()
        rule.onNodeWithText("Create").assertIsNotEnabled()

        val fields = rule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("correcthorse")
        fields[1].performTextInput("wronghorse")
        rule.waitForIdle()

        // Both fields must outline as error and expose error semantics
        val updatedFields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes()
        assertEquals(2, updatedFields.size)
        assertNotNull("First passphrase field must set SemanticsProperties.Error on mismatch", updatedFields[0].config.getOrNull(SemanticsProperties.Error))
        assertNotNull("Repeat passphrase field must set SemanticsProperties.Error on mismatch", updatedFields[1].config.getOrNull(SemanticsProperties.Error))
        rule.onNodeWithText("The passphrases do not match").assertIsDisplayed()
    }

    @Test
    fun passphrase_show_hide_icon_toggles_visibility() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CreateBackupPassphraseDialog(
                    onDismiss = {},
                    onCreate = {},
                )
            }
        }

        val fields = rule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("secretpass123")
        rule.waitForIdle()

        // Passphrase is masked initially, unmasked when "Show passphrase" is clicked, and masked again on "Hide passphrase"
        val hiddenNode = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes()[0]
        assertNotNull("Passphrase should be marked password initially", hiddenNode.config.getOrNull(SemanticsProperties.Password))
        assertEquals("•".repeat(13), hiddenNode.config.getOrNull(SemanticsProperties.EditableText)?.text)

        rule.onNodeWithContentDescription("Show passphrase").assertIsDisplayed().performClick()
        rule.waitForIdle()

        rule.onNodeWithContentDescription("Hide passphrase").assertIsDisplayed()
        val shownNode = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes()[0]
        assertEquals("secretpass123", shownNode.config.getOrNull(SemanticsProperties.EditableText)?.text)

        rule.onNodeWithContentDescription("Hide passphrase").performClick()
        rule.waitForIdle()
        val maskedAgainNode = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes()[0]
        assertEquals("•".repeat(13), maskedAgainNode.config.getOrNull(SemanticsProperties.EditableText)?.text)
    }
}
