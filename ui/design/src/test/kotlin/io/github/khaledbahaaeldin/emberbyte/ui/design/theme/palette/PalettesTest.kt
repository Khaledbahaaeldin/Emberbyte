package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PalettesTest {
    @Test
    fun generated_palettes_have_the_twelve_ids() {
        val expected = setOf(
            "dusk",
            "ember",
            "monochrome",
            "catppuccin",
            "gruvbox",
            "nord",
            "rosepine",
            "solarized",
            "dracula",
            "tokyonight",
            "everforest",
            "kanagawa",
        )
        assertEquals(expected, GeneratedPalettes.keys)
    }

    @Test
    fun every_theme_has_distinct_light_and_dark() {
        for ((id, pair) in GeneratedPalettes) {
            val (dark, light) = pair
            assertNotEquals("Theme $id has identical light and dark backgrounds", dark.background, light.background)
        }
    }

    @Test
    fun dark_backgrounds_are_dark_and_light_backgrounds_are_light() {
        for ((id, pair) in GeneratedPalettes) {
            val (dark, light) = pair
            val darkLum = dark.background.luminance()
            val lightLum = light.background.luminance()
            assertTrue("Theme $id dark background luminance ($darkLum) must be < 0.2", darkLum < 0.2f)
            assertTrue("Theme $id light background luminance ($lightLum) must be > 0.5", lightLum > 0.5f)
        }
    }

    @Test
    fun dusk_dark_matches_the_approved_reference() {
        val (duskDark, _) = checkNotNull(GeneratedPalettes["dusk"])
        assertEquals(Color(0xFFD0BCFF), duskDark.primary)
        assertEquals(Color(0xFF2A1858), duskDark.onPrimary)
        assertEquals(Color(0xFF110F19), duskDark.background)
        assertEquals(Color(0xFFC9F0D3), duskDark.accent)
    }
}
