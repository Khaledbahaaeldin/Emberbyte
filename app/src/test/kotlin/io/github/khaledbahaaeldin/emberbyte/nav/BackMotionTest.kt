package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The single definition of what back and push look like (task T4). */
class BackMotionTest {
    private val base = "src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/"

    @Test fun slide_is_a_fraction_of_the_width_not_a_fixed_pixel_count() {
        assertEquals(86, BackTransitions.slideOffsetPx(1080, LayoutDirection.Ltr))
        assertEquals(173, BackTransitions.slideOffsetPx(2160, LayoutDirection.Ltr))
        assertEquals(-86, BackTransitions.slideOffsetPx(1080, LayoutDirection.Rtl))
    }

    @Test fun shrinkFor_is_linear_between_one_and_090() {
        assertEquals(1.0f, BackTransitions.shrinkFor(0f), 0.0001f)
        assertEquals(0.95f, BackTransitions.shrinkFor(0.5f), 0.0001f)
        assertEquals(0.90f, BackTransitions.shrinkFor(1f), 0.0001f)
        assertEquals(1.0f, BackTransitions.shrinkFor(-0.4f), 0.0001f)
        assertEquals(0.90f, BackTransitions.shrinkFor(1.7f), 0.0001f)
    }

    @Test fun shadowDpFor_reaches_8dp_at_a_quarter_of_the_gesture() {
        assertEquals(0.dp, BackTransitions.shadowDpFor(0f))
        assertEquals(4.dp, BackTransitions.shadowDpFor(0.125f))
        assertEquals(8.dp, BackTransitions.shadowDpFor(0.25f))
        assertEquals(8.dp, BackTransitions.shadowDpFor(1f))
    }

    @Test fun predictive_transitions_follow_the_finger_linearly() {
        assertSame(LinearEasing, BackTransitions.PREDICTIVE_EASING)
        assertEquals(300, BackTransitions.PREDICTIVE_DURATION_MS)
        assertEquals(0.85f, BackTransitions.PREDICTIVE_FADE_START, 0.0001f)
    }

    @Test fun push_and_pop_are_mirror_images_built_from_the_same_scale_constants() {
        assertEquals(BackTransitions.POP_EXIT_SCALE, BackTransitions.PUSH_ENTER_SCALE, 0f)
        assertEquals(BackTransitions.POP_ENTER_SCALE, BackTransitions.PUSH_EXIT_SCALE, 0f)
        assertEquals(BackTransitions.SHRINK, BackTransitions.POP_EXIT_SCALE, 0f)
        assertEquals(BackTransitions.BEHIND_SCALE_START, BackTransitions.POP_ENTER_SCALE, 0f)
    }

    @Test fun the_nav_host_is_given_the_predictive_pop_transitions() {
        val app = File(base + "EmberbyteApp.kt").readText()
        assertTrue("NavHost needs predictivePopEnterTransition", app.contains("predictivePopEnterTransition = transitions.predictivePopEnter"))
        assertTrue("NavHost needs predictivePopExitTransition", app.contains("predictivePopExitTransition = transitions.predictivePopExit"))
    }

    @Test fun guarded_back_uses_the_shared_motion_math() {
        val guarded = File(base + "nav/GuardedBack.kt").readText()
        assertTrue(guarded.contains("BackTransitions.shrinkFor("))
        assertTrue(guarded.contains("BackTransitions.shadowDpFor("))
        assertTrue(guarded.contains("BackTransitions.cornerRadiusFor("))
    }

    @Test fun the_sub_screen_derives_its_corners_and_shadow_from_the_shared_math() {
        val back = File(base + "nav/BackTransitions.kt").readText()
        assertTrue(back.contains("BackTransitions.cornerRadiusFor(progress)"))
        assertTrue(back.contains("BackTransitions.shadowDpFor(progress)"))
    }

    @Test fun the_navbar_stays_composed_and_animates_with_the_route() {
        val app = File(base + "EmberbyteApp.kt").readText()
        assertTrue("navbar visibility must include isTopLevel", app.contains("visible = visibility.visible && isTopLevel"))
        assertTrue("navbar must not be wrapped in an if(isTopLevel) block", !app.contains("if (isTopLevel) {"))
    }
}
