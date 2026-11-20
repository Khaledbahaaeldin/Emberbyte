package io.github.khaledbahaaeldin.emberbyte.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
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
class ScreenScaffoldTest {
    @get:Rule
    val rule = createComposeRule()

    private var padding: PaddingValues = PaddingValues()

    private fun show(
        onBack: (() -> Unit)? = {},
        reserve: androidx.compose.ui.unit.Dp? = null,
    ) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                val content: @androidx.compose.runtime.Composable (PaddingValues) -> Unit = { p ->
                    padding = p
                    LazyColumn(
                        modifier = Modifier.height(600.dp),
                        contentPadding = p,
                    ) {
                        items((1..60).toList()) { Text("Row $it") }
                    }
                }
                if (reserve == null) {
                    EmberbyteScreenScaffold(title = "Settings", onBack = onBack, content = content)
                } else {
                    EmberbyteScreenScaffold(
                        title = "Settings",
                        onBack = onBack,
                        floatingToolbarReserve = reserve,
                        content = content,
                    )
                }
            }
        }
    }

    @Test
    fun title_is_shown() {
        show()
        rule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun title_is_announced_as_a_heading() {
        show()
        // The flexible bar composes the title for its collapsed and expanded layouts, so more than one node may match.
        val headings = rule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading) and
                androidx.compose.ui.test.hasText("Settings"),
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        assertTrue("the title must be announced as a heading", headings.isNotEmpty())
    }

    @Test
    fun bottom_reserve_is_zero_when_a_bottomBar_is_provided() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteScreenScaffold(
                    title = "Edit plan",
                    bottomBar = { Text("Save", Modifier.height(80.dp)) },
                ) { p ->
                    padding = p
                    Text("body")
                }
            }
        }
        rule.waitForIdle()
        val bottom = padding.calculateBottomPadding()
        assertTrue("with a bottomBar the content must not also reserve 120dp, was $bottom", bottom < 100.dp)
    }

    @Test
    fun bottomBar_reserve_can_still_be_forced() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteScreenScaffold(
                    title = "Edit plan",
                    bottomBar = { Text("Save", Modifier.height(80.dp)) },
                    floatingToolbarReserve = 40.dp,
                ) { p ->
                    padding = p
                    Text("body")
                }
            }
        }
        rule.waitForIdle()
        assertTrue(padding.calculateBottomPadding() >= 120.dp) // 80 dp bar + 40 dp reserve
    }

    @Test
    fun subtitle_is_shown() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteScreenScaffold(title = "History", subtitle = "Last 14 days") { Text("body") }
            }
        }
        rule.onNodeWithText("Last 14 days").assertIsDisplayed()
    }

    @Test
    fun custom_title_content_replaces_the_plain_text_title() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                EmberbyteScreenScaffold(
                    title = "Today",
                    titleContent = { Text("Custom hero title") },
                ) { Text("body") }
            }
        }
        rule.onNodeWithText("Custom hero title").assertIsDisplayed()
        rule.onAllNodesWithText("Today").assertCountEquals(0)
    }


    @Test
    fun title_stays_visible_when_collapsed() {
        show()
        rule.onNode(hasScrollAction()).performScrollToIndex(40)
        rule.waitForIdle()
        rule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun scrim_is_drawn_over_scrolling_content() {
        show()
        rule.onNodeWithTag(SCREEN_SCAFFOLD_SCRIM).assertIsDisplayed()
    }

    @Test
    fun content_never_starts_under_the_top_bar() {
        show()
        rule.waitForIdle()
        val top = padding.calculateTopPadding()
        assertTrue("content top padding ($top) must clear the top bar", top > 40.dp)
    }

    @Test
    fun bottom_reserve_is_120dp_by_default() {
        show()
        rule.waitForIdle()
        assertTrue(
            "bottom padding ${padding.calculateBottomPadding()} must reserve 120dp for the floating bar",
            padding.calculateBottomPadding() >= 120.dp,
        )
    }

    @Test
    fun bottom_reserve_can_be_changed() {
        show(reserve = 200.dp)
        rule.waitForIdle()
        assertTrue(padding.calculateBottomPadding() >= 200.dp)
    }

    @Test
    fun back_button_calls_onBack_and_is_a_48dp_target() {
        var backs = 0
        show(onBack = { backs++ })
        val bounds = rule.onNodeWithContentDescription("Back").getUnclippedBoundsInRoot()
        assertTrue("back target ${bounds.height} must be at least 48dp", bounds.height >= 48.dp)
        rule.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun no_back_button_without_onBack() {
        show(onBack = null)
        rule.onNodeWithContentDescription("Back").assertDoesNotExistCompat()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertDoesNotExistCompat() {
        val exists = try {
            fetchSemanticsNode()
            true
        } catch (e: AssertionError) {
            false
        }
        assertTrue("node should not exist", !exists)
    }
}
