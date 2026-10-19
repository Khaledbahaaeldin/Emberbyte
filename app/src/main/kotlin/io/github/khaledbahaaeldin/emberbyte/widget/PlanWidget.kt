package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.engine.plan.percentUsed
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.nav.DeepLinks
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import java.time.Instant

/**
 * 2x2 Plan widget (Contract 13.7, Task E4).
 *
 * Displays the wavy ring bitmap with usage percentage, plan name, and remaining allowance.
 * Dormant front plan shows plan name and start date with no ring.
 * Exceeded plan shows danger state and "Over the cap" notice.
 * Root clickable deep links to Plans (emberbyte://plans).
 */
class PlanWidget : GlanceAppWidget() {
    companion object {
        val SIZE_STANDARD = DpSize(110.dp, 110.dp)
        val SIZE_WIDE = DpSize(250.dp, 110.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_STANDARD, SIZE_WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val graph = (context.applicationContext as EmberbyteApplication).graph
        val data = loadWidgetData(graph, Instant.now(graph.clock))
        val palette = WidgetTheme.resolve(context)
        val budget = ImageBudget()

        provideContent {
            val size = LocalSize.current
            PlanContent(data, palette, budget, size)
        }
    }

    @Composable
    @VisibleForTesting
    internal fun PlanContent(
        data: WidgetData,
        palette: EmberbytePalette,
        budget: ImageBudget,
        size: DpSize,
        action: Action? = null,
    ) {
        PlanLayout(data, palette, budget, size, action)
    }
}

@Composable
fun PlanLayout(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    size: DpSize,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.PLANS))
    val units = data.units.toByteUnits()
    val plan = data.plan

    if (plan == null) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(palette.surface))
                .clickable(click)
                .padding(12.dp)
                .semantics { contentDescription = "No active plan. Set up a plan" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set up a plan",
                style = TextStyle(
                    color = ColorProvider(palette.primary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        return
    }

    if (plan.isDormant) {
        val dormantStart = data.dormantText ?: (plan.startsAt?.let { "Starts $it" } ?: "Starts next cycle")
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(palette.surface))
                .clickable(click)
                .padding(12.dp)
                .semantics { contentDescription = "${plan.plan.name}. $dormantStart" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.plan.name,
                style = TextStyle(
                    color = ColorProvider(palette.text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = dormantStart,
                style = TextStyle(
                    color = ColorProvider(palette.muted),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
        }
        return
    }

    val isExceeded = plan.usedBytes >= plan.effectiveCapBytes || plan.fractionUsed >= 1.0f
    val isWarning = !isExceeded && plan.percentUsed() >= 80

    val ringState = when {
        isExceeded -> BudgetStateUi.Exceeded
        isWarning -> BudgetStateUi.Warning
        else -> BudgetStateUi.Ok
    }

    val percent = plan.percentUsed()
    val remainingCapText = if (isExceeded) "Over the cap" else remainingText(plan.remainingBytes, units)
    val ringDesc = when (ringState) {
        BudgetStateUi.Exceeded -> "${plan.plan.name}, Exceeded, Over the cap, $percent% used"
        BudgetStateUi.Warning -> "${plan.plan.name}, Warning, $percent% used, $remainingCapText"
        BudgetStateUi.Ok -> "${plan.plan.name}, $percent% used, $remainingCapText"
    }

    val ringBitmap = try {
        val bmp = WidgetImages.ring(plan.fractionUsed, ringState, palette, sizePx = 160)
        if (budget.add(bmp)) bmp else null
    } catch (_: Throwable) {
        null
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.surface))
            .clickable(click)
            .padding(12.dp)
            .semantics { contentDescription = ringDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ringBitmap != null) {
            Image(
                provider = ImageProvider(ringBitmap),
                contentDescription = "$percent% used ring",
                modifier = GlanceModifier.size(80.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isExceeded) {
                    Text(
                        text = "!",
                        style = TextStyle(
                            color = ColorProvider(palette.danger),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                }
                Text(
                    text = "$percent%",
                    style = TextStyle(
                        color = ColorProvider(if (isExceeded) palette.danger else palette.primary),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = plan.plan.name,
            style = TextStyle(
                color = ColorProvider(palette.text),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Text(
            text = remainingCapText,
            style = TextStyle(
                color = ColorProvider(if (isExceeded) palette.danger else palette.muted),
                fontSize = 11.sp,
                fontWeight = if (isExceeded) FontWeight.Bold else FontWeight.Normal
            ),
            maxLines = 1
        )
    }
}
