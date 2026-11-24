package io.github.khaledbahaaeldin.emberbyte.nav

import android.app.Application
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BatteryPromptTest {
    @Test
    fun the_fix_button_shows_the_stop_battery_optimisation_prompt_for_this_app() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.requestIgnoreBatteryOptimizations()

        val started = shadowOf(app).nextStartedActivity
        assertNotNull("an activity must be started", started)
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, started.action)
        assertEquals("package:${app.packageName}", started.dataString)
    }
}
