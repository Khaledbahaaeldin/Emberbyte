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

    @Test
    fun sampler_service_passes_today_bytes_to_live_progress() {
        val samplerSource = java.io.File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/sampler/SamplerService.kt").readText()
        assertTrue(
            "SamplerService must pass today.totalBytes to liveProgressOf",
            samplerSource.contains("liveProgressOf(active, units, today.totalBytes)")
        )
        assertFalse(
            "SamplerService must not use 2-arg liveProgressOf without todayBytes",
            samplerSource.contains("liveProgressOf(active, units)")
        )
    }

    @Test
    fun sampler_service_path_wires_chip_text_for_ok_warning_and_exceeded() {
        val todayBytes = 1_400_000_000L // 1.4 GB
        val units = io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits.DECIMAL

        val okPlan = samplePlanState(remainingBytes = 8_000_000_000L, fractionUsed = 0.20f)
        val okProgress = liveProgressOf(okPlan, units, todayBytes)
        assertNotNull(okProgress)
        assertEquals("1.4 GB", okProgress?.chip)

        val warnPlan = samplePlanState(remainingBytes = 1_200_000_000L, fractionUsed = 0.88f)
        val warnProgress = liveProgressOf(warnPlan, units, todayBytes)
        assertNotNull(warnProgress)
        assertEquals("!1.2 GB", warnProgress?.chip)

        val exceededPlan = samplePlanState(remainingBytes = 0L, fractionUsed = 1.0f)
        val exceededProgress = liveProgressOf(exceededPlan, units, todayBytes)
        assertNotNull(exceededProgress)
        assertEquals("Over", exceededProgress?.chip)
    }

    private fun samplePlanState(
        remainingBytes: Long,
        fractionUsed: Float,
    ) = io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState(
        plan = io.github.khaledbahaaeldin.emberbyte.engine.model.Plan(
            id = 1L,
            name = "Primary",
            subscriptionId = null,
            capBytes = 10_000_000_000L,
            cycle = io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle.MonthlyOnDay(1, java.time.LocalTime.MIDNIGHT, java.time.ZoneId.of("UTC")),
            rollover = io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover.None,
        ),
        window = io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow(java.time.Instant.EPOCH, java.time.Instant.EPOCH.plusSeconds(86400 * 30)),
        effectiveCapBytes = 10_000_000_000L,
        usedBytes = (10_000_000_000L * fractionUsed).toLong(),
        remainingBytes = remainingBytes,
        rolledOverBytes = 0L,
        addOnBytes = 0L,
        freeBytes = 0L,
        daysLeft = 10,
        fractionUsed = fractionUsed,
        isApproximate = false,
    )
}
