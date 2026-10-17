package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h3000dp")
class AppDetailScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val sampleBars = listOf(
        BarUi(label = "Mon", bytes = 100_000_000L, description = "Monday, 100 megabytes"),
        BarUi(label = "Tue", bytes = 200_000_000L, description = "Tuesday, 200 megabytes"),
        BarUi(label = "Wed", bytes = 700_000_000L, description = "Wednesday, 700 megabytes"),
    )

    private val sampleState = AppDetailUiState(
        packageName = "com.video",
        label = "Video",
        totalBytes = 1_000_000_000L,
        mobileBytes = 600_000_000L,
        wifiBytes = 400_000_000L,
        foregroundBytes = 750_000_000L,
        backgroundBytes = 250_000_000L,
        bars = sampleBars,
        units = ByteUnits.DECIMAL,
        loaded = true,
    )

    private fun show(state: AppDetailUiState = sampleState) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppDetailContent(
                    state = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun total_is_one_node_with_unit() {
        show(sampleState)
        rule.onNodeWithContentDescription("1.00 gigabytes used in the last 7 days").assertIsDisplayed()
    }

    @Test
    fun daily_chart_has_a_text_alternative_for_each_bar() {
        show(sampleState)
        sampleState.bars.forEach { bar ->
            rule.onNodeWithContentDescription(bar.description).assertExists()
        }
    }

    @Test
    fun unknown_package_shows_the_letter_icon_and_no_crash() {
        val unknownState = AppDetailUiState(
            packageName = "com.unknown.app",
            label = "Unknown",
            totalBytes = 500_000L,
            loaded = true,
        )
        show(unknownState)
        rule.onNodeWithText("U", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Unknown").assertIsDisplayed()
    }

    @Test
    fun foreground_background_tiles_show_both_values() {
        show(sampleState)
        rule.onNodeWithText("Foreground").assertIsDisplayed()
        rule.onNodeWithText("750 MB").assertIsDisplayed()
        rule.onNodeWithText("Background").assertIsDisplayed()
        rule.onNodeWithText("250 MB").assertIsDisplayed()
    }

    @Test
    fun font_scale_2_no_clip() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppDetailContent(
                        state = sampleState,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Video").assertIsDisplayed()
    }

    @Test
    fun rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppDetailContent(
                        state = sampleState,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Video").assertIsDisplayed()
    }
}
