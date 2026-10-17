package io.github.khaledbahaaeldin.emberbyte

import android.os.Build
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec

internal fun themeSpecOf(
    settings: Settings,
    systemDark: Boolean,
    sdkInt: Int = Build.VERSION.SDK_INT,
): EmberbyteThemeSpec {
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    return EmberbyteThemeSpec(
        presetId = settings.themeId,
        darkTheme = dark,
        amoled = dark && settings.amoledBlack,
        wallpaperColors = settings.useWallpaperColors && sdkInt >= 31,
    )
}
