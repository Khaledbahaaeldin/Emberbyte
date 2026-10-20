package io.github.khaledbahaaeldin.emberbyte.ui.design.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EmberbyteShapesTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun shape_for_is_stable_and_uses_all_four_shapes_over_200_names() {
        // Stability: same package name always resolves to same shape
        val pkg = "io.github.khaledbahaaeldin.emberbyte"
        val initial = shapeFor(pkg)
        repeat(10) {
            assertEquals(initial, shapeFor(pkg))
        }

        // Distribution: 200 distinct package names must use all four shapes
        val sampleNames = (0 until 200).map { "com.example.sample.app$it" }
        val resolved = sampleNames.map { shapeFor(it) }
        val uniqueShapes = resolved.toSet()

        assertEquals(
            "Expected all 4 AppShape values to be represented across 200 package names",
            AppShape.entries.toSet(),
            uniqueShapes,
        )

        for (shape in AppShape.entries) {
            val count = resolved.count { it == shape }
            assertTrue(
                "Expected shape $shape to be chosen at least once across 200 package names, but count was $count",
                count > 0,
            )
        }
    }

    @Test
    fun mixed_corners_gives_three_large_and_one_small() {
        val density = Density(1f)
        val size = Size(100f, 100f)
        val large = 38.dp
        val small = 12.dp

        // Corner.TopStart
        val topStartShape = mixedCorners(large = large, small = small, smallCorner = Corner.TopStart)
        val topStartOutline = topStartShape.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(topStartOutline is Outline.Rounded)
        val topStartRect = (topStartOutline as Outline.Rounded).roundRect
        assertEquals(12f, topStartRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(12f, topStartRect.topLeftCornerRadius.y, 0.01f)
        assertEquals(38f, topStartRect.topRightCornerRadius.x, 0.01f)
        assertEquals(38f, topStartRect.topRightCornerRadius.y, 0.01f)
        assertEquals(38f, topStartRect.bottomRightCornerRadius.x, 0.01f)
        assertEquals(38f, topStartRect.bottomRightCornerRadius.y, 0.01f)
        assertEquals(38f, topStartRect.bottomLeftCornerRadius.x, 0.01f)
        assertEquals(38f, topStartRect.bottomLeftCornerRadius.y, 0.01f)

        // Corner.TopEnd
        val topEndShape = mixedCorners(large = large, small = small, smallCorner = Corner.TopEnd)
        val topEndOutline = topEndShape.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(topEndOutline is Outline.Rounded)
        val topEndRect = (topEndOutline as Outline.Rounded).roundRect
        assertEquals(38f, topEndRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(38f, topEndRect.topLeftCornerRadius.y, 0.01f)
        assertEquals(12f, topEndRect.topRightCornerRadius.x, 0.01f)
        assertEquals(12f, topEndRect.topRightCornerRadius.y, 0.01f)
        assertEquals(38f, topEndRect.bottomRightCornerRadius.x, 0.01f)
        assertEquals(38f, topEndRect.bottomRightCornerRadius.y, 0.01f)
        assertEquals(38f, topEndRect.bottomLeftCornerRadius.x, 0.01f)
        assertEquals(38f, topEndRect.bottomLeftCornerRadius.y, 0.01f)

        // Corner.BottomEnd
        val bottomEndShape = mixedCorners(large = large, small = small, smallCorner = Corner.BottomEnd)
        val bottomEndOutline = bottomEndShape.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(bottomEndOutline is Outline.Rounded)
        val bottomEndRect = (bottomEndOutline as Outline.Rounded).roundRect
        assertEquals(38f, bottomEndRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(38f, bottomEndRect.topLeftCornerRadius.y, 0.01f)
        assertEquals(38f, bottomEndRect.topRightCornerRadius.x, 0.01f)
        assertEquals(38f, bottomEndRect.topRightCornerRadius.y, 0.01f)
        assertEquals(12f, bottomEndRect.bottomRightCornerRadius.x, 0.01f)
        assertEquals(12f, bottomEndRect.bottomRightCornerRadius.y, 0.01f)
        assertEquals(38f, bottomEndRect.bottomLeftCornerRadius.x, 0.01f)
        assertEquals(38f, bottomEndRect.bottomLeftCornerRadius.y, 0.01f)

        // Corner.BottomStart
        val bottomStartShape = mixedCorners(large = large, small = small, smallCorner = Corner.BottomStart)
        val bottomStartOutline = bottomStartShape.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(bottomStartOutline is Outline.Rounded)
        val bottomStartRect = (bottomStartOutline as Outline.Rounded).roundRect
        assertEquals(38f, bottomStartRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(38f, bottomStartRect.topLeftCornerRadius.y, 0.01f)
        assertEquals(38f, bottomStartRect.topRightCornerRadius.x, 0.01f)
        assertEquals(38f, bottomStartRect.topRightCornerRadius.y, 0.01f)
        assertEquals(38f, bottomStartRect.bottomRightCornerRadius.x, 0.01f)
        assertEquals(38f, bottomStartRect.bottomRightCornerRadius.y, 0.01f)
        assertEquals(12f, bottomStartRect.bottomLeftCornerRadius.x, 0.01f)
        assertEquals(12f, bottomStartRect.bottomLeftCornerRadius.y, 0.01f)
    }

    @Test
    fun mixed_corners_flip_in_rtl() {
        val density = Density(1f)
        val size = Size(100f, 100f)
        val shape = mixedCorners(large = 38.dp, small = 12.dp, smallCorner = Corner.TopStart)

        // LTR: TopStart is at top-left
        val ltrOutline = shape.createOutline(size, LayoutDirection.Ltr, density) as Outline.Rounded
        assertEquals(12f, ltrOutline.roundRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(38f, ltrOutline.roundRect.topRightCornerRadius.x, 0.01f)

        // RTL: TopStart flips to top-right
        val rtlOutline = shape.createOutline(size, LayoutDirection.Rtl, density) as Outline.Rounded
        assertEquals(38f, rtlOutline.roundRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(12f, rtlOutline.roundRect.topRightCornerRadius.x, 0.01f)

        // Also test BottomStart in RTL
        val bottomStartShape = mixedCorners(large = 38.dp, small = 12.dp, smallCorner = Corner.BottomStart)
        val bottomLtr = bottomStartShape.createOutline(size, LayoutDirection.Ltr, density) as Outline.Rounded
        assertEquals(12f, bottomLtr.roundRect.bottomLeftCornerRadius.x, 0.01f)
        assertEquals(38f, bottomLtr.roundRect.bottomRightCornerRadius.x, 0.01f)

        val bottomRtl = bottomStartShape.createOutline(size, LayoutDirection.Rtl, density) as Outline.Rounded
        assertEquals(38f, bottomRtl.roundRect.bottomLeftCornerRadius.x, 0.01f)
        assertEquals(12f, bottomRtl.roundRect.bottomRightCornerRadius.x, 0.01f)
    }

    @Test
    fun every_app_shape_resolves_to_a_nonempty_outline() {
        val density = Density(1f)
        val size = Size(100f, 100f)
        val resolvedShapes = mutableMapOf<AppShape, Shape>()

        rule.setContent {
            for (shape in AppShape.entries) {
                resolvedShapes[shape] = shape.toShape()
            }
        }

        assertEquals(AppShape.entries.size, resolvedShapes.size)

        for (shape in AppShape.entries) {
            val composeShape = checkNotNull(resolvedShapes[shape])
            val outline = composeShape.createOutline(size, LayoutDirection.Ltr, density)
            val bounds = outline.bounds

            assertFalse("Shape $shape resolved to empty bounds", bounds.isEmpty)
            assertTrue("Shape $shape width must be positive: ${bounds.width}", bounds.width > 0f)
            assertTrue("Shape $shape height must be positive: ${bounds.height}", bounds.height > 0f)
            assertTrue("Shape $shape right bounds should not exceed size width: ${bounds.right}", bounds.right <= 100.01f)
            assertTrue("Shape $shape bottom bounds should not exceed size height: ${bounds.bottom}", bounds.bottom <= 100.01f)
        }
    }
}
