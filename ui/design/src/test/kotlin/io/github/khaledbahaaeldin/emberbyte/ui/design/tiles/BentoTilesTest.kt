package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroEntranceGate
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
class BentoTilesTest {
    @get:Rule
    val rule = createComposeRule()

    @Before
    fun fresh() = HeroEntranceGate.reset()

    @After
    fun reset() = HeroEntranceGate.reset()

    private val rxHistory = List(60) { 100_000L + it * 3_000L }
    private val txHistory = List(60) { 8_000L + it * 100L }

    private fun config(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config

    private fun setLive(
        reduced: Boolean = false,
        fontScale: Float = 1f,
        direction: () -> LiveDirection = { LiveDirection.Download },
        onToggle: () -> Unit = {},
        playEntrance: Boolean = false,
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalReduceMotion provides reduced,
                    LocalDensity provides Density(LocalDensity.current.density, fontScale),
                ) {
                    LiveBentoTile(
                        rxBps = 1_200_000L,
                        txBps = 70_000L,
                        rxHistory = rxHistory,
                        txHistory = txHistory,
                        networkLabel = "Wi-Fi",
                        units = ByteUnits.DECIMAL,
                        direction = direction(),
                        onToggle = onToggle,
                        modifier = Modifier.width(170.dp),
                        playEntrance = playEntrance,
                    )
                }
            }
        }
    }

    private fun setNetwork(
        mobile: Long,
        wifi: Long,
        fontScale: Float = 1f,
        playEntrance: Boolean = false,
        rtl: Boolean = false,
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalDensity provides Density(LocalDensity.current.density, fontScale),
                    LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                ) {
                    NetworkBentoTile(
                        mobileBytes = mobile,
                        wifiBytes = wifi,
                        units = ByteUnits.DECIMAL,
                        playEntrance = playEntrance,
                        modifier = Modifier.width(170.dp),
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ network tile

    @Test
    fun network_tile_shows_the_larger_network_as_the_big_number() {
        setNetwork(mobile = 9_350_000L, wifi = 3_700_000_000L)
        rule.mainClock.advanceTimeBy(50)
        rule.onAllNodesWithText("3.70", useUnmergedTree = true).assertCountIs(1)
        rule.onAllNodesWithText("GB on Wi-Fi", useUnmergedTree = true).assertCountIs(1)
        rule.onAllNodesWithText("Mobile 9.35 MB", useUnmergedTree = true).assertCountIs(1)

    }

    @Test
    fun network_tile_swaps_roles_when_mobile_is_larger() {
        setNetwork(mobile = 800_000_000L, wifi = 12_000_000L)
        rule.mainClock.advanceTimeBy(50)
        rule.onAllNodesWithText("MB on mobile", useUnmergedTree = true).assertCountIs(1)
        rule.onAllNodesWithText("800", useUnmergedTree = true).assertCountIs(1)
        rule.onAllNodesWithText("Wi-Fi 12.0 MB", useUnmergedTree = true).assertCountIs(1)
    }

    @Test
    fun mobile_segment_is_visible_for_a_tiny_share() {
        setNetwork(mobile = 9_000_000L, wifi = 3_700_000_000L)
        rule.mainClock.advanceTimeBy(50)
        val width = rule.onNodeWithTag("network_bar_mobile", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("mobile segment at least 14 dp (was ${width.right - width.left})", (width.right - width.left) >= 13.9.dp)
    }

    @Test
    fun network_big_number_is_centred_and_never_clips_at_font_scale_2() {
        setNetwork(mobile = 0L, wifi = 124_500_000_000L, fontScale = 2f)
        rule.mainClock.advanceTimeBy(50)
        val tile = rule.onNodeWithTag("network_bento_tile", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val number = rule.onNodeWithTag("network_big_number", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("number inside the tile", number.left >= tile.left && number.right <= tile.right)
        val centreOffset = ((number.left + number.right) / 2) - ((tile.left + tile.right) / 2)
        assertTrue("number is centred (offset $centreOffset)", centreOffset.value in -2f..2f)
    }

    @Test
    fun network_tile_with_no_data_renders_zero_on_wifi() {
        setNetwork(mobile = 0L, wifi = 0L)
        rule.mainClock.advanceTimeBy(50)
        rule.onAllNodesWithText("GB on Wi-Fi", useUnmergedTree = true).assertCountIs(0)
        rule.onNodeWithTag("network_big_number", useUnmergedTree = true).assertExists()
    }

    @Test
    fun network_number_counts_up_and_never_exceeds_the_value() {
        setNetwork(mobile = 0L, wifi = 12_340_000_000L, playEntrance = true)
        var previous = -1.0
        repeat(90) {
            rule.mainClock.advanceTimeBy(16)
            val text = config("network_big_number")[SemanticsProperties.Text].joinToString("") { it.text }
            val value = text.toDouble()
            assertTrue("never above the real value ($value)", value <= 12.3 + 1e-6)
            assertTrue("never goes backwards", value >= previous - 1e-9)
            previous = value
        }
        rule.mainClock.advanceTimeBy(2_000)
        val finalText = config("network_big_number")[SemanticsProperties.Text].joinToString("") { it.text }
        assertEquals("12.3", finalText)
    }

    @Test
    fun tiles_enter_in_order() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                Row {
                    LiveBentoTile(
                        rxBps = 1L, txBps = 1L, rxHistory = emptyList(), txHistory = emptyList(), networkLabel = "Wi-Fi",
                        units = ByteUnits.DECIMAL, direction = LiveDirection.Download, onToggle = {},
                        modifier = Modifier.width(170.dp), playEntrance = true,
                    )
                    NetworkBentoTile(0L, 1L, ByteUnits.DECIMAL, playEntrance = true, modifier = Modifier.width(170.dp))
                }
            }
        }
        fun live() = config("live_bento_tile")[BentoEntranceProgressKey]
        fun network() = config("network_bento_tile")[BentoEntranceProgressKey]
        assertEquals(0f, live(), 0.001f)
        rule.mainClock.advanceTimeBy(190)
        assertTrue("live has started at 190 ms", live() > 0.01f)
        assertEquals("network starts 110 ms later", 0f, network(), 0.001f)
        rule.mainClock.advanceTimeBy(100)
        assertTrue("network has started at 290 ms", network() > 0.01f)
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(1f, live(), 0.01f)
        assertEquals(1f, network(), 0.01f)
    }

    @Test
    fun entrance_plays_once_per_app_start() {
        var visible by mutableStateOf(true)
        val seen = mutableListOf<Boolean>()
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                if (visible) seen += rememberPlayHomeEntrance()
            }
        }
        assertEquals(listOf(true), seen.take(1))
        // The hero marks the entrance as played; a later composition (another visit) must not replay it.
        HeroEntranceGate.played = true
        rule.runOnUiThread { visible = false }
        rule.waitForIdle()
        rule.runOnUiThread { visible = true }
        rule.waitForIdle()
        assertEquals(false, seen.last())
    }

    @Test
    fun ambient_motion_is_static_when_reduced() {
        var seconds: androidx.compose.runtime.State<Float>? = null
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    seconds = rememberAmbientSeconds(enabled = true)
                }
            }
        }
        repeat(60) { rule.mainClock.advanceTimeBy(16) }
        assertEquals(0f, seconds!!.value, 0f)
    }

    @Test
    fun ambient_loops_run_while_started_and_stop_when_not_visible() {
        val owner = object : androidx.lifecycle.LifecycleOwner {
            val registry = androidx.lifecycle.LifecycleRegistry.createUnsafe(this)
            override val lifecycle: androidx.lifecycle.Lifecycle get() = registry
        }
        owner.registry.currentState = androidx.lifecycle.Lifecycle.State.STARTED
        var seconds: androidx.compose.runtime.State<Float>? = null
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(androidx.lifecycle.compose.LocalLifecycleOwner provides owner) {
                    seconds = rememberAmbientSeconds(enabled = true)
                }
            }
        }
        repeat(70) { rule.mainClock.advanceTimeBy(16) }
        val running = seconds!!.value
        assertTrue("advances while started (was $running)", running > 0.5f)

        owner.registry.currentState = androidx.lifecycle.Lifecycle.State.CREATED
        repeat(5) { rule.mainClock.advanceTimeBy(16) }
        val stoppedAt = seconds!!.value
        repeat(70) { rule.mainClock.advanceTimeBy(16) }
        assertEquals("stops when the screen is not visible", stoppedAt, seconds!!.value, 0.001f)
    }

    // ------------------------------------------------------------------ live tile

    @Test
    fun live_tile_shows_the_other_direction_in_the_chip() {
        setLive()
        rule.mainClock.advanceTimeBy(50)
        rule.onAllNodesWithText("⇅", useUnmergedTree = true).assertCountIs(1)
        rule.onAllNodesWithText("↑ 70.0 KB/s", useUnmergedTree = true).assertCountIs(1)
    }

    @Test
    fun pressing_the_live_tile_switches_to_upload_and_back() {
        var direction by mutableStateOf(LiveDirection.Download)
        setLive(direction = { direction }, onToggle = {
            direction = if (direction == LiveDirection.Download) LiveDirection.Upload else LiveDirection.Download
        })
        rule.onNodeWithTag("live_bento_tile").performClick()
        rule.mainClock.advanceTimeBy(2_000)
        assertEquals(LiveDirection.Upload, direction)
        rule.onAllNodesWithText("↓ 1.20 MB/s", useUnmergedTree = true).assertCountIs(1)
        rule.onNodeWithTag("live_bento_tile").performClick()
        rule.mainClock.advanceTimeBy(2_000)
        assertEquals(LiveDirection.Download, direction)
        rule.onAllNodesWithText("↑ 70.0 KB/s", useUnmergedTree = true).assertCountIs(1)
    }

    @Test
    fun semantics_announce_the_direction_and_the_action() {
        var direction by mutableStateOf(LiveDirection.Download)
        setLive(direction = { direction }, onToggle = { direction = LiveDirection.Upload })
        rule.mainClock.advanceTimeBy(50)
        val download = rule.onNodeWithTag("live_bento_tile").fetchSemanticsNode().config
        assertEquals("Showing download speed", download[SemanticsProperties.StateDescription])
        assertEquals("Show upload speed", download[SemanticsActions.OnClick].label)

        rule.onNodeWithTag("live_bento_tile").performClick()
        rule.mainClock.advanceTimeBy(2_000)
        val upload = rule.onNodeWithTag("live_bento_tile").fetchSemanticsNode().config
        assertEquals("Showing upload speed", upload[SemanticsProperties.StateDescription])
        assertEquals("Show download speed", upload[SemanticsActions.OnClick].label)
    }

    @Test
    fun arrow_overshoots_then_settles_at_180() {
        var direction by mutableStateOf(LiveDirection.Download)
        setLive(direction = { direction })
        rule.mainClock.advanceTimeBy(50)
        rule.runOnUiThread { direction = LiveDirection.Upload }
        var peak = 0f
        repeat(120) {
            rule.mainClock.advanceTimeBy(16)
            peak = maxOf(peak, config("live_bento_tile")[LiveArrowRotationKey])
        }
        assertTrue("overshoots 180 (peak $peak)", peak > 185f)
        assertTrue("stays near 208 (peak $peak)", peak < 235f)
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(180f, config("live_bento_tile")[LiveArrowRotationKey], 0.5f)
    }

    @Test
    fun trace_morphs_between_the_two_series_and_the_tint_animates() {
        var direction by mutableStateOf(LiveDirection.Download)
        setLive(direction = { direction })
        rule.mainClock.advanceTimeBy(50)
        val startTint = config("live_bento_tile")[LiveContainerColorKey]
        rule.runOnUiThread { direction = LiveDirection.Upload }
        rule.mainClock.advanceTimeBy(160)
        val midMorph = config("live_bento_tile")[LiveTraceMorphKey]
        val midTint = config("live_bento_tile")[LiveContainerColorKey]
        assertTrue("trace is between the series (was $midMorph)", midMorph > 0.02f && midMorph < 1f)
        assertNotEquals("tint is mid-transition", startTint, midTint)
        rule.mainClock.advanceTimeBy(3_000)
        assertEquals(1f, config("live_bento_tile")[LiveTraceMorphKey], 0.02f)
        assertNotEquals(startTint, config("live_bento_tile")[LiveContainerColorKey])
    }

    @Test
    fun roll_transition_has_in_between_frames() {
        var direction by mutableStateOf(LiveDirection.Download)
        setLive(direction = { direction })
        rule.mainClock.advanceTimeBy(50)
        rule.runOnUiThread { direction = LiveDirection.Upload }
        rule.mainClock.advanceTimeBy(80)
        // Mid-roll both the outgoing download number and the incoming upload number exist.
        rule.onAllNodesWithText("Download", useUnmergedTree = true).assertCountIs(1)
        rule.onAllNodesWithText("Upload", useUnmergedTree = true).assertCountIs(1)
        rule.mainClock.advanceTimeBy(2_000)
        rule.onAllNodesWithText("Download", useUnmergedTree = true).assertCountIs(0)
    }

    @Test
    fun switch_is_instant_when_reduced() {
        var direction by mutableStateOf(LiveDirection.Download)
        setLive(reduced = true, direction = { direction })
        rule.mainClock.advanceTimeBy(50)
        rule.runOnUiThread { direction = LiveDirection.Upload }
        // The first frames only apply the state change; by 200 ms a reduced switch is complete, an animated one is not.
        rule.mainClock.advanceTimeBy(100)
        config("live_bento_tile") // reading the tree applies the pending state change
        rule.mainClock.advanceTimeBy(100)
        assertEquals(180f, config("live_bento_tile")[LiveArrowRotationKey], 0.01f)
        assertEquals(1f, config("live_bento_tile")[LiveTraceMorphKey], 0.01f)
        rule.onAllNodesWithText("Download", useUnmergedTree = true).assertCountIs(0)
    }

    @Test
    fun live_tile_does_not_clip_at_font_scale_2() {
        setLive(fontScale = 2f)
        rule.mainClock.advanceTimeBy(50)
        val tile = rule.onNodeWithTag("live_bento_tile", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val chip = rule.onNodeWithTag("live_other_direction_chip", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(chip.right <= tile.right + 0.5.dp && chip.left >= tile.left - 0.5.dp)
    }

    @Test
    fun live_tile_without_samples_still_renders() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                LiveBentoTile(0L, 0L, emptyList(), emptyList(), "Wi-Fi", ByteUnits.DECIMAL, LiveDirection.Download, {})
            }
        }
        rule.mainClock.advanceTimeBy(50)
        rule.onNodeWithTag("live_bento_tile").assertExists()
        assertFalse(config("live_bento_tile").contains(BentoEntranceProgressKey))
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.assertCountIs(n: Int) = this.assertCountEquals(n)
