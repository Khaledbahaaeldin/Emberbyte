package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import java.time.Instant

/**
 * Kept for backwards compatibility so that an already placed Hero widget
 * continues to function after upgrading to the expressive redesign (Contract 13.7, Task E3).
 *
 * Renders the modern Today layout while keeping the original receiver class name.
 */
class HeroWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            TodayWidget.SIZE_COMPACT,
            TodayWidget.SIZE_STANDARD,
            TodayWidget.SIZE_WIDE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val graph = (context.applicationContext as EmberbyteApplication).graph
        val data = loadWidgetData(graph, Instant.now(graph.clock))
        val palette = WidgetTheme.resolve(context)
        val budget = ImageBudget()

        provideContent {
            val size = LocalSize.current
            TodayLayout(data, palette, budget, size)
        }
    }
}

/**
 * Original receiver component name kept in AndroidManifest.xml.
 */
class HeroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeroWidget()
}
