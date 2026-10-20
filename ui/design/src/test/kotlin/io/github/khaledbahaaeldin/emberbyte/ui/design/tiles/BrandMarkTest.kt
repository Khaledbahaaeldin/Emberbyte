package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BrandMarkTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun brand_mark_is_hidden_from_semantics() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                BrandMark(size = 32.dp)
            }
        }
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription), useUnmergedTree = true)
            .assertCountEquals(0)
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .assertCountEquals(0)
    }
}
