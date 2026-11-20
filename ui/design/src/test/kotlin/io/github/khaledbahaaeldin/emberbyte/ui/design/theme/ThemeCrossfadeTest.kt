package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.DuskDark
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.DuskLight
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * T9: a theme or dark-mode change must cross-fade every colour instead of flipping in one frame, and snap under
 * reduced motion. This drives the real [EmberbyteTheme], so removing the animation from the theme fails it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeCrossfadeTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setAnimatorScale(scale: Float) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
    }

    private fun switchToDark(spec: () -> Unit) {
        rule.runOnUiThread {
            spec()
            Snapshot.sendApplyNotifications()
        }
    }

    @Test
    fun switching_to_dark_cross_fades_the_palette() {
        setAnimatorScale(1f)
        var spec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
        var seen: EmberbytePalette? = null
        rule.mainClock.autoAdvance = false
        rule.setContent { EmberbyteTheme(spec) { seen = LocalEmberbytePalette.current } }
        rule.mainClock.advanceTimeByFrame()
        assertEquals(DuskLight.background, seen?.background)

        switchToDark { spec = EmberbyteThemeSpec(presetId = "dusk", darkTheme = true) }
        val light = DuskLight.background.luminance()
        val dark = DuskDark.background.luminance()
        var sawBetween = false
        repeat(40) {
            rule.mainClock.advanceTimeBy(16)
            val l = seen?.background?.luminance() ?: light
            if (l < light - 0.02f && l > dark + 0.02f) sawBetween = true
        }
        assertTrue("the background should pass through intermediate colours", sawBetween)
        rule.mainClock.advanceTimeBy(1500)
        val settled = seen?.background?.luminance() ?: -1f
        assertEquals("the palette settles on the dark colours", dark, settled, 0.02f)
    }

    @Test
    fun switching_to_dark_snaps_when_animations_are_off() {
        setAnimatorScale(0f)
        try {
            var spec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
            var seen: EmberbytePalette? = null
            rule.mainClock.autoAdvance = false
            rule.setContent { EmberbyteTheme(spec) { seen = LocalEmberbytePalette.current } }
            rule.mainClock.advanceTimeByFrame()

            switchToDark { spec = EmberbyteThemeSpec(presetId = "dusk", darkTheme = true) }
            // Reduced motion uses snap(): the very first frames after the change already show the final colours.
            repeat(3) { rule.mainClock.advanceTimeBy(16) }
            val l = seen?.background?.luminance() ?: -1f
            assertEquals("reduced motion must snap straight to dark", DuskDark.background.luminance(), l, 0.02f)
        } finally {
            setAnimatorScale(1f)
        }
    }
}
