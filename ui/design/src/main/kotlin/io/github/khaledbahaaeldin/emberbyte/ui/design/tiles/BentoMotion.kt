package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroEntranceGate
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/** The ambient loops (rings, tile waves, badge spin) update at most this often: ~30 fps, minus a frame of slack. */
internal const val AMBIENT_FRAME_NANOS = 30_000_000L

/** First tile enters this long after the screen's first composition, and each next one this much later. */
internal const val BENTO_ENTRANCE_FIRST_MS = 140
internal const val BENTO_ENTRANCE_STEP_MS = 110

/** The count-up of the network number: starts 380 ms in and lasts 950 ms. */
internal const val BENTO_COUNT_UP_DELAY_MS = 380
internal const val BENTO_COUNT_UP_MS = 950

/**
 * True once per app start, together with the Home hero entrance: the first time the Home tiles are composed. It reads
 * the same process flag as the hero, so a later visit, a rotation or a scroll back into view does not replay it, and it
 * is never true when animations are reduced.
 */
@Composable
fun rememberPlayHomeEntrance(): Boolean {
    val reduce = LocalReduceMotion.current
    return remember { !reduce && !HeroEntranceGate.played }
}

/**
 * Seconds of ambient time, advancing only while the screen is started and [enabled] (loops stop when Home is not
 * visible and under reduced motion). Read it inside a draw or graphics-layer lambda so nothing recomposes per frame.
 */
@Composable
internal fun rememberAmbientSeconds(enabled: Boolean): State<Float> {
    val reduce = LocalReduceMotion.current
    val running by rememberUpdatedState(enabled && !reduce)
    val seconds = remember { mutableFloatStateOf(0f) }
    if (enabled && !reduce) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var last = 0L
                while (true) {
                    val now = withInfiniteAnimationFrameNanos { it }
                    if (last == 0L) {
                        last = now
                    } else if (now - last >= AMBIENT_FRAME_NANOS) {
                        // Decorative loops run at about 30 fps: half the redraws, indistinguishable at their speeds.
                        if (running) seconds.floatValue += (now - last) / 1_000_000_000f
                        last = now
                    }
                }
            }
        }
    }
    return seconds
}

/** Test-only readout of a tile's entrance progress (0 before it starts, 1 when settled). */
val BentoEntranceProgressKey = SemanticsPropertyKey<Float>("BentoEntranceProgress")

/**
 * Springs a tile in: scale [fromScale] to 1, translate [fromDy] to 0 and fade, starting [startMs] after the screen's
 * first composition. Does nothing when [play] is false, so a tile already on screen is never replayed.
 */
@Composable
internal fun Modifier.entrance(
    startMs: Int,
    play: Boolean,
    fromScale: Float = 0.86f,
    fromDy: androidx.compose.ui.unit.Dp = 18.dp,
): Modifier {
    if (!play || LocalReduceMotion.current) return this
    val motion = rememberEmberbyteMotion()
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(startMs.toLong())
        progress.animateTo(1f, motion.spatial())
    }
    return this.semantics { set(BentoEntranceProgressKey, progress.value) }.graphicsLayer {
        val p = progress.value
        val scale = fromScale + (1f - fromScale) * p
        scaleX = scale
        scaleY = scale
        translationY = fromDy.toPx() * (1f - p)
        alpha = p.coerceIn(0f, 1f)
    }
}

/** The bento tiles: [order] places each after the first (140 ms, then 110 ms apart). */
@Composable
internal fun Modifier.bentoEntrance(order: Int, play: Boolean): Modifier =
    entrance(BENTO_ENTRANCE_FIRST_MS + BENTO_ENTRANCE_STEP_MS * order, play)

/**
 * Staggered screen entrance for a page's blocks: each block fades and rises (translate 16 dp, scale 0.97) on the
 * spatial spring, [order] x 70 ms after the first block, which starts 80 ms after the screen is shown. Pass
 * `play = false` (or reduced motion) and it does nothing.
 */
@Composable
fun Modifier.staggerEntrance(order: Int, play: Boolean): Modifier =
    entrance(startMs = 80 + 70 * order, play = play, fromScale = 0.97f, fromDy = 16.dp)

/** Switch for the reveal-on-first-visibility entrance; tests turn it off so content is at its final state at once. */
object RevealGate {
    @Volatile
    var enabled: Boolean = true
}

/**
 * A section reveals once per visit: it rises 22 dp, scales 0.97 to 1 and fades in on the spatial spring, [order] x 90 ms
 * after its siblings that appear together. [seen] remembers which keys already revealed, so scrolling back does not
 * replay it. Reduced motion shows it at once.
 */
@Composable
fun Modifier.revealOnce(key: String, seen: MutableSet<String>, order: Int = 0): Modifier {
    val play = remember { RevealGate.enabled && key !in seen }
    // The section counts as revealed once its entrance has had time to finish, so a lazy list that composes it twice at
    // start-up still plays it, while scrolling back to it afterwards does not.
    if (play) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(90L * order + 1_200L)
            seen.add(key)
        }
    }
    return entrance(startMs = 90 * order, play = play, fromScale = 0.97f, fromDy = 22.dp)
}
