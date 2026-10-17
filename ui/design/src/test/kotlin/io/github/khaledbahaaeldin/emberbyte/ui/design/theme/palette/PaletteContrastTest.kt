package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

                // Track visibility and side-of-fill rules
                val lumTrack = palette.track.luminance()
                val lumPrimary = palette.primary.luminance()
                if (mode == "light") {
                    if (lumTrack < lumPrimary) {
                        failures.add("$id/$mode/track side-of-fill: track lum ($lumTrack) is darker than primary ($lumPrimary)")
                    }
                } else {
                    if (lumTrack > lumPrimary) {
                        failures.add("$id/$mode/track side-of-fill: track lum ($lumTrack) is lighter than primary ($lumPrimary)")
                    }
                }
                val rTrackSurf = contrastRatio(palette.track, palette.surface)
                if (rTrackSurf < 1.3) {
                    failures.add("$id/$mode/track vs surface $rTrackSurf (expected >= 1.3)")
                }
                val rTrackBg = contrastRatio(palette.track, palette.background)
                if (rTrackBg < 1.3) {
                    failures.add("$id/$mode/track vs background $rTrackBg (expected >= 1.3)")
                }
                val rPrimaryTrack = contrastRatio(palette.primary, palette.track)
                if (rPrimaryTrack < 2.2) {
                    failures.add("$id/$mode/primary vs track $rPrimaryTrack (expected >= 2.2)")
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
