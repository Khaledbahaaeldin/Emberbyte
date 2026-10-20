package io.github.khaledbahaaeldin.emberbyte

import android.content.Intent
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DebugClockExtraTest {

    @Before
    fun setUp() {
        AppClock.reset()
    }

    @After
    fun tearDown() {
        AppClock.reset()
    }

    @Test
    fun setting_debug_fake_now_intent_extra_sets_process_clock() {
        val targetEpochMillis = 1792065600000L // 2026-10-15T12:00:00Z
        val intent = Intent().apply {
            putExtra("debug_fake_now", targetEpochMillis)
        }

        Robolectric.buildActivity(MainActivity::class.java, intent).setup()

        assertEquals(Instant.ofEpochMilli(targetEpochMillis), AppClock.instant())
    }

    @Test
    fun missing_intent_extra_keeps_default_clock() {
        val intent = Intent()
        Robolectric.buildActivity(MainActivity::class.java, intent).setup()

        assertNull(AppClock.fakeNow)
    }
}
