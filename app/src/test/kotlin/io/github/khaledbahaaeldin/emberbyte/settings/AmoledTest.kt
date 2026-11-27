package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.res.Configuration
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class AmoledTest {
    @get:Rule val rule = createComposeRule()

    private fun wcagRatio(fg: Color, bg: Color): Double {
        val l1 = fg.luminance() + 0.05
        val l2 = bg.luminance() + 0.05
        return maxOf(l1, l2) / minOf(l1, l2)
    }

    @Test
    fun amoled_cards_are_distinguishable_from_the_background() {
        var currentPreset by mutableStateOf("dusk")
        var resolved: EmberbytePalette? = null
        rule.setContent {
            EmberbyteTheme(
                EmberbyteThemeSpec(presetId = currentPreset, darkTheme = true, amoled = true),
            ) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    resolved = LocalEmberbytePalette.current
                }
            }
        }

        for (preset in ThemePresets) {
            currentPreset = preset.id
            rule.waitForIdle()
            val p = resolved!!
            assertEquals("AMOLED background must stay pure black for ${preset.id}", Color.Black, p.background)
            val surfaceRatio = wcagRatio(p.surface, p.background)
            val surface2Ratio = wcagRatio(p.surface2, p.background)
            assertTrue(
                "AMOLED surface for ${preset.id} must be distinguishable from black (>= 1.05:1), was $surfaceRatio",
                surfaceRatio >= 1.05,
            )
            assertTrue(
                "AMOLED surface2 for ${preset.id} must be distinguishable from black (>= 1.05:1), was $surface2Ratio",
                surface2Ratio >= 1.05,
            )
            assertTrue(
                "AMOLED text on surface for ${preset.id} must meet 4.5:1",
                wcagRatio(p.text, p.surface) >= 4.5,
            )
            assertTrue(
                "AMOLED muted on surface for ${preset.id} must meet 4.5:1",
                wcagRatio(p.muted, p.surface) >= 4.5,
            )
        }
    }

    @Test
    fun amoled_switch_enables_when_system_turns_dark() {
        val repo = FakeSettingsRepository(Settings(themeMode = ThemeMode.SYSTEM))
        val vm = AppearanceViewModel(repo, isSystemDark = { false })
        var nightMode by mutableStateOf(Configuration.UI_MODE_NIGHT_NO)

        rule.setContent {
            val baseConfig = LocalConfiguration.current
            val config = Configuration(baseConfig).apply {
                uiMode = (baseConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
            }
            CompositionLocalProvider(LocalConfiguration provides config) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppearanceScreen(viewModel = vm, onBack = {})
                }
            }
        }

        rule.onNodeWithText("Pure black (AMOLED)").assertIsNotEnabled()

        // Flipping system configuration to dark mode at runtime must enable the AMOLED switch.
        nightMode = Configuration.UI_MODE_NIGHT_YES
        rule.waitForIdle()

        rule.onNodeWithText("Pure black (AMOLED)").assertIsEnabled()
        assertTrue(vm.uiState.value.amoledEnabled)
    }

    @Test
    fun amoled_row_matches_between_settings_and_appearance_and_swatches_include_selected_theme() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec(presetId = "kanagawa", darkTheme = false)) {
                AppearanceContent(
                    uiState = AppearanceUiState(themeId = "kanagawa", themeMode = ThemeMode.LIGHT),
                    onEvent = {},
                    onBack = {},
                    isDark = false,
                )
            }
        }

        rule.onNodeWithText("Pure black (AMOLED)").assertIsDisplayed()
        rule.onNodeWithText("Dark mode only").assertIsDisplayed()
        rule.onNodeWithTag("theme_card_kanagawa").assertIsDisplayed()
    }

    @Test
    fun selection_border_animates() {
        rule.mainClock.autoAdvance = false
        var selected by mutableStateOf("dusk")
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppearanceContent(
                    uiState = AppearanceUiState(themeId = selected),
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.mainClock.advanceTimeBy(500)

        selected = "ember"
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(48)

        val emberNode = rule.onNodeWithTag("theme_card_ember").fetchSemanticsNode()
        val borderAlpha = emberNode.config.firstOrNull { it.key.name == "ThemeCardSelectionAlpha" }?.value as? Float
        assertTrue("Expected animated in-between selection alpha in (0, 1), got $borderAlpha", borderAlpha != null && borderAlpha > 0f && borderAlpha < 1f)
    }

    @Test
    fun selection_border_snaps_when_reduced_motion() {
        rule.mainClock.autoAdvance = false
        var selected by mutableStateOf("dusk")
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    AppearanceContent(
                        uiState = AppearanceUiState(themeId = selected),
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()

        selected = "ember"
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()

        val emberNode = rule.onNodeWithTag("theme_card_ember").fetchSemanticsNode()
        val borderAlpha = emberNode.config.firstOrNull { it.key.name == "ThemeCardSelectionAlpha" }?.value as? Float
        assertEquals(1f, borderAlpha ?: -1f, 0.005f)
    }
}
