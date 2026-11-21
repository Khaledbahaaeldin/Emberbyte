package io.github.khaledbahaaeldin.emberbyte.apps.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailUiState
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.DetailType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.warnText
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

internal fun UsagePoint.dayTitle(zone: java.time.ZoneId, locale: Locale): String {
    val d = start.atZone(zone)
    return "${d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)} ${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.SHORT, locale)}"
}

/** Back, icon, title and package on one left edge, with pills for rank, share and network. */
@Composable
internal fun DetailHeader(
    state: AppDetailUiState,
    icon: androidx.compose.ui.graphics.ImageBitmap?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(palette.primary.copy(alpha = 0.16f))
                .semantics { contentDescription = "Back" }
                .testTag("detail_back"),
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = palette.text)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon(
                label = state.label.ifEmpty { state.packageName },
                icon = icon,
                shape = io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor(state.packageName),
                size = 76.dp,
                modifier = Modifier.testTag("detail_icon"),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = state.label.ifEmpty { state.packageName },
                    style = EmberbyteType.grotesk(DetailType.Title, 700),
                    color = palette.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }.testTag("detail_title"),
                )
                if (state.label.isNotEmpty() && state.label != state.packageName) {
                    Text(
                        text = state.packageName,
                        style = EmberbyteType.grotesk(DetailType.Package, 500),
                        color = palette.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        val pills = detailPills(state)
        if (pills.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pills.forEachIndexed { index, text ->
                    Text(
                        text = text,
                        style = EmberbyteType.grotesk(DetailType.Pill, 700),
                        color = palette.linkText,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("detail_pill_$index"),
                    )
                }
            }
        }
    }
}

/** "#2 of 47 apps", "34% of all app data", "Mostly Wi-Fi" or "Uses mobile"; nothing for an app that is not ranked. */
internal fun detailPills(state: AppDetailUiState): List<String> = buildList {
    if (state.rank > 0) {
        add("#${state.rank} of ${state.appCount} apps")
        add("${(state.shareOfAll * 100).roundToInt()}% of all app data")
    }
    if (state.totalBytes > 0L) {
        val mobileShare = state.mobileBytes.toFloat() / state.totalBytes.toFloat()
        add(if (mobileShare > 0.20f) "Uses mobile" else "Mostly Wi-Fi")
    }
}

/** Three connected buttons: 7, 30 and 90 days. The selected one is primary-filled. */
@Composable
internal fun DetailRangeButtons(
    selected: DetailRange,
    onSelect: (DetailRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val shapes = listOf(
        io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes.PeriodStart,
        io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes.PeriodMiddle,
        io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes.PeriodEnd,
    )
    Row(modifier.fillMaxWidth().testTag("detail_range"), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        DetailRange.entries.forEachIndexed { index, range ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .weight(if (isSelected) 1.25f else 1f)
                    .heightIn(min = 48.dp)
                    .clip(shapes[index])
                    .background(if (isSelected) palette.primary else palette.text.copy(alpha = 0.09f))
                    .clickable(role = androidx.compose.ui.semantics.Role.Button) { onSelect(range) }
                    .semantics {
                        this.selected = isSelected
                        contentDescription = "${range.days} days"
                    }
                    .testTag("detail_range_${range.days}"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${range.days} days",
                    style = EmberbyteType.grotesk(DetailType.Button, 700),
                    color = if (isSelected) palette.onPrimary else palette.text,
                    maxLines = 1,
                )
            }
        }
    }
}

