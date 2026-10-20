package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpSize
import androidx.glance.Emittable
import androidx.glance.EmittableWithChildren
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import io.github.khaledbahaaeldin.emberbyte.R
import androidx.glance.background
import androidx.glance.color.ColorProviders
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage
import io.github.khaledbahaaeldin.emberbyte.engine.plan.percentUsed
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.nav.DeepLinks
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.toArgb
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.toColorScheme

/**
 * Builds Glance ColorProviders using colors from [EmberbytePalette].
 */
fun glanceColorProviders(palette: EmberbytePalette): ColorProviders =
    ColorProviders(
        palette.toColorScheme(dark = false),
        palette.toColorScheme(dark = true)
    )

/**
 * Applies rounded corner styling (API 31+ Glance cornerRadius, rounded drawable fallback below API 31).
 */
internal fun GlanceModifier.tileShape(
    palette: EmberbytePalette,
    cornerRadius: Dp = 20.dp,
    fallbackDrawableRes: Int = R.drawable.widget_tile_rounded,
): GlanceModifier {
    return if (android.os.Build.VERSION.SDK_INT >= 31) {
        this.cornerRadius(cornerRadius).background(ColorProvider(palette.surface))
    } else {
        this.background(ImageProvider(fallbackDrawableRes))
    }
}

/**
 * Hero block: renders the hero usage number, unit badge, and front plan status line.
 * Root clickable opens Home.
 */
@Composable
fun HeroBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.HOME))
    val units = data.units.toByteUnits()
    val formattedToday = formatBytes(data.todayBytes, units)

    val planCaption = when {
        data.plan == null && data.dormantText != null -> data.dormantText
        data.plan == null -> "Set up a plan"
        data.plan.isDormant -> data.dormantText ?: "Starts next cycle"
        else -> remainingText(data.plan.remainingBytes, units)
    }

    val heroDesc = "${formattedToday.value} ${formattedToday.unit} used today. $planCaption"

    val context = LocalContext.current
    // Try rendering hero number bitmap if budget allows
    val heroBitmap = try {
        val bmp = WidgetImages.heroNumber(
            text = formattedToday.value,
            unit = formattedToday.unit,
            palette = palette,
            widthPx = 260,
            heightPx = 108,
            context = context,
        )
        if (budget.add(bmp)) bmp else null
    } catch (e: Throwable) {
        null
    }

    Column(
        modifier = modifier
            .tileShape(palette, 20.dp, R.drawable.widget_tile_mixed_hero)
            .clickable(click)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = heroDesc },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Today",
            style = TextStyle(color = ColorProvider(palette.muted), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        )
        Spacer(GlanceModifier.height(2.dp))

        if (heroBitmap != null) {
            Image(
                provider = ImageProvider(heroBitmap),
                contentDescription = "${formattedToday.value} ${formattedToday.unit}",
                modifier = GlanceModifier.width(130.dp).height(54.dp)
            )
        } else {
            // Bold system font fallback
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formattedToday.value,
                    style = TextStyle(color = ColorProvider(palette.primary), fontSize = 38.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = formattedToday.unit,
                    style = TextStyle(color = ColorProvider(palette.primary), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = planCaption,
            style = TextStyle(color = ColorProvider(palette.primary), fontSize = 13.sp, fontWeight = FontWeight.Medium),
            maxLines = 1
        )
    }
}

/**
 * Ring block: renders the wavy progress ring or percentage for the front plan.
 * Root clickable opens Plans.
 */
