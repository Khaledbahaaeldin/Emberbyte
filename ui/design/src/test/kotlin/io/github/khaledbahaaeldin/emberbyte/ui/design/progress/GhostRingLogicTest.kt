package io.github.khaledbahaaeldin.emberbyte.ui.design.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class GhostRingLogicTest {
    @Test
    fun ghost_arc_grows_from_zero_to_the_target_and_stops() {
        assertEquals(0f, ghostArcFraction(0f, 0.7f), 0f)
        assertEquals(0.35f, ghostArcFraction(0.5f, 0.7f), 1e-6f)
        assertEquals(0.7f, ghostArcFraction(1f, 0.7f), 1e-6f)
    }

    @Test
    fun ghost_arc_is_clamped_outside_zero_to_one() {
        assertEquals(0f, ghostArcFraction(-0.4f, 0.7f), 0f)
        assertEquals(0.7f, ghostArcFraction(3f, 0.7f), 1e-6f)
    }
}
