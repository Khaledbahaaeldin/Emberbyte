package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteMappingTest {

    @Test
    fun mapping_table_matches_spec() {
        val palette = GeneratedPalettes.getValue("dusk").first
        val scheme = palette.toColorScheme(dark = true)

        assertEquals(palette.primary, scheme.primary)
        assertEquals(palette.onPrimary, scheme.onPrimary)
        assertEquals(palette.primary, scheme.primaryContainer)
        assertEquals(palette.onPrimary, scheme.onPrimaryContainer)
        assertEquals(palette.primary, scheme.secondary)
        assertEquals(palette.surface2, scheme.secondaryContainer)
        assertEquals(palette.text, scheme.onSecondaryContainer)
        assertEquals(palette.accent, scheme.tertiary)
        assertEquals(palette.onAccent, scheme.onTertiary)
        assertEquals(palette.accent, scheme.tertiaryContainer)
        assertEquals(palette.onAccent, scheme.onTertiaryContainer)
        assertEquals(palette.danger, scheme.error)
        assertEquals(palette.onDanger, scheme.onError)
        assertEquals(palette.danger, scheme.errorContainer)
        assertEquals(palette.onDanger, scheme.onErrorContainer)
        assertEquals(palette.background, scheme.background)
        assertEquals(palette.text, scheme.onBackground)
        assertEquals(palette.background, scheme.surface)
        assertEquals(palette.text, scheme.onSurface)
        assertEquals(palette.surface2, scheme.surfaceVariant)
        assertEquals(palette.muted, scheme.onSurfaceVariant)
        assertEquals(palette.muted, scheme.outline)
        assertEquals(palette.track, scheme.outlineVariant)
        assertEquals(palette.background, scheme.surfaceContainerLowest)
        assertEquals(palette.surface, scheme.surfaceContainerLow)
        assertEquals(palette.surface, scheme.surfaceContainer)
        assertEquals(palette.surface2, scheme.surfaceContainerHigh)
        assertEquals(palette.surface2, scheme.surfaceContainerHighest)
        assertEquals(palette.text, scheme.inverseSurface)
        assertEquals(palette.background, scheme.inverseOnSurface)
        assertEquals(palette.primary, scheme.inversePrimary)
        assertEquals(palette.primary, scheme.surfaceTint)
    }

    @Test
    fun amoled_turns_dark_background_and_surface_pure_black_only_in_dark() {
        val darkSpec = EmberbyteThemeSpec(
            presetId = "dusk",
            darkTheme = true,
            amoled = true,
            wallpaperColors = false,
        )
        val darkPalette = resolvePalette(darkSpec, sdkInt = 35) { error("dynamic lambda should not be called") }
        assertEquals(Color.Black, darkPalette.background)
        assertEquals(Color.Black, darkPalette.surface)

        val lightSpec = EmberbyteThemeSpec(
            presetId = "dusk",
            darkTheme = false,
            amoled = true,
            wallpaperColors = false,
        )
        val lightPalette = resolvePalette(lightSpec, sdkInt = 35) { error("dynamic lambda should not be called") }
        val lightExpected = GeneratedPalettes.getValue("dusk").second
        assertEquals(lightExpected.background, lightPalette.background)
        assertEquals(lightExpected.surface, lightPalette.surface)
        assertNotEquals(Color.Black, lightPalette.background)
    }

    @Test
    fun unknown_preset_id_falls_back_to_dusk() {
        val darkSpec = EmberbyteThemeSpec(
            presetId = "unknown_nonexistent_preset",
            darkTheme = true,
            amoled = false,
            wallpaperColors = false,
        )
        val darkPalette = resolvePalette(darkSpec, sdkInt = 35) { error("unused") }
        assertEquals(GeneratedPalettes.getValue("dusk").first, darkPalette)

        val lightSpec = EmberbyteThemeSpec(
            presetId = "unknown_nonexistent_preset",
            darkTheme = false,
            amoled = false,
            wallpaperColors = false,
        )
        val lightPalette = resolvePalette(lightSpec, sdkInt = 35) { error("unused") }
        assertEquals(GeneratedPalettes.getValue("dusk").second, lightPalette)
    }

    @Test
    fun wallpaper_colors_use_the_dynamic_scheme_on_sdk_31_and_are_ignored_on_sdk_30() {
        val fakeCustomPrimary = Color(0xFF123456)
        val fakeScheme = darkColorScheme(primary = fakeCustomPrimary)
        val spec = EmberbyteThemeSpec(
            presetId = "dusk",
            darkTheme = true,
            amoled = false,
            wallpaperColors = true,
        )

        var dynamicCalled = false
        val palette31 = resolvePalette(spec, sdkInt = 31) {
            dynamicCalled = true
            fakeScheme
        }
        assertTrue(dynamicCalled)
        assertEquals(fakeCustomPrimary, palette31.primary)

        dynamicCalled = false
        val palette30 = resolvePalette(spec, sdkInt = 30) {
            dynamicCalled = true
            fakeScheme
        }
        assertFalse(dynamicCalled)
        assertEquals(GeneratedPalettes.getValue("dusk").first, palette30)
    }

    @Test
    fun wallpaper_palette_keeps_dusk_warn_and_danger() {
        val darkFake = darkColorScheme()
        val darkPalette = EmberbytePalette.fromDynamic(darkFake)
        assertEquals(DuskDark.warn, darkPalette.warn)
        assertEquals(DuskDark.onWarn, darkPalette.onWarn)
        assertEquals(DuskDark.danger, darkPalette.danger)
        assertEquals(DuskDark.onDanger, darkPalette.onDanger)

        val lightFake = lightColorScheme()
        val lightPalette = EmberbytePalette.fromDynamic(lightFake)
        assertEquals(DuskLight.warn, lightPalette.warn)
        assertEquals(DuskLight.onWarn, lightPalette.onWarn)
        assertEquals(DuskLight.danger, lightPalette.danger)
        assertEquals(DuskLight.onDanger, lightPalette.onDanger)
    }
}
