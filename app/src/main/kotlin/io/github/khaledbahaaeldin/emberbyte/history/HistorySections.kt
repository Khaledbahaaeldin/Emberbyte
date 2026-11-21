package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.apps.detail.DetailCard
import io.github.khaledbahaaeldin.emberbyte.apps.detail.hourLabel
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.HistoryType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/** One sentence from the numbers (weekend lift, busiest hour, mobile share); hidden with under 14 days of data. */
internal fun historySentence(rows: List<DayUsage>, hourTotals: LongArray, mobileShare: Float): String? {
    val lift = weekendLift(rows) ?: return null
    val peak = hourTotals.withIndex().maxByOrNull { it.value }?.takeIf { it.value > 0L }?.index ?: return null
    val part = when (peak) {
        in 0..5 -> "night"
        in 6..11 -> "morning"
        in 12..17 -> "afternoon"
        else -> "evening"
    }
    val weekend = when {
        lift >= 0.03f -> "You use ${(lift * 100).roundToInt()}% more data on weekends."
        lift <= -0.03f -> "You use ${(-lift * 100).roundToInt()}% less data on weekends."
        else -> "You use about the same data on weekends and weekdays."
    }
    return "$weekend Most flows in the $part, around ${hourLabel(peak)}, and ${(mobileShare * 100).roundToInt()}% of it is mobile."
}

/** The four tiles: daily average, Wi-Fi share, heaviest app and active days. */
@Composable
internal fun HistoryTiles(state: HistoryUiState, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val windowDays = state.rows.takeLast(14).ifEmpty { state.rows }
    val average = if (windowDays.isEmpty()) 0L else windowDays.sumOf { it.totalBytes } / windowDays.size
    val total = state.rows.takeLast(14).sumOf { it.totalBytes }
    val wifi = state.rows.takeLast(14).sumOf { it.wifiBytes }
    val wifiPercent = if (total <= 0L) 0 else (wifi * 100.0 / total).roundToInt()
    val active = windowDays.count { it.totalBytes > 0L }
    val heaviest = state.topApps.firstOrNull()
    val avg = formatBytes(average, state.units)
    val wifiAmount = formatBytes(wifi, state.units)
    val totalAmount = formatBytes(total, state.units)
    val tiles = listOf(
        HistoryTile("Daily average", "${avg.value} ${avg.unit}", "last ${windowDays.size} days", Corner.TopStart, "history_tile_average"),
        HistoryTile("Wi-Fi share", "$wifiPercent%", "${wifiAmount.value} ${wifiAmount.unit} of ${totalAmount.value} ${totalAmount.unit}", Corner.TopEnd, "history_tile_wifi"),
        HistoryTile(
            "Heaviest app",
            heaviest?.label ?: "None",
            heaviest?.let { "${it.percentOfAll}% of everything" } ?: "No app data",
            Corner.BottomStart, "history_tile_heaviest", container = palette.primary.copy(alpha = 0.16f),
        ),
        HistoryTile("Active days", "$active of ${windowDays.size}", "days with data", Corner.BottomEnd, "history_tile_active"),
    )
    val wide = androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.5f
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (wide) tiles.forEach { TileCard(it, Modifier.fillMaxWidth()) }
        else tiles.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { pair.forEach { TileCard(it, Modifier.weight(1f)) } }
        }
    }
}

private data class HistoryTile(
    val title: String,
    val value: String,
    val sub: String,
    val corner: Corner,
    val tag: String,
    val container: androidx.compose.ui.graphics.Color? = null,
)

