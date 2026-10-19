package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PillSpringTest {
    private class FakeFrameClock : MonotonicFrameClock {
        var currentTimeNanos = 0L
        private val waiters = mutableListOf<(Long) -> Unit>()

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            var result: R? = null
            var invoked = false
            val callback: (Long) -> Unit = { time ->
                result = onFrame(time)
                invoked = true
            }
            waiters.add(callback)
            kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
                val wrapper: (Long) -> Unit = { time ->
                    if (cont.isActive) {
                        callback(time)
                        cont.resume(Unit) {}
                    }
                }
                waiters.remove(callback)
                waiters.add(wrapper)
            }
            @Suppress("UNCHECKED_CAST")
            return result as R
        }

        fun advanceByMillis(millis: Long) {
            currentTimeNanos += millis * 1_000_000L
            val toRun = waiters.toList()
            waiters.clear()
            for (w in toRun) {
                w(currentTimeNanos)
            }
        }
    }

    @Test
    fun moving_right_the_right_edge_overshoots_its_target_then_settles_within_half_a_dp() = runTest {
        val clock = FakeFrameClock()
        val spring = PillSpring(
            initialLeft = 6f,
            initialRight = 98f,
            barInnerLeft = 2f,
            barInnerRight = 250f,
            reduceMotion = false,
        )
        val job = launch(clock) {
            spring.retarget(left = 118f, right = 210f)
        }
        testScheduler.runCurrent()

        var maxRawRight = 98f
        // Advance 1.5 seconds (94 frames of 16ms = 1504ms)
        repeat(94) {
            clock.advanceByMillis(16)
            testScheduler.runCurrent()
            if (spring.rawRight > maxRawRight) {
                maxRawRight = spring.rawRight
            }
        }

        val overshoot = maxRawRight - 210f
        assertTrue("Overshoot ($overshoot) must be between 2 and 24 dp", overshoot in 2f..24f)
        assertTrue("Left edge (${spring.rawLeft}) must settle within 0.5 dp of 118", abs(spring.rawLeft - 118f) <= 0.5f)
        assertTrue("Right edge (${spring.rawRight}) must settle within 0.5 dp of 210", abs(spring.rawRight - 210f) <= 0.5f)
        job.cancelAndJoin()
    }

    @Test
    fun the_leading_edge_is_ahead_of_the_trailing_edge_during_the_first_150_ms() = runTest {
        val clock = FakeFrameClock()
        val spring = PillSpring(
            initialLeft = 6f,
            initialRight = 98f,
            barInnerLeft = 2f,
            barInnerRight = 250f,
            reduceMotion = false,
        )
        val job = launch(clock) {
            spring.retarget(left = 118f, right = 210f)
        }
        testScheduler.runCurrent()

        // Initialize first frame
        clock.advanceByMillis(16)
        testScheduler.runCurrent()

        var stretchedCount = 0
        // Next 8 frames (16ms..144ms)
        repeat(8) {
            clock.advanceByMillis(16)
            testScheduler.runCurrent()
            val leftProgress = (spring.rawLeft - 6f) / (118f - 6f)
            val rightProgress = (spring.rawRight - 98f) / (210f - 98f)
            assertTrue(
                "Leading right edge progress ($rightProgress) must be ahead of trailing left edge ($leftProgress)",
                rightProgress > leftProgress,
            )
            if (spring.rawRight - spring.rawLeft > 92f) {
                stretchedCount++
            }
        }
        assertTrue("Pill must stretch wider than 92 dp during first 150 ms", stretchedCount > 0)
        job.cancelAndJoin()
    }

    @Test
    fun retargeting_mid_flight_keeps_position_continuous() = runTest {
        val clock = FakeFrameClock()
        val spring = PillSpring(
            initialLeft = 6f,
            initialRight = 98f,
            barInnerLeft = 2f,
            barInnerRight = 250f,
            reduceMotion = false,
        )
        val job1 = launch(clock) {
            spring.retarget(left = 118f, right = 210f)
        }
        testScheduler.runCurrent()

        var prevL = spring.rawLeft
        var prevR = spring.rawRight
        var maxStepBeforeL = 0f
        var maxStepBeforeR = 0f

        repeat(4) {
            clock.advanceByMillis(16)
            testScheduler.runCurrent()
            val dL = abs(spring.rawLeft - prevL)
            val dR = abs(spring.rawRight - prevR)
            if (dL > maxStepBeforeL) maxStepBeforeL = dL
            if (dR > maxStepBeforeR) maxStepBeforeR = dR
            prevL = spring.rawLeft
            prevR = spring.rawRight
        }

        val velBeforeL = spring.leftVelocity
        val velBeforeR = spring.rightVelocity
        assertTrue("Velocity before retarget must be positive ($velBeforeR)", velBeforeR > 0f)

        // Retarget mid-flight back to middle tab (62..154) while job1 is still in flight
        val job2 = launch(clock) {
            spring.retarget(left = 62f, right = 154f)
        }
        testScheduler.runCurrent()
        job1.cancel()

        // Immediately upon retarget (before advancing clock), position and velocity must not jump
        assertEquals(prevL, spring.rawLeft, 0.01f)
        assertEquals(prevR, spring.rawRight, 0.01f)
        assertEquals(velBeforeL, spring.leftVelocity, 1f)
        assertEquals(velBeforeR, spring.rightVelocity, 1f)

        // Advance one frame after retarget: step must not exceed maxStepBefore * 1.10
        clock.advanceByMillis(16)
        testScheduler.runCurrent()
        val stepAfterL = abs(spring.rawLeft - prevL)
        val stepAfterR = abs(spring.rawRight - prevR)
        assertTrue(
            "Left step after retarget ($stepAfterL) must be <= 1.1x max step before ($maxStepBeforeL)",
            stepAfterL <= maxStepBeforeL * 1.1f,
        )
        assertTrue(
            "Right step after retarget ($stepAfterR) must be <= 1.1x max step before ($maxStepBeforeR)",
            stepAfterR <= maxStepBeforeR * 1.1f,
        )
        job2.cancelAndJoin()
    }

    @Test
    fun flick_velocity_carries_the_landing_to_the_far_tab() {
        // Dragging from Home (0) and releasing inside Apps cell (e.g., pill at 35..127, center = 81):
        // With 0 dp/s: center + 0 = 81 -> lands on Apps (index 1)
        // With 3000 dp/s: center + 3000 * 0.08 = 81 + 240 = 321 -> lands on Plans (index 2)
        val slowLanding = NavBarSpring.landingIndex(
            pillLeft = 35f,
            pillRight = 127f,
            velocityPxPerSec = 0f,
            paddingPx = 6f,
            innerWidthPx = 204f,
            itemCount = 3,
        )
        val flickLanding = NavBarSpring.landingIndex(
            pillLeft = 35f,
            pillRight = 127f,
            velocityPxPerSec = 3000f,
            paddingPx = 6f,
            innerWidthPx = 204f,
            itemCount = 3,
        )
        assertEquals("0 dp/s release at Apps should land on Apps (1)", 1, slowLanding)
        assertEquals("3000 dp/s flick from Home should land on Plans (2)", 2, flickLanding)
    }

    @Test
    fun reduce_motion_places_the_pill_at_once_with_zero_overshoot() = runTest {
        val clock = FakeFrameClock()
        val spring = PillSpring(
            initialLeft = 6f,
            initialRight = 98f,
            barInnerLeft = 2f,
            barInnerRight = 214f,
            reduceMotion = true,
        )
        spring.retarget(left = 118f, right = 210f)
        assertEquals(118f, spring.left, 0.01f)
        assertEquals(210f, spring.right, 0.01f)

        var maxRight = spring.right
        repeat(10) {
            clock.advanceByMillis(16)
            if (spring.right > maxRight) maxRight = spring.right
        }
        assertEquals("Zero overshoot in reduce motion", 210f, maxRight, 0.01f)
    }

    @Test
    fun edges_never_leave_the_bar_bounds() = runTest {
        val clock = FakeFrameClock()
        val spring = PillSpring(
            initialLeft = 6f,
            initialRight = 98f,
            barInnerLeft = 2f,
            barInnerRight = 214f,
            reduceMotion = false,
        )
        // Drag past left and right walls
        spring.dragTo(left = -50f, right = 42f)
        assertEquals("Left edge clamped to barInnerLeft (2f)", 2f, spring.left, 0.01f)
        spring.dragTo(left = 180f, right = 272f)
        assertTrue("Left edge moved right (${spring.left})", spring.left >= 120f)
        assertEquals("Right edge clamped to barInnerRight (214f)", 214f, spring.right, 0.01f)

        // High-velocity spring toward right wall
        spring.dragTo(left = 6f, right = 98f)
        val job = launch(clock) {
            spring.retarget(left = 118f, right = 210f, velocity = 2500f)
        }
        testScheduler.runCurrent()
        repeat(40) {
            clock.advanceByMillis(16)
            testScheduler.runCurrent()
            assertTrue("Clamped left (${spring.left}) >= 2f", spring.left >= 2f)
            assertTrue("Clamped right (${spring.right}) <= 214f", spring.right <= 214f)
        }
        job.cancelAndJoin()
    }

    @Test
    fun damping_below_1_is_used_so_motion_is_underdamped() {
        assertTrue(
            "Damping ratio (${NavBarSpring.DAMPING_RATIO}) must be < 1f for underdamped spring",
            NavBarSpring.DAMPING_RATIO < 1f,
        )
        assertEquals(0.58f, NavBarSpring.DAMPING_RATIO, 0.001f)
        assertEquals(420f, NavBarSpring.BASE_STIFFNESS, 0.001f)
        assertEquals(1.7f, NavBarSpring.EDGE_LEAD, 0.001f)
    }
}
