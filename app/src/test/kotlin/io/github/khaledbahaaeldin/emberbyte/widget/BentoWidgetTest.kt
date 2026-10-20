package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Emittable
import androidx.glance.EmittableImage
import androidx.glance.EmittableWithChildren
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
class BentoWidgetTest {

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

    private fun sampleData(
        todayBytes: Long = 1_240_000_000L,
        mobileBytes: Long = 1_000_000_000L,
        wifiBytes: Long = 240_000_000L,
        permissionGranted: Boolean = true,
        plan: PlanState? = samplePlanState(),
    ): WidgetData = WidgetData(
        todayBytes = todayBytes,
        plan = plan,
        topApps = emptyList(),
        mobileBytes = mobileBytes,
        wifiBytes = wifiBytes,
        dailyBytes = listOf(todayBytes),
        permissionGranted = permissionGranted,
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

    private fun findSemanticsDescriptions(root: Emittable): List<String> {
        val results = mutableListOf<String>()
        fun visit(node: Emittable) {
            node.modifier.foldIn(Unit) { _, element ->
                if (element is SemanticsModifier) {
                    val config = element.configuration
                    config.getOrNull(SemanticsProperties.ContentDescription)?.let { list ->
                        results.addAll(list)
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
    fun shows_three_tiles_at_4x2() {
        val data = sampleData()
        val budget = ImageBudget()
        val root = renderGlanceTree(context, BentoWidget.SIZE_STANDARD) {
            BentoWidget().BentoContent(data, palette, budget, BentoWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        // 1. Hero tile has "Today" label or value
        assertTrue("Hero tile should show 'Today': $texts", texts.any { it == "Today" || it.contains("Today") })
        // 2. Ring tile has plan name
        assertTrue("Plan tile should show plan name: $texts", texts.any { it.contains("Primary SIM") })
        // 3. Network bar tile shows Mobile and Wi-Fi
        assertTrue("Network bar should show Mobile: $texts", texts.any { it.contains("Mobile") })
        assertTrue("Network bar should show Wi-Fi: $texts", texts.any { it.contains("Wi-Fi") })
    }

    @Test
    fun bento_widget_at_280x180_dp_shows_hero_tile_and_two_stacked_tiles() {
        val data = sampleData()
        val budget = ImageBudget()
        val root = renderGlanceTree(context, BentoWidget.SIZE_4X2) {
            BentoWidget().BentoContent(data, palette, budget, BentoWidget.SIZE_4X2)
        }

        val texts = findAllTexts(root)
        assertTrue("Hero tile should show 'Today': $texts", texts.any { it == "Today" || it.contains("Today") })
        assertTrue("Plan tile should show plan name: $texts", texts.any { it.contains("Primary SIM") })
        assertTrue("Network bar should show Mobile: $texts", texts.any { it.contains("Mobile") })
        assertTrue("Network bar should show Wi-Fi: $texts", texts.any { it.contains("Wi-Fi") })
    }

    @Test
    fun compact_width_shows_hero_and_one_tile() {
        val data = sampleData()
        val budget = ImageBudget()
        val root = renderGlanceTree(context, BentoWidget.SIZE_COMPACT) {
            BentoWidget().BentoContent(data, palette, budget, BentoWidget.SIZE_COMPACT)
        }

        val texts = findAllTexts(root)
        // Shows hero tile
        assertTrue("Hero tile present: $texts", texts.any { it == "Today" || it.contains("Today") })
        // Shows plan tile, but drops network tile (only one tile in second column)
        assertTrue("Plan tile present: $texts", texts.any { it.contains("Primary SIM") })
        assertFalse("Network tile dropped in compact width: $texts", texts.any { it.contains("Wi-Fi") })
    }

    @Test
    fun network_bar_has_a_description_with_both_values() {
        val data = sampleData(mobileBytes = 1_000_000_000L, wifiBytes = 240_000_000L)
        val budget = ImageBudget()
        val root = renderGlanceTree(context, BentoWidget.SIZE_STANDARD) {
            BentoWidget().BentoContent(data, palette, budget, BentoWidget.SIZE_STANDARD)
        }

        val descriptions = findSemanticsDescriptions(root)
        val netDesc = descriptions.find { it.contains("Mobile") && it.contains("Wi-Fi") }
        assertNotNull("Network bar must have description containing both Mobile and Wi-Fi values: $descriptions", netDesc)
    }

    @Test
    fun permission_missing_shows_the_permission_block() {
        val data = sampleData(permissionGranted = false)
        val budget = ImageBudget()
        val root = renderGlanceTree(context, BentoWidget.SIZE_STANDARD) {
            BentoWidget().BentoContent(data, palette, budget, BentoWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        assertTrue("Should show permission grant message: $texts", texts.any { it.contains("Allow usage access") })
        assertFalse("Tiles should not be shown without permission: $texts", texts.any { it == "Primary SIM" })
    }

    @Test
    fun image_budget_is_respected() {
        val data = sampleData()
        val budget = ImageBudget(maxBytes = 1_572_864)

        renderGlanceTree(context, BentoWidget.SIZE_STANDARD) {
            BentoWidget().BentoContent(data, palette, budget, BentoWidget.SIZE_STANDARD)
        }

        assertTrue("Total image allocations must be <= 1.5 MiB: ${budget.used}", budget.used <= 1_572_864)
        assertTrue("Bitmaps were allocated: ${budget.used}", budget.used > 0)
    }
}