@Composable
fun RingBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.PLANS))
    val units = data.units.toByteUnits()
    val plan = data.plan

    val ringState = when {
        plan == null -> BudgetStateUi.Ok
        plan.usedBytes >= plan.effectiveCapBytes || plan.fractionUsed >= 1.0f -> BudgetStateUi.Exceeded
        plan.percentUsed() >= 80 -> BudgetStateUi.Warning
        else -> BudgetStateUi.Ok
    }

    val fraction = plan?.fractionUsed ?: 0f
    val percent = plan?.percentUsed() ?: 0
    val ringDesc = if (plan == null) "No active plan" else "${plan.plan.name}, $percent% used, ${remainingText(plan.remainingBytes, units)}"

    val ringBitmap = try {
        val bmp = WidgetImages.ring(fraction, ringState, palette, sizePx = 160, percentText = "$percent%")
        if (budget.add(bmp)) bmp else null
    } catch (_: Throwable) {
        null
    }

    Column(
        modifier = modifier
            .tileShape(palette, 20.dp, R.drawable.widget_tile_mixed_plan)
            .clickable(click)
            .padding(8.dp)
            .semantics { contentDescription = ringDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ringBitmap != null) {
            Image(
                provider = ImageProvider(ringBitmap),
                contentDescription = "$percent percent used ring",
                modifier = GlanceModifier.size(72.dp)
            )
        } else {
            Text(
                text = "$percent%",
                style = TextStyle(color = ColorProvider(palette.primary), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = plan?.plan?.name ?: "No plan",
            style = TextStyle(color = ColorProvider(palette.text), fontSize = 12.sp, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
        Text(
            text = if (plan != null) remainingText(plan.remainingBytes, units) else "Set up a plan",
            style = TextStyle(color = ColorProvider(palette.muted), fontSize = 11.sp),
            maxLines = 1
        )
    }
}

/**
 * App rows block: renders top apps with their icons, name and data count.
 * Root clickable opens Apps.
 */
@Composable
fun AppRowsBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    count: Int = 2,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.APPS))
    val units = data.units.toByteUnits()
    val apps = data.topApps.take(count)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorProvider(palette.surface))
            .clickable(click)
            .padding(8.dp)
            .semantics { contentDescription = "Top ${apps.size} data apps today" }
    ) {
        for (app in apps) {
            val appIconBitmap = try {
                val shape = shapeFor(app.packageName)
                val bmp = WidgetImages.appIcon(
                    icon = null,
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
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appIconBitmap != null) {
                    Image(
                        provider = ImageProvider(appIconBitmap),
                        contentDescription = "${app.label} icon",
                        modifier = GlanceModifier.size(24.dp)
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

/**
 * Network block: shows Mobile vs Wi-Fi usage breakdown with a bar indicator.
 * Root clickable opens Home.
 */
@Composable
fun NetworkBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.HOME))
    val units = data.units.toByteUnits()
    val total = data.mobileBytes + data.wifiBytes
    val mobileFrac = if (total > 0L) (data.mobileBytes.toFloat() / total) else 0f

    val barBitmap = try {
        val bmp = WidgetImages.bar(
            fraction = mobileFrac,
            color = palette.primary.toArgb(),
            widthPx = 200,
            heightPx = 16,
            wavy = false
        )
        if (budget.add(bmp)) bmp else null
    } catch (_: Throwable) {
        null
    }

    val desc = "Network breakdown: Mobile ${plainBytes(data.mobileBytes, units)}, Wi-Fi ${plainBytes(data.wifiBytes, units)}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .tileShape(palette, 20.dp, R.drawable.widget_tile_rounded)
            .clickable(click)
            .padding(8.dp)
            .semantics { contentDescription = desc }
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mobile: ${plainBytes(data.mobileBytes, units)}",
                style = TextStyle(color = ColorProvider(palette.text), fontSize = 11.sp, fontWeight = FontWeight.Medium),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = "Wi-Fi: ${plainBytes(data.wifiBytes, units)}",
                style = TextStyle(color = ColorProvider(palette.muted), fontSize = 11.sp)
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        if (barBitmap != null) {
            Image(
                provider = ImageProvider(barBitmap),
                contentDescription = "Mobile data proportion bar",
                modifier = GlanceModifier.fillMaxWidth().height(8.dp)
            )
        }
    }
}

/**
 * Chart block: 7-day daily usage history bar chart.
 * Root clickable opens History screen.
 */
@Composable
fun ChartBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.HOME))

    val chartBitmap = try {
        val bmp = WidgetImages.chart(
            values = data.dailyBytes,
            palette = palette,
            widthPx = 360,
            heightPx = 90
        )
        if (budget.add(bmp)) bmp else null
    } catch (_: Throwable) {
        null
    }

    val desc = "7-day daily usage chart"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .tileShape(palette, 20.dp, R.drawable.widget_tile_rounded)
            .clickable(click)
            .padding(8.dp)
            .semantics { contentDescription = desc }
    ) {
        Text(
            text = "Last 7 days",
            style = TextStyle(color = ColorProvider(palette.muted), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )
        Spacer(GlanceModifier.height(4.dp))
        if (chartBitmap != null) {
            Image(
                provider = ImageProvider(chartBitmap),
                contentDescription = "7-day usage mini bar chart",
                modifier = GlanceModifier.fillMaxWidth().height(30.dp)
            )
        }
    }
}

