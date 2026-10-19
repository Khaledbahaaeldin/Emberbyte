package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.AppPreferences
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.ui.graphics.Color
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import org.junit.After
import org.junit.Before

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetThemeTest {

    private lateinit var fakeSettings: FakeSettingsRepository

    @Before
    fun setUp() {
        fakeSettings = FakeSettingsRepository()
        WidgetTheme.settingsProvider = { fakeSettings }
    }

    @After
    fun tearDown() {
        WidgetTheme.settingsProvider = null
    }

    private fun preset(id: String) = ThemePresets.first { it.id == id }

    @Test
    fun resolve_follows_theme_id_and_mode() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeSettings.update {
            it.copy(
                themeId = "rosepine",
                themeMode = ThemeMode.LIGHT,
                amoledBlack = false,
                useWallpaperColors = false,
            )
        }

        val palette = WidgetTheme.resolve(context)
        val expected = preset("rosepine").light
        assertEquals(expected.primary, palette.primary)
        assertEquals(expected.background, palette.background)
    }

    @Test
    fun system_mode_follows_night_mode() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeSettings.update {
            it.copy(
                themeId = "nord",
                themeMode = ThemeMode.SYSTEM,
                amoledBlack = false,
                useWallpaperColors = false,
            )
        }

        // Under Robolectric default night mode is not dark (or standard light mode)
        val lightConfig = context.resources.configuration
        lightConfig.uiMode = (lightConfig.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv()) or
            android.content.res.Configuration.UI_MODE_NIGHT_NO
        val lightContext = context.createConfigurationContext(lightConfig)

        val paletteLight = WidgetTheme.resolve(lightContext)
        val expectedLight = preset("nord").light
        assertEquals(expectedLight.primary, paletteLight.primary)

        val darkConfig = context.resources.configuration
        darkConfig.uiMode = (darkConfig.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv()) or
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val darkContext = context.createConfigurationContext(darkConfig)

        val paletteDark = WidgetTheme.resolve(darkContext)
        val expectedDark = preset("nord").dark
        assertEquals(expectedDark.primary, paletteDark.primary)
    }

    @Test
    fun amoled_makes_the_background_black_in_dark_only() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeSettings.update {
            it.copy(
                themeId = "dusk",
                themeMode = ThemeMode.DARK,
                amoledBlack = true,
                useWallpaperColors = false,
            )
        }

        val darkPalette = WidgetTheme.resolve(context)
        assertEquals(Color.Black, darkPalette.background)
        assertEquals(Color.Black, darkPalette.surface)

        fakeSettings.update {
            it.copy(
                themeId = "dusk",
                themeMode = ThemeMode.LIGHT,
                amoledBlack = true,
                useWallpaperColors = false,
            )
        }

        val lightPalette = WidgetTheme.resolve(context)
        val expectedLight = preset("dusk").light
        assertNotEquals(Color.Black, lightPalette.background)
        assertEquals(expectedLight.background, lightPalette.background)
    }

    @Test
    @Config(sdk = [30])
    fun wallpaper_colours_ignored_below_sdk_31() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeSettings.update {
            it.copy(
                themeId = "catppuccin",
                themeMode = ThemeMode.DARK,
                amoledBlack = false,
                useWallpaperColors = true,
            )
        }

        val palette = WidgetTheme.resolve(context)
        val expectedCatppuccin = preset("catppuccin").dark
        assertEquals(expectedCatppuccin.primary, palette.primary)
    }
}
