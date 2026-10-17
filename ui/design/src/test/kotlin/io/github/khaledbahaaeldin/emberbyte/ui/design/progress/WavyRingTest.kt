package io.github.khaledbahaaeldin.emberbyte.ui.design.progress

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ContrastPairs
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.GeneratedPalettes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WavyRingTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun state_colours_follow_the_palette() {
        val palette = GeneratedPalettes.getValue("dusk").first
        assertEquals(palette.primary to palette.track, ringColors(BudgetStateUi.Ok, palette))
        assertEquals(palette.warn to palette.track, ringColors(BudgetStateUi.Warning, palette))
        assertEquals(palette.danger to palette.track, ringColors(BudgetStateUi.Exceeded, palette))
    }

    @Test
    fun warning_and_exceeded_show_a_described_mark() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Column {
                    WavyRing(fraction = 0.85f, state = BudgetStateUi.Warning, size = 72.dp, label = "85%")
                    WavyRing(fraction = 1.15f, state = BudgetStateUi.Exceeded, size = 72.dp, label = "115%")
                }
            }
        }
        rule.onNodeWithContentDescription("Nearly used up").assertIsDisplayed()
        rule.onNodeWithContentDescription("Over the cap").assertIsDisplayed()
    }

    @Test
    fun label_inside_the_ring_at_font_scale_2() {
        val baseSp = 17f
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    WavyRing(fraction = 1.37f, state = BudgetStateUi.Exceeded, size = 72.dp, label = "137%")
                }
            }
        }
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        val node = rule.onNodeWithText("137%", useUnmergedTree = true).fetchSemanticsNode()
        node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        val drawnSp = results.first().layoutInput.style.fontSize.value
        val drawn = drawnSp * 2f
        val uncapped = baseSp * 2f
        assertTrue("drawn $drawn must be <= 1.3 x base (${uncapped / 2f * 1.3f + 0.01f})", drawn <= uncapped / 2f * 1.3f + 0.01f)
        assertTrue("drawn $drawn must be smaller than the uncapped $uncapped", drawn < uncapped - 1f)
    }

    @Test
    fun zero_percent_is_visible() {
        val palette = GeneratedPalettes.getValue("dusk").first
        val outline = ringOutlineColor(palette)
        val pair = ContrastPairs.all.first { it.name == "muted on surface" }
        assertTrue("Outline pair minRatio must be at least 3.0", pair.minRatio >= 3.0)
        assertTrue("Outline contrast on surface must be >= 3.0", contrastRatio(outline, palette.surface) >= 3.0)
    }

    @Test
    fun fraction_above_one_is_a_full_ring() {
        assertEquals(1f, effectiveSweep(1.25f), 0.0001f)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WavyRing(fraction = 1.25f, state = BudgetStateUi.Exceeded, size = 72.dp, label = "125%")
            }
        }
        rule.onNodeWithText("125%").assertIsDisplayed()
        rule.onNodeWithContentDescription("Over the cap").assertIsDisplayed()
    }

    @Test
    fun reduce_motion_snaps() {
        var capturedSpec: AnimationSpec<Float>? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    WavyRing(
                        fraction = 0.5f,
                        state = BudgetStateUi.Ok,
                        size = 72.dp,
                        label = "50%",
                        onAnimationSpecResolved = { capturedSpec = it },
                    )
                }
            }
        }
        assertTrue("Spec must be snap() when reduce motion is enabled", capturedSpec is SnapSpec)
    }
}
