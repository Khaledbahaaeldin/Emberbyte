package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.test.core.app.ApplicationProvider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val EmberLightColors = lightColorScheme(
    primary = Color(0xFF8B5037),
    onPrimary = Color(0xFFFFF7F5),
    primaryContainer = Color(0xFFFFB193),
    onPrimaryContainer = Color(0xFF63301A),
    inversePrimary = Color(0xFFFFB193),
    secondary = Color(0xFF77584C),
    onSecondary = Color(0xFFFFF7F5),
    secondaryContainer = Color(0xFFFFDBCE),
    onSecondaryContainer = Color(0xFF694B3F),
    tertiary = Color(0xFF725C25),
    onTertiary = Color(0xFFFFF8F0),
    tertiaryContainer = Color(0xFFFBDC98),
    onTertiaryContainer = Color(0xFF624D17),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF3E2F2A),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF3E2F2A),
    surfaceVariant = Color(0xFFF6DDD5),
    onSurfaceVariant = Color(0xFF6D5B55),
    inverseSurface = Color(0xFF130D0A),
    inverseOnSurface = Color(0xFFA79A96),
    error = Color(0xFFA83836),
    onError = Color(0xFFFFF7F6),
    errorContainer = Color(0xFFFA746F),
    onErrorContainer = Color(0xFF6E0A12),
    outline = Color(0xFF8A7770),
    outlineVariant = Color(0xFFC3ADA6),
    surfaceBright = Color(0xFFFFF8F6),
    surfaceDim = Color(0xFFEDD5CD),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1EC),
    surfaceContainer = Color(0xFFFEEAE3),
    surfaceContainerHigh = Color(0xFFF9E4DC),
    surfaceContainerHighest = Color(0xFFF6DDD5),
)

private val EmberDarkColors = darkColorScheme(
    primary = Color(0xFFF8B89F),
    onPrimary = Color(0xFF603523),
    primaryContainer = Color(0xFF754633),
    onPrimaryContainer = Color(0xFFFFDCCF),
    inversePrimary = Color(0xFF84533F),
    secondary = Color(0xFFE7BEAF),
    onSecondary = Color(0xFF553A2F),
    secondaryContainer = Color(0xFF50352A),
    onSecondaryContainer = Color(0xFFDFB7A8),
    tertiary = Color(0xFFFFECC6),
    onTertiary = Color(0xFF6B561F),
    tertiaryContainer = Color(0xFFFBDC98),
    onTertiaryContainer = Color(0xFF624D17),
    background = Color(0xFF130D0A),
    onBackground = Color(0xFFF9E0D8),
    surface = Color(0xFF130D0A),
    onSurface = Color(0xFFF9E0D8),
    surfaceVariant = Color(0xFF30231E),
    onSurfaceVariant = Color(0xFFBCA79F),
    inverseSurface = Color(0xFFFFF8F6),
    inverseOnSurface = Color(0xFF5D534F),
    error = Color(0xFFFA746F),
    onError = Color(0xFF490006),
    errorContainer = Color(0xFF871F21),
    onErrorContainer = Color(0xFFFF9993),
    outline = Color(0xFF84716B),
    outlineVariant = Color(0xFF54443E),
    surfaceBright = Color(0xFF372924),
    surfaceDim = Color(0xFF130D0A),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF1A110E),
    surfaceContainer = Color(0xFF211713),
    surfaceContainerHigh = Color(0xFF281D19),
    surfaceContainerHighest = Color(0xFF30231E),
)