@Composable
private fun TileCard(tile: HistoryTile, modifier: Modifier) {
    val palette = LocalEmberbytePalette.current
    val shape = remember(tile.corner) { mixedCorners(large = 28.dp, small = 10.dp, smallCorner = tile.corner) }
    Column(
        modifier = modifier
            .heightIn(min = 112.dp)
            .clip(shape)
            .background(tile.container ?: palette.surface2)
            .padding(16.dp)
            .semantics(mergeDescendants = true) { contentDescription = "${tile.title}: ${tile.value}. ${tile.sub}" }
            .testTag(tile.tag),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(tile.title, style = MaterialTheme.typography.labelMedium, color = palette.muted)
        ForceLtr { Text(tile.value, style = EmberbyteType.grotesk(HistoryType.TileValue, 700), color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        Text(tile.sub, style = MaterialTheme.typography.labelSmall, color = palette.muted)
    }
}

/** 13 weeks by 7 days (Monday first), squares tinted by usage; tap a day to select it. */
@Composable
internal fun ActivityCalendar(
    rows: List<DayUsage>,
    today: LocalDate,
    selected: LocalDate?,
    units: ByteUnits,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val byDate = remember(rows) { rows.associateBy { it.date } }
    val max = rows.maxOfOrNull { it.totalBytes } ?: 0L
    val lastMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val firstMonday = lastMonday.minusWeeks(12)
    DetailCard(modifier = modifier.testTag("history_calendar"), corner = Corner.BottomStart) {
        Text("Activity calendar, last 13 weeks", style = EmberbyteType.grotesk(HistoryType.CardTitle, 700), color = palette.text)
        ForceLtr {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0 until 13).forEach { week ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        (0 until 7).forEach { weekday ->
                            val date = firstMonday.plusWeeks(week.toLong()).plusDays(weekday.toLong())
                            if (date.isAfter(today)) {
                                Box(Modifier.fillMaxWidth().aspectRatio(1f))
                            } else {
                                val day = byDate[date]
                                val total = day?.totalBytes ?: 0L
                                val isSelected = selected == date
                                val label = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)} ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, locale)}, ${spokenBytes(total, units)}"
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(palette.primary.copy(alpha = calendarIntensity(total, max)))
                                        .then(if (date == today) Modifier.border(2.dp, palette.text, MaterialTheme.shapes.extraSmall) else Modifier)
                                        .then(if (isSelected) Modifier.border(3.dp, palette.onPrimary, MaterialTheme.shapes.extraSmall) else Modifier)
                                        .semantics { contentDescription = label; this.selected = isSelected }
                                        .testTag("history_cell_$date"),
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text("Less", style = MaterialTheme.typography.labelSmall, color = palette.muted)
            listOf(0f, 0.33f, 0.66f, 1f).forEach {
                Box(Modifier.padding(horizontal = 2.dp).size(12.dp).clip(MaterialTheme.shapes.extraSmall).background(palette.primary.copy(alpha = calendarIntensity((it * 100).toLong(), 100))))
            }
            Text("More", style = MaterialTheme.typography.labelSmall, color = palette.muted)
        }
    }
}

/** Seven bars Monday to Sunday (average over the data), weekends in the full colour. */
@Composable
internal fun WeekdayPattern(rows: List<DayUsage>, units: ByteUnits, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val averages = remember(rows) { weekdayAverages(rows) }
    val max = (averages.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val weekdayAvg = averages.take(5).average().toLong()
    val weekendAvg = averages.drop(5).average().toLong()
    val lift = weekendLift(rows)
    DetailCard(modifier = modifier.testTag("history_weekday_card"), corner = Corner.TopEnd) {
        Text("Weekday pattern, average over the last 12 weeks", style = EmberbyteType.grotesk(HistoryType.CardTitle, 700), color = palette.text)
        ForceLtr {
            Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                averages.forEachIndexed { index, value ->
                    val weekend = index >= 5
                    val amount = formatBytes(value, units)
                    val day = DayOfWeek.of(index + 1)
                    Column(
                        Modifier.weight(1f).fillMaxHeight().semantics(mergeDescendants = true) {
                            contentDescription = "${day.getDisplayName(TextStyle.FULL, locale)}, average ${amount.value} ${amount.unit}"
                        }.testTag("history_weekday_$index"),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("${amount.value}", style = MaterialTheme.typography.labelSmall, color = palette.muted, maxLines = 1)
                        Box(
                            Modifier.fillMaxWidth().fillMaxHeight(0.7f * value / max + 0.04f).clip(MaterialTheme.shapes.small)
                                .background(palette.primary.copy(alpha = if (weekend) 1f else 0.35f)),
                        )
                        Text(day.getDisplayName(TextStyle.NARROW, locale), style = MaterialTheme.typography.labelSmall, color = palette.muted)
                    }
                }
            }
        }
        val wd = formatBytes(weekdayAvg, units)
        val we = formatBytes(weekendAvg, units)
        val tail = lift?.let { " ${if (it >= 0) "${(it * 100).roundToInt()}% more" else "${(-it * 100).roundToInt()}% less"}." } ?: ""
        Text(
            "Weekdays average ${wd.value} ${wd.unit}, weekends ${we.value} ${we.unit}.$tail",
            style = EmberbyteType.grotesk(HistoryType.Label, 600),
            color = palette.text,
            modifier = Modifier.testTag("history_weekday_text"),
        )
    }
}

