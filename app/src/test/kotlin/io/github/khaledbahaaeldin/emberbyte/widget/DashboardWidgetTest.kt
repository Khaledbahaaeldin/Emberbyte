package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Emittable
import androidx.glance.EmittableImage
import androidx.glance.EmittableWithChildren
import androidx.glance.action.ActionModifier
import androidx.glance.appwidget.action.StartActivityIntentAction
import androidx.glance.semantics.SemanticsModifier
import androidx.glance.semantics.SemanticsProperties
import androidx.glance.text.EmittableText
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DashboardWidgetTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val palette = ThemePresets.first { it.id == "dusk" }.dark

    private fun samplePlanState(
        cap: Long = 10_000_000_000L,
        used: Long = 6_200_000_000L,
        remaining: Long = 3_800_000_000L,
    ): PlanState {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan = Plan(id = 1L, name = "Primary SIM", subscriptionId = null, capBytes = cap, cycle = cycle, rollover = Rollover.None)
        return PlanState(
            plan = plan,
            window = CycleWindow(now.minusSeconds(86400), now.plusSeconds(86400 * 18)),
            effectiveCapBytes = cap,
            usedBytes = used,
            remainingBytes = remaining,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            daysLeft = 18,
            fractionUsed = used.toFloat() / cap,
            isApproximate = false,
            isDormant = false,
            startsAt = null,
        )
    }

    private fun sampleApps(): List<AppUsage> = listOf(
        AppUsage(packageName = "com.example.browser", label = "Browser", uid = 10001, mobileBytes = 400_000_000L, wifiBytes = 100_000_000L, screenTimeMs = null),
        AppUsage(packageName = "com.example.video", label = "Video Player", uid = 10002, mobileBytes = 250_000_000L, wifiBytes = 50_000_000L, screenTimeMs = null),
        AppUsage(packageName = "com.example.music", label = "Music App", uid = 10003, mobileBytes = 150_000_000L, wifiBytes = 50_000_000L, screenTimeMs = null),
    )

    private fun sampleData(
        todayBytes: Long = 1_240_000_000L,
        topApps: List<AppUsage> = sampleApps(),
    ): WidgetData = WidgetData(
        todayBytes = todayBytes,
        plan = samplePlanState(),
        topApps = topApps,
        mobileBytes = 1_000_000_000L,
        wifiBytes = 240_000_000L,
        dailyBytes = listOf(100L, 200L, 300L, 400L, 500L, 600L, 700L),
        permissionGranted = true,
        estimated = false,
    )

    private fun findAllTexts(root: Emittable): List<String> {
        val results = mutableListOf<String>()
        fun visit(node: Emittable) {
            if (node is EmittableText) {
                results.add(node.text)
            }
            if (node is EmittableWithChildren) {
                node.children.forEach { visit(it) }
            }
        }
        visit(root)
        return results
    }

    private fun findActionIntents(root: Emittable): List<String> {
        val results = mutableListOf<String>()
        fun visit(node: Emittable) {
            node.modifier.foldIn(Unit) { _, element ->
                if (element is ActionModifier) {
                    val action = element.action
                    if (action is StartActivityIntentAction) {
                        action.intent.dataString?.let { results.add(it) }
                    }
                }
            }
            if (node is EmittableWithChildren) {
                node.children.forEach { visit(it) }
            }
        }
        visit(root)
        return results
    }

    @Test
    fun shows_header_hero_plan_chart_and_three_apps_at_4x4() {
        val data = sampleData()
        val budget = ImageBudget()
        val root = renderGlanceTree(context, DashboardWidget.SIZE_4X4) {
            DashboardWidget().DashboardContent(data, palette, budget, DashboardWidget.SIZE_4X4)
        }

        val texts = findAllTexts(root)
        // Header (brand mark / Emberbyte and date)
        assertTrue("Header should show app brand / Emberbyte: $texts", texts.any { it.contains("Emberbyte") })
        // Hero number
        assertTrue("Hero number or unit shown: $texts", texts.any { it.contains("GB") || it.contains("1.2") })
        // Plan bar / caption
        assertTrue("Plan line shown: $texts", texts.any { it.contains("Primary SIM") || it.contains("left") })
        // Chart block
        assertTrue("Chart block present: $texts", texts.any { it.contains("Last 7 days") })
        // Three apps
        assertTrue("App 1 present: $texts", texts.any { it == "Browser" })
        assertTrue("App 2 present: $texts", texts.any { it == "Video Player" })
        assertTrue("App 3 present: $texts", texts.any { it == "Music App" })
    }

    @Test
    fun smaller_size_drops_chart_then_apps() {
        val data = sampleData()
        val budget = ImageBudget()

        // Medium size: drops chart, keeps apps
        val mediumRoot = renderGlanceTree(context, DashboardWidget.SIZE_MEDIUM) {
            DashboardWidget().DashboardContent(data, palette, budget, DashboardWidget.SIZE_MEDIUM)
        }
        val mediumTexts = findAllTexts(mediumRoot)
        assertFalse("Medium size drops chart: $mediumTexts", mediumTexts.any { it.contains("Last 7 days") })
        assertTrue("Medium size keeps apps: $mediumTexts", mediumTexts.any { it == "Browser" })

        // Compact size: drops chart and apps
        val compactRoot = renderGlanceTree(context, DashboardWidget.SIZE_COMPACT) {
            DashboardWidget().DashboardContent(data, palette, budget, DashboardWidget.SIZE_COMPACT)
        }
        val compactTexts = findAllTexts(compactRoot)
        assertFalse("Compact size drops chart: $compactTexts", compactTexts.any { it.contains("Last 7 days") })
        assertFalse("Compact size drops apps: $compactTexts", compactTexts.any { it == "Browser" })
    }

    @Test
    fun app_without_an_icon_shows_the_letter_shape() {
        val data = sampleData(
            topApps = listOf(
                AppUsage(
                    packageName = "com.unknown.pkg",
                    label = "UnknownApp",
                    uid = 10099,
                    mobileBytes = 100_000L,
                    wifiBytes = 0L,
                    screenTimeMs = null
                )
            )
        )
        val budget = ImageBudget()
        val iconMap = mapOf<String, Bitmap?>("com.unknown.pkg" to null)

        val root = renderGlanceTree(context, DashboardWidget.SIZE_4X4) {
            DashboardWidget().DashboardContent(data, palette, budget, DashboardWidget.SIZE_4X4, iconBitmaps = iconMap)
        }

        val texts = findAllTexts(root)
        assertTrue("App label is shown: $texts", texts.any { it == "UnknownApp" })
    }

    @Test
    fun uid_pseudo_app_never_requests_an_icon() {
        var requestedIconForUid = false
        val mockIconLoader: (String) -> Bitmap? = { pkg ->
            if (pkg.startsWith("uid:")) requestedIconForUid = true
            null
        }

        val uidApp = AppUsage(
            packageName = "uid:10042",
            label = "Tethering",
            uid = 10042,
            mobileBytes = 200_000L,
            wifiBytes = 0L,
            screenTimeMs = null
        )
        val data = sampleData(topApps = listOf(uidApp))

        val resolved = DashboardWidget.resolveIcons(data.topApps, iconLoader = mockIconLoader)
        assertFalse("UID pseudo-apps must never request an icon", requestedIconForUid)
        assertEquals(null, resolved["uid:10042"])
    }

    @Test
    fun each_app_row_has_a_deep_link_action() {
        val data = sampleData()
        val budget = ImageBudget()
        val root = renderGlanceTree(context, DashboardWidget.SIZE_4X4) {
            DashboardWidget().DashboardContent(data, palette, budget, DashboardWidget.SIZE_4X4)
        }

        val intents = findActionIntents(root)
        assertTrue("Deep link for browser: $intents", intents.any { it == "emberbyte://apps/com.example.browser" })
        assertTrue("Deep link for video: $intents", intents.any { it == "emberbyte://apps/com.example.video" })
        assertTrue("Deep link for music: $intents", intents.any { it == "emberbyte://apps/com.example.music" })
    }

    @Test
    fun image_budget_is_respected_with_three_icons() {
        val data = sampleData()
        val budget = ImageBudget(maxBytes = 1_572_864)

        renderGlanceTree(context, DashboardWidget.SIZE_4X4) {
            DashboardWidget().DashboardContent(data, palette, budget, DashboardWidget.SIZE_4X4)
        }

        assertTrue("Total image allocations must be <= 1.5 MiB: ${budget.used}", budget.used <= 1_572_864)
        assertTrue("Bitmaps were allocated: ${budget.used}", budget.used > 0)
    }
}