private fun resolveColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    amoledBlack: Boolean,
    sdkInt: Int,
    dynamicLight: () -> ColorScheme,
    dynamicDark: () -> ColorScheme,
): ColorScheme {
    val base = when {
        dynamicColor && sdkInt >= 31 -> if (darkTheme) dynamicDark() else dynamicLight()
        darkTheme -> EmberDarkColors
        else -> EmberLightColors
    }
    return if (darkTheme && amoledBlack) {
        base.copy(background = Color.Black, surface = Color.Black, surfaceContainerLowest = Color.Black)
    } else {
        base
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TileContrastTest {
    private fun contrast(fg: Color, bg: Color): Double {
        val l1 = fg.compositeOver(bg).luminance() + 0.05
        val l2 = bg.luminance() + 0.05
        return maxOf(l1, l2) / minOf(l1, l2)
    }

    private fun assertContrast(name: String, fg: Color, bg: Color, minRatio: Double = 4.5) {
        val ratio = contrast(fg, bg)
        assertTrue("$name contrast ($ratio) must be >= $minRatio", ratio >= minRatio)
    }

    @Test
    fun tile_title_alpha_constant() {
        assertTrue(TILE_TITLE_ALPHA >= 0.87f)
    }

    @Test
    fun dismiss_button_on_tertiary_container_meets_contrast() {
        val lightContrast = contrast(EmberLightColors.onTertiaryContainer, EmberLightColors.tertiaryContainer)
        val darkContrast = contrast(EmberDarkColors.onTertiaryContainer, EmberDarkColors.tertiaryContainer)
        assertTrue("Light contrast ($lightContrast) >= 4.5", lightContrast >= 4.5)
        assertTrue("Dark contrast ($darkContrast) >= 4.5", darkContrast >= 4.5)

        // The old colour (primary on tertiaryContainer) failed contrast in dark theme (< 4.5:1)
        val oldDarkContrast = contrast(EmberDarkColors.primary, EmberDarkColors.tertiaryContainer)
        assertTrue("Old dark primary contrast ($oldDarkContrast) was < 4.5", oldDarkContrast < 4.5)
    }

    @Test
    fun old_progress_ring_track_at_0_percent_failed_contrast_at_1_05() {
        val lightTrack = EmberLightColors.surfaceContainerHighest
        val lightCard = EmberLightColors.surfaceContainerHigh
        val ratio = contrast(lightTrack, lightCard)
        assertTrue("Old track against card ($ratio) was < 3.0", ratio < 3.0)
    }

    @Test
    fun old_selected_chip_container_failed_contrast_at_1_48() {
        val darkSelected = EmberDarkColors.secondaryContainer
        val darkUnselected = EmberDarkColors.surfaceContainer
        val ratio = contrast(darkSelected, darkUnselected)
        assertTrue("Old selected chip against unselected ($ratio) was < 3.0", ratio < 3.0)
    }

    private fun assertSchemeMeetsWcag(schemeName: String, s: ColorScheme) {
        // Core text
        assertContrast("$schemeName onPrimary / primary", s.onPrimary, s.primary)
        assertContrast("$schemeName onPrimaryContainer / primaryContainer", s.onPrimaryContainer, s.primaryContainer)
        assertContrast("$schemeName onSecondary / secondary", s.onSecondary, s.secondary)
        assertContrast("$schemeName onSecondaryContainer / secondaryContainer", s.onSecondaryContainer, s.secondaryContainer)
        assertContrast("$schemeName onTertiary / tertiary", s.onTertiary, s.tertiary)
        assertContrast("$schemeName onTertiaryContainer / tertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer)
        assertContrast("$schemeName onBackground / background", s.onBackground, s.background)
        assertContrast("$schemeName onSurface / surface", s.onSurface, s.surface)
        assertContrast("$schemeName onSurfaceVariant / surface", s.onSurfaceVariant, s.surface)
        assertContrast("$schemeName onSurface / surfaceContainer", s.onSurface, s.surfaceContainer)
        assertContrast("$schemeName onSurfaceVariant / surfaceContainer", s.onSurfaceVariant, s.surfaceContainer)
        assertContrast("$schemeName onSurface / surfaceContainerHigh", s.onSurface, s.surfaceContainerHigh)
        assertContrast("$schemeName onSurfaceVariant / surfaceContainerHigh", s.onSurfaceVariant, s.surfaceContainerHigh)
        assertContrast("$schemeName onSurface / surfaceContainerHighest", s.onSurface, s.surfaceContainerHighest)
        assertContrast("$schemeName onSurfaceVariant / surfaceContainerHighest", s.onSurfaceVariant, s.surfaceContainerHighest)
        assertContrast("$schemeName onError / error", s.onError, s.error)
        assertContrast("$schemeName onErrorContainer / errorContainer", s.onErrorContainer, s.errorContainer)

        // Tertiary text on surfaceContainerHigh (PlanCard forecast line)
        assertContrast("$schemeName tertiary / surfaceContainerHigh (PlanCard forecast)", s.tertiary, s.surfaceContainerHigh, minRatio = 4.5)

        // Ring arcs on card (primary/tertiary/error)
        assertContrast("$schemeName primary / surfaceContainerHigh (ring arc)", s.primary, s.surfaceContainerHigh, minRatio = 3.0)
        assertContrast("$schemeName tertiary / surfaceContainerHigh (ring arc warning)", s.tertiary, s.surfaceContainerHigh, minRatio = 3.0)
        assertContrast("$schemeName error / surfaceContainerHigh (ring arc error)", s.error, s.surfaceContainerHigh, minRatio = 3.0)

        // Ring arcs on track (primary/tertiary/error on surfaceContainerHighest)
        assertContrast("$schemeName primary / surfaceContainerHighest (ring arc on track)", s.primary, s.surfaceContainerHighest, minRatio = 3.0)
        assertContrast("$schemeName tertiary / surfaceContainerHighest (ring arc warning on track)", s.tertiary, s.surfaceContainerHighest, minRatio = 3.0)
        assertContrast("$schemeName error / surfaceContainerHighest (ring arc error on track)", s.error, s.surfaceContainerHighest, minRatio = 3.0)

        // Ring track at 0% (outline boundary on card >= 3:1)
        assertContrast("$schemeName outline / surfaceContainerHigh (ring track 0% boundary)", s.outline, s.surfaceContainerHigh, minRatio = 3.0)

        // Selected chip container (primary) vs unselected container (surfaceContainer / surface >= 3:1)
        assertContrast("$schemeName primary / surfaceContainer (selected chip container)", s.primary, s.surfaceContainer, minRatio = 3.0)
        assertContrast("$schemeName primary / surface (selected chip container)", s.primary, s.surface, minRatio = 3.0)

        // Unselected bar graphics on container/surface (outline boundary >= 3:1)
        assertContrast("$schemeName outline / surface (unselected bar graphic)", s.outline, s.surface, minRatio = 3.0)
        assertContrast("$schemeName outline / surfaceContainer (unselected bar graphic)", s.outline, s.surfaceContainer, minRatio = 3.0)
        assertContrast("$schemeName outline / surfaceContainerHigh (unselected bar graphic)", s.outline, s.surfaceContainerHigh, minRatio = 3.0)

        // Indicators & graphics on surface
        assertContrast("$schemeName primary / surface", s.primary, s.surface, minRatio = 3.0)
        assertContrast("$schemeName error / surface (exceeded state)", s.error, s.surface, minRatio = 3.0)
        assertContrast("$schemeName outline / surface (boundary/graphic)", s.outline, s.surface, minRatio = 3.0)
    }

    @Test
    fun static_light_palette_contrast_meets_wcag_aa_for_all_text_and_indicators() {
        assertSchemeMeetsWcag("Static Light", EmberLightColors)
    }

    @Test
    fun static_dark_palette_contrast_meets_wcag_aa_for_all_text_and_indicators() {
        assertSchemeMeetsWcag("Static Dark", EmberDarkColors)
    }

    @Test
    fun amoled_black_palette_contrast_meets_wcag_aa_for_all_text_and_indicators() {
        val amoled = resolveColorScheme(
            darkTheme = true,
            dynamicColor = false,
            amoledBlack = true,
            sdkInt = 34,
            dynamicLight = { EmberLightColors },
            dynamicDark = { EmberDarkColors },
        )
        assertSchemeMeetsWcag("AMOLED Black", amoled)
    }

    @Test
    fun dynamic_light_palette_contrast_meets_wcag_aa_for_all_text_and_indicators() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dynamicLight = resolveColorScheme(
            darkTheme = false,
            dynamicColor = true,
            amoledBlack = false,
            sdkInt = 34,
            dynamicLight = { dynamicLightColorScheme(context) },
            dynamicDark = { dynamicDarkColorScheme(context) },
        )
        assertSchemeMeetsWcag("Dynamic Light", dynamicLight)
    }

    @Test
    fun dynamic_dark_palette_contrast_meets_wcag_aa_for_all_text_and_indicators() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dynamicDark = resolveColorScheme(
            darkTheme = true,
            dynamicColor = true,
            amoledBlack = false,
            sdkInt = 34,
            dynamicLight = { dynamicLightColorScheme(context) },
            dynamicDark = { dynamicDarkColorScheme(context) },
        )
        assertSchemeMeetsWcag("Dynamic Dark", dynamicDark)
    }
}
