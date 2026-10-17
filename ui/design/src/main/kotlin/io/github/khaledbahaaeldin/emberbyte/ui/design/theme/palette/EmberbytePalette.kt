package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class EmberbytePalette(
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val text: Color,
    val muted: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val onAccent: Color,
    val warn: Color,
    val onWarn: Color,
    val danger: Color,
    val onDanger: Color,
    val track: Color,
) {
    companion object
}
