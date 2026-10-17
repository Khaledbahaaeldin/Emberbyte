package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Index of the item whose measured bounds contain [x]; in gaps or beyond the ends, the item with the nearest edge.
 */
internal fun itemIndexAt(x: Float, starts: List<Float>, ends: List<Float>): Int {
    var best = 0
    var bestDistance = Float.MAX_VALUE
    for (i in starts.indices) {
        val distance = when {
            x < starts[i] -> starts[i] - x
            x > ends[i] -> x - ends[i]
            else -> 0f
        }
        if (distance < bestDistance) {
            best = i
            bestDistance = distance
        }
    }
    return best
}

/** Pixels the previewed pill trails behind the finger: it follows with resistance, never 1:1. */
internal fun dragResistance(deltaPx: Float): Float = deltaPx * 0.5f

/** Motion specs for compact nav bar pill following and transitions. */
internal object CompactNavMotion {
    fun <T> pillFollowSpec(previewIndex: Int?, reduceMotion: Boolean, motion: MotionScheme): AnimationSpec<T> =
        if (previewIndex != null || reduceMotion) snap() else motion.fastSpatialSpec()
}

/**
 * Hides after [thresholdPx] of accumulated downward scroll; any upward scroll shows again.
 * `downPx` is positive when the user scrolls the content down (finger moves up).
 */
internal class ScrollVisibilityTracker(private val thresholdPx: Float) {
    var visible: Boolean = true
        private set
    private var accumulatedDown = 0f

    fun onScroll(downPx: Float): Boolean {
        when {
            downPx < 0f -> {
                accumulatedDown = 0f
                visible = true
            }
            downPx > 0f -> {
                accumulatedDown += downPx
                if (accumulatedDown > thresholdPx) visible = false
            }
        }
        return visible
    }

    fun reset() {
        accumulatedDown = 0f
        visible = true
    }
}

@Stable
class NavBarVisibility internal constructor(thresholdPx: Float) {
    private val tracker = ScrollVisibilityTracker(thresholdPx)

    var visible: Boolean by mutableStateOf(true)
        private set

    /** Attach with `Modifier.nestedScroll(connection)` on an ancestor of the scrolling content. */
    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            visible = tracker.onScroll(downPx = -available.y)
            return Offset.Zero
        }
    }

    fun show() {
        tracker.reset()
        visible = true
    }
}

@Composable
fun rememberNavBarVisibility(threshold: Dp = 40.dp): NavBarVisibility {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    return remember(thresholdPx) { NavBarVisibility(thresholdPx) }
}
