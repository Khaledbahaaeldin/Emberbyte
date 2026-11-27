package io.github.khaledbahaaeldin.emberbyte.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingMotionTest {
    @Test
    fun there_are_three_pages() {
        assertEquals(3, ONBOARDING_PAGE_COUNT)
    }

    @Test
    fun the_permission_order_is_usage_notifications_phone_battery() {
        assertEquals(PermissionAction.Usage, nextPermissionAction(false, false, false, false))
        assertEquals(PermissionAction.Notifications, nextPermissionAction(true, false, false, false))
        assertEquals(PermissionAction.Phone, nextPermissionAction(true, true, false, false))
        assertEquals(PermissionAction.Battery, nextPermissionAction(true, true, true, false))
        assertEquals(PermissionAction.Done, nextPermissionAction(true, true, true, true))
    }

    @Test
    fun page_one_always_says_get_started() {
        assertEquals(OnboardingCta.Start, ctaFor(0, false, false, false, false))
        assertEquals(OnboardingCta.Start, ctaFor(0, true, true, true, true, hasPlan = true))
    }

    @Test
    fun the_access_button_names_the_next_permission_then_continues() {
        assertEquals(OnboardingCta.Usage, ctaFor(1, false, false, false, false))
        assertEquals(OnboardingCta.Notifications, ctaFor(1, true, false, false, false))
        assertEquals(OnboardingCta.Phone, ctaFor(1, true, true, false, false))
        assertEquals(OnboardingCta.Battery, ctaFor(1, true, true, true, false))
        assertEquals(OnboardingCta.Continue, ctaFor(1, true, true, true, true))
    }

    @Test
    fun the_plan_page_offers_set_up_until_a_plan_exists() {
        assertEquals(OnboardingCta.SetUpPlan, ctaFor(2, true, true, true, true, hasPlan = false))
        assertEquals(OnboardingCta.Done, ctaFor(2, true, true, true, true, hasPlan = true))
    }

    @Test
    fun the_quiet_button_exists_only_where_something_can_still_be_skipped() {
        assertNull(quietFor(0, allPermissionsDone = false, hasPlan = false))
        assertEquals(OnboardingQuiet.NotNow, quietFor(1, allPermissionsDone = false, hasPlan = false))
        assertNull(quietFor(1, allPermissionsDone = true, hasPlan = false))
        assertEquals(OnboardingQuiet.NotNowHome, quietFor(2, allPermissionsDone = true, hasPlan = false))
        assertNull(quietFor(2, allPermissionsDone = true, hasPlan = true))
    }

    @Test
    fun skip_shows_on_the_first_two_pages_only() {
        assertTrue(showsSkip(0))
        assertTrue(showsSkip(1))
        assertFalse(showsSkip(2))
    }

    @Test
    fun the_active_pill_is_28dp_wide_and_the_others_8dp() {
        assertEquals(28f, pillWidthDp(1, 1f), 0.001f)
        assertEquals(8f, pillWidthDp(0, 1f), 0.001f)
        assertEquals(8f, pillWidthDp(2, 1f), 0.001f)
        assertEquals(18f, pillWidthDp(0, 0.5f), 0.001f)
    }

    @Test
    fun the_outgoing_page_travels_at_30_percent() {
        assertEquals(-0.3f * 1000f, parallaxOffset(-1f, 1000f, isOutgoing = true), 0.001f)
        assertEquals(1000f, parallaxOffset(1f, 1000f, isOutgoing = false), 0.001f)
    }

    @Test
    fun a_page_fades_out_before_it_leaves_and_in_late() {
        assertEquals(1f, pageAlpha(0f), 0.001f)
        assertEquals(0f, pageAlpha(1f), 0.001f)
        assertEquals(0f, revealFraction(0.2f, 0), 0.001f)
        assertEquals(1f, revealFraction(1f, 2), 0.001f)
        assertTrue(revealFraction(0.5f, 0) in 0f..1f)
    }

    @Test
    fun only_three_pieces_are_staggered_per_page() {
        // Welcome, Access and Plan reveal indices 0..2 only: a fourth would still be hidden at q = 0.9.
        assertTrue(revealFraction(1f, 2) > 0.99f)
    }
}