/**
 * Permission block: displayed when usage access permission is missing.
 * Root clickable prompts to open app / grant access.
 */
@Composable
fun PermissionBlock(
    data: WidgetData,
    palette: EmberbytePalette,
    budget: ImageBudget,
    modifier: GlanceModifier = GlanceModifier,
    action: Action? = null,
) {
    val click = action ?: actionStartActivity(DeepLinks.intent(LocalContext.current, DeepLinks.HOME))

    // Shape icon for permission warning
    val warnIcon = try {
        val bmp = WidgetImages.appIcon(
            icon = null,
            shape = AppShape.Sunny,
            letter = "!",
            sizePx = 64,
            palette = palette
        )
        if (budget.add(bmp)) bmp else null
    } catch (_: Throwable) {
        null
    }

    val desc = "Allow usage access for data figures"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorProvider(palette.surface))
            .clickable(click)
            .padding(12.dp)
            .semantics { contentDescription = desc },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (warnIcon != null) {
            Image(
                provider = ImageProvider(warnIcon),
                contentDescription = "Permission warning badge",
                modifier = GlanceModifier.size(32.dp)
            )
            Spacer(GlanceModifier.height(8.dp))
        }
        Text(
            text = "Allow usage access",
            style = TextStyle(color = ColorProvider(palette.primary), fontSize = 15.sp, fontWeight = FontWeight.Bold),
            maxLines = 2
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "Tap to grant permission",
            style = TextStyle(color = ColorProvider(palette.muted), fontSize = 11.sp),
            maxLines = 2
        )
    }
}

/**
 * Test harness function: renders a Glance @Composable hierarchy into an [Emittable] root tree
 * so unit tests can inspect semantic properties, content descriptions, and modifiers.
 */
@androidx.annotation.VisibleForTesting
@android.annotation.SuppressLint("RestrictedApi")
fun renderGlanceTree(
    context: Context,
    size: DpSize = DpSize(200.dp, 200.dp),
    content: @Composable () -> Unit
): Emittable = kotlinx.coroutines.runBlocking {
    kotlinx.coroutines.withTimeout(10_000) {
        val root = androidx.glance.appwidget.RemoteViewsRoot(maxDepth = 20)
        val applier = androidx.glance.Applier(root)
        val clock = androidx.compose.runtime.BroadcastFrameClock()
        val recomposer = androidx.compose.runtime.Recomposer(coroutineContext + clock)
        val job = launch(clock, start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            recomposer.runRecomposeAndApplyChanges()
        }
        val composition = androidx.compose.runtime.Composition(applier, recomposer)
        composition.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.glance.LocalContext provides context,
                androidx.glance.LocalSize provides size
            ) {
                content()
            }
        }
        clock.sendFrame(0L)
        recomposer.cancel()
        recomposer.close()
        job.cancel()
        root
    }
}

@androidx.annotation.VisibleForTesting
@android.annotation.SuppressLint("RestrictedApi")
fun renderGlanceTree(
    context: Context,
    content: @Composable () -> Unit
): Emittable = renderGlanceTree(context, DpSize(200.dp, 200.dp), content)
