package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Colours of one of the colour-blocked Home tiles. [onContainer] is the primary label (at least 7:1 on
 * [container]); [onContainerSecondary] is secondary text (at least 4.5:1).
 */
data class TileColours(
    val container: Color,
    val onContainer: Color,
    val onContainerSecondary: Color,
)

/**
 * Mixes [tint] into the card colour by [mix]. A mid-tone container cannot reach 7:1 with either black or white text, so
 * when the nominal mix does not allow it the tint is eased back, a tenth of [mix] at a time, until the label can reach
 * 7:1. Palettes where the nominal mix already works keep it exactly.
 */
private fun EmberbytePalette.tileMixed(tint: Color, mix: Float): TileColours {
    var container = surface2
    var label = text
    for (step in 0..10) {
        container = lerp(surface2, tint, mix * (1f - step / 10f))
        label = ensureContrast(text, container, 7.0)
        if (contrastRatio(label, container) >= 7.0) break
    }
    return TileColours(
        container = container,
        onContainer = label,
        onContainerSecondary = ensureContrast(muted, container, 4.5),
    )
}

/** The Live tile: the theme's primary mixed into the card colour (16 percent). */
fun EmberbytePalette.liveTile(): TileColours = tileMixed(primary, 0.16f)

/** The "Today by network" tile: the theme's green accent (28 percent). */
fun EmberbytePalette.networkTile(): TileColours = tileMixed(accent, 0.28f)

/** The Live tile while it shows upload: a warm tint (20 percent; a tint only, not a warning state). */
fun EmberbytePalette.uploadTile(): TileColours = tileMixed(warn, 0.20f)
