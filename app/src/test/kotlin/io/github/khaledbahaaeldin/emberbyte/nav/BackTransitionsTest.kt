package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackTransitionsTest {

    @Test
    fun pop_constants_match_the_contract() {
        assertEquals(0.90f, BackTransitions.SHRINK, 0.001f)
        assertEquals(28, BackTransitions.SLIDE_DP)
        assertEquals(0.94f, BackTransitions.BEHIND_SCALE_START, 0.001f)
        assertEquals(32, BackTransitions.CORNER_DP)
    }

    @Test
    fun corner_radius_grows_from_0_to_32dp_over_the_first_quarter_of_progress_and_stays() {
        assertEquals(0.dp, BackTransitions.cornerRadiusFor(0.0f))
        assertEquals(16.dp, BackTransitions.cornerRadiusFor(0.125f))
        assertEquals(32.dp, BackTransitions.cornerRadiusFor(0.25f))
        assertEquals(32.dp, BackTransitions.cornerRadiusFor(0.50f))
        assertEquals(32.dp, BackTransitions.cornerRadiusFor(1.0f))
    }

    @Test
    fun pop_exit_and_enter_differ_in_rtl_and_ltr_by_origin_and_slide_direction() {
        val ltrOrigin = BackTransitions.transformOriginFor(LayoutDirection.Ltr)
        val rtlOrigin = BackTransitions.transformOriginFor(LayoutDirection.Rtl)

        // In LTR, dragged from left edge toward right, so transform origin is right edge (pivotRight = 1.0f)
        // In RTL, dragged from right edge toward left, so transform origin is left edge (pivotLeft = 0.0f)
        assertEquals(1.0f, ltrOrigin.pivotFractionX, 0.01f)
        assertEquals(0.0f, rtlOrigin.pivotFractionX, 0.01f)
        assertNotEquals(ltrOrigin.pivotFractionX, rtlOrigin.pivotFractionX)

        val ltrSlideSign = BackTransitions.slideSignFor(LayoutDirection.Ltr)
        val rtlSlideSign = BackTransitions.slideSignFor(LayoutDirection.Rtl)
        assertEquals(1, ltrSlideSign)
        assertEquals(-1, rtlSlideSign)
    }

    @Test
    fun reduced_motion_uses_a_90ms_fade_only() {
        assertEquals(90, BackTransitions.REDUCED_MOTION_DURATION_MS)
    }

    @Test
    fun tab_transitions_are_not_used_for_pop() {
        val appFile = File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/EmberbyteApp.kt")
        assertTrue("EmberbyteApp.kt should exist", appFile.exists())
        val content = appFile.readText()

        // NavHost must not use tabEnterTransition or tabExitTransition for popEnterTransition or popExitTransition
        val popEnterMatch = Regex("""popEnterTransition\s*=\s*\{\s*tabEnterTransition""").containsMatchIn(content)
        val popExitMatch = Regex("""popExitTransition\s*=\s*\{\s*tabExitTransition""").containsMatchIn(content)
        assertTrue("popEnterTransition must not use tabEnterTransition", !popEnterMatch)
        assertTrue("popExitTransition must not use tabExitTransition", !popExitMatch)
        assertTrue("EmberbyteApp must reference BackTransitions", content.contains("BackTransitions"))
    }
}
