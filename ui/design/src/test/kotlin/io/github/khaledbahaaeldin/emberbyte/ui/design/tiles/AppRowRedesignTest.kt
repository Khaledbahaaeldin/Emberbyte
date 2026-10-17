package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
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
class AppRowRedesignTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun row_is_one_node_with_click_action_and_56dp_minimum_height() {
        var clicked = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppRow(
                    icon = null,
                    shape = AppShape.Cookie,
                    label = "Chrome",
                    bytes = 1_200_000_000L,
                    fraction = 0.43f,
                    mobileBytes = 800_000_000L,
                    wifiBytes = 400_000_000L,
                    onClick = { clicked = true },
                )
            }
        }

        val node = rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
        node.assertExists()
        node.assertHeightIsAtLeast(56.dp)
        node.performClick()
        assertTrue("Row must be clickable", clicked)
    }

    @Test
    fun row_with_null_icon_shows_the_letter_shape() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppRow(
                    icon = null,
                    shape = AppShape.Cookie,
                    label = "YouTube",
                    bytes = 500_000_000L,
                    fraction = 0.2f,
                    mobileBytes = 300_000_000L,
                    wifiBytes = 200_000_000L,
                    onClick = {},
                )
            }
        }

        // AppIcon fallback letter in unmerged tree
        rule.onNodeWithTag("AppIcon:Cookie", useUnmergedTree = true).assertExists()
    }
}
