package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AllWidgetsBudgetTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val palette = ThemePresets.first { it.id == "dusk" }.dark

    private fun samplePlanState(): PlanState {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan = Plan(id = 1L, name = "Primary SIM", subscriptionId = null, capBytes = 10_000_000_000L, cycle = cycle, rollover = Rollover.None)
        return PlanState(
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
            startsAt = null,
        )
    }

    private fun fullData(): WidgetData = WidgetData(
        todayBytes = 1_240_000_000L,
        plan = samplePlanState(),
        topApps = listOf(
            AppUsage("com.example.browser", "Browser", 10001, 500_000_000L, 100_000_000L, null),
            AppUsage("com.example.video", "Video Player", 10002, 300_000_000L, 50_000_000L, null),
            AppUsage("com.example.music", "Music App", 10003, 200_000_000L, 50_000_000L, null),
        ),
        mobileBytes = 1_000_000_000L,
        wifiBytes = 240_000_000L,
        dailyBytes = listOf(100L, 200L, 300L, 400L, 500L, 600L, 700L),
        permissionGranted = true,
        estimated = false,
    )

    @Test
    fun each_widget_at_its_largest_size_stays_under_1_5_MiB_of_bitmaps() {
        val data = fullData()
        val maxBudget = 1_572_864 // 1.5 MiB

        // 1. TodayWidget at 4x2
        val todayBudget = ImageBudget(maxBytes = maxBudget)
        renderGlanceTree(context, DpSize(280.dp, 150.dp)) {
            TodayWidget().TodayContent(data, palette, todayBudget, DpSize(280.dp, 150.dp))
        }
        assertTrue("TodayWidget must stay <= 1.5 MiB (was ${todayBudget.used})", todayBudget.used <= maxBudget)

        // 2. HeroWidget at 4x2
        val heroBudget = ImageBudget(maxBytes = maxBudget)
        renderGlanceTree(context, DpSize(280.dp, 150.dp)) {
            HeroWidget().HeroContent(data, palette, heroBudget, DpSize(280.dp, 150.dp))
        }
        assertTrue("HeroWidget must stay <= 1.5 MiB (was ${heroBudget.used})", heroBudget.used <= maxBudget)

        // 3. PlanWidget at 4x2
        val planBudget = ImageBudget(maxBytes = maxBudget)
        renderGlanceTree(context, DpSize(280.dp, 150.dp)) {
            PlanWidget().PlanContent(data, palette, planBudget, DpSize(280.dp, 150.dp))
        }
        assertTrue("PlanWidget must stay <= 1.5 MiB (was ${planBudget.used})", planBudget.used <= maxBudget)

        // 4. BentoWidget at 4x2
        val bentoBudget = ImageBudget(maxBytes = maxBudget)
        renderGlanceTree(context, DpSize(300.dp, 150.dp)) {
            BentoWidget().BentoContent(data, palette, bentoBudget, DpSize(300.dp, 150.dp))
        }
        assertTrue("BentoWidget must stay <= 1.5 MiB (was ${bentoBudget.used})", bentoBudget.used <= maxBudget)

        // 5. DashboardWidget at 4x4
        val dashBudget = ImageBudget(maxBytes = maxBudget)
        renderGlanceTree(context, DpSize(300.dp, 300.dp)) {
            DashboardWidget().DashboardContent(data, palette, dashBudget, DpSize(300.dp, 300.dp))
        }
        assertTrue("DashboardWidget must stay <= 1.5 MiB (was ${dashBudget.used})", dashBudget.used <= maxBudget)
    }
}
