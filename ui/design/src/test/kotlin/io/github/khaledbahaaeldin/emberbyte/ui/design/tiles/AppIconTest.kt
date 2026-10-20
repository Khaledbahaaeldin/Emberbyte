package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppIconTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun shows_the_first_letter_when_there_is_no_icon() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppIcon(
                    label = "Chrome",
                    icon = null,
                    shape = AppShape.Cookie,
                    size = 40.dp,
                )
            }
        }
        rule.onNodeWithText("C", useUnmergedTree = true).assertExists()
    }

    @Test
    fun the_whole_icon_is_hidden_from_semantics() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppIcon(
                    label = "Chrome",
                    icon = null,
                    shape = AppShape.Cookie,
                    size = 40.dp,
                )
            }
        }
        rule.onNodeWithText("C", useUnmergedTree = false).assertDoesNotExist()
        rule.onNodeWithTag("AppIcon:Cookie", useUnmergedTree = false).assertDoesNotExist()
    }

    @Test
    fun the_shape_comes_from_the_argument() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Column {
                    for (shape in AppShape.entries) {
                        AppIcon(
                            label = "Emberbyte",
                            icon = null,
                            shape = shape,
                            size = 40.dp,
                        )
                    }
                }
            }
        }
        for (shape in AppShape.entries) {
            rule.onNodeWithTag("AppIcon:${shape.name}", useUnmergedTree = true).assertExists()
        }
    }
}