/** A line of the mobile percentage per bucket with a one-line summary. */
@Composable
internal fun MobileShareChart(shares: List<Float>, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val first = shares.firstOrNull { it > 0f } ?: 0f
    val last = shares.lastOrNull() ?: 0f
    val summary = when {
        kotlin.math.abs(last - first) < 0.02f -> "Mobile is ${(last * 100).roundToInt()}% of your usage now, about where it started."
        last > first -> "Mobile is ${(last * 100).roundToInt()}% of your usage now, up from ${(first * 100).roundToInt()}% at the start."
        else -> "Mobile is ${(last * 100).roundToInt()}% of your usage now, down from ${(first * 100).roundToInt()}% at the start."
    }
    DetailCard(modifier = modifier.testTag("history_mobile_card"), corner = Corner.BottomEnd) {
        Text("Mobile share over time", style = EmberbyteType.grotesk(HistoryType.CardTitle, 700), color = palette.text)
        ForceLtr {
            Canvas(Modifier.fillMaxWidth().height(90.dp).testTag("history_mobile_line")) {
                if (shares.size < 2) return@Canvas
                val max = (shares.maxOrNull() ?: 0.01f).coerceAtLeast(0.01f)
                fun x(i: Int) = size.width * i / (shares.size - 1)
                fun y(v: Float) = size.height - (size.height - 12.dp.toPx()) * (v / max) - 6.dp.toPx()
                val line = Path().apply {
                    moveTo(x(0), y(shares[0]))
                    for (i in 1 until shares.size) {
                        val mx = (x(i - 1) + x(i)) / 2f
                        cubicTo(mx, y(shares[i - 1]), mx, y(shares[i]), x(i), y(shares[i]))
                    }
                }
                val area = Path().apply { addPath(line); lineTo(x(shares.size - 1), size.height); lineTo(0f, size.height); close() }
                drawPath(area, Brush.verticalGradient(listOf(palette.primary.copy(alpha = 0.22f), palette.primary.copy(alpha = 0f))))
                drawPath(line, palette.primary, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(palette.primary, 5.dp.toPx(), Offset(x(shares.size - 1), y(shares.last())))
            }
        }
        Text(summary, style = EmberbyteType.grotesk(HistoryType.Label, 600), color = palette.text, modifier = Modifier.testTag("history_mobile_summary"))
    }
}

/** Busiest and lightest day, the longest run under the daily average, and the peak hour. */
@Composable
internal fun HistoryRecords(rows: List<DayUsage>, hourTotals: LongArray, units: ByteUnits, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val r = remember(rows, hourTotals.toList()) { records(rows, hourTotals) }
    fun dayText(d: DayUsage?) = d?.let { "${it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)} ${it.date.dayOfMonth} ${it.date.month.getDisplayName(TextStyle.SHORT, locale)}" } ?: "None"
    fun amountText(d: DayUsage?) = d?.let { formatBytes(it.totalBytes, units).let { f -> "${f.value} ${f.unit}" } } ?: ""
    val tiles = listOf(
        HistoryTile("Busiest day", amountText(r.busiest), dayText(r.busiest), Corner.TopStart, "history_record_busiest"),
        HistoryTile("Lightest day", amountText(r.lightest), dayText(r.lightest), Corner.TopEnd, "history_record_lightest"),
        HistoryTile("Longest run under average", "${r.longestRunUnderAverage} days", "in a row", Corner.BottomStart, "history_record_run"),
        HistoryTile("Peak hour", r.peakHour?.let { hourLabel(it) } ?: "None", "busiest hour of the day", Corner.BottomEnd, "history_record_peak", container = palette.primary.copy(alpha = 0.16f)),
    )
    val wide = androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.5f
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Records", style = EmberbyteType.grotesk(HistoryType.CardTitle, 700), color = palette.text)
        if (wide) tiles.forEach { TileCard(it, Modifier.fillMaxWidth()) }
        else tiles.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { pair.forEach { TileCard(it, Modifier.weight(1f)) } }
        }
    }
}

/** The heaviest apps with their share and the change against the period before. */
@Composable
internal fun HistoryTopApps(apps: List<AppShare>, units: ByteUnits, onOpenApp: (String) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val first = apps.firstOrNull()?.bytes ?: 0L
    DetailCard(modifier = modifier.testTag("history_top_apps"), corner = Corner.TopStart) {
        Text("Top apps in this period", style = EmberbyteType.grotesk(HistoryType.CardTitle, 700), color = palette.text)
        apps.forEach { app ->
            val amount = formatBytes(app.bytes, units)
            val changeText = when {
                app.change == null -> "new"
                kotlin.math.abs(app.change) < 0.03f -> "steady"
                app.change > 0f -> "▲ ${(app.change * 100).roundToInt()}%"
                else -> "▼ ${(-app.change * 100).roundToInt()}%"
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(palette.text.copy(alpha = 0.07f))
                    .clickable { onOpenApp(app.packageName) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${app.label}, ${plainBytes(app.bytes, units)}, ${app.percentOfAll}% of all data, $changeText"
                    }
                    .testTag("history_top_${app.packageName}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(app.label, style = EmberbyteType.grotesk(HistoryType.Label, 700), color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${app.percentOfAll}% of all data · $changeText", style = MaterialTheme.typography.labelSmall, color = palette.muted)
                }
                ForceLtr { Text("${amount.value} ${amount.unit}", style = EmberbyteType.grotesk(HistoryType.Label, 700), color = palette.text) }
            }
        }
    }
}

