package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
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
class AppsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val sampleFeatured = AppRowUi("com.video", "YouTube", 600_000_000L, 400_000_000L)
    private val sampleApps = listOf(
        sampleFeatured,
        AppRowUi("com.chat", "Chat", 100_000_000L, 0L),
    )

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
        rule.onNodeWithText("No apps match \"unknown\".").assertIsDisplayed()
        // "Clear search" action is available
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
}
