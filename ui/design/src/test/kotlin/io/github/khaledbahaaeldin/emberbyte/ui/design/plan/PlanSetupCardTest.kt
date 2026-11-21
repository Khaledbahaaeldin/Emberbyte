package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroEntranceGate
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.GhostRingFractionKey
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.GhostRingPhaseKey
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoEntranceProgressKey
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.GeneratedPalettes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.liveTile
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlanSetupCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Before
    fun fresh() = HeroEntranceGate.reset()

    @After
    fun reset() = HeroEntranceGate.reset()

    private fun config(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config

    private fun show(
        reduced: Boolean = false,
        fontScale: Float = 1f,
        rtl: Boolean = false,
        playEntrance: Boolean = false,
        onSetUp: () -> Unit = {},
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalReduceMotion provides reduced,
                    LocalDensity provides Density(LocalDensity.current.density, fontScale),
                    LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                ) {
                    PlanSetupCard(onSetUp = onSetUp, modifier = Modifier.width(360.dp), playEntrance = playEntrance)
                }
            }
        }
        rule.mainClock.advanceTimeBy(50)
    }

    @Test
    fun card_shows_the_title_the_body_and_the_button() {
        show()
        rule.onAllNodesWithText("Set up your data plan", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("See what is left and when it runs out.", useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("Set up plan", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun button_is_at_least_52dp_and_calls_onSetUp() {
        var calls = 0
        show(onSetUp = { calls++ })
        val button = rule.onNodeWithTag("plan_setup_button", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("at least 52 dp (was ${button.bottom - button.top})", (button.bottom - button.top) >= 51.5.dp)
        rule.onNodeWithTag("plan_setup_button").performClick()
        assertEquals(1, calls)
    }

    @Test
    fun title_is_a_heading_and_the_ring_is_not_announced() {
        show()
        assertTrue(config("plan_setup_title").contains(SemanticsProperties.Heading))
        // The ghost ring is decorative: it publishes no progress or description to accessibility services.
        val ring = config("plan_setup_ring")
        assertFalse(ring.contains(SemanticsProperties.ProgressBarRangeInfo))
        assertFalse(ring.contains(SemanticsProperties.ContentDescription))
    }

    @Test
    fun the_button_is_announced_as_setting_up_the_data_plan() {
        show()
        rule.onNodeWithContentDescription("Set up your data plan", useUnmergedTree = true).assertExists()
    }

    @Test
    fun ring_sits_at_the_start_and_moves_to_the_right_in_rtl() {
        show()
        val ltr = rule.onNodeWithTag("plan_setup_ring", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val ltrCard = rule.onNodeWithTag("plan_setup_card", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("LTR: ring on the left", (ltr.left - ltrCard.left) < 40.dp)
        assertEquals("ring is 76 dp", 76f, (ltr.right - ltr.left).value, 0.5f)
    }

    @Test
    fun ring_moves_to_the_right_in_rtl() {
        show(rtl = true)
        val ring = rule.onNodeWithTag("plan_setup_ring", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val card = rule.onNodeWithTag("plan_setup_card", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("RTL: ring on the right", (card.right - ring.right) < 40.dp)
    }

    @Test
    fun card_text_meets_contrast_in_every_palette() {
        val failures = mutableListOf<String>()
        for ((id, pair) in GeneratedPalettes) {
            for ((mode, palette) in listOf("dark" to pair.first, "light" to pair.second)) {
                val tile = palette.liveTile()
                if (contrastRatio(tile.onContainer, tile.container) < 7.0) failures += "$id/$mode title"
                if (contrastRatio(tile.onContainerSecondary, tile.container) < 4.5) failures += "$id/$mode body"
                if (contrastRatio(palette.onPrimary, palette.primary) < 4.5) failures += "$id/$mode button"
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun card_does_not_clip_at_font_scale_2() {
        show(fontScale = 2f)
        val card = rule.onNodeWithTag("plan_setup_card", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val button = rule.onNodeWithTag("plan_setup_button", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val title = rule.onNodeWithTag("plan_setup_title", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("button inside the card", button.bottom <= card.bottom && button.right <= card.right)
        assertTrue("title inside the card", title.right <= card.right)
        rule.onNodeWithTag("plan_setup_button", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun ghost_ring_draws_over_time_and_ends_at_70_percent() {
        show(playEntrance = false)
        var previous = -1f
        var sawMiddle = false
        repeat(100) {
            rule.mainClock.advanceTimeBy(16)
            val f = config("plan_setup_ring")[GhostRingFractionKey]
            assertTrue("never goes backwards", f >= previous - 1e-6f)
            assertTrue("never above 70 percent", f <= 0.7f + 1e-6f)
            if (f in 0.1f..0.6f) sawMiddle = true
            previous = f
        }
        assertTrue("has in-between frames", sawMiddle)
        rule.mainClock.advanceTimeBy(1_500)
        assertEquals(0.7f, config("plan_setup_ring")[GhostRingFractionKey], 1e-3f)
    }

    @Test
    fun ghost_ring_is_static_when_reduced() {
        show(reduced = true)
        assertEquals(0.7f, config("plan_setup_ring")[GhostRingFractionKey], 1e-4f)
        assertEquals(0f, config("plan_setup_ring")[GhostRingPhaseKey], 0f)
        repeat(60) { rule.mainClock.advanceTimeBy(16) }
        assertEquals(0f, config("plan_setup_ring")[GhostRingPhaseKey], 0f)
    }

    @Test
    fun entrance_has_in_between_frames_and_starts_after_80ms() {
        show(playEntrance = true)
        rule.mainClock.autoAdvance = false
        val key = BentoEntranceProgressKey
        assertEquals(0f, config("plan_setup_card")[key], 0.001f)
        rule.mainClock.advanceTimeBy(120)
        assertTrue("started after 80 ms", config("plan_setup_card")[key] > 0.01f)
        var sawMiddle = false
        repeat(60) {
            rule.mainClock.advanceTimeBy(16)
            if (config("plan_setup_card")[key] in 0.1f..0.9f) sawMiddle = true
        }
        assertTrue(sawMiddle)
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(1f, config("plan_setup_card")[key], 0.01f)
    }

    @Test
    fun entrance_snaps_when_reduced() {
        show(reduced = true, playEntrance = true)
        assertFalse("no entrance is played under reduced motion", config("plan_setup_card").contains(BentoEntranceProgressKey))
    }
}
