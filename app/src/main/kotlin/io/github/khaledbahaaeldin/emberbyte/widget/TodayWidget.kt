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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.nav.DeepLinks
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import java.time.Instant

/**
 * 2x2 Today widget (Contract 13.7, Task E3).
 *
 * Shows hero number bitmap, unit, and the front plan line or "Set up a plan".
 * Responsive sizing:
 * - 2x1 compact layout shows only the number and unit
 * - 2x2 and wider (up to 4x2) shows the full number, unit, and plan line
 * Root clickable opens Home (emberbyte://home).
 */
class TodayWidget : GlanceAppWidget() {
    companion object {
        val SIZE_COMPACT = DpSize(110.dp, 60.dp)
        val SIZE_STANDARD = DpSize(110.dp, 110.dp)
        val SIZE_WIDE = DpSize(250.dp, 110.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_COMPACT, SIZE_STANDARD, SIZE_WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val graph = (context.applicationContext as EmberbyteApplication).graph
        val data = loadWidgetData(graph, Instant.now(graph.clock))
        val palette = WidgetTheme.resolve(context)
        val budget = ImageBudget()

        provideContent {
            val size = LocalSize.current
            TodayContent(data, palette, budget, size)
        }
    }

    @Composable
    @VisibleForTesting
    internal fun TodayContent(
        data: WidgetData,
        palette: EmberbytePalette,
        budget: ImageBudget,
        size: DpSize,
        action: Action? = null,
    ) {
        TodayLayout(data, palette, budget, size, action)
    }
}


/**
 * Shared Today layout function usable by both [TodayWidget] and [HeroWidget].
 */
@Composable
fun TodayLayout(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    size: DpSize,
    action: Action? = null,
) {
    if (!data.permissionGranted) {
        PermissionBlock(data = data, palette = palette, budget = budget, action = action)
        return
    }

    val isCompact = size.height < 100.dp
    if (isCompact) {
        CompactTodayBlock(data = data, palette = palette, budget = budget, action = action)
    } else {
        HeroBlock(
            data = data,
            palette = palette,
            budget = budget,
            modifier = GlanceModifier.fillMaxSize().cornerRadius(24.dp),
            action = action,
        )
    }
}

@Composable
private fun CompactTodayBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.HOME))
    val units = data.units.toByteUnits()
    val formattedToday = formatBytes(data.todayBytes, units)
    val compactDesc = "${formattedToday.value} ${formattedToday.unit} used today"

    val context = LocalContext.current
    val heroBitmap = try {
        val bmp = WidgetImages.heroNumber(
            text = formattedToday.value,
            unit = formattedToday.unit,
            palette = palette,
            widthPx = 280,
            heightPx = 120,
            context = context,
        )
        if (budget.add(bmp)) bmp else null
    } catch (_: Throwable) {
        null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(palette.surface))
            .clickable(click)
            .padding(8.dp)
            .semantics { contentDescription = compactDesc },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (heroBitmap != null) {
            Image(
                provider = ImageProvider(heroBitmap),
                contentDescription = "${formattedToday.value} ${formattedToday.unit}",
                modifier = GlanceModifier.width(140.dp).height(60.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formattedToday.value,
                    style = TextStyle(color = ColorProvider(palette.primary), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = formattedToday.unit,
                    style = TextStyle(color = ColorProvider(palette.primary), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