/**
 * The always-visible card for the selected day, week or month (the latest one by default): its total, how that compares
 * with the daily average, the Wi-Fi / mobile split and the top three apps.
 */
@Composable
internal fun SelectedCard(state: HistoryUiState, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val today = state.rows.lastOrNull()?.date
    val selection = state.selection
    val granularity = state.granularity
    val point = when (selection) {
        is HistorySelection.Day -> state.rows.firstOrNull { it.date == selection.date }
            ?.let { io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint(selection.date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant(), it.mobileBytes, it.wifiBytes) }
        is HistorySelection.Bucket -> state.points.getOrNull(selection.index)
        HistorySelection.Default -> state.points.lastOrNull()
    }
    val unitWord = when (granularity) {
        io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.WEEK -> "week"
        io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.MONTH -> "month"
        else -> "day"
    }
    val title = when (selection) {
        is HistorySelection.Day -> selection.date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
        is HistorySelection.Bucket -> state.bars.getOrNull(selection.index)?.let { it.title ?: it.label } ?: "Selected"
        HistorySelection.Default -> when (granularity) {
            io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.WEEK -> "This week so far"
            io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.MONTH -> "This month so far"
            else -> "Today so far"
        }
    }
    val days = when {
        selection is HistorySelection.Day -> 1
        granularity == io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.WEEK ->
            if (selection == HistorySelection.Default && today != null) today.dayOfWeek.value else 7
        granularity == io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity.MONTH ->
            if (selection == HistorySelection.Default && today != null) today.dayOfMonth else 30
        else -> 1
    }
    val recent = state.rows.takeLast(14)
    val dailyAverage = if (recent.isEmpty()) 0L else recent.sumOf { it.totalBytes } / recent.size
    val total = point?.totalBytes ?: 0L
    val versus = versusAverage(total, days, dailyAverage)
    val versusText = when (versus.kind) {
        VersusAverage.Above -> "▲ ${versus.percent}% above your daily average"
        VersusAverage.Below -> "▼ ${versus.percent}% below your daily average"
        else -> "near your daily average"
    }
    DetailCard(modifier = modifier.testTag("history_selected_card"), corner = Corner.BottomStart) {
        Text(title, style = EmberbyteType.grotesk(HistoryType.CardTitle, 700), color = palette.text, modifier = Modifier.testTag("history_selected_title"))
        val formatted = formatBytes(total, state.units)
        ForceLtr {
            Text(
                "${formatted.value} ${formatted.unit}",
                style = EmberbyteType.grotesk(HistoryType.SelectedTotal, 700),
                color = palette.primary,
                modifier = Modifier.testTag("history_selected_total"),
            )
        }
        if (dailyAverage > 0L) {
            Text(versusText, style = EmberbyteType.grotesk(HistoryType.Label, 600), color = palette.text, modifier = Modifier.testTag("history_selected_versus"))
        }
        if (point != null) {
            val wifi = formatBytes(point.wifiBytes, state.units)
            val mobile = formatBytes(point.mobileBytes, state.units)
            val sum = (point.wifiBytes + point.mobileBytes).coerceAtLeast(1L)
            val wifiShare = point.wifiBytes.toFloat() / sum
            ForceLtr {
                Row(Modifier.fillMaxWidth().height(14.dp).testTag("history_selected_split"), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (point.wifiBytes > 0L) Box(Modifier.weight(wifiShare.coerceAtLeast(0.047f)).height(14.dp).clip(androidx.compose.foundation.shape.CircleShape).background(palette.primary))
                    if (point.mobileBytes > 0L) Box(Modifier.weight((1f - wifiShare).coerceAtLeast(0.047f)).height(14.dp).clip(androidx.compose.foundation.shape.CircleShape).background(palette.accent))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Wi-Fi ${wifi.value} ${wifi.unit} · ${(wifiShare * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = palette.text)
                    Text("Mobile ${mobile.value} ${mobile.unit} · ${((1f - wifiShare) * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = palette.text)
                }
            }
        }
        state.selectedApps.forEach { app ->
            val amount = formatBytes(app.bytes, state.units)
            Row(
                Modifier.fillMaxWidth().heightIn(min = 40.dp).testTag("history_selected_app_${app.packageName}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${app.label} · ${app.percentOfAll}% of this $unitWord", style = MaterialTheme.typography.bodyMedium, color = palette.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                ForceLtr { Text("${amount.value} ${amount.unit}", style = EmberbyteType.grotesk(HistoryType.Label, 700), color = palette.text) }
            }
        }
    }
}
