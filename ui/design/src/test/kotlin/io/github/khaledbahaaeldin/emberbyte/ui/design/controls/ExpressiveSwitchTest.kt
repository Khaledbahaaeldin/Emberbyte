package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExpressiveSwitchTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun thumb_shows_a_check_only_when_on() {
        var checkedState by mutableStateOf(true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ExpressiveSwitch(
                    checked = checkedState,
                    onCheckedChange = { checkedState = it },
                    modifier = Modifier.testTag("switch"),
                )
            }
        }
        rule.onNodeWithTag("switch_check_icon", useUnmergedTree = true).assertExists()

        checkedState = false
        rule.waitForIdle()
        rule.onNodeWithTag("switch_check_icon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun toggle_calls_the_callback() {
        var callbackValue: Boolean? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ExpressiveSwitch(
                    checked = false,
                    onCheckedChange = { callbackValue = it },
                    modifier = Modifier.testTag("switch"),
                )
            }
        }
        rule.onNodeWithTag("switch").performClick()
        assertEquals(true, callbackValue)
    }

    @Test
    fun has_role_switch_and_is_48dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ExpressiveSwitch(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("switch"),
                )
            }
        }
        val node = rule.onNodeWithTag("switch")
        val semantics = node.fetchSemanticsNode()
        assertEquals(Role.Switch, semantics.config.getOrNull(SemanticsProperties.Role))

        val bounds = node.getUnclippedBoundsInRoot()
        val height = bounds.bottom - bounds.top
        assertTrue(
            "Switch height must be >= 48dp, was $height",
            height >= 48.dp
        )
    }
}
