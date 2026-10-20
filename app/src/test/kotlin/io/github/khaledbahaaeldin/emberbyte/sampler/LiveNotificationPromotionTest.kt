package io.github.khaledbahaaeldin.emberbyte.sampler

import android.app.Application
import android.app.Notification
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LiveNotificationPromotionTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext<Application>()

    private class RecordingCaller : PlatformNotificationCaller {
        var requestedPromoted: Boolean? = null
        var shortCriticalText: String? = null
        var progressStyleCalled = false
        var color: Int = 0

        override fun setProgressStyle(builder: Notification.Builder, fractionUsed: Float, color: Int): Notification.Builder {
            progressStyleCalled = true
            this.color = color
            return builder
        }

        override fun setShortCriticalText(builder: Notification.Builder, chip: String): Notification.Builder {
            shortCriticalText = chip
            return builder
        }

        override fun setRequestPromotedOngoing(builder: Notification.Builder, request: Boolean): Notification.Builder {
            requestedPromoted = request
            return builder
        }
    }

    @Test
    fun api_36_0_sets_the_extra_and_not_the_builder_flag() {
        val caller = RecordingCaller()
        val builder = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600000,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600000 }, canPostProvider = { true }),
            settingsSupplier = { Settings(liveNotificationEnabled = true) },
            promotedDismissedOnSupplier = { null },
        )
        val notification = builder.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )
        assertEquals(null, caller.requestedPromoted)
        assertTrue(notification.extras.getBoolean("android.requestPromotedOngoing", false))
    }

    @Test
    fun api_36_1_sets_the_builder_flag() {
        val caller = RecordingCaller()
        val builder = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { true }),
            settingsSupplier = { Settings(liveNotificationEnabled = true) },
            promotedDismissedOnSupplier = { null },
        )
        val notification = builder.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )
        assertEquals(true, caller.requestedPromoted)
        assertFalse(notification.extras.containsKey("android.requestPromotedOngoing"))
    }

    @Test
    fun no_promotion_when_the_user_switch_is_off() {
        val caller = RecordingCaller()
        val builder = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { true }),
            settingsSupplier = { Settings(liveNotificationEnabled = false) },
            promotedDismissedOnSupplier = { null },
        )
        val notification = builder.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )
        assertNull(caller.requestedPromoted)
        assertFalse(notification.extras.getBoolean("android.requestPromotedOngoing", false))
    }

    @Test
    fun no_promotion_when_not_allowed_by_the_system() {
        val caller = RecordingCaller()
        val builder = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { false }),
            settingsSupplier = { Settings(liveNotificationEnabled = true) },
            promotedDismissedOnSupplier = { null },
        )
        val notification = builder.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )
        assertNull(caller.requestedPromoted)
        assertFalse(notification.extras.getBoolean("android.requestPromotedOngoing", false))
    }

    @Test
    fun no_promotion_after_todays_dismissal_and_promotion_again_the_next_day() {
        val caller = RecordingCaller()
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        val builderDismissedToday = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { true }),
            settingsSupplier = { Settings(liveNotificationEnabled = true) },
            promotedDismissedOnSupplier = { today },
        )
        val notif1 = builderDismissedToday.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )
        assertNull(caller.requestedPromoted)

        val callerNextDay = RecordingCaller()
        val builderNextDay = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = callerNextDay,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { true }),
            settingsSupplier = { Settings(liveNotificationEnabled = true) },
            promotedDismissedOnSupplier = { yesterday },
        )
        val notif2 = builderNextDay.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )
        assertEquals(true, callerNextDay.requestedPromoted)
    }

    @Test
    fun built_notification_meets_every_eligibility_rule() {
        val caller = RecordingCaller()
        val builder = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { true }),
            settingsSupplier = { Settings(liveNotificationEnabled = true) },
            promotedDismissedOnSupplier = { null },
        )
        val notification = builder.build(
            LiveNotificationText(
                title = "1.4 GB today",
                text = "1.2 GB left",
                progress = LiveProgress(0.5f, "1.4 GB"),
            )
        )

        // 1. Ongoing
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        // 2. Title present
        assertNotNull(notification.extras.getCharSequence(Notification.EXTRA_TITLE))
        // 3. No custom views
        assertNull(notification.contentView)
        assertNull(notification.bigContentView)
        assertNull(notification.headsUpContentView)
        // 4. Not colorized
        assertFalse(notification.extras.getBoolean(Notification.EXTRA_COLORIZED, false))
        // 5. Channel importance above min
        assertEquals(NotificationChannels.LIVE, notification.channelId)
        // 6. Not group summary
        assertEquals(0, notification.flags and Notification.FLAG_GROUP_SUMMARY)
        // 7. Short critical text present
        assertEquals("1.4 GB", caller.shortCriticalText)
        // 8. Delete intent present
        assertNotNull(notification.deleteIntent)
    }

    @Test
    fun accent_colour_still_follows_the_theme_state() {
        val caller = RecordingCaller()
        val dusk = ThemePresets.first { it.id == "dusk" }.dark
        val builder = LiveNotificationBuilder(
            context = context,
            sdkInt = 36,
            sdkIntFull = 3600001,
            platformCaller = caller,
            promotedGate = PromotedGate(context, { 36 }, { 3600001 }, canPostProvider = { true }),
            settingsSupplier = { Settings(themeId = "dusk", liveNotificationEnabled = true) },
            isNight = { true },
            promotedDismissedOnSupplier = { null },
        )
        val nOk = builder.build(LiveNotificationText("1.4 GB today", "ok", progress = LiveProgress(0.1f, "1.4 GB"), state = BudgetStateUi.Ok))
        assertEquals(dusk.primary.toArgb(), caller.color)

        val nWarn = builder.build(LiveNotificationText("1.4 GB today", "warn", progress = LiveProgress(0.85f, "!1.2 GB"), state = BudgetStateUi.Warning))
        assertEquals(dusk.warn.toArgb(), caller.color)

        val nDanger = builder.build(LiveNotificationText("1.4 GB today", "over", progress = LiveProgress(1.0f, "Over"), state = BudgetStateUi.Exceeded))
        assertEquals(dusk.danger.toArgb(), caller.color)
    }
}
