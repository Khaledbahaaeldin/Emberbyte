package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Stable
class PillSpring(
    initialLeft: Float = 0f,
    initialRight: Float = 0f,
    barInnerLeft: Float = 2f,
    barInnerRight: Float = 214f,
    val minPillWidth: Float = 40f,
    var reduceMotion: Boolean = false,
) {
    private val leftAnim = Animatable(initialLeft, visibilityThreshold = 0.1f)
    private val rightAnim = Animatable(initialRight, visibilityThreshold = 0.1f)

    var barInnerLeft by mutableFloatStateOf(barInnerLeft)
    var barInnerRight by mutableFloatStateOf(barInnerRight)

    var targetLeft by mutableFloatStateOf(initialLeft)
        private set
    var targetRight by mutableFloatStateOf(initialRight)
        private set

    val rawLeft: Float
        get() = leftAnim.value

    val rawRight: Float
        get() = rightAnim.value

    val leftVelocity: Float
        get() = leftAnim.velocity

    val rightVelocity: Float
        get() = rightAnim.velocity

    /**
     * Left edge clamped to the bar's inner bounds for draw-phase reading.
     */
    val left: Float
        get() = leftAnim.value.coerceIn(
            barInnerLeft,
            (barInnerRight - minPillWidth).coerceAtLeast(barInnerLeft),
        )

    /**
     * Right edge clamped to the bar's inner bounds for draw-phase reading.
     */
    val right: Float
        get() {
            val l = left
            val r = rightAnim.value.coerceIn(l + minPillWidth, barInnerRight)
            return r.coerceAtMost(barInnerRight)
        }

    suspend fun retarget(left: Float, right: Float, velocity: Float = 0f) {
        val currentL = leftAnim.value
        val currentR = rightAnim.value
        val startVelLeft = if (velocity != 0f) velocity else leftAnim.velocity
        val startVelRight = if (velocity != 0f) velocity else rightAnim.velocity

        targetLeft = left
        targetRight = right
        if (reduceMotion) {
            leftAnim.snapTo(left)
            rightAnim.snapTo(right)
            return
        }

        val movingRight = right > currentR - 0.01f && left > currentL - 0.01f
        val movingLeft = left < currentL + 0.01f && right < currentR + 0.01f

        val kLeft = when {
            movingRight -> NavBarSpring.trailingStiffness
            movingLeft -> NavBarSpring.leadingStiffness
            else -> NavBarSpring.leadingStiffness
        }
        val kRight = when {
            movingRight -> NavBarSpring.leadingStiffness
            movingLeft -> NavBarSpring.trailingStiffness
            else -> NavBarSpring.leadingStiffness
        }

        coroutineScope {
            launch {
                leftAnim.animateTo(
                    targetValue = left,
                    animationSpec = spring(
                        dampingRatio = NavBarSpring.DAMPING_RATIO,
                        stiffness = kLeft,
                        visibilityThreshold = 0.1f,
                    ),
                    initialVelocity = startVelLeft,
                )
            }
            launch {
                rightAnim.animateTo(
                    targetValue = right,
                    animationSpec = spring(
                        dampingRatio = NavBarSpring.DAMPING_RATIO,
                        stiffness = kRight,
                        visibilityThreshold = 0.1f,
                    ),
                    initialVelocity = startVelRight,
                )
            }
        }
    }

    suspend fun dragTo(left: Float, right: Float) {
        val width = (right - left).coerceAtLeast(minPillWidth)
        val maxLeft = (barInnerRight - width).coerceAtLeast(barInnerLeft)
        val clampedLeft = left.coerceIn(barInnerLeft, maxLeft)
        val clampedRight = (clampedLeft + width).coerceAtMost(barInnerRight)
        targetLeft = clampedLeft
        targetRight = clampedRight
        leftAnim.snapTo(clampedLeft)
        rightAnim.snapTo(clampedRight)
    }
}
