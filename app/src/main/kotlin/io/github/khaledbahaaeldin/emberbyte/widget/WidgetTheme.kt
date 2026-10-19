package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.annotation.VisibleForTesting
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import io.github.khaledbahaaeldin.emberbyte.data.AppPreferences
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.themeSpecOf
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.fromDynamic
import kotlinx.coroutines.flow.first

/**
 * Resolves the active [EmberbytePalette] for Glance app widgets (Contract 13.7).
 *
 * Reads theme preferences from the shared DataStore (`themeId`, `themeMode`, `amoledBlack`, `useWallpaperColors`).
 * ThemeMode.SYSTEM follows the current night mode of [context].
 * On API 31+, wallpaper colors use system dynamic color schemes.
 */
object WidgetTheme {
    @VisibleForTesting
    internal var settingsProvider: ((Context) -> SettingsRepository)? = null

    suspend fun resolve(context: Context): EmberbytePalette {
        val settingsRepo = settingsProvider?.invoke(context)
            ?: (context.applicationContext as? EmberbyteApplication)?.graph?.settings
            ?: AppPreferences.create(context).settings
        val settings = settingsRepo.observe().first()

        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val systemDark = uiMode == Configuration.UI_MODE_NIGHT_YES
        val spec = themeSpecOf(settings, systemDark = systemDark, sdkInt = Build.VERSION.SDK_INT)

        val base = if (spec.wallpaperColors && Build.VERSION.SDK_INT >= 31) {
            val dynamicScheme: ColorScheme = if (spec.darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            EmberbytePalette.fromDynamic(dynamicScheme)
        } else {
            val preset = ThemePresets.firstOrNull { it.id == spec.presetId }
                ?: ThemePresets.first { it.id == "dusk" }
            if (spec.darkTheme) preset.dark else preset.light
        }

        return if (spec.darkTheme && spec.amoled) {
            base.copy(background = Color.Black, surface = Color.Black)
        } else {
            base
        }
    }
}
