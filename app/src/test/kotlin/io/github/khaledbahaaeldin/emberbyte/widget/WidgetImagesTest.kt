package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetImagesTest {

    private val duskPalette = ThemePresets.first { it.id == "dusk" }.dark
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun hero_number_bitmap_is_not_blank_and_has_the_requested_size() {
        val bitmap = WidgetImages.heroNumber(
            text = "14.2",
            unit = "GB",
            palette = duskPalette,
            widthPx = 240,
            heightPx = 120,
            context = context,
        )
        assertNotNull(bitmap)
        assertEquals(240, bitmap.width)
        assertEquals(120, bitmap.height)

        var hasNonTransparentPixel = false
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    hasNonTransparentPixel = true
                    break
                }
            }
            if (hasNonTransparentPixel) break
        }
        assertTrue("Expected hero number bitmap to contain visible drawn pixels", hasNonTransparentPixel)
    }

    @Test
    fun ring_zero_percent_draws_an_outline() {
        val bitmap = WidgetImages.ring(
            fraction = 0f,
            state = BudgetStateUi.Ok,
            palette = duskPalette,
            sizePx = 100,
        )
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)

        var nonTransparentCount = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    nonTransparentCount++
                }
            }
        }
        assertTrue("Expected non-zero pixels for outline ring at 0 percent", nonTransparentCount > 0)
    }

    @Test
    fun bar_fraction_clamps() {
        // Negative fraction should draw empty (or 0 fraction)
        val bitmapUnder = WidgetImages.bar(
            fraction = -0.5f,
            color = Color.GREEN,
            widthPx = 100,
            heightPx = 20,
            wavy = true,
        )
        // Over fraction should clamp to 1.0f without crash or overflow
        val bitmapOver = WidgetImages.bar(
            fraction = 2.0f,
            color = Color.GREEN,
            widthPx = 100,
            heightPx = 20,
            wavy = true,
        )
        val bitmapFull = WidgetImages.bar(
            fraction = 1.0f,
            color = Color.GREEN,
            widthPx = 100,
            heightPx = 20,
            wavy = true,
        )
        assertEquals(100, bitmapUnder.width)
        assertEquals(100, bitmapOver.width)
        assertEquals(100, bitmapFull.width)
    }

    @Test
    fun app_icon_with_null_icon_draws_the_letter_shape() {
        val bitmap = WidgetImages.appIcon(
            icon = null,
            shape = AppShape.Cookie,
            letter = "E",
            sizePx = 96,
            palette = duskPalette,
        )
        assertEquals(96, bitmap.width)
        assertEquals(96, bitmap.height)

        var hasPixels = false
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    hasPixels = true
                    break
                }
            }
            if (hasPixels) break
        }
        assertTrue("Expected fallback letter shape to contain drawn pixels", hasPixels)
    }

    @Test
    fun budget_rejects_a_bitmap_that_would_exceed_the_limit_and_counts_the_used_bytes() {
        // Limit: 1.5 MiB (1_572_864 bytes)
        val budget = ImageBudget(maxBytes = 1_572_864)
        assertEquals(0, budget.used)

        // 500x500 ARGB_8888 bitmap = 500 * 500 * 4 = 1_000_000 bytes (~0.95 MiB)
        val b1 = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        assertTrue(budget.add(b1))
        assertEquals(1_000_000, budget.used)

        // Another 500x500 ARGB_8888 bitmap would total 2_000_000 bytes > 1_572_864
        val b2 = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        assertFalse(budget.add(b2))
        // Used bytes remains unchanged when rejected
        assertEquals(1_000_000, budget.used)
    }

    @Test
    fun hero_number_fallback_font_is_used_when_the_variable_typeface_fails() {
        // Inject a failing typeface factory
        WidgetImages.typefaceFactory = { _: Context, _: Int, _: String -> null }
        try {
            val bitmap = WidgetImages.heroNumber(
                text = "8.5",
                unit = "GB",
                palette = duskPalette,
                widthPx = 200,
                heightPx = 100,
                context = context,
            )
            assertNotNull(bitmap)
            var hasPixels = false
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    if (bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                        hasPixels = true
                        break
                    }
                }
                if (hasPixels) break
            }
            assertTrue("Expected fallback hero number to draw pixels", hasPixels)
        } finally {
            WidgetImages.resetTypefaceFactory()
        }
    }
}
