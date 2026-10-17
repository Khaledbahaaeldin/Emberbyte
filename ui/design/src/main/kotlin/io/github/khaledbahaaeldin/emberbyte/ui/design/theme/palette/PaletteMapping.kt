package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec

val LocalEmberbytePalette: ProvidableCompositionLocal<EmberbytePalette> = staticCompositionLocalOf {
    GeneratedPalettes.getValue("dusk").first
}

fun EmberbytePalette.toColorScheme(dark: Boolean): ColorScheme {
    return if (dark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary,
            onPrimaryContainer = onPrimary,
            inversePrimary = primary,
            secondary = primary,
            onSecondary = onPrimary,
            secondaryContainer = surface2,
            onSecondaryContainer = text,
            tertiary = accent,
            onTertiary = onAccent,
            tertiaryContainer = accent,
            onTertiaryContainer = onAccent,
            background = background,
            onBackground = text,
            surface = background,
            onSurface = text,
            surfaceVariant = surface2,
            onSurfaceVariant = muted,
            surfaceTint = primary,
            inverseSurface = text,
            inverseOnSurface = background,
            error = danger,
            onError = onDanger,
            errorContainer = danger,
            onErrorContainer = onDanger,
            outline = muted,
            outlineVariant = track,
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surface,
            surfaceContainerHigh = surface2,
            surfaceContainerHighest = surface2,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary,
            onPrimaryContainer = onPrimary,
            inversePrimary = primary,
            secondary = primary,
            onSecondary = onPrimary,
            secondaryContainer = surface2,
            onSecondaryContainer = text,
            tertiary = accent,
            onTertiary = onAccent,
            tertiaryContainer = accent,
            onTertiaryContainer = onAccent,
            background = background,
            onBackground = text,
            surface = background,
            onSurface = text,
            surfaceVariant = surface2,
            onSurfaceVariant = muted,
            surfaceTint = primary,
            inverseSurface = text,
            inverseOnSurface = background,
            error = danger,
            onError = onDanger,
            errorContainer = danger,
            onErrorContainer = onDanger,
            outline = muted,
            outlineVariant = track,
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surface,
            surfaceContainerHigh = surface2,
            surfaceContainerHighest = surface2,
        )
    }
}

fun EmberbytePalette.Companion.fromDynamic(scheme: ColorScheme): EmberbytePalette {
    val isDark = scheme.background.luminance() < 0.5f
    val dusk = if (isDark) DuskDark else DuskLight
    return EmberbytePalette(
        background = scheme.background,
        surface = scheme.surface,
        surface2 = scheme.surfaceVariant,
        text = scheme.onSurface,
        muted = scheme.onSurfaceVariant,
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        accent = scheme.tertiary,
        onAccent = scheme.onTertiary,
        warn = dusk.warn,
        onWarn = dusk.onWarn,
        danger = dusk.danger,
        onDanger = dusk.onDanger,
        track = scheme.outlineVariant,
    )
}

internal fun resolvePalette(
    spec: EmberbyteThemeSpec,
    sdkInt: Int,
    dynamic: () -> ColorScheme,
): EmberbytePalette {
    val base = if (spec.wallpaperColors && sdkInt >= 31) {
        EmberbytePalette.fromDynamic(dynamic())
    } else {
        val pair = GeneratedPalettes[spec.presetId] ?: GeneratedPalettes.getValue("dusk")
        if (spec.darkTheme) pair.first else pair.second
    }
    return if (spec.darkTheme && spec.amoled) {
        base.copy(background = Color.Black, surface = Color.Black)
    } else {
        base
    }
}
