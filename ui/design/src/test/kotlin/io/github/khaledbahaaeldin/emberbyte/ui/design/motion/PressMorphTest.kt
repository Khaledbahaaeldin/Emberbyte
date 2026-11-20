package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.navbar.PlusButton
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PressMorphTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun bento_tile_morphs_corners_when_pressed() {
        rule.setContent {
            EmberbyteTheme {
                BentoTile(title = "Bento", onClick = {}) {
                    Text("Body")
                }
            }
        }
        val tile = rule.onNodeWithTag("bento_tile_default")
        val restRadius = tile.currentCornerRadius()
        assertEquals(32f, restRadius, 0.5f)
        assertEquals(1f, tile.currentScale(), 0.01f)

        tile.performTouchInput { down(center) }
        rule.waitForIdle()

        val pressedRadius = tile.currentCornerRadius()
        assertEquals(12f, pressedRadius, 0.5f)
        assertEquals(0.97f, tile.currentScale(), 0.01f)

        tile.performTouchInput { up() }
        rule.waitForIdle()
        assertEquals(32f, tile.currentCornerRadius(), 0.5f)
        assertEquals(1f, tile.currentScale(), 0.01f)
    }

    @Test
    fun app_row_morphs_corners_when_pressed() {
        rule.setContent {
            EmberbyteTheme {
                AppRow(
                    model = AppRowUi(
                        packageName = "com.example.app",
                        label = "Example App",
                        mobileBytes = 500L,
                        wifiBytes = 500L,
                    ),
                    units = ByteUnits.DECIMAL,
                    onClick = {},
                )
            }
        }
        val row = rule.onNodeWithTag("app_row")
        val restRadius = row.currentCornerRadius()
        assertEquals(16f, restRadius, 0.5f)
        assertEquals(1f, row.currentScale(), 0.01f)

        row.performTouchInput { down(center) }
        rule.waitForIdle()

        val pressedRadius = row.currentCornerRadius()
        assertEquals(12f, pressedRadius, 0.5f)
        assertEquals(0.97f, row.currentScale(), 0.01f)

        row.performTouchInput { up() }
        rule.waitForIdle()
        assertEquals(16f, row.currentCornerRadius(), 0.5f)
        assertEquals(1f, row.currentScale(), 0.01f)
    }

    @Test
    fun plus_button_morphs_when_pressed() {
        rule.setContent {
            EmberbyteTheme {
                PlusButton(
                    onClick = {},
                    contentDescription = "Add",
                )
            }
        }
        val button = rule.onNodeWithTag("plus_button")
        val restRadius = button.currentCornerRadius(Size(56f, 56f))
        assertEquals(28f, restRadius, 0.5f)
        assertEquals(1f, button.currentScale(), 0.01f)

        button.performTouchInput { down(center) }
        rule.waitForIdle()

        val pressedRadius = button.currentCornerRadius(Size(56f, 56f))
        assertEquals(12f, pressedRadius, 0.5f)
        assertEquals(1f, button.currentScale(), 0.01f)

        button.performTouchInput { up() }
        rule.waitForIdle()
        assertEquals(28f, button.currentCornerRadius(Size(56f, 56f)), 0.5f)
    }

    @Test
    fun reduced_motion_press_snaps() {
        rule.setContent {
            CompositionLocalProvider(LocalReduceMotion provides true) {
                EmberbyteTheme {
                    Box(
                        modifier = Modifier
                            .testTag("target")
                            .size(100.dp)
                            .pressMorph(
                                shape = RoundedCornerShape(28.dp),
                                pressedScale = 0.97f,
                                pressedCorner = 12.dp,
                            )
                    )
                }
            }
        }
        val target = rule.onNodeWithTag("target")
        assertEquals(28f, target.currentCornerRadius(), 0.5f)

        target.performTouchInput { down(center) }
        rule.waitForIdle()
        assertEquals(12f, target.currentCornerRadius(), 0.5f)
        assertEquals(0.97f, target.currentScale(), 0.01f)
    }

    private fun SemanticsNodeInteraction.currentCornerRadius(size: Size = Size(100f, 100f)): Float {
        val shape = fetchLayerShape()
        val outline = shape.createOutline(size, LayoutDirection.Ltr, Density(1f))
        return (outline as Outline.Rounded).roundRect.topLeftCornerRadius.x
    }

    private fun SemanticsNodeInteraction.currentScale(): Float {
        val node = fetchSemanticsNode()
        val layoutNodeField = node.javaClass.declaredFields.firstOrNull { it.name.contains("layoutNode") }
            ?.apply { isAccessible = true } ?: error("layoutNode field not found")
        val layoutNode = layoutNodeField.get(node) ?: error("layoutNode not found")
        val getModifierInfo = layoutNode.javaClass.getMethod("getModifierInfo")
        val infos = getModifierInfo.invoke(layoutNode) as List<*>
        for (info in infos) {
            val mod = info!!.javaClass.getMethod("getModifier").invoke(info)
            if (mod != null && mod.javaClass.name.contains("GraphicsLayerElement")) {
                val scaleXField = mod.javaClass.declaredFields.firstOrNull { it.name == "scaleX" }
                    ?.apply { isAccessible = true }
                return (scaleXField?.get(mod) as? Float) ?: 1f
            }
        }
        return 1f
    }

    private fun SemanticsNodeInteraction.fetchLayerShape(): Shape {
        val node = fetchSemanticsNode()
        val layoutNodeField = node.javaClass.declaredFields.firstOrNull { it.name.contains("layoutNode") }
            ?.apply { isAccessible = true } ?: error("layoutNode field not found")
        val layoutNode = layoutNodeField.get(node) ?: error("layoutNode not found")
        val getModifierInfo = layoutNode.javaClass.getMethod("getModifierInfo")
        val infos = getModifierInfo.invoke(layoutNode) as List<*>
        for (info in infos) {
            val mod = info!!.javaClass.getMethod("getModifier").invoke(info)
            if (mod != null && mod.javaClass.name.contains("GraphicsLayerElement")) {
                val shapeField = mod.javaClass.declaredFields.firstOrNull { it.name == "shape" }
                    ?.apply { isAccessible = true }
                val shape = shapeField?.get(mod) as? Shape
                if (shape != null) return shape
            }
        }
        error("GraphicsLayerElement with shape not found on node")
    }
}
