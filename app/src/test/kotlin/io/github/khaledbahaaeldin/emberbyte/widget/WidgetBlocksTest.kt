package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Emittable
import androidx.glance.EmittableImage
import androidx.glance.EmittableWithChildren
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionModifier
import androidx.glance.semantics.SemanticsModifier
import androidx.glance.semantics.SemanticsProperties
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetBlocksTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val palette = ThemePresets.first { it.id == "dusk" }.dark

    private fun sampleData(
        todayBytes: Long = 1_380_000_000L,
        plan: PlanState? = samplePlanState(),
        topApps: List<AppUsage> = listOf(
            AppUsage("com.example.app1", "Chrome", 1001, 412_000_000L, 0L, null),
            AppUsage("com.example.app2", "YouTube", 1002, 388_000_000L, 0L, null),
            AppUsage("com.example.app3", "WhatsApp", 1003, 96_000_000L, 0L, null),
            AppUsage("com.example.app4", "Maps", 1004, 71_000_000L, 0L, null)
        ),
        mobileBytes: Long = 1_000_000_000L,
        wifiBytes: Long = 380_000_000L,
        dailyBytes: List<Long> = listOf(200L, 400L, 150L, 300L, 800L, 100L, 440L),
        permissionGranted: Boolean = true,
        estimated: Boolean = false
    ): WidgetData = WidgetData(
        todayBytes = todayBytes,
        plan = plan,
        topApps = topApps,
        mobileBytes = mobileBytes,
        wifiBytes = wifiBytes,
        dailyBytes = dailyBytes,
        permissionGranted = permissionGranted,
        estimated = estimated
    )

    private fun samplePlanState(
        cap: Long = 20_000_000_000L,
        used: Long = 7_600_000_000L,
        daysLeft: Int = 18,
        fraction: Float = 0.38f,
        isDormant: Boolean = false,
        startsAt: Instant? = null
    ): PlanState {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan = Plan(id = 1L, name = "Primary SIM", subscriptionId = null, capBytes = cap, cycle = cycle, rollover = Rollover.None)
        return PlanState(
            plan = plan,
            window = CycleWindow(now.minusSeconds(86400), now.plusSeconds(86400 * 18)),
            effectiveCapBytes = cap,
            usedBytes = used,
            remainingBytes = cap - used,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            daysLeft = daysLeft,
            fractionUsed = fraction,
            isApproximate = false,
            isDormant = isDormant,
            startsAt = startsAt
        )
    }

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

    private fun hasContentDescription(emittable: Emittable): Boolean {
        var found = false
        emittable.modifier.foldIn(Unit) { _, element ->
            if (element is SemanticsModifier) {
                val desc = element.configuration.getOrNull(SemanticsProperties.ContentDescription)
                if (desc != null && desc.isNotEmpty()) {
                    found = true
                }
            }
        }
        return found
    }

    private fun hasClickAction(emittable: Emittable): Boolean {
        var found = false
        emittable.modifier.foldIn(Unit) { _, element ->
            if (element is ActionModifier) {
                found = true
            }
        }
        return found
    }

    @Test
    fun blocks_have_descriptions_for_every_image() {
        val budget = ImageBudget()
        val data = sampleData()

        val blocks: List<Pair<String, @Composable () -> Unit>> = listOf(
            "HeroBlock" to { HeroBlock(data, palette, budget) },
            "RingBlock" to { RingBlock(data, palette, budget) },
            "AppRowsBlock" to { AppRowsBlock(data, palette, budget, count = 2) },
            "NetworkBlock" to { NetworkBlock(data, palette, budget) },
            "ChartBlock" to { ChartBlock(data, palette, budget) },
            "PermissionBlock" to { PermissionBlock(data, palette, budget) }
        )

        for ((name, composable) in blocks) {
            val root = renderGlanceTree(context, composable)
            val nodes = collectEmittables(root)

            val clickFound = nodes.any { hasClickAction(it) }
            val blockRoot = (root as? EmittableWithChildren)?.children?.firstOrNull() ?: root
            assertTrue("$name root must have a clickable action", clickFound)

            val images = nodes.filterIsInstance<EmittableImage>()
            for ((index, image) in images.withIndex()) {
                val hasDesc = hasContentDescription(image)
                assertTrue("$name image #$index must have content description", hasDesc)
            }
        }
    }
}
