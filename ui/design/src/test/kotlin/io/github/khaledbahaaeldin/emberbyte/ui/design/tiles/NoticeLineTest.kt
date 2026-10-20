package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
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
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NoticeLineTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun notice_line_is_one_line_at_font_scale_1_and_wraps_at_2() {
        val notice = "Not measured 00:10–00:33 (restart)"
        rule.setContent {
            val density = LocalDensity.current
            Column {
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.0f)) {
                    EmberbyteTheme(EmberbyteThemeSpec.Default) {
                        Box(Modifier.width(360.dp).testTag("box1")) {
                            NoticeLine(text = notice)
                        }
                    }
                }
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2.0f)) {
                    EmberbyteTheme(EmberbyteThemeSpec.Default) {
                        Box(Modifier.width(360.dp).testTag("box2")) {
                            NoticeLine(text = notice)
                        }
                    }
                }
            }
        }
        val results1 = mutableListOf<TextLayoutResult>()
        val node1 = rule.onNode(hasText(notice) and hasAnyAncestor(hasTestTag("box1")), useUnmergedTree = true).fetchSemanticsNode()
        node1.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results1)
        val linesAt1 = results1.first().lineCount
        assertEquals(1, linesAt1)

        val results2 = mutableListOf<TextLayoutResult>()
        val node2 = rule.onNode(hasText(notice) and hasAnyAncestor(hasTestTag("box2")), useUnmergedTree = true).fetchSemanticsNode()
        node2.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results2)
        val linesAt2 = results2.first().lineCount
        assertTrue("Expected lines at font scale 2.0 to be >= 2, but was $linesAt2", linesAt2 >= 2)
    }
}
