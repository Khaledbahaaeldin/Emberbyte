package io.github.khaledbahaaeldin.emberbyte

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.lifecycle.lifecycleScope
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.widget.BentoWidget
import io.github.khaledbahaaeldin.emberbyte.widget.DashboardWidget
import io.github.khaledbahaaeldin.emberbyte.widget.HeroWidget
import io.github.khaledbahaaeldin.emberbyte.widget.ImageBudget
import io.github.khaledbahaaeldin.emberbyte.widget.PlanWidget
import io.github.khaledbahaaeldin.emberbyte.widget.TodayWidget
import io.github.khaledbahaaeldin.emberbyte.widget.WidgetData
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@SuppressLint("SetTextI18n")
class WidgetPreviewActivity : ComponentActivity() {

    private lateinit var rootContainer: FrameLayout
    private lateinit var widgetContainer: FrameLayout
    private lateinit var infoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootContainer = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        widgetContainer = FrameLayout(this).apply {
            id = android.view.View.generateViewId()
        }

        infoText = TextView(this).apply {
            textSize = 12f
            setPadding(24, 48, 24, 16)
        }

        val frameParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        rootContainer.addView(widgetContainer, frameParams)

        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        )
        rootContainer.addView(infoText, textParams)

        setContentView(rootContainer)
        renderPreview(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderPreview(intent)
    }

    private fun renderPreview(intent: Intent) {
        val widgetName = intent.getStringExtra("widget") ?: "today"
        val themeId = intent.getStringExtra("theme") ?: "dusk"
        val dark = intent.getBooleanExtra("dark", true)
        val amoled = intent.getBooleanExtra("amoled", false)
        val widthDp = intent.getIntExtra("width", 260)
        val heightDp = intent.getIntExtra("height", 180)
        val permissionGranted = intent.getBooleanExtra("permission", true)
        val isDormant = intent.getBooleanExtra("dormant", false)
        val isOvercap = intent.getBooleanExtra("overcap", false)

        val preset = ThemePresets.firstOrNull { it.id.equals(themeId, ignoreCase = true) }
            ?: ThemePresets.first { it.id == "dusk" }
        val basePalette = if (dark) preset.dark else preset.light
        val palette = if (dark && amoled) {
            basePalette.copy(
                background = androidx.compose.ui.graphics.Color(0xFF000000),
                surface = androidx.compose.ui.graphics.Color(0xFF000000),
                surface2 = androidx.compose.ui.graphics.Color(0xFF121212)
            )
        } else {
            basePalette
        }

        val bgHex = if (dark && amoled) 0xFF000000.toInt() else if (dark) 0xFF141218.toInt() else 0xFFF5F5F7.toInt()
        rootContainer.setBackgroundColor(bgHex)
        infoText.setTextColor(if (dark) AndroidColor.WHITE else AndroidColor.DKGRAY)
        infoText.text = "Widget: $widgetName | Theme: ${preset.name} (${if (dark) "Dark" else "Light"}${if (amoled) " AMOLED" else ""}) | ${widthDp}x${heightDp}dp"

        val density = resources.displayMetrics.density
        val pixelWidth = (widthDp * density).toInt()
        val pixelHeight = (heightDp * density).toInt()

        val layoutParams = widgetContainer.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = pixelWidth
        layoutParams.height = pixelHeight
        widgetContainer.layoutParams = layoutParams

        val data = createSampleData(permissionGranted, isDormant, isOvercap)
        val size = DpSize(widthDp.dp, heightDp.dp)

        lifecycleScope.launch {
            try {
                val budget = ImageBudget()
                val glanceRemoteViews = GlanceRemoteViews()
                val rv = glanceRemoteViews.compose(this@WidgetPreviewActivity, size) {
                    when (widgetName.lowercase()) {
                        "plan" -> PlanWidget().PlanContent(data, palette, budget, size)
                        "bento" -> BentoWidget().BentoContent(data, palette, budget, size)
                        "dashboard" -> DashboardWidget().DashboardContent(data, palette, budget, size)
                        "hero" -> HeroWidget().HeroContent(data, palette, budget, size)
                        else -> TodayWidget().TodayContent(data, palette, budget, size)
                    }
                }
                val view = rv.remoteViews.apply(this@WidgetPreviewActivity, widgetContainer)
                widgetContainer.removeAllViews()
                widgetContainer.addView(view)
            } catch (e: Exception) {
                widgetContainer.removeAllViews()
                val errorView = TextView(this@WidgetPreviewActivity).apply {
                    text = "Error composing $widgetName:\n${e.message}"
                    setTextColor(AndroidColor.RED)
                }
                widgetContainer.addView(errorView)
            }
        }
    }

    private fun createSampleData(
        permissionGranted: Boolean,
        isDormant: Boolean,
        isOvercap: Boolean
    ): WidgetData {
        val now = Instant.parse("2026-10-10T12:00:00Z")
        val cap = 10_000_000_000L
        val used = if (isOvercap) 10_800_000_000L else 6_200_000_000L
        val remaining = if (isOvercap) 0L else 3_800_000_000L
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan = Plan(id = 1L, name = "Primary SIM", subscriptionId = null, capBytes = cap, cycle = cycle, rollover = Rollover.None)
        val planState = PlanState(
            plan = plan,
            window = CycleWindow(now.minusSeconds(86400 * 12), now.plusSeconds(86400 * 18)),
            effectiveCapBytes = cap,
            usedBytes = used,
            remainingBytes = remaining,
            rolledOverBytes = 0L,
            addOnBytes = 0L,
            freeBytes = 0L,
            daysLeft = 18,
            fractionUsed = if (isOvercap) 1.08f else 0.62f,
            isApproximate = false,
            isDormant = isDormant,
            startsAt = if (isDormant) now.plusSeconds(86400 * 2) else null
        )

        val topApps = listOf(
            io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage("com.android.chrome", "Chrome", 10001, 1_200_000_000L, 650_000_000L, 3600_000L),
            io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage("com.google.android.youtube", "YouTube", 10002, 900_000_000L, 520_000_000L, 4200_000L),
            io.github.khaledbahaaeldin.emberbyte.engine.model.AppUsage("com.spotify.music", "Spotify", 10003, 500_000_000L, 280_000_000L, 1800_000L)
        )

        val daily = listOf(800_000_000L, 1_100_000_000L, 950_000_000L, 1_400_000_000L, 600_000_000L, 1_700_000_000L, 1_240_000_000L)

        return WidgetData(
            todayBytes = 1_240_000_000L,
            plan = planState,
            topApps = topApps,
            mobileBytes = 1_000_000_000L,
            wifiBytes = 240_000_000L,
            dailyBytes = daily,
            permissionGranted = permissionGranted,
            estimated = false
        )
    }
}
