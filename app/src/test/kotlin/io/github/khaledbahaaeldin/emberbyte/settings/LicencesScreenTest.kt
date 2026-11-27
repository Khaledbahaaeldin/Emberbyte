package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.height
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
class LicencesScreenTest {
    @get:Rule val rule = createComposeRule()

    private val sampleItems = listOf(
        LicenceItem(
            name = "Roboto Flex",
            version = "1.0",
            licence = "OFL-1.1",
            url = "https://github.com/googlefonts/roboto-flex",
            category = "Fonts and designs",
            fullLicenceText = "SIL OPEN FONT LICENSE Version 1.1",
        ),
        LicenceItem(
            name = "Bouncy Castle",
            version = "1.86",
            licence = "MIT",
            url = "https://www.bouncycastle.org/",
            category = "Built with",
            fullLicenceText = "Bouncy Castle MIT License text",
        ),
        LicenceItem(
            name = "okio",
            version = "3.9.1",
            licence = "Apache-2.0",
            url = "https://github.com/square/okio",
            category = "Built with",
            fullLicenceText = "Apache License 2.0 text",
        ),
    )

    private fun expandHeights(reduced: Boolean): List<Float> {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides reduced) {
                    LicencesContent(
                        items = sampleItems,
                        searchQuery = "",
                        onSearchQueryChange = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Roboto Flex"))
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeByFrame()
        val start = rule.onNodeWithText("Roboto Flex").getUnclippedBoundsInRoot().height.value
        rule.onNodeWithText("Roboto Flex").performClick()
        val heights = mutableListOf(start)
        repeat(30) {
            rule.mainClock.advanceTimeBy(16)
            heights += rule.onNodeWithText("Roboto Flex").getUnclippedBoundsInRoot().height.value
        }
        return heights
    }

    @Test
    fun licences_uses_reduced_motion() {
        val heights = expandHeights(reduced = true)
        val settled = heights.last()
        assertTrue("the card should have grown, saw $heights", settled > heights.first() + 1f)
        val inBetween = heights.filter { it > heights.first() + 1f && it < settled - 1f }
        assertTrue("reduced motion must not animate the expansion, saw $heights", inBetween.isEmpty())
    }

    @Test
    fun licences_expansion_animates_with_normal_motion() {
        val heights = expandHeights(reduced = false)
        val settled = heights.last()
        assertTrue("the card should have grown, saw $heights", settled > heights.first() + 1f)
        val inBetween = heights.filter { it > heights.first() + 1f && it < settled - 1f }
        assertTrue("expected in-between heights, saw $heights", inBetween.isNotEmpty())
    }

    @Test
    fun screen_shows_the_list_and_a_search_filters_it() {
        var query by androidx.compose.runtime.mutableStateOf("")
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                val filtered = if (query.isBlank()) sampleItems else sampleItems.filter { it.name.contains(query, ignoreCase = true) }
                LicencesContent(
                    items = filtered,
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Open-source licences").assertIsDisplayed()
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Roboto Flex"))
        rule.onNodeWithText("Roboto Flex").assertIsDisplayed()
        rule.onNodeWithText("Bouncy Castle").assertIsDisplayed()
        rule.onNodeWithText("okio").assertIsDisplayed()

        // Filter via TextInput in search field (search field is at top)
        rule.onNode(hasScrollAction()).performScrollToNode(androidx.compose.ui.test.hasTestTag("LicencesSearchField"))
        rule.onNodeWithTag("LicencesSearchField").performTextInput("Bouncy")

        rule.onNodeWithText("Bouncy Castle").assertIsDisplayed()
        rule.onNodeWithText("Roboto Flex").assertDoesNotExist()
        rule.onNodeWithText("okio").assertDoesNotExist()
    }

    @Test
    fun each_entry_is_one_node_with_name_and_licence() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LicencesContent(
                    items = listOf(sampleItems[1]),
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onBack = {},
                )
            }
        }

        // Each entry row merges descendants: name and licence pill form one semantic node
        val rowNode = rule.onNode(
            androidx.compose.ui.test.hasText("Bouncy Castle") and androidx.compose.ui.test.hasText("MIT")
        )
        rowNode.assertIsDisplayed()
    }

    @Test
    fun empty_search_shows_the_empty_state() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LicencesContent(
                    items = emptyList(),
                    searchQuery = "nonexistent_dependency",
                    onSearchQueryChange = {},
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("No licences found").assertIsDisplayed()
    }

    @Test
    @Config(fontScale = 2.0f)
    fun font_scale_2_no_clip() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LicencesContent(
                    items = sampleItems,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Open-source licences").assertIsDisplayed()
        rule.onNodeWithText("Emberbyte is free software").assertIsDisplayed()
        rule.onNodeWithText("Bouncy Castle").assertIsDisplayed()
    }

    @Test
    fun icon_buttons_keep_48dp_and_their_content_description() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LicencesContent(
                    items = sampleItems,
                    searchQuery = "Roboto",
                    onSearchQueryChange = {},
                    onBack = {},
                )
            }
        }

        val clearButton = rule.onNodeWithContentDescription("Clear search")
        clearButton
            .assertIsDisplayed()
            .performTouchInput { down(center); up() }

        assertNoTouchTargetSmallerThan48dp(rule)
    }
}
