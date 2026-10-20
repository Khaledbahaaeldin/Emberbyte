package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import java.time.Instant

/**
 * 4x2 Bento widget (Contract 13.7, Task E5).
 *
 * Left: Today hero tile.
 * Right: Stacked tiles (front plan ring, network split bar).
 * Responsive layout drops right column to one tile when width is compact (<220dp).
 */
class BentoWidget : GlanceAppWidget() {
    companion object {
        val SIZE_COMPACT = DpSize(180.dp, 110.dp)
        val SIZE_STANDARD = DpSize(260.dp, 140.dp)
        val SIZE_4X2 = DpSize(280.dp, 180.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_COMPACT, SIZE_STANDARD, SIZE_4X2))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val graph = (context.applicationContext as EmberbyteApplication).graph
        val data = loadWidgetData(graph, Instant.now(graph.clock))
        val palette = WidgetTheme.resolve(context)
        val budget = ImageBudget()

        provideContent {
            val size = LocalSize.current
            BentoContent(data, palette, budget, size)
        }
    }

    @Composable
    @VisibleForTesting
    internal fun BentoContent(
        data: WidgetData,
        palette: EmberbytePalette,
        budget: ImageBudget,
        size: DpSize,
        action: Action? = null,
    ) {
        BentoLayout(data, palette, budget, size, action)
    }
}

class BentoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BentoWidget()
}

@Composable
fun BentoLayout(
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

    val isCompactWidth = size.width < 220.dp

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(palette.surface2))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Today Hero tile
        HeroBlock(
            data = data,
            palette = palette,
            budget = budget,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            action = action,
        )

        Spacer(GlanceModifier.width(6.dp))

        if (isCompactWidth) {
            // Right Column: one tile (Plan Ring)
            RingBlock(
                data = data,
                palette = palette,
                budget = budget,
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                action = action,
            )
        } else {
            // Right Column: two stacked tiles (Plan Ring, Network Split Bar)
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight()
            ) {
                RingBlock(
                    data = data,
                    palette = palette,
                    budget = budget,
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    action = action,
                )
                Spacer(GlanceModifier.height(6.dp))
                NetworkBlock(
                    data = data,
                    palette = palette,
                    budget = budget,
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    action = action,
                )
            }
        }
    }
}
