package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
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
class FiltersSheetTest {
    @get:Rule
    val rule = createComposeRule()

    private data class Applied(val network: AppsNetwork, val sort: AppSort, val system: Boolean)

    private fun show(
        network: AppsNetwork = AppsNetwork.ALL,
        sort: AppSort = AppSort.BYTES_DESC,
        system: Boolean = false,
        onApply: (Applied) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                FiltersSheetContent(
                    network = network,
                    sort = sort,
                    showSystemApps = system,
                    onApply = { n, s, sys -> onApply(Applied(n, s, sys)) },
                    onDismiss = onDismiss,
                )
            }
        }
    }

    @Test
    fun sheet_has_network_sort_and_system_apps_but_no_period() {
        show()
        rule.onNodeWithText("Network").assertIsDisplayed()
        rule.onNodeWithText("Sort by").assertIsDisplayed()
        rule.onNodeWithText("Show system apps").assertIsDisplayed()
        rule.onAllNodesWithText("Period").assertCountEquals(0)
        rule.onAllNodesWithText("7 days").assertCountEquals(0)
        listOf("Most", "Least", "Name").forEach { rule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun sheet_apply_closes_and_reset_only_resets_the_draft() {
        var applied: Applied? = null
        var applyCalls = 0
        var dismissed = 0
        show(network = AppsNetwork.MOBILE, sort = AppSort.NAME, system = true, onApply = { applied = it; applyCalls++ }, onDismiss = { dismissed++ })

        rule.onNodeWithTag("apps_filters_reset").performScrollTo().performClick()
        // Reset alone commits nothing and keeps the sheet open.
        assertEquals(0, applyCalls)
        assertEquals(0, dismissed)

        rule.onNodeWithTag("apps_filters_apply").performScrollTo().performClick()
        assertEquals(Applied(AppsNetwork.ALL, AppSort.BYTES_DESC, false), applied)
        assertEquals(1, dismissed)
    }

    @Test
    fun apply_sends_the_chosen_network_sort_and_system_apps() {
        var applied: Applied? = null
        show(onApply = { applied = it })
        rule.onNodeWithText("Wi-Fi").performClick()
        rule.onNodeWithText("Least").performClick()
        rule.onNodeWithTag("apps_filters_apply").performScrollTo().performClick()
        assertEquals(Applied(AppsNetwork.WIFI, AppSort.BYTES_ASC, false), applied)
    }

    @Test
    fun reset_is_a_tall_visible_button_and_apply_is_wider() {
        show()
        val reset = rule.onNodeWithTag("apps_filters_reset").getUnclippedBoundsInRoot()
        val apply = rule.onNodeWithTag("apps_filters_apply").getUnclippedBoundsInRoot()
        assertTrue("reset at least 52 dp", (reset.bottom - reset.top) >= 51.5.dp)
        val resetWidth = (reset.right - reset.left).value
        val applyWidth = (apply.right - apply.left).value
        assertEquals("Apply is about 1.6 times wider (${applyWidth / resetWidth})", 1.6f, applyWidth / resetWidth, 0.1f)
    }

    @Test
    fun sheet_is_keyboard_safe_and_scrolls_at_font_scale_2() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(modifier = Modifier.height(400.dp)) {
                        FiltersSheetContent(
                            network = AppsNetwork.ALL,
                            sort = AppSort.BYTES_DESC,
                            showSystemApps = false,
                            onApply = { _, _, _ -> },
                            onDismiss = {},
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("Network").assertIsDisplayed()
        rule.onNodeWithTag("apps_filters_apply").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("apps_filters_reset").performScrollTo().assertIsDisplayed()
    }
}
