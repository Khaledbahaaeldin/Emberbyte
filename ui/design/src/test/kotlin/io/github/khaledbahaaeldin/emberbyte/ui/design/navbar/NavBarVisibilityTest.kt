package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavBarVisibilityTest {
    @Test fun connection_hides_after_threshold_shows_on_upward_scroll_and_never_consumes() {
        val nav = NavBarVisibility(thresholdPx = 40f)
        assertTrue(nav.visible)
        val first = nav.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)
        assertEquals(Offset.Zero, first)
        assertFalse(nav.visible)
        nav.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)
        assertFalse(nav.visible)
        val up = nav.connection.onPreScroll(Offset(0f, 10f), NestedScrollSource.UserInput)
        assertEquals(Offset.Zero, up)
        assertTrue(nav.visible)
    }

    @Test fun show_restores_visibility_after_being_hidden() {
        val nav = NavBarVisibility(thresholdPx = 40f)
        nav.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)
        assertFalse(nav.visible)
        nav.show()
        assertTrue(nav.visible)
    }
    @Test fun pill_reappears_at_the_end_of_a_list() {
        val nav = NavBarVisibility(thresholdPx = 40f)
        nav.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)
        assertFalse(nav.visible)
        // The list consumed nothing and the scroll still wants to go further down: the list is at its end.
        nav.connection.onPostScroll(Offset.Zero, Offset(0f, -30f), NestedScrollSource.UserInput)
        assertTrue(nav.visible)
    }

    @Test fun pill_stays_hidden_while_the_list_still_scrolls() {
        val nav = NavBarVisibility(thresholdPx = 40f)
        nav.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)
        assertFalse(nav.visible)
        // The list consumed the whole scroll, nothing is left over.
        nav.connection.onPostScroll(Offset(0f, -50f), Offset.Zero, NestedScrollSource.UserInput)
        assertFalse(nav.visible)
    }

    @Test fun reappearing_at_the_end_restarts_the_hide_threshold() {
        val nav = NavBarVisibility(thresholdPx = 40f)
        nav.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)
        nav.connection.onPostScroll(Offset.Zero, Offset(0f, -30f), NestedScrollSource.UserInput)
        assertTrue(nav.visible)
        // A small further push must not hide it again straight away.
        nav.connection.onPreScroll(Offset(0f, -10f), NestedScrollSource.UserInput)
        assertTrue(nav.visible)
    }
}
