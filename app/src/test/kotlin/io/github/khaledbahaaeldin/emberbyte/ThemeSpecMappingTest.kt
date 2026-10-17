package io.github.khaledbahaaeldin.emberbyte

import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSpecMappingTest {

    @Test
    fun system_mode_follows_system_dark() {
        val settings = Settings(themeMode = ThemeMode.SYSTEM)
        assertTrue(themeSpecOf(settings, systemDark = true, sdkInt = 34).darkTheme)
        assertFalse(themeSpecOf(settings, systemDark = false, sdkInt = 34).darkTheme)
    }

    @Test
    fun light_and_dark_modes_ignore_system_dark() {
        val lightSettings = Settings(themeMode = ThemeMode.LIGHT)
        assertFalse(themeSpecOf(lightSettings, systemDark = true, sdkInt = 34).darkTheme)
        assertFalse(themeSpecOf(lightSettings, systemDark = false, sdkInt = 34).darkTheme)

        val darkSettings = Settings(themeMode = ThemeMode.DARK)
        assertTrue(themeSpecOf(darkSettings, systemDark = true, sdkInt = 34).darkTheme)
        assertTrue(themeSpecOf(darkSettings, systemDark = false, sdkInt = 34).darkTheme)
    }

    @Test
    fun amoled_is_only_enabled_when_in_dark_theme_and_amoled_black_is_true() {
        // amoledBlack = true with dark theme -> true
        val darkWithAmoled = Settings(themeMode = ThemeMode.DARK, amoledBlack = true)
        assertTrue(themeSpecOf(darkWithAmoled, systemDark = false, sdkInt = 34).amoled)

        val systemDarkWithAmoled = Settings(themeMode = ThemeMode.SYSTEM, amoledBlack = true)
        assertTrue(themeSpecOf(systemDarkWithAmoled, systemDark = true, sdkInt = 34).amoled)

        // amoledBlack = true with light theme -> false
        val lightWithAmoled = Settings(themeMode = ThemeMode.LIGHT, amoledBlack = true)
        assertFalse(themeSpecOf(lightWithAmoled, systemDark = true, sdkInt = 34).amoled)

        val systemLightWithAmoled = Settings(themeMode = ThemeMode.SYSTEM, amoledBlack = true)
        assertFalse(themeSpecOf(systemLightWithAmoled, systemDark = false, sdkInt = 34).amoled)

        // amoledBlack = false -> always false
        val darkNoAmoled = Settings(themeMode = ThemeMode.DARK, amoledBlack = false)
        assertFalse(themeSpecOf(darkNoAmoled, systemDark = false, sdkInt = 34).amoled)
    }

    @Test
    fun wallpaper_colors_only_enabled_when_requested_and_sdk_is_at_least_31() {
        val wallpaperOn = Settings(useWallpaperColors = true)
        assertTrue(themeSpecOf(wallpaperOn, systemDark = false, sdkInt = 31).wallpaperColors)
        assertTrue(themeSpecOf(wallpaperOn, systemDark = false, sdkInt = 35).wallpaperColors)
        assertFalse(themeSpecOf(wallpaperOn, systemDark = false, sdkInt = 30).wallpaperColors)
        assertFalse(themeSpecOf(wallpaperOn, systemDark = false, sdkInt = 29).wallpaperColors)

        val wallpaperOff = Settings(useWallpaperColors = false)
        assertFalse(themeSpecOf(wallpaperOff, systemDark = false, sdkInt = 31).wallpaperColors)
        assertFalse(themeSpecOf(wallpaperOff, systemDark = false, sdkInt = 35).wallpaperColors)
        assertFalse(themeSpecOf(wallpaperOff, systemDark = false, sdkInt = 30).wallpaperColors)
    }

    @Test
    fun preset_id_matches_the_settings_theme_id() {
        val defaultSettings = Settings()
        assertEquals("dusk", themeSpecOf(defaultSettings, systemDark = false, sdkInt = 34).presetId)

        val emberSettings = Settings(themeId = "ember")
        assertEquals("ember", themeSpecOf(emberSettings, systemDark = false, sdkInt = 34).presetId)

        val nordSettings = Settings(themeId = "nord")
        assertEquals("nord", themeSpecOf(nordSettings, systemDark = false, sdkInt = 34).presetId)
    }
}
