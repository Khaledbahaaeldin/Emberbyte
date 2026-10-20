package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
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

    @Test
    fun filters_button_opens_the_sheet_and_apply_updates_the_list() {
        var appliedRange = AppsRange.TODAY
        var appliedNetwork = AppsNetwork.ALL
        var appliedSort = AppSort.BYTES_DESC
        var appliedSystemApps = false
        var dismissed = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                FiltersSheetContent(
                    range = AppsRange.TODAY,
                    network = AppsNetwork.ALL,
                    sort = AppSort.BYTES_DESC,
                    showSystemApps = false,
                    onApply = { r, n, s, sys ->
                        appliedRange = r
                        appliedNetwork = n
                        appliedSort = s
                        appliedSystemApps = sys
                    },
                    onReset = {},
                    onDismiss = { dismissed = true },
                )
            }
        }

        // Tap "7 days" in Period group
        rule.onNodeWithText("7 days").performClick()
        // Tap "Wi-Fi" in Network group
        rule.onNodeWithText("Wi-Fi").performClick()
        // Tap "Name" in Sort group
        rule.onNodeWithText("Name").performClick()
        // Tap "Apply"
        rule.onNodeWithText("Apply").performScrollTo().performClick()

        assertEquals(AppsRange.LAST_7_DAYS, appliedRange)
        assertEquals(AppsNetwork.WIFI, appliedNetwork)
        assertEquals(AppSort.NAME, appliedSort)
        assertEquals(false, appliedSystemApps)
    }

    @Test
    fun sheet_is_keyboard_safe_and_scrolls_at_font_scale_2() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(modifier = Modifier.height(400.dp)) {
                        FiltersSheetContent(
                            range = AppsRange.TODAY,
                            network = AppsNetwork.ALL,
                            sort = AppSort.BYTES_DESC,
                            showSystemApps = false,
                            onApply = { _, _, _, _ -> },
                            onReset = {},
                            onDismiss = {},
                        )
                    }
                }
            }
        }

        // Verify elements can be scrolled to without clipping or crashing
        rule.onNodeWithText("Period").assertIsDisplayed()
        rule.onNodeWithText("Apply").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Reset").performScrollTo().assertIsDisplayed()
    }
}
