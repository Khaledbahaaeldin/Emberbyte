package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
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

import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TodayWidgetTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val palette = ThemePresets.first { it.id == "dusk" }.dark

    private fun samplePlanState(
        cap: Long = 10_000_000_000L,
        used: Long = 6_200_000_000L,
        remaining: Long = 3_800_000_000L
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
            startsAt = null
        )
    }

    private fun sampleData(
        todayBytes: Long = 1_240_000_000L,
        plan: PlanState? = samplePlanState(),
        permissionGranted: Boolean = true
    ): WidgetData = WidgetData(
        todayBytes = todayBytes,
        plan = plan,
        topApps = emptyList(),
        mobileBytes = 1_000_000_000L,
        wifiBytes = 240_000_000L,
        dailyBytes = emptyList(),
        permissionGranted = permissionGranted,
        estimated = false
    )

    private fun collectEmittables(root: Emittable): List<Emittable> {
        val list = mutableListOf<Emittable>()
        fun dfs(node: Emittable) {
            list.add(node)
            if (node is EmittableWithChildren) {
                for (child in node.children) {
                    dfs(child)
                }
            }
        }
        dfs(root)
        return list
    }

    private fun findAction(emittable: Emittable): StartActivityIntentAction? {
        var action: StartActivityIntentAction? = null
        emittable.modifier.foldIn(Unit) { _, element ->
            if (element is ActionModifier && element.action is StartActivityIntentAction) {
                action = element.action as StartActivityIntentAction
            }
        }
        return action
    }

    private fun findContentDescription(emittable: Emittable): String? {
        var desc: String? = null
        emittable.modifier.foldIn(Unit) { _, element ->
            if (element is SemanticsModifier) {
                desc = element.configuration.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
            }
        }
        return desc
    }

    @Test
    fun renders_the_number_the_unit_and_the_plan_line() {
        val widget = TodayWidget()
        val data = sampleData(todayBytes = 1_240_000_000L, plan = samplePlanState(remaining = 3_800_000_000L))
        val budget = ImageBudget()

        // 2x2 standard size
        val root = renderGlanceTree(context) {
            widget.TodayContent(data, palette, budget, DpSize(150.dp, 150.dp))
        }
        val nodes = collectEmittables(root)

        // Verifies hero number bitmap and unit or fallback
        val images = nodes.filterIsInstance<EmittableImage>()
        assertTrue("Should render hero number bitmap", images.isNotEmpty())
        val heroImageDesc = images.mapNotNull { findContentDescription(it) }
        assertTrue("Hero image should describe number and unit", heroImageDesc.any { it.contains("1.24") && it.contains("GB") })

        // Verifies plan caption line
        val texts = nodes.filterIsInstance<EmittableText>()
        val planLine = texts.find { it.text.contains("3.80 GB left") }
        assertNotNull("Plan line '3.80 GB left' should be present", planLine)

        // Tap opens Home (emberbyte://home)
        val clickAction = nodes.mapNotNull { findAction(it) }.firstOrNull()
        assertNotNull("Root or block must have click action", clickAction)
        assertEquals("emberbyte://home", clickAction?.intent?.dataString)
    }

    @Test
    fun compact_size_shows_only_the_number() {
        val widget = TodayWidget()
        val data = sampleData(todayBytes = 1_240_000_000L, plan = samplePlanState(remaining = 3_800_000_000L))
        val budget = ImageBudget()

        // Compact 2x1 size: height is small (e.g. 70.dp or height < 110.dp)
        val root = renderGlanceTree(context) {
            widget.TodayContent(data, palette, budget, DpSize(150.dp, 70.dp))
        }
        val nodes = collectEmittables(root)

        // Verifies hero number/unit is rendered
        val images = nodes.filterIsInstance<EmittableImage>()
        val heroImageDesc = images.mapNotNull { findContentDescription(it) }
        assertTrue("Hero image should be present in compact mode", heroImageDesc.any { it.contains("1.24") && it.contains("GB") })

        // Compact size shows only the number: plan line must not be present
        val texts = nodes.filterIsInstance<EmittableText>()
        val planLine = texts.find { it.text.contains("left") || it.text.contains("plan") }
        assertTrue("Plan line must NOT be present in compact 2x1 layout", planLine == null)
    }

    @Test
    fun permission_missing_shows_the_permission_block() {
        val widget = TodayWidget()
        val data = sampleData(permissionGranted = false)
        val budget = ImageBudget()

        val root = renderGlanceTree(context) {
            widget.TodayContent(data, palette, budget, DpSize(150.dp, 150.dp))
        }
        val nodes = collectEmittables(root)

        val texts = nodes.filterIsInstance<EmittableText>()
        assertTrue("Should show permission grant message", texts.any { it.text.contains("Allow usage access") })
    }

    @Test
    fun image_budget_is_respected() {
        val widget = TodayWidget()
        val data = sampleData()
        val budget = ImageBudget(maxBytes = 1_572_864) // 1.5 MiB

        // Render at 4x2 maximum resize
        renderGlanceTree(context) {
            widget.TodayContent(data, palette, budget, DpSize(280.dp, 150.dp))
        }

        assertTrue("Total image allocations must be <= 1.5 MiB", budget.used <= 1_572_864)
        assertTrue("Bitmap was allocated", budget.used > 0)
    }

    @Test
    fun provider_info_declares_the_resize_range_and_a_preview() {
        val parser = context.resources.getXml(
            context.resources.getIdentifier("today_widget_info", "xml", context.packageName)
        )
        assertNotNull("today_widget_info.xml must exist in resources", parser)
        var eventType = parser.eventType
        while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT && eventType != org.xmlpull.v1.XmlPullParser.START_TAG) {
            eventType = parser.next()
        }
        assertEquals("appwidget-provider", parser.name)
        val resizeModeInt = parser.getAttributeIntValue("http://schemas.android.com/apk/res/android", "resizeMode", 0)
        val resizeModeStr = parser.getAttributeValue("http://schemas.android.com/apk/res/android", "resizeMode")
        val previewLayout = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "previewLayout", 0)
        val previewImage = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "previewImage", 0)
        val minResizeWidth = parser.getAttributeValue("http://schemas.android.com/apk/res/android", "minResizeWidth")
        val maxResizeWidth = parser.getAttributeValue("http://schemas.android.com/apk/res/android", "maxResizeWidth")

        val isBoth = (resizeModeInt and android.appwidget.AppWidgetProviderInfo.RESIZE_BOTH) == android.appwidget.AppWidgetProviderInfo.RESIZE_BOTH ||
            (resizeModeStr != null && resizeModeStr.contains("horizontal") && resizeModeStr.contains("vertical"))
        assertTrue("Must support both horizontal and vertical resize", isBoth)
        assertTrue("Must declare a preview layout or previewImage", previewLayout != 0 || previewImage != 0)
        assertNotNull("Must declare minResizeWidth", minResizeWidth)
        assertNotNull("Must declare maxResizeWidth", maxResizeWidth)

        // Also verify receiver is declared in package manager
        val pm = context.packageManager
        val intent = android.content.Intent("android.appwidget.action.APPWIDGET_UPDATE").setPackage(context.packageName)
        val receivers = pm.queryBroadcastReceivers(intent, 0)
        val todayReceiver = receivers.find { it.activityInfo.name == "${context.packageName}.widget.TodayWidgetReceiver" }
        assertNotNull("TodayWidgetReceiver must be declared in AndroidManifest.xml", todayReceiver)
    }
}
