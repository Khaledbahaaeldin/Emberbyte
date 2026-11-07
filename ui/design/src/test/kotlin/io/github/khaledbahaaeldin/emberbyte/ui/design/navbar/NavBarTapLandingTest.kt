package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavBarTapLandingTest {
    // Three cells: home 0..56, apps 56..112, plans 112..224 (selected plans is wide).
    private val starts = listOf(0f, 56f, 112f)
    private val ends = listOf(56f, 112f, 224f)
    private val slop = 20f
    private val flick = 600f

    @Test fun drift_within_the_slop_selects_the_cell_that_was_pressed() {
        assertEquals(1, resolveTapLanding(startX = 60f, totalDragPx = -14f, flickPxPerSec = 0f, starts, ends, slop, flick))
    }

    @Test fun drift_over_the_slop_is_a_real_drag() {
        assertNull(resolveTapLanding(startX = 60f, totalDragPx = -30f, flickPxPerSec = 0f, starts, ends, slop, flick))
    }

    @Test fun a_fast_flick_is_a_real_drag_even_when_short() {
        assertNull(resolveTapLanding(startX = 60f, totalDragPx = 10f, flickPxPerSec = 900f, starts, ends, slop, flick))
    }

    @Test fun drift_that_started_on_the_selected_tab_returns_that_tab() {
        assertEquals(2, resolveTapLanding(startX = 150f, totalDragPx = 8f, flickPxPerSec = 0f, starts, ends, slop, flick))
    }

    @Test fun the_slop_boundary_still_counts_as_a_tap() {
        assertEquals(0, resolveTapLanding(startX = 10f, totalDragPx = 20f, flickPxPerSec = 599f, starts, ends, slop, flick))
    }
}
