@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NavTransitionsTest {
    private val motion = MotionScheme.expressive()

    @Test fun reduce_motion_switches_tabs_without_any_transition() {
        assertEquals(EnterTransition.None, tabEnterTransition(motion, reduceMotion = true))
        assertEquals(ExitTransition.None, tabExitTransition(motion, reduceMotion = true))
    }

    @Test fun normal_motion_cross_fades_with_the_motion_scheme() {
        assertNotEquals(EnterTransition.None, tabEnterTransition(motion, reduceMotion = false))
        assertNotEquals(ExitTransition.None, tabExitTransition(motion, reduceMotion = false))
    }

    @Test fun isTabRoute_is_true_only_for_the_three_tab_routes() {
        assertTrue(isTabRoute("home"))
        assertTrue(isTabRoute("apps"))
        assertTrue(isTabRoute("plans"))
        assertFalse(isTabRoute("plans/new"))
        assertFalse(isTabRoute("apps/{packageName}"))
        assertFalse(isTabRoute("plans/{planId}"))
        assertFalse(isTabRoute("settings"))
        assertFalse(isTabRoute(null))
    }

    @Test fun tabDirection_follows_navbar_order() {
        assertEquals(1, tabDirection("home", "apps"))
        assertEquals(1, tabDirection("home", "plans"))
        assertEquals(1, tabDirection("apps", "plans"))
        assertEquals(-1, tabDirection("plans", "apps"))
        assertEquals(-1, tabDirection("apps", "home"))
        assertEquals(-1, tabDirection("plans", "home"))
        assertEquals(0, tabDirection("home", "home"))
        assertEquals(0, tabDirection("home", "settings"))
        assertEquals(0, tabDirection(null, "apps"))
    }

    @Test fun isTabSwitch_needs_two_different_tab_routes() {
        assertTrue(isTabSwitch("apps", "home"))
        assertTrue(isTabSwitch("plans", "apps"))
        assertFalse(isTabSwitch("home", "home"))
        assertFalse(isTabSwitch("home", "settings"))
        assertFalse(isTabSwitch("plans/new", "plans"))
        assertFalse(isTabSwitch(null, "home"))
    }

    @Test fun reduce_motion_returns_none_for_directional_transitions() {
        assertEquals(EnterTransition.None, tabEnterTransition(motion, reduceMotion = true, direction = 1))
        assertEquals(ExitTransition.None, tabExitTransition(motion, reduceMotion = true, direction = -1))
    }

    @Test fun directional_transitions_are_not_the_plain_fade() {
        assertNotEquals(tabEnterTransition(motion, reduceMotion = false, direction = 0), tabEnterTransition(motion, reduceMotion = false, direction = 1))
        assertNotEquals(tabExitTransition(motion, reduceMotion = false, direction = 0), tabExitTransition(motion, reduceMotion = false, direction = -1))
    }
}
