package io.github.khaledbahaaeldin.emberbyte.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingMotionTest {
    private val sampleRects = listOf(
        LayerRect(24f, 40f, 72f, 72f),
        LayerRect(24f, 40f, 360f, 220f),
        LayerRect(96f, 40f, 216f, 216f),
        LayerRect(24f, 140f, 360f, 160f),
    )

    @Test
    fun next_action_is_usage_then_notifications_then_done() {
        assertEquals(PermissionAction.Usage, nextPermissionAction(usageAccess = false, notifications = false))
        assertEquals(PermissionAction.Notifications, nextPermissionAction(usageAccess = true, notifications = false))
        assertEquals(PermissionAction.Done, nextPermissionAction(usageAccess = true, notifications = true))
    }

    @Test
    fun next_action_skips_notifications_when_already_allowed() {
        assertEquals(PermissionAction.Usage, nextPermissionAction(usageAccess = false, notifications = true))
        assertEquals(PermissionAction.Done, nextPermissionAction(usageAccess = true, notifications = true))
    }

    @Test
    fun cta_label_follows_the_permission_state() {
        assertEquals("Get started", ctaLabel(0, usageAccess = false, notifications = false))
        assertEquals("Continue", ctaLabel(1, usageAccess = false, notifications = false))
        assertEquals("Continue", ctaLabel(2, usageAccess = false, notifications = false))
        assertEquals("Allow usage access", ctaLabel(3, usageAccess = false, notifications = false))
        assertEquals("Allow notifications", ctaLabel(3, usageAccess = true, notifications = false))
        assertEquals("Done", ctaLabel(3, usageAccess = true, notifications = true))
    }

    @Test
    fun skip_label_changes_from_step_four() {
        assertEquals("Skip intro", skipLabel(0f))
        assertEquals("Skip intro", skipLabel(2.4f))
        assertEquals("Skip for now", skipLabel(2.5f))
        assertEquals("Skip for now", skipLabel(3f))
    }

    @Test
    fun back_alpha_is_zero_on_step_one() {
        assertEquals(0f, backAlpha(0f), 0.001f)
        assertEquals(0.5f, backAlpha(0.25f), 0.001f)
        assertEquals(1f, backAlpha(0.5f), 0.001f)
        assertEquals(1f, backAlpha(3f), 0.001f)
    }

    @Test
    fun pill_width_stretches_between_steps() {
        assertEquals(28f, pillWidthDp(0, 0f), 0.01f)
        assertEquals(8f, pillWidthDp(1, 0f), 0.01f)
        assertEquals(18f, pillWidthDp(0, 0.5f), 0.01f)
        assertEquals(18f, pillWidthDp(1, 0.5f), 0.01f)
    }

    @Test
    fun parallax_outgoing_is_thirty_percent() {
        // Negative d (outgoing left) or positive d when outgoing
        assertEquals(-30f, parallaxOffset(d = -0.1f, widthPx = 1000f, isOutgoing = true), 0.01f)
        assertEquals(100f, parallaxOffset(d = 0.1f, widthPx = 1000f, isOutgoing = false), 0.01f)
    }

    @Test
    fun layer_state_interpolates_between_the_slots() {
        val mid = layerState(position = 0.5f, rects = sampleRects)
        assertEquals(216f, mid.rect.w, 0.1f)
        assertEquals(146f, mid.rect.h, 0.1f)
        assertEquals(0.5f, mid.primaryMix, 0.01f)
        assertTrue(mid.visible)
    }

    @Test
    fun layer_state_is_hidden_at_rest_on_step_four() {
        val rest = layerState(position = 3.0f, rects = sampleRects)
        assertFalse(rest.visible)
        val almost = layerState(position = 2.9995f, rects = sampleRects)
        assertFalse(almost.visible)
    }

    @Test
    fun layer_content_alphas_cross_fade_at_the_thresholds() {
        val atZero = layerState(0f, sampleRects)
        assertEquals(1f, atZero.alphaMark, 0.01f)
        assertEquals(0f, atZero.alphaCard, 0.01f)

        val at035 = layerState(0.35f, sampleRects)
        assertEquals(0f, at035.alphaMark, 0.01f)

        val at085 = layerState(0.85f, sampleRects)
        assertEquals(1f, at085.alphaCard, 0.01f)

        val at190 = layerState(1.9f, sampleRects)
        assertEquals(1f, at190.alphaDisc, 0.01f)

        val at290 = layerState(2.9f, sampleRects)
        assertEquals(1f, at290.alphaPermission, 0.01f)
    }

    @Test
    fun reveal_fraction_is_one_at_rest() {
        for (j in 0..4) {
            assertEquals(1f, revealFraction(q = 1f, index = j), 0.001f)
        }
        assertEquals(0f, revealFraction(q = 0.1f, index = 0), 0.001f)
    }
}
