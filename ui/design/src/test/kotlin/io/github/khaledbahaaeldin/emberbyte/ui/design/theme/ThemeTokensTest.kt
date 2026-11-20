package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class ThemeTokensTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun theme_passes_typography_and_shapes() {
        var typography: Typography? = null
        var shapes: Shapes? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                typography = MaterialTheme.typography
                shapes = MaterialTheme.shapes
            }
        }
        assertEquals(EmberbyteTypography, typography)
        assertEquals(EmberbyteShapeScale, shapes)
    }

    @Test
    fun shape_scale_uses_only_the_agreed_radii() {
        assertEquals(8f, EmberbyteShapeRadii.ExtraSmall.value, 0f)
        assertEquals(12f, EmberbyteShapeRadii.Small.value, 0f)
        assertEquals(16f, EmberbyteShapeRadii.Medium.value, 0f)
        assertEquals(24f, EmberbyteShapeRadii.Large.value, 0f)
        assertEquals(32f, EmberbyteShapeRadii.ExtraLarge.value, 0f)
        assertEquals(28f, EmberbyteShapeRadii.LargeIncreased.value, 0f)
        assertEquals(36f, EmberbyteShapeRadii.ExtraLargeIncreased.value, 0f)
        assertNotEquals(Shapes(), EmberbyteShapeScale)
    }

    @Test
    fun emphasized_styles_are_defined_and_bolder_than_their_base() {
        val t = EmberbyteTypography
        val pairs = listOf(
            "displayLarge" to (t.displayLargeEmphasized to t.displayLarge),
            "headlineMedium" to (t.headlineMediumEmphasized to t.headlineMedium),
            "titleLarge" to (t.titleLargeEmphasized to t.titleLarge),
            "titleMedium" to (t.titleMediumEmphasized to t.titleMedium),
            "bodyLarge" to (t.bodyLargeEmphasized to t.bodyLarge),
            "labelLarge" to (t.labelLargeEmphasized to t.labelLarge),
        )
        for ((name, pair) in pairs) {
            val (emphasized, base) = pair
            assertNotEquals("$name emphasized must differ from its base", base, emphasized)
            assertEquals("$name keeps the base size", base.fontSize, emphasized.fontSize)
            val emphasizedWeight = emphasized.fontWeight?.weight ?: 400
            val baseWeight = base.fontWeight?.weight ?: 400
            assertTrue("$name emphasized weight $emphasizedWeight must be above base $baseWeight", emphasizedWeight > baseWeight)
        }
    }

    @Test
    fun non_emphasized_styles_match_the_material_defaults_so_nothing_shifts() {
        val stock = Typography()
        val t = EmberbyteTypography
        assertEquals(stock.bodyMedium, t.bodyMedium)
        assertEquals(stock.titleLarge, t.titleLarge)
        assertEquals(stock.labelLarge, t.labelLarge)
        assertEquals(stock.headlineSmall, t.headlineSmall)
    }
}
