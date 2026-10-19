package io.github.khaledbahaaeldin.emberbyte.widget

import android.appwidget.AppWidgetProviderInfo
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
import io.github.khaledbahaaeldin.emberbyte.engine.plan.percentUsed
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
class PlanWidgetTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val palette = ThemePresets.first { it.id == "dusk" }.dark

    private fun samplePlanState(
        cap: Long = 10_000_000_000L,
        used: Long = 6_200_000_000L,
        remaining: Long = 3_800_000_000L,
        isDormant: Boolean = false,
        startsAt: Instant? = null,
        enteredBytes: Long? = null,
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
            isDormant = isDormant,
            startsAt = startsAt,
            enteredBytes = enteredBytes,
        )
    }

    private fun sampleData(
        plan: PlanState? = samplePlanState(),
        dormantText: String? = null,
    ): WidgetData = WidgetData(
        todayBytes = 1_240_000_000L,
        plan = plan,
        topApps = emptyList(),
        mobileBytes = 1_000_000_000L,
        wifiBytes = 240_000_000L,
        dailyBytes = listOf(1_240_000_000L),
        permissionGranted = true,
        estimated = false,
        dormantText = dormantText,
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

    private fun findAllImages(root: Emittable): List<EmittableImage> {
        val results = mutableListOf<EmittableImage>()
        fun visit(node: Emittable) {
            if (node is EmittableImage) {
                results.add(node)
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
    fun shows_ring_name_and_left_text() {
        val data = sampleData(plan = samplePlanState(cap = 10_000_000_000L, used = 6_200_000_000L, remaining = 3_800_000_000L))
        val budget = ImageBudget()
        val root = renderGlanceTree(context, PlanWidget.SIZE_STANDARD) {
            PlanWidget().PlanContent(data, palette, budget, PlanWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        assertTrue("Expected plan name in texts: $texts", texts.any { it.contains("Primary SIM") })
        assertTrue("Expected 'left' in texts: $texts", texts.any { it.contains("left") })

        val images = findAllImages(root)
        assertTrue("Expected ring bitmap image when budget allows", images.isNotEmpty())

        val intents = findActionIntents(root)
        assertTrue("Expected deep link to emberbyte://plans", intents.any { it == "emberbyte://plans" })
    }

    @Test
    fun exceeded_shows_the_mark_and_the_text_over_the_cap() {
        val data = sampleData(plan = samplePlanState(cap = 10_000_000_000L, used = 12_000_000_000L, remaining = 0L))
        val budget = ImageBudget()
        val root = renderGlanceTree(context, PlanWidget.SIZE_STANDARD) {
            PlanWidget().PlanContent(data, palette, budget, PlanWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        assertTrue("Expected 'Over the cap' in texts: $texts", texts.any { it.contains("Over the cap") })

        val descriptions = findSemanticsDescriptions(root)
        assertTrue("Expected exceeded state mark or description: $descriptions",
            descriptions.any { it.contains("Over the cap") || it.contains("Exceeded") || it.contains("!") } || texts.any { it.contains("!") })
    }

    @Test
    fun no_plan_shows_set_up_a_plan_and_opens_the_plan_flow() {
        val data = sampleData(plan = null)
        val budget = ImageBudget()
        val root = renderGlanceTree(context, PlanWidget.SIZE_STANDARD) {
            PlanWidget().PlanContent(data, palette, budget, PlanWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        assertTrue("Expected 'Set up a plan' when no plan: $texts", texts.any { it.contains("Set up a plan") })

        val intents = findActionIntents(root)
        assertTrue("Expected deep link to plans: $intents", intents.any { it == "emberbyte://plans" })
    }

    @Test
    fun dormant_plan_shows_the_start_text_and_no_ring() {
        val data = sampleData(
            plan = samplePlanState(isDormant = true, startsAt = Instant.parse("2026-11-01T00:00:00Z")),
            dormantText = "Starts 1 Nov",
        )
        val budget = ImageBudget()
        val root = renderGlanceTree(context, PlanWidget.SIZE_STANDARD) {
            PlanWidget().PlanContent(data, palette, budget, PlanWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        assertTrue("Expected dormant start text: $texts", texts.any { it.contains("Starts 1 Nov") })
        assertTrue("Expected plan name: $texts", texts.any { it.contains("Primary SIM") })

        val images = findAllImages(root)
        assertTrue("Dormant plan must not render a ring bitmap", images.isEmpty())
    }

    @Test
    fun entered_plan_note_is_not_shown_in_the_widget() {
        val data = sampleData(plan = samplePlanState(enteredBytes = 500_000_000L))
        val budget = ImageBudget()
        val root = renderGlanceTree(context, PlanWidget.SIZE_STANDARD) {
            PlanWidget().PlanContent(data, palette, budget, PlanWidget.SIZE_STANDARD)
        }

        val texts = findAllTexts(root)
        assertFalse("Entered plan note must not appear in the compact widget: $texts", texts.any { it.contains("you entered") })
    }

    @Test
    fun image_budget_is_respected() {
        val data = sampleData()
        val budget = ImageBudget(maxBytes = 1_572_864) // 1.5 MiB

        val root = renderGlanceTree(context, PlanWidget.SIZE_STANDARD) {
            PlanWidget().PlanContent(data, palette, budget, PlanWidget.SIZE_STANDARD)
        }

        assertTrue("Total image allocations must be <= 1.5 MiB", budget.used <= 1_572_864)
        assertTrue("Bitmap was allocated", budget.used > 0)
    }
}
