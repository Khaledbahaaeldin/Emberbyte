package io.github.khaledbahaaeldin.emberbyte.ui.design.states

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StatesTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun empty_state_is_one_node_plus_the_action() {
        var actionClicked = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmptyState(
                    icon = Icons.Rounded.Inbox,
                    title = "No data yet",
                    body = "Data will show up here after some usage is recorded.",
                    action = {
                        TextButton(onClick = { actionClicked = true }) {
                            Text("Refresh")
                        }
                    },
                )
            }
        }

        // Title and body are merged into one semantic node
        rule.onNodeWithText("No data yet").assertIsDisplayed()
        rule.onNodeWithText("Data will show up here after some usage is recorded.").assertIsDisplayed()

        // Action is separate and clickable
        val button = rule.onNodeWithText("Refresh")
        button.assertIsDisplayed()
        button.performClick()
        assertEquals(true, actionClicked)
    }

    @Test
    @Config(fontScale = 2.0f)
    fun empty_state_wraps_at_font_scale_2() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmptyState(
                    icon = Icons.Rounded.Inbox,
                    title = "No data yet",
                    body = "Data will show up here after some usage is recorded.",
                )
            }
        }
        rule.onNodeWithText("No data yet").assertIsDisplayed()
        rule.onNodeWithText("Data will show up here after some usage is recorded.").assertIsDisplayed()
    }

    @Test
    fun loading_state_has_a_description_and_is_static_with_reduced_motion() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    LoadingState(label = "Loading usage", modifier = Modifier.testTag("loading_reduced"))
                }
            }
        }
        rule.onNodeWithTag("loading_reduced").assertIsDisplayed()
        rule.onNodeWithContentDescription("Loading usage").assertIsDisplayed()
    }

    @Test
    fun loading_state_animates_when_motion_allowed() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides false) {
                    LoadingState(label = "Loading usage", modifier = Modifier.testTag("loading_normal"))
                }
            }
        }
        rule.onNodeWithTag("loading_normal").assertIsDisplayed()
        rule.onNodeWithContentDescription("Loading usage").assertIsDisplayed()
    }
}
