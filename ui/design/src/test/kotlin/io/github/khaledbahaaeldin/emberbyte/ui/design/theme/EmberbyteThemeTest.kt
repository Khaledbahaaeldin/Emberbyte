package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.GeneratedPalettes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.fromDynamic
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.resolvePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EmberbyteThemeTest {
    @get:Rule val rule = createComposeRule()

    private val dynLight = lightColorScheme(primary = Color.Red)
    private val dynDark = darkColorScheme(primary = Color.Blue)

    private fun resolve(dark: Boolean, dynamic: Boolean, amoled: Boolean, sdk: Int) =
        resolvePalette(
            spec = EmberbyteThemeSpec(
                presetId = "dusk",
                darkTheme = dark,
                amoled = amoled,
                wallpaperColors = dynamic,
            ),
            sdkInt = sdk,
            dynamic = { if (dark) dynDark else dynLight },
        )

    @Test fun static_light_below_android_12() =
        assertEquals(GeneratedPalettes.getValue("dusk").second, resolve(dark = false, dynamic = true, amoled = false, sdk = 30))

    @Test fun static_dark_below_android_12() =
        assertEquals(GeneratedPalettes.getValue("dusk").first, resolve(dark = true, dynamic = true, amoled = false, sdk = 30))

    @Test fun dynamic_light_on_android_12() =
        assertEquals(EmberbytePalette.fromDynamic(dynLight), resolve(dark = false, dynamic = true, amoled = false, sdk = 31))

    @Test fun dynamic_dark_on_android_12() =
        assertEquals(EmberbytePalette.fromDynamic(dynDark), resolve(dark = true, dynamic = true, amoled = false, sdk = 34))

    @Test fun dynamic_disabled_uses_static_palette() =
        assertEquals(GeneratedPalettes.getValue("dusk").first, resolve(dark = true, dynamic = false, amoled = false, sdk = 34))

    @Test fun amoled_makes_dark_surfaces_black() {
        val palette = resolve(dark = true, dynamic = false, amoled = true, sdk = 34)
        assertEquals(Color.Black, palette.background)
        assertEquals(Color.Black, palette.surface)
    }

    @Test fun amoled_is_ignored_in_light_theme() =
        assertEquals(GeneratedPalettes.getValue("dusk").second, resolve(dark = false, dynamic = false, amoled = true, sdk = 34))

    @Test fun reduce_motion_only_when_scale_is_zero() {
        assertTrue(isReduceMotion(0f))
        assertFalse(isReduceMotion(1f))
        assertFalse(isReduceMotion(0.5f))
    }

    @Test fun amoled_over_dynamic_dark_blackens_three_surfaces_and_keeps_the_rest() {
        val lifted = dynDark.copy(
            background = Color.Gray,
            surface = Color.Gray,
        )
        val palette = resolvePalette(
            spec = EmberbyteThemeSpec(presetId = "dusk", darkTheme = true, amoled = true, wallpaperColors = true),
            sdkInt = 34,
            dynamic = { lifted },
        )
        assertEquals(Color.Black, palette.background)
        assertEquals(Color.Black, palette.surface)
        assertEquals(Color.Blue, palette.primary)
        assertEquals(lifted.surfaceVariant, palette.surface2)
        assertEquals(lifted.onSurface, palette.text)
    }

    @Test
    fun default_theme_provides_dusk_dark_palette_and_material_scheme() {
        var palette: EmberbytePalette? = null
        var primary: Color? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                palette = LocalEmberbytePalette.current
                primary = MaterialTheme.colorScheme.primary
            }
        }
        assertEquals(GeneratedPalettes.getValue("dusk").first, palette)
        assertEquals(Color(0xFFD0BCFF), primary)
    }

    @Test
    fun local_reduce_motion_is_provided() {
        var reduceMotion: Boolean? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                reduceMotion = LocalReduceMotion.current
            }
        }
        assertEquals(false, reduceMotion)
    }
}
