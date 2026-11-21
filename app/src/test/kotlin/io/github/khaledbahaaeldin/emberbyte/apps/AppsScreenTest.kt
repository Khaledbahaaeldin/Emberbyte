package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.common.SCREEN_SCAFFOLD_SCRIM
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val sampleFeatured = AppRowUi("com.video", "YouTube", 600_000_000L, 400_000_000L)
    private val sampleApps = listOf(
        sampleFeatured,
        AppRowUi("com.chat", "Chat", 100_000_000L, 0L),
    )

    @Test
    fun active_filters_visibility_snaps_under_reduced_motion() {
        var state by mutableStateOf(AppsUiState(loaded = true))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    AppsContent(
                        state = state,
                        onEvent = {},
                        onOpenApp = {},
                        onPromptAction = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("Remove 7 days filter").assertDoesNotExist()

        state = state.copy(range = AppsRange.LAST_7_DAYS)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithContentDescription("Remove 7 days filter").assertIsDisplayed()

        state = state.copy(range = AppsRange.TODAY)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithContentDescription("Remove 7 days filter").assertDoesNotExist()
    }

    @Test
    fun active_filters_retain_chips_while_shrinking_with_motion_enabled() {
        var state by mutableStateOf(AppsUiState(loaded = true, range = AppsRange.LAST_7_DAYS))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides false) {
                    AppsContent(
                        state = state,
                        onEvent = {},
                        onOpenApp = {},
                        onPromptAction = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithContentDescription("Remove 7 days filter").assertExists()

        // Clearing filters under normal motion retains the chip during exit shrink animation
        state = state.copy(range = AppsRange.TODAY)
        rule.mainClock.advanceTimeBy(16)
        rule.onNodeWithContentDescription("Remove 7 days filter").assertExists()

        // Once the exit animation completes, the chip is removed
        rule.mainClock.advanceTimeBy(1000)
        rule.onNodeWithContentDescription("Remove 7 days filter").assertDoesNotExist()
    }

    @Test
    fun search_clear_icon_snaps_under_reduced_motion() {
        var state by mutableStateOf(AppsUiState(loaded = true, query = ""))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    AppsContent(
                        state = state,
                        onEvent = {},
                        onOpenApp = {},
                        onPromptAction = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("Clear search").assertDoesNotExist()

        state = state.copy(query = "video")
        val enterWidths = mutableListOf<Float>()
        repeat(10) {
            rule.mainClock.advanceTimeBy(16)
            val nodes = rule.onAllNodes(hasContentDescription("Clear search")).fetchSemanticsNodes()
            if (nodes.isNotEmpty()) {
                val bounds = rule.onNodeWithContentDescription("Clear search").getBoundsInRoot()
                enterWidths += (bounds.right - bounds.left).value
            }
        }
        assertTrue("Clear search icon must be present across frames, saw ${enterWidths.size}", enterWidths.size >= 8)
        val fullWidth = enterWidths.last()
        assertTrue("Clear search icon must snap to full width without scale-in, saw $enterWidths", fullWidth >= 40f)
        assertTrue(
            "reduced motion must not show intermediate scale-in widths, saw $enterWidths",
            enterWidths.all { it >= fullWidth - 0.5f },
        )
        rule.onNodeWithContentDescription("Clear search").assertIsDisplayed()

        state = state.copy(query = "")
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
    }

    @Test
    fun apps_loading_crossfade_snaps_under_reduced_motion() {
        var state by mutableStateOf(AppsUiState(loaded = false))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    AppsContent(
                        state = state,
                        onEvent = {},
                        onOpenApp = {},
                        onPromptAction = {},
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Loading apps").assertIsDisplayed()

        // Snaps out of loading to content within 2 frames (32ms)
        state = state.copy(loaded = true, apps = sampleApps, totalBytes = 1_100_000_000L)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithContentDescription("Loading apps").assertDoesNotExist()
        rule.onNodeWithText("1.10 GB (apps only)").assertIsDisplayed()
        rule.onNodeWithText("YouTube").assertIsDisplayed()
    }

    @Test
    fun active_filters_update_live_while_non_empty() {
        var state by mutableStateOf(AppsUiState(loaded = true, range = AppsRange.LAST_7_DAYS))
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = state,
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }
        rule.onNodeWithContentDescription("Remove 7 days filter").assertIsDisplayed()

        // Updating filter while activeFilters remains non-empty must recompose to the new chip
        state = state.copy(range = AppsRange.LAST_30_DAYS)
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Remove 7 days filter").assertDoesNotExist()
        rule.onNodeWithContentDescription("Remove 30 days filter").assertIsDisplayed()
    }

    @Test
    fun apps_screen_renders_inside_screen_scaffold_with_scrim() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = AppsUiState(loaded = true),
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }
        rule.onNodeWithTag(SCREEN_SCAFFOLD_SCRIM).assertIsDisplayed()
        rule.onNode(hasText("Apps") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertIsDisplayed()
    }

    @Test
    fun each_non_default_filter_shows_a_removable_chip_with_a_remove_action_described() {
        val events = mutableListOf<AppsEvent>()
        val state = AppsUiState(
            range = AppsRange.LAST_7_DAYS,
            network = AppsNetwork.WIFI,
            sort = AppSort.NAME,
            showSystemApps = true,
            apps = sampleApps,
            totalBytes = 1_100_000_000L,
            loaded = true,
        )

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = state,
                    onEvent = { events += it },
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }

        // Removable chips for non-default filters:
        // 1. Range: "7 days" with remove action
        rule.onNodeWithContentDescription("Remove 7 days filter").assertIsDisplayed().performClick()
        assertEquals(AppsEvent.RemoveFilter(AppFilterKind.RANGE), events.last())

        // 2. Network: "Wi-Fi" with remove action
        rule.onNodeWithContentDescription("Remove Wi-Fi filter").assertIsDisplayed().performClick()
        assertEquals(AppsEvent.RemoveFilter(AppFilterKind.NETWORK), events.last())

        // 3. Sort: "Name" with remove action
        rule.onNodeWithContentDescription("Remove Name filter").assertIsDisplayed().performClick()
        assertEquals(AppsEvent.RemoveFilter(AppFilterKind.SORT), events.last())

        // 4. System apps: "System apps" with remove action
        rule.onNodeWithContentDescription("Remove System apps filter").assertIsDisplayed().performClick()
        assertEquals(AppsEvent.RemoveFilter(AppFilterKind.SYSTEM_APPS), events.last())
    }

    @Test
    fun featured_card_reads_as_one_node() {
        var clicked = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                FeaturedAppCard(
                    app = sampleFeatured,
                    totalBytes = 2_000_000_000L,
                    units = ByteUnits.DECIMAL,
                    icon = null,
                    onClick = { clicked = true },
                )
            }
        }

        // Must be one merged semantic node with description containing label and data
        val cardNode = rule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                .and(hasContentDescription("YouTube", substring = true))
        )
        cardNode.assertIsDisplayed()
        cardNode.performClick()
        assertTrue(clicked)
    }

    @Test
    fun empty_search_result_shows_the_empty_state_with_a_clear_action() {
        val events = mutableListOf<AppsEvent>()
        val state = AppsUiState(
            query = "unknown",
            apps = emptyList(),
            loaded = true,
        )

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = state,
                    onEvent = { events += it },
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }

        // Shows empty state message
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("No apps match \"unknown\"."))
        rule.onNodeWithText("No apps match \"unknown\".").assertIsDisplayed()
        // "Clear search" action is available
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Clear search"))
        rule.onNodeWithText("Clear search").assertIsDisplayed().performClick()
        assertEquals(AppsEvent.SetQuery(""), events.last())
    }

    @Test
    fun rtl() {
        val state = AppsUiState(
            apps = sampleApps,
            featured = sampleFeatured,
            totalBytes = 1_100_000_000L,
            loaded = true,
        )

        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppsContent(
                        state = state,
                        onEvent = {},
                        onOpenApp = {},
                        onPromptAction = {},
                    )
                }
            }
        }

        // Screen renders without crash in RTL
        rule.onNodeWithText("Apps").assertIsDisplayed()
        rule.onNodeWithText("Filters").assertIsDisplayed()
    }

    @Test
    fun removing_an_item_keeps_the_others_and_does_not_crash() {
        var apps by mutableStateOf(sampleApps)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = AppsUiState(apps = apps, loaded = true),
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }

        rule.onNodeWithText("YouTube").assertIsDisplayed()
        rule.onNodeWithText("Chat").assertIsDisplayed()

        // Remove YouTube
        apps = listOf(AppRowUi("com.chat", "Chat", 100_000_000L, 0L))
        rule.waitForIdle()

        rule.onNodeWithText("Chat").assertIsDisplayed()
        rule.onNodeWithText("YouTube").assertDoesNotExist()
    }

    @Test
    fun total_readout_explicitly_specifies_apps_only() {
        val state = AppsUiState(
            apps = sampleApps,
            totalBytes = 1_100_000_000L,
            units = ByteUnits.DECIMAL,
            loaded = true,
        )

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppsContent(
                    state = state,
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }

        rule.onNodeWithText("1.10 GB (apps only)").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun font_scale_2_no_clip() {
        val state = AppsUiState(
            apps = sampleApps,
            featured = sampleFeatured,
            totalBytes = 1_100_000_000L,
            units = ByteUnits.DECIMAL,
            range = AppsRange.LAST_7_DAYS,
            loaded = true,
        )
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppsContent(
                        state = state,
                        onEvent = {},
                        onOpenApp = {},
                        onPromptAction = {},
                    )
                }
            }
        }
        for (label in listOf("Filters", "7 days", "YouTube", "Chat")) {
            val node = rule.onNodeWithText(label, useUnmergedTree = true)
            node.assertIsDisplayed()
            val bounds = node.getBoundsInRoot()
            assertTrue("$label must fit within 360dp", bounds.right.value <= 360f + 1f)
            assertTrue("$label must have positive size", (bounds.right - bounds.left).value > 0f && (bounds.bottom - bounds.top).value > 0f)
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals("$label must stay on a single line", 1, layouts.first().lineCount)
        }
    }

    @Test
    fun clear_search_uses_link_text_and_meets_aa_contrast_across_all_palettes() {
        var currentSpec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
        var expectedLinkColor = Color.Unspecified
        var expectedBackground = Color.Unspecified
        rule.setContent {
            EmberbyteTheme(currentSpec) {
                val palette = LocalEmberbytePalette.current
                SideEffect {
                    expectedLinkColor = palette.linkText
                    expectedBackground = palette.background
                }
                AppsContent(
                    state = AppsUiState(query = "unknown", apps = emptyList(), loaded = true),
                    onEvent = {},
                    onOpenApp = {},
                    onPromptAction = {},
                )
            }
        }
        val variants = listOf(false to false, true to false, true to true)
        for (preset in ThemePresets) {
            for ((dark, amoled) in variants) {
                currentSpec = EmberbyteThemeSpec(presetId = preset.id, darkTheme = dark, amoled = amoled)
                rule.waitForIdle()
                val results = mutableListOf<TextLayoutResult>()
                rule.onNodeWithText("Clear search", useUnmergedTree = true)
                    .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                val actualColor = results.first().layoutInput.style.color
                assertEquals(
                    "Clear search must bind to palette.linkText on ${preset.id} dark=$dark amoled=$amoled",
                    expectedLinkColor,
                    actualColor,
                )
                val fg = actualColor.compositeOver(expectedBackground)
                val l1 = fg.luminance() + 0.05
                val l2 = expectedBackground.luminance() + 0.05
                val ratio = maxOf(l1, l2) / minOf(l1, l2)
                assertTrue(
                    "Clear search on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1 on background, was $ratio",
                    ratio >= 4.5,
                )
            }
        }
    }
}
