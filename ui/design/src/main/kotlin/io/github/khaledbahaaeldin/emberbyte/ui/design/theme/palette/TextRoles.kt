package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Returns [foreground] unchanged when it already meets [minRatio] on [background]; otherwise the same hue and
 * saturation with the lightness moved away from the background (lighter on dark backgrounds, darker on light ones)
 * in 1% steps until the ratio is met. Falls back to white or black when no lightness of that hue is enough.
 */
internal fun ensureContrast(foreground: Color, background: Color, minRatio: Double = 4.5): Color {
    if (contrastRatio(foreground, background) >= minRatio) return foreground
    val hsl = foreground.toHsl()
    // White beats black as the target only below a background luminance of about 0.18; above it, darkening keeps the hue.
    val lighten = background.luminance() < 0.18f
    var lightness = hsl[2]
    repeat(100) {
        lightness = if (lighten) min(1f, lightness + 0.01f) else max(0f, lightness - 0.01f)
        val candidate = hslToColor(hsl[0], hsl[1], lightness)
        if (contrastRatio(candidate, background) >= minRatio) return candidate
        if (lightness == 0f || lightness == 1f) return@repeat
    }
    return listOf(Color.White, Color.Black).firstOrNull { contrastRatio(it, background) >= minRatio } ?: foreground
}

/** Applies [ensureContrast] against every surface a piece of text can sit on: surface2, surface and background. */
private fun EmberbytePalette.readableOnAllSurfaces(role: Color): Color =
    listOf(surface2, surface, background).fold(role) { colour, surface -> ensureContrast(colour, surface) }

/**
 * [accent] is a container colour (pale in light themes). As text it would be unreadable, so text uses this role:
 * the accent hue, nudged to at least 4.5:1 on every surface.
 */
val EmberbytePalette.accentText: Color get() = readableOnAllSurfaces(accent)

/** [warn] used as text (for example "already over your cap" or a legend), at least 4.5:1 on every surface. */
val EmberbytePalette.warnText: Color get() = readableOnAllSurfaces(warn)

/** [primary] used as link or action text (for example "See all"), at least 4.5:1 on every surface. */
val EmberbytePalette.linkText: Color get() = readableOnAllSurfaces(primary)

private fun Color.toHsl(): FloatArray {
    val r = red
    val g = green
    val b = blue
    val maxC = max(r, max(g, b))
    val minC = min(r, min(g, b))
    val l = (maxC + minC) / 2f
    val d = maxC - minC
    if (d == 0f) return floatArrayOf(0f, 0f, l)
    val s = if (l > 0.5f) d / (2f - maxC - minC) else d / (maxC + minC)
    val h = when (maxC) {
        r -> ((g - b) / d + (if (g < b) 6f else 0f)) * 60f
        g -> ((b - r) / d + 2f) * 60f
        else -> ((r - g) / d + 4f) * 60f
    }
    return floatArrayOf(h, s, l)
}

private fun hslToColor(h: Float, s: Float, l: Float): Color {
    if (s == 0f) return Color(l, l, l)
    val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
    val p = 2f * l - q
    fun channel(t0: Float): Float {
        var t = t0
        if (t < 0f) t += 1f
        if (t > 1f) t -= 1f
        return when {
            t < 1f / 6f -> p + (q - p) * 6f * t
            t < 1f / 2f -> q
            t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
            else -> p
        }
    }
    val hue = h / 360f
    return Color(channel(hue + 1f / 3f), channel(hue), channel(hue - 1f / 3f))
}
