package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextRolesTest {
    @Test
    fun ensure_contrast_keeps_a_passing_colour_unchanged() {
        val ink = Color(0xFF1D1730)
        assertEquals(ink, ensureContrast(ink, Color.White))
    }

    @Test
    fun ensure_contrast_meets_the_ratio_on_light_and_dark_backgrounds() {
        val pale = Color(0xFFB9ECE1)
        for (background in listOf(Color(0xFFE4D8F5), Color(0xFFFDF8FF), Color(0xFF2A2440), Color(0xFF000000))) {
            val result = ensureContrast(pale, background)
            assertTrue("$background: ${contrastRatio(result, background)}", contrastRatio(result, background) >= 4.5)
        }
    }

    @Test
    fun ensure_contrast_moves_toward_black_on_light_and_toward_white_on_dark() {
        val paleOnLight = Color(0xFF9090A0)
        assertTrue(ensureContrast(paleOnLight, Color(0xFFF1E9FB)).luminance() < paleOnLight.luminance())
        val dimOnDark = Color(0xFF3A3250)
        assertTrue(ensureContrast(dimOnDark, Color(0xFF1D1929)).luminance() > dimOnDark.luminance())
    }

    @Test
    fun ensure_contrast_keeps_the_hue() {
        val teal = Color(0xFF3DB8A6)
        val result = ensureContrast(teal, Color(0xFFFDF8FF))
        // Same dominant channel ordering: green and blue stay above red.
        assertTrue(result.green >= result.red && result.blue >= result.red)
    }

    @Test
    fun accent_and_link_text_roles_pass_on_every_surface_in_every_theme() {
        val failures = mutableListOf<String>()
        for ((id, pair) in GeneratedPalettes) {
            for ((mode, palette) in listOf("dark" to pair.first, "light" to pair.second)) {
                for ((surfaceName, surface) in listOf("surface2" to palette.surface2, "surface" to palette.surface, "background" to palette.background)) {
                    if (contrastRatio(palette.accentText, surface) < 4.5) failures += "$id/$mode accentText on $surfaceName"
                    if (contrastRatio(palette.linkText, surface) < 4.5) failures += "$id/$mode linkText on $surfaceName"
                }
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}
