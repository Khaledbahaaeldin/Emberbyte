package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/**
 * Duration for effects (fade) when reduced motion is enabled.
 */
const val REDUCED_MOTION_EFFECTS_DURATION_MS = 90

/**
 * Accessor for app-wide motion specifications.
 * When [LocalReduceMotion] is true, returns snap/instant animation specs.
 */
interface EmberbyteMotion {
    fun <T> spatial(): FiniteAnimationSpec<T>
    fun <T> effects(): FiniteAnimationSpec<T>
    fun <T> fastSpatial(): FiniteAnimationSpec<T>
    fun <T> fastEffects(): FiniteAnimationSpec<T>
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberEmberbyteMotion(): EmberbyteMotion {
    val reduceMotion = LocalReduceMotion.current
    val motion = MaterialTheme.motionScheme
    return remember(reduceMotion, motion) {
        object : EmberbyteMotion {
            override fun <T> spatial(): FiniteAnimationSpec<T> =
                if (reduceMotion) snap() else motion.defaultSpatialSpec()

            override fun <T> effects(): FiniteAnimationSpec<T> =
                if (reduceMotion) tween(REDUCED_MOTION_EFFECTS_DURATION_MS) else motion.defaultEffectsSpec()

            override fun <T> fastSpatial(): FiniteAnimationSpec<T> =
                if (reduceMotion) snap() else motion.fastSpatialSpec()

            override fun <T> fastEffects(): FiniteAnimationSpec<T> =
                if (reduceMotion) snap() else motion.fastEffectsSpec()
        }
    }
}
