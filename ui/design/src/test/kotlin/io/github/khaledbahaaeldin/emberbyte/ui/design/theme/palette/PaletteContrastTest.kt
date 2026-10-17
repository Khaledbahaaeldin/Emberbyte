package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteContrastTest {

    @Test
    fun every_pair_passes_in_every_theme_and_mode() {
        val failures = mutableListOf<String>()
        val themeIds = listOf(
            "dusk", "ember", "monochrome", "catppuccin", "gruvbox", "nord",
            "rosepine", "solarized", "dracula", "tokyonight", "everforest", "kanagawa"
        )
        for (id in themeIds) {
            val (dark, light) = checkNotNull(GeneratedPalettes[id]) { "Missing palette: $id" }
            val amoled = dark.copy(background = Color.Black, surface = Color.Black)
            val variants = listOf(
                "dark" to dark,
                "amoled" to amoled,
                "light" to light,
            )
            for ((mode, palette) in variants) {
                for (pair in ContrastPairs.all) {
                    val fg = pair.foreground(palette)
                    val bg = pair.background(palette)
                    val ratio = contrastRatio(fg, bg)
                    if (ratio < pair.minRatio) {
                        failures.add("$id/$mode/${pair.name} $ratio (expected >= ${pair.minRatio})")
                    }
                }
            }
        }
        assertTrue(
            "Expected all contrast pairs to pass, but found failures:\n${failures.joinToString("\n")}",
            failures.isEmpty()
        )
    }

    @Test
    fun contrast_ratio_matches_wcag_reference_values() {
        val blackOnWhite = contrastRatio(Color.Black, Color.White)
        assertEquals(21.0, blackOnWhite, 0.01)

        val grayOnWhite = contrastRatio(Color(0xFF777777), Color.White)
        assertEquals(4.48, grayOnWhite, 0.02)
    }
}
