package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
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
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.nav.DeepLinks
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 4x4 Dashboard widget (Contract 13.7, Task E6).
 *
 * Full overview featuring header (brand, date), hero number, front plan bar,
 * 7-day usage chart, and top three apps with real icons.
 * Responsive layout drops the chart then the apps as widget size decreases.
 */
class DashboardWidget : GlanceAppWidget() {
    companion object {
        val SIZE_COMPACT = DpSize(200.dp, 110.dp)
        val SIZE_MEDIUM = DpSize(240.dp, 180.dp)
        val SIZE_4X4 = DpSize(260.dp, 260.dp)

        fun resolveIcons(
            apps: List<AppUsage>,
            iconLoader: (String) -> Bitmap?,
        ): Map<String, Bitmap?> {
            val result = mutableMapOf<String, Bitmap?>()
            for (app in apps) {
                if (app.packageName.startsWith("uid:") || app.packageName.isBlank()) {
                    result[app.packageName] = null
                } else {
                    result[app.packageName] = iconLoader(app.packageName)
                }
            }
            return result
        }
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_COMPACT, SIZE_MEDIUM, SIZE_4X4))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val graph = (context.applicationContext as EmberbyteApplication).graph
        val data = loadWidgetData(graph, Instant.now(graph.clock))
        val palette = WidgetTheme.resolve(context)
        val budget = ImageBudget()

        val icons = mutableMapOf<String, Bitmap?>()
        for (app in data.topApps.take(3)) {
            if (!app.packageName.startsWith("uid:") && app.packageName.isNotBlank()) {
                val imgBmp = graph.appIcons.icon(app.packageName)
                icons[app.packageName] = imgBmp?.asAndroidBitmap()
            } else {
                icons[app.packageName] = null
            }
        }

        provideContent {
            val size = LocalSize.current
            DashboardContent(data, palette, budget, size, icons)
        }
    }

    @Composable
    @VisibleForTesting
    internal fun DashboardContent(
        data: WidgetData,
        palette: EmberbytePalette,
        budget: ImageBudget,
        size: DpSize,
        iconBitmaps: Map<String, Bitmap?> = emptyMap(),
        action: Action? = null,
    ) {
        DashboardLayout(data, palette, budget, size, iconBitmaps, action)
    }
}

@android.annotation.SuppressLint("NonObservableLocale")
@Composable
fun DashboardLayout(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    size: DpSize,
    iconBitmaps: Map<String, Bitmap?> = emptyMap(),
    action: Action? = null,
) {
    if (!data.permissionGranted) {
        PermissionBlock(data = data, palette = palette, budget = budget, action = action)
        return
    }

    val context = LocalContext.current
    val rootClick = action ?: actionStartActivity(DeepLinks.intent(context, DeepLinks.HOME))
    val units = data.units.toByteUnits()
    val formattedToday = formatBytes(data.todayBytes, units)

    val showChart = size.height >= 240.dp
    val showApps = size.height >= 160.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(palette.surface))
            .clickable(rootClick)
            .padding(12.dp)
    ) {
        // 1. Header: Brand and date
        val dateText = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
            .format(Instant.now().atZone(ZoneId.systemDefault()))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emberbyte",
                style = TextStyle(
                    color = ColorProvider(palette.primary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = dateText,
                style = TextStyle(
                    color = ColorProvider(palette.muted),
                    fontSize = 11.sp
                )
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        // 2. Hero usage and plan caption
        val heroBitmap = try {
            val bmp = WidgetImages.heroNumber(
                text = formattedToday.value,
                unit = formattedToday.unit,
                palette = palette,
                widthPx = 240,
                heightPx = 100,
                context = context,
            )
            if (budget.add(bmp)) bmp else null
        } catch (_: Throwable) {
            null
        }

        if (heroBitmap != null) {
            Image(
                provider = ImageProvider(heroBitmap),
                contentDescription = "${formattedToday.value} ${formattedToday.unit} used today",
                modifier = GlanceModifier.width(140.dp).height(50.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formattedToday.value,
                    style = TextStyle(color = ColorProvider(palette.primary), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = formattedToday.unit,
                    style = TextStyle(color = ColorProvider(palette.primary), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Plan caption line
        val plan = data.plan
        val planLine = when {
            plan == null -> "Set up a plan"
            plan.isDormant -> data.dormantText ?: "Starts next cycle"
            else -> "${plan.plan.name} · ${remainingText(plan.remainingBytes, units)}"
        }
        Text(
            text = planLine,
            style = TextStyle(color = ColorProvider(palette.muted), fontSize = 11.sp),
            maxLines = 1
        )

        // 3. Optional 7-day Chart (dropped in smaller layouts)
        if (showChart) {
            Spacer(GlanceModifier.height(8.dp))
            ChartBlock(
                data = data,
                palette = palette,
                budget = budget,
                action = action
            )
        }

        // 4. Optional Top Apps list (dropped in compact layout)
        if (showApps) {
            Spacer(GlanceModifier.height(8.dp))
            val apps = data.topApps.take(3)
            for (app in apps) {
                val appClick = action ?: actionStartActivity(
                    DeepLinks.intent(context, DeepLinks.app(app.packageName))
                )
                val shape = shapeFor(app.packageName)
                val iconBmp = iconBitmaps[app.packageName]
                val appIconBitmap = try {
                    val bmp = WidgetImages.appIcon(
                        icon = iconBmp,
                        shape = shape,
                        letter = app.label,
                        sizePx = 48,
                        palette = palette
                    )
                    if (budget.add(bmp)) bmp else null
                } catch (_: Throwable) {
                    null
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable(appClick)
                        .semantics { contentDescription = "${app.label}, ${plainBytes(app.totalBytes, units)}" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appIconBitmap != null) {
                        Image(
                            provider = ImageProvider(appIconBitmap),
                            contentDescription = "${app.label} icon",
                            modifier = GlanceModifier.size(20.dp)
                        )
                        Spacer(GlanceModifier.width(6.dp))
                    }
                    Text(
                        text = app.label,
                        style = TextStyle(color = ColorProvider(palette.text), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = plainBytes(app.totalBytes, units),
                        style = TextStyle(color = ColorProvider(palette.muted), fontSize = 11.sp)
                    )
                }
            }
        }
    }
}
