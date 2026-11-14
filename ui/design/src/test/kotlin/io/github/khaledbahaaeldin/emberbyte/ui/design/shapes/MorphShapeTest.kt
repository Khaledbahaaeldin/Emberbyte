package io.github.khaledbahaaeldin.emberbyte.ui.design.shapes

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphShapeTest {
    private val density = Density(1f)
    private val size = Size(100f, 100f)

    @Test
    fun morph_shape_creates_valid_outline_at_start_and_end() {
        val morph = Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided)
        val startShape = morphShape(morph, 0f)
        val endShape = morphShape(morph, 1f)

        val startOutline = startShape.createOutline(size, LayoutDirection.Ltr, density)
        val endOutline = endShape.createOutline(size, LayoutDirection.Ltr, density)

        assertTrue(startOutline is Outline.Generic)
        assertTrue(endOutline is Outline.Generic)

        val startBounds = startOutline.bounds
        val endBounds = endOutline.bounds

        assertTrue(startBounds.width > 0f)
        assertTrue(startBounds.height > 0f)
        assertTrue(endBounds.width > 0f)
        assertTrue(endBounds.height > 0f)
    }

    @Test
    fun morph_shape_equality_and_hashcode() {
        val morph = Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided)
        val shape1 = morphShape(morph, 0.5f)
        val shape2 = morphShape(morph, 0.5f)
        val shape3 = morphShape(morph, 1.0f)

        assertEquals(shape1, shape2)
        assertEquals(shape1.hashCode(), shape2.hashCode())
        assertNotEquals(shape1, shape3)
    }
}
