package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1200dp")
class AppearanceScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun shows_twelve_theme_cards_with_names_and_the_selected_one_has_a_check_and_the_description_dusk_theme_selected() {
        val names = listOf(
            "Dusk", "Ember", "Monochrome", "Catppuccin", "Gruvbox", "Nord",
            "Rosé Pine", "Solarized", "Dracula", "Tokyo Night", "Everforest", "Kanagawa",
        )
        var selectedTheme: String? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppearanceContent(
                    uiState = AppearanceUiState(themeId = "dusk"),
                    onEvent = { event ->
                        if (event is AppearanceEvent.SetTheme) selectedTheme = event.id
                    },
                    onBack = {},
                )
            }
        }

        for (name in names) {
            rule.onNodeWithText(name).assertIsDisplayed()
        }

        rule.onNodeWithContentDescription("Dusk theme, selected").assertIsDisplayed()
        rule.onNodeWithContentDescription("Selected").assertIsDisplayed()
        rule.onNodeWithContentDescription("Ember theme").assertIsDisplayed()

        rule.onNodeWithText("Ember").performClick()
        assertEquals("ember", selectedTheme)
    }

    @Test
    fun cards_preview_the_dark_colours_in_dark_mode_and_light_in_light_mode() {
        var isDarkMode by mutableStateOf(true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppearanceContent(
                    uiState = AppearanceUiState(themeId = "dusk"),
                    onEvent = {},
                    onBack = {},
                    isDark = isDarkMode,
                )
            }
        }

        for (preset in ThemePresets) {
            val node = rule.onNodeWithTag("theme_card_${preset.id}")
            val palette = node.fetchSemanticsNode().config[ThemeCardPaletteKey]
            assertEquals("Card ${preset.id} dark primary mismatch", preset.dark.primary, palette.primary)
            assertEquals("Card ${preset.id} dark surface2 mismatch", preset.dark.surface2, palette.surface2)
        }

        isDarkMode = false
        rule.waitForIdle()

        for (preset in ThemePresets) {
            val node = rule.onNodeWithTag("theme_card_${preset.id}")
            val palette = node.fetchSemanticsNode().config[ThemeCardPaletteKey]
            assertEquals("Card ${preset.id} light primary mismatch", preset.light.primary, palette.primary)
            assertEquals("Card ${preset.id} light surface2 mismatch", preset.light.surface2, palette.surface2)
        }
    }

    @Test
    fun mode_group_has_three_segments_in_this_order_system_dark_light() {
        var clickedMode: ThemeMode? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppearanceContent(
                    uiState = AppearanceUiState(themeMode = ThemeMode.SYSTEM),
                    onEvent = { event ->
                        if (event is AppearanceEvent.SetMode) clickedMode = event.mode
                    },
                    onBack = {},
                )
            }
        }

        val radioButtons = rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).fetchSemanticsNodes()
        val texts = radioButtons.map { node ->
            node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
        assertEquals(listOf("System", "Dark", "Light"), texts)

        rule.onNodeWithText("Dark").performClick()
        assertEquals(ThemeMode.DARK, clickedMode)

        rule.onNodeWithText("Light").performClick()
        assertEquals(ThemeMode.LIGHT, clickedMode)

        rule.onNodeWithText("System").performClick()
        assertEquals(ThemeMode.SYSTEM, clickedMode)
    }

    @Test
    fun wallpaper_switch_hidden_on_sdk_30() {
        var state by mutableStateOf(AppearanceUiState(wallpaperSupported = false))
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppearanceContent(
                    uiState = state,
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("Wallpaper colours").assertDoesNotExist()

        state = AppearanceUiState(wallpaperSupported = true)
        rule.waitForIdle()
        rule.onNodeWithText("Wallpaper colours").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun all_targets_are_48dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppearanceContent(
                    uiState = AppearanceUiState(wallpaperSupported = true, amoledEnabled = true),
                    onEvent = {},
                    onBack = {},
                )
            }
        }
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun grid_has_two_columns_at_font_scale_2_and_three_otherwise() {
        var currentDensity by mutableStateOf(Density(density = 2.625f, fontScale = 1.0f))
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides currentDensity) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppearanceContent(
                        uiState = AppearanceUiState(),
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }

        val duskY1 = rule.onNodeWithTag("theme_card_dusk").fetchSemanticsNode().positionInRoot.y
        val emberY1 = rule.onNodeWithTag("theme_card_ember").fetchSemanticsNode().positionInRoot.y
        val monoY1 = rule.onNodeWithTag("theme_card_monochrome").fetchSemanticsNode().positionInRoot.y
        val catY1 = rule.onNodeWithTag("theme_card_catppuccin").fetchSemanticsNode().positionInRoot.y

        assertEquals("Dusk and Ember should be in same row", duskY1, emberY1, 1f)
        assertEquals("Dusk and Monochrome should be in same row", duskY1, monoY1, 1f)
        assertTrue("Catppuccin should be in the next row", catY1 > duskY1)

        currentDensity = Density(density = 2.625f, fontScale = 2.0f)
        rule.waitForIdle()

        val duskY2 = rule.onNodeWithTag("theme_card_dusk").fetchSemanticsNode().positionInRoot.y
        val emberY2 = rule.onNodeWithTag("theme_card_ember").fetchSemanticsNode().positionInRoot.y
        val monoY2 = rule.onNodeWithTag("theme_card_monochrome").fetchSemanticsNode().positionInRoot.y

        assertEquals("Dusk and Ember should be in same row at fontScale 2", duskY2, emberY2, 1f)
        assertTrue("Monochrome should be in the next row at fontScale 2", monoY2 > duskY2)
    }

    @Test
    fun rtl_mirrors_the_grid() {
        var layoutDir by mutableStateOf(LayoutDirection.Ltr)
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    AppearanceContent(
                        uiState = AppearanceUiState(),
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }

        val duskX1 = rule.onNodeWithTag("theme_card_dusk").fetchSemanticsNode().positionInRoot.x
        val emberX1 = rule.onNodeWithTag("theme_card_ember").fetchSemanticsNode().positionInRoot.x
        val monoX1 = rule.onNodeWithTag("theme_card_monochrome").fetchSemanticsNode().positionInRoot.x
        assertTrue("LTR: Dusk < Ember", duskX1 < emberX1)
        assertTrue("LTR: Ember < Monochrome", emberX1 < monoX1)

        layoutDir = LayoutDirection.Rtl
        rule.waitForIdle()

        val duskX2 = rule.onNodeWithTag("theme_card_dusk").fetchSemanticsNode().positionInRoot.x
        val emberX2 = rule.onNodeWithTag("theme_card_ember").fetchSemanticsNode().positionInRoot.x
        val monoX2 = rule.onNodeWithTag("theme_card_monochrome").fetchSemanticsNode().positionInRoot.x
        assertTrue("RTL: Dusk > Ember", duskX2 > emberX2)
        assertTrue("RTL: Ember > Monochrome", emberX2 > monoX2)
    }
}
