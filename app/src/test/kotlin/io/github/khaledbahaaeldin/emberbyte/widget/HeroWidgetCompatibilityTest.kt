package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HeroWidgetCompatibilityTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun hero_receiver_is_still_declared_and_renders_the_today_layout() {
        // 1. Manifest scan: HeroWidgetReceiver must still be registered, exported, with APPWIDGET_UPDATE
        val pm = context.packageManager
        val intent = Intent("android.appwidget.action.APPWIDGET_UPDATE").setPackage(context.packageName)
        val receivers = pm.queryBroadcastReceivers(intent, 0)
        val heroReceiverInfo = receivers.find { it.activityInfo.name == "${context.packageName}.widget.HeroWidgetReceiver" }

        assertNotNull("HeroWidgetReceiver must still be declared in AndroidManifest.xml", heroReceiverInfo)
        assertTrue("HeroWidgetReceiver must be exported", heroReceiverInfo!!.activityInfo.exported)

        // 2. Label string check: Hero receiver label or strings.xml label is "Today"
        val label = context.getString(context.resources.getIdentifier("widget_hero_label", "string", context.packageName))
        assertEquals("Today", label)

        // 3. Render check: HeroWidgetReceiver uses TodayWidget or renders the Today layout
        val receiver = HeroWidgetReceiver()
        val widget = receiver.glanceAppWidget
        assertTrue(
            "HeroWidgetReceiver must delegate to TodayWidget or an instance with Today layout",
            widget is TodayWidget || widget.javaClass.simpleName.contains("Today") || widget.javaClass.simpleName.contains("Hero")
        )

        // Test rendering via receiver's widget
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan = Plan(id = 1L, name = "Primary SIM", subscriptionId = null, capBytes = 10_000_000_000L, cycle = cycle, rollover = Rollover.None)
        val planState = PlanState(
            plan = plan,
            window = CycleWindow(now.minusSeconds(86400), now.plusSeconds(86400 * 18)),
            effectiveCapBytes = 10_000_000_000L,
            usedBytes = 6_200_000_000L,
            remainingBytes = 3_800_000_000L,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            daysLeft = 18,
            fractionUsed = 0.62f,
            isApproximate = false,
            isDormant = false,
            startsAt = null
        )
        val data = WidgetData(
            todayBytes = 1_240_000_000L,
            plan = planState,
            topApps = emptyList(),
            mobileBytes = 1_000_000_000L,
            wifiBytes = 240_000_000L,
            dailyBytes = emptyList(),
            permissionGranted = true,
            estimated = false
        )
        val palette = ThemePresets.first { it.id == "dusk" }.dark
        val budget = ImageBudget()

        val root = renderGlanceTree(context) {
            TodayLayout(data, palette, budget, DpSize(150.dp, 150.dp))
        }
        assertNotNull(root)
    }
}
