package io.github.khaledbahaaeldin.emberbyte.sampler

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PromotedGateTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun api_35_does_not_support_promotion() {
        val gate = PromotedGate(
            context = context,
            sdkInt = { 35 },
            sdkIntFull = { 0 },
        )
        assertFalse(gate.supportsPromotion())
    }

    @Test
    fun api_36_0_supports_promotion_through_the_extra() {
        val gate = PromotedGate(
            context = context,
            sdkInt = { 36 },
            sdkIntFull = { 3600000 },
        )
        assertTrue(gate.supportsPromotion())
        assertTrue(gate.usesExtraForPromotion())
    }

    @Test
    fun api_36_1_supports_promotion_through_the_builder() {
        val gate = PromotedGate(
            context = context,
            sdkInt = { 36 },
            sdkIntFull = { 3600001 },
        )
        assertTrue(gate.supportsPromotion())
        assertFalse(gate.usesExtraForPromotion())
    }

    @Test
    fun mayPost_follows_canPostPromotedNotifications() {
        val gate = PromotedGate(
            context = context,
            sdkInt = { 36 },
            sdkIntFull = { 3600001 },
            canPostProvider = { true },
        )
        assertTrue(gate.mayPost())

        val gateBlocked = PromotedGate(
            context = context,
            sdkInt = { 36 },
            sdkIntFull = { 3600001 },
            canPostProvider = { false },
        )
        assertFalse(gateBlocked.mayPost())

        val gateBelow36 = PromotedGate(
            context = context,
            sdkInt = { 35 },
            sdkIntFull = { 0 },
            canPostProvider = { true },
        )
        assertFalse(gateBelow36.mayPost())
    }

    @Test
    fun settings_intent_targets_this_package() {
        val gate36 = PromotedGate(context = context, sdkInt = { 36 }, sdkIntFull = { 3600000 })
        val intent36 = gate36.settingsIntent()
        assertEquals(PromotedGate.ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS, intent36.action)
        assertEquals("package:${context.packageName}", intent36.dataString)

        val gate35 = PromotedGate(context = context, sdkInt = { 35 }, sdkIntFull = { 0 })
        val intent35 = gate35.settingsIntent()
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent35.action)
        assertEquals(context.packageName, intent35.getStringExtra(Settings.EXTRA_APP_PACKAGE))
    }
}
