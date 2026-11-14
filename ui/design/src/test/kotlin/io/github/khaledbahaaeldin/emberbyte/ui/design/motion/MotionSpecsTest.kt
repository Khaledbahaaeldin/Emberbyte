package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MotionSpecsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun reduced_motion_lists_use_snap_specs() {
        var motion: EmberbyteMotion? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    motion = rememberEmberbyteMotion()
                }
            }
        }

        val resolved = motion ?: error("Motion specs not resolved")
        assertEquals(snap<Float>(), resolved.spatial<Float>())
        assertEquals(tween<Float>(REDUCED_MOTION_EFFECTS_DURATION_MS), resolved.effects<Float>())
        assertEquals(snap<Float>(), resolved.fastSpatial<Float>())
        assertEquals(snap<Float>(), resolved.fastEffects<Float>())
    }

    @Test
    fun standard_motion_uses_material_theme_specs() {
        var motion: EmberbyteMotion? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides false) {
                    motion = rememberEmberbyteMotion()
                }
            }
        }

        val resolved = motion ?: error("Motion specs not resolved")
        rule.runOnIdle {
            // Under normal motion, spatial and effects are non-snap specs from Material motionScheme
            assertFalse(resolved.spatial<Float>() is SnapSpec)
            assertFalse(resolved.effects<Float>() is SnapSpec)
            assertFalse(resolved.fastSpatial<Float>() is SnapSpec)
            assertFalse(resolved.fastEffects<Float>() is SnapSpec)
        }
    }
}