/** A card with the shared interlocking corners. */
@Composable
internal fun DetailCard(
    modifier: Modifier = Modifier,
    corner: Corner = Corner.BottomStart,
    container: androidx.compose.ui.graphics.Color = LocalEmberbytePalette.current.surface2,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val shape = remember(corner) { mixedCorners(large = 28.dp, small = 10.dp, smallCorner = corner) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

/** The stacked daily chart: Wi-Fi at the bottom, Mobile above, a dashed average line, tap a bar for its numbers. */
@Composable
internal fun DetailChart(
    state: AppDetailUiState,
    zone: java.time.ZoneId,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val reduce = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val points = remember(state.dailySeries, state.range) {
        if (state.range == DetailRange.Quarter) weeklyBuckets(state.dailySeries, zone) else state.dailySeries
    }
    var selected by remember(state.range) { mutableStateOf<Int?>(null) }
    val max = (points.maxOfOrNull { it.totalBytes } ?: 0L).coerceAtLeast(1L)
    val average = if (points.isEmpty()) 0L else points.sumOf { it.totalBytes } / points.size
    val perUnit = if (state.range == DetailRange.Quarter) "a week" else "a day"
    val title = if (state.range == DetailRange.Quarter) "Each week" else "Each day"
    val busiest = points.withIndex().maxByOrNull { it.value.totalBytes }

    DetailCard(modifier = modifier.testTag("detail_chart_card"), corner = Corner.BottomStart) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = EmberbyteType.grotesk(DetailType.CardTitle, 700), color = palette.text)
            val avg = formatBytes(average, state.units)
            Text("average ${avg.value} ${avg.unit} $perUnit", style = MaterialTheme.typography.labelMedium, color = palette.muted)
        }
        ForceLtr {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    // Tapping anywhere on a bar selects it; one gesture on the chart keeps every touch target wide enough.
                    .pointerInput(points.size) {
                        detectTapGestures { offset ->
                            if (points.isNotEmpty()) {
                                val index = (offset.x / size.width * points.size).toInt().coerceIn(0, points.size - 1)
                                selected = if (selected == index) null else index
                            }
                        }
                    }
                    .testTag("detail_chart"),
            ) {
                Row(
                    Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(if (points.size > 20) 2.dp else 6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    points.forEachIndexed { index, point ->
                        val grow = remember(state.range, points.size) { Animatable(if (reduce) 1f else 0f) }
                        LaunchedEffect(state.range, points.size, reduce) {
                            if (reduce) grow.snapTo(1f) else {
                                kotlinx.coroutines.delay(35L * index)
                                grow.animateTo(1f, motion.spatial())
                            }
                        }
                        val dim = selected != null && selected != index
                        val dayText = point.dayTitle(zone, locale)
                        val amountText = spokenBytes(point.totalBytes, state.units)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer { alpha = if (dim) 0.4f else 1f }
                                .semantics {
                                    contentDescription = "$dayText, $amountText"
                                    this.selected = selected == index
                                }
                                .testTag("detail_bar_$index"),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val height = (150f * (point.totalBytes.toFloat() / max) * grow.value)
                            val wifiShare = if (point.totalBytes == 0L) 0f else point.wifiBytes.toFloat() / point.totalBytes
                            Column(Modifier.fillMaxWidth().height(height.dp), verticalArrangement = Arrangement.Bottom) {
                                if (point.mobileBytes > 0L) {
                                    Box(
                                        Modifier.fillMaxWidth().weight((1f - wifiShare).coerceAtLeast(0.001f))
                                            .clip(MaterialTheme.shapes.small).background(palette.accent).testTag("detail_bar_mobile_$index"),
                                    )
                                }
                                if (point.mobileBytes > 0L && point.wifiBytes > 0L) Box(Modifier.height(2.dp))
                                if (point.wifiBytes > 0L) {
                                    Box(
                                        Modifier.fillMaxWidth().weight(wifiShare.coerceAtLeast(0.001f))
                                            .clip(MaterialTheme.shapes.small).background(palette.primary).testTag("detail_bar_wifi_$index"),
                                    )
                                }
                            }
                        }
                    }
                }
                // The dashed average line.
                Canvas(Modifier.fillMaxSize().testTag("detail_average_line")) {
                    val y = size.height * (1f - average.toFloat() / max)
                    drawLine(
                        color = palette.muted,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                    )
                }
            }
        }
        // Labels under the bars.
        ForceLtr {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (points.size > 20) 2.dp else 6.dp)) {
                points.forEachIndexed { index, point ->
                    val d = point.start.atZone(zone)
                    val show = when (state.range) {
                        DetailRange.Week -> true
                        DetailRange.Month -> index % 5 == 0 || index == points.lastIndex
                        DetailRange.Quarter -> index % 3 == 0
                    }
                    Text(
                        text = if (!show) "" else when (state.range) {
                            DetailRange.Week -> d.dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
                            DetailRange.Month -> d.dayOfMonth.toString()
                            DetailRange.Quarter -> d.month.getDisplayName(TextStyle.SHORT, locale)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.muted,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        val shownIndex = selected ?: busiest?.index
        val shown = shownIndex?.let { points.getOrNull(it) }
        if (shown != null) {
            val amount = formatBytes(shown.totalBytes, state.units)
            val wifi = formatBytes(shown.wifiBytes, state.units)
            val mobile = formatBytes(shown.mobileBytes, state.units)
            ForceLtr {
                Column(Modifier.semantics(mergeDescendants = true) {}.testTag("detail_chart_caption")) {
                    Text(
                        "${shown.dayTitle(zone, locale)} · ${amount.value} ${amount.unit}",
                        style = EmberbyteType.grotesk(DetailType.Label, 700),
                        color = palette.text,
                    )
                    Text(
                        "Wi-Fi ${wifi.value} ${wifi.unit} · Mobile ${mobile.value} ${mobile.unit}",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.muted,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot("Wi-Fi", palette.primary)
            LegendDot("Mobile", palette.accent)
            LegendDot("average", palette.muted)
        }
    }
}

@Composable
private fun LegendDot(label: String, colour: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(colour, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalEmberbytePalette.current.muted)
    }
}

/** The 24-hour strip, the four parts of the day and the busiest window. Tap an hour for its numbers. */
@Composable
internal fun HourPattern(
    hours: LongArray,
    units: ByteUnits,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var selected by remember { mutableStateOf<Int?>(null) }
    val max = hours.maxOrNull() ?: 0L
    val total = hours.sum()
    val window = remember(hours.toList()) { busiestWindow(hours) }
    val parts = remember(hours.toList()) { partsOfDay(hours) }
    val largest = parts.maxByOrNull { it.percent }

    DetailCard(modifier = modifier.testTag("detail_hours_card"), corner = Corner.TopEnd) {
        Text("When you use it, by hour", style = EmberbyteType.grotesk(DetailType.CardTitle, 700), color = palette.text)
        ForceLtr {
            Box(
                Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val index = (offset.x / size.width * 24).toInt().coerceIn(0, 23)
                            selected = if (selected == index) null else index
                        }
                    }
                    .testTag("detail_hour_grid"),
            ) {
                Row(Modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    hours.forEachIndexed { hour, value ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(palette.primary.copy(alpha = hourIntensity(value, max)))
                                .then(if (selected == hour) Modifier.background(palette.text.copy(alpha = 0.25f)) else Modifier)
                                .semantics {
                                    contentDescription = "${hourLabel(hour)}, ${spokenBytes(value, units)}"
                                    this.selected = selected == hour
                                }
                                .testTag("detail_hour_$hour"),
                        )
                    }
                }
            }
        }
        ForceLtr {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(0, 6, 12, 18, 23).forEach {
                    Text(hourLabel(it), style = MaterialTheme.typography.labelSmall, color = palette.muted, maxLines = 1)
                }
            }
        }
        val caption = selected?.let {
            val amount = formatBytes(hours[it], units)
            val percent = if (total == 0L) 0 else Math.round(hours[it] * 100.0 / total).toInt()
            "${hourLabel(it)} · ${amount.value} ${amount.unit} · $percent% of its data"
        } ?: if (total > 0L) {
            "Busiest: ${hourLabel(window.startHour)} to ${hourLabel(window.startHour + window.width)}, ${window.percentOfTotal}% of its data"
        } else {
            "No hourly pattern yet"
        }
        Text(caption, style = EmberbyteType.grotesk(DetailType.Label, 600), color = palette.text, modifier = Modifier.testTag("detail_hour_caption"))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            parts.forEach { part ->
                val isLargest = part == largest && part.percent > 0
                Column(
                    Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (isLargest) palette.primary.copy(alpha = 0.24f) else palette.text.copy(alpha = 0.07f))
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .semantics(mergeDescendants = true) { contentDescription = "${part.name}, ${part.percent}%" }
                        .testTag("detail_part_${part.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${part.percent}%", style = EmberbyteType.grotesk(DetailType.PartPercent, 700), color = palette.text)
                    Text(part.name, style = MaterialTheme.typography.labelSmall, color = palette.muted, maxLines = 1)
                }
            }
        }
    }
}

