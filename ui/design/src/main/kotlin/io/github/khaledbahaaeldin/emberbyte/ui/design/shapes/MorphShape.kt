package io.github.khaledbahaaeldin.emberbyte.ui.design.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

/**
 * A Compose [Shape] that draws a graphics-shapes [Morph] at [progress] (0f to 1f),
 * scaled to the layout size and centered.
 */
data class MorphShape(
    val morph: Morph,
    val progress: Float,
) : Shape {
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = morph.toPath(progress.coerceIn(0f, 1f)).asComposePath()
        matrix.reset()
        matrix.scale(size.width, size.height)
        path.transform(matrix)
        val bounds = path.getBounds()
        val offset = size.center - bounds.center
        path.translate(offset)
        return Outline.Generic(path)
    }
}

/**
 * Factory function creating a Compose [Shape] representing the given [morph] at [progress].
 */
fun morphShape(morph: Morph, progress: Float): Shape = MorphShape(morph, progress)