/** Wi-Fi / mobile split with an impact line against the active plan. */
@Composable
internal fun NetworkImpactCard(state: AppDetailUiState, modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val total = (state.wifiBytes + state.mobileBytes).coerceAtLeast(0L)
    val wifiShare = if (total == 0L) 0.5f else state.wifiBytes.toFloat() / total
    val mobileShare = 1f - wifiShare
    DetailCard(modifier = modifier.testTag("detail_network_card"), corner = Corner.BottomStart) {
        Text("Wi-Fi and mobile", style = EmberbyteType.grotesk(DetailType.CardTitle, 700), color = palette.text)
        ForceLtr {
            Row(Modifier.fillMaxWidth().height(22.dp).testTag("detail_split_bar"), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (state.wifiBytes > 0L) {
                    Box(Modifier.weight(wifiShare.coerceAtLeast(0.047f)).height(22.dp).clip(CircleShape).background(palette.primary).testTag("detail_split_wifi"))
                }
                if (state.mobileBytes > 0L) {
                    Box(Modifier.weight(mobileShare.coerceAtLeast(0.047f)).height(22.dp).clip(CircleShape).background(palette.accent).testTag("detail_split_mobile"))
                }
            }
        }
        val wifi = formatBytes(state.wifiBytes, state.units)
        val mobile = formatBytes(state.mobileBytes, state.units)
        ForceLtr {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Wi-Fi ${wifi.value} ${wifi.unit} · ${(wifiShare * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge, color = palette.text, modifier = Modifier.testTag("app_detail_wifi_label"))
                Text("Mobile ${mobile.value} ${mobile.unit} · ${(mobileShare * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge, color = palette.text, modifier = Modifier.testTag("app_detail_mobile_label"))
            }
        }
        state.planMobileImpact?.let { impact ->
            val percent = impact.fraction * 100f
            val text = String.format(Locale.US, "%.1f", percent)
            Text(
                "Its mobile data is $text% of your plan ${impact.periodLabel}.",
                style = EmberbyteType.grotesk(DetailType.Label, 600),
                color = palette.text,
                modifier = Modifier.testTag("detail_plan_impact"),
            )
            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(palette.text.copy(alpha = 0.1f))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(impact.fraction.coerceIn(0.03f, 1f)).clip(CircleShape).background(palette.primary))
            }
        }
    }
}

@Composable
internal fun StatTile(
    title: String,
    value: String,
    sub: String,
    corner: Corner,
    modifier: Modifier = Modifier,
    container: androidx.compose.ui.graphics.Color = LocalEmberbytePalette.current.surface2,
    tag: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    val shape = remember(corner) { mixedCorners(large = 28.dp, small = 10.dp, smallCorner = corner) }
    Row(
        modifier = modifier
            .heightIn(min = 122.dp)
            .clip(shape)
            .background(container)
            .padding(16.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$title: $value. $sub" }
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = palette.muted)
            ForceLtr {
                Text(value, style = EmberbyteType.grotesk(DetailType.TileValue, 700), color = palette.text, maxLines = 1)
            }
            Text(sub, style = DetailSubStyle(), color = palette.muted)
        }
        trailing?.invoke()
    }
}

@Composable
private fun DetailSubStyle() = MaterialTheme.typography.labelSmall
