@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.UsageShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.UsageType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import java.time.format.TextStyle
import java.util.Locale

/**
 * The merged "Top apps" card: a week strip as the day picker, a ranked list of the top four apps for the selected day
 * (the first one featured), removable filter chips and three connected buttons: All apps, Filters and History.
 */
@Composable
fun UsageCard(
    state: UsageCardState,
    onSelectDay: (Int) -> Unit,
    onOpenApp: (String) -> Unit,
    onAllApps: () -> Unit,
    onOpenFilters: () -> Unit,
    onRemoveFilter: (UsageFilter) -> Unit,
    onOpenHistory: (selectedDayIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: (String) -> ImageBitmap? = { null },
    onResetFilters: () -> Unit = {},
    playEntrance: Boolean = false,
    locale: Locale = Locale.getDefault(),
    onHistoryBounds: (androidx.compose.ui.geometry.Rect) -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    val shape = remember { mixedCorners(large = 30.dp, small = 10.dp, smallCorner = Corner.BottomStart) }
    val selected = state.days.getOrNull(state.selectedIndex)
    val today = state.days.lastOrNull()?.date
    val isToday = state.selectedIndex == state.days.lastIndex
    val weekday = selected?.date?.dayOfWeek?.getDisplayName(TextStyle.FULL, locale).orEmpty()
    val pillText = if (isToday) "Today" else weekday
    val dayWord = if (isToday) "today" else weekday
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressMorph(shape, pressedScale = 0.99f, pressedCorner = 14.dp)
            .clip(shape)
            .background(palette.surface2)
            .padding(horizontal = 14.dp, vertical = 16.dp)
            .testTag("usage_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: title and the selected-day pill.
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Top apps",
                style = EmberbyteType.grotesk(UsageType.Title, 700).copy(letterSpacing = (-0.01).em),
                color = palette.text,
                modifier = Modifier.semantics { heading() }.testTag("usage_title"),
            )
            Text(
                text = pillText,
                style = EmberbyteType.grotesk(UsageType.Pill, 700),
                color = palette.linkText,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.primary.copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("usage_pill"),
            )
        }

        if (selected != null && today != null) {
            ForceLtr {
                Text(
                    text = summaryAnnotated(summaryText(selected.date, today, selected.totalBytes, state.units, locale), state, selected.totalBytes),
                    style = EmberbyteType.grotesk(UsageType.Summary, 500),
                    color = palette.text,
                    modifier = Modifier.testTag("usage_summary"),
                )
            }
        }

        // Active filters as removable chips with full labels.
        if (state.filters.activeCount > 0) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.filters.network != NetworkFilter.All) {
                    FilterChipItem("Network: ${if (state.filters.network == NetworkFilter.Mobile) "Mobile" else "Wi-Fi"}") {
                        onRemoveFilter(UsageFilter.Network)
                    }
                }
                if (state.filters.showSystemApps) {
                    FilterChipItem("System apps") { onRemoveFilter(UsageFilter.SystemApps) }
                }
            }
        }

        UsageWeekStrip(
            days = state.days,
            selectedIndex = state.selectedIndex,
            units = state.units,
            onSelect = onSelectDay,
            playEntrance = playEntrance,
            locale = locale,
        )

        if (state.rows.isEmpty()) {
            EmptyRanking(filtersActive = state.filters.activeCount > 0, onReset = onResetFilters)
        } else {
            RankedList(
                state = state,
                dayWord = dayWord,
                expanded = expanded,
                onToggle = { pkg -> expanded = if (expanded == pkg) null else pkg },
                onOpenApp = onOpenApp,
                icons = icons,
            )
        }

        ConnectedButtons(
            activeFilters = state.filters.activeCount,
            onAllApps = onAllApps,
            onOpenFilters = onOpenFilters,
            onOpenHistory = { onOpenHistory(state.selectedIndex) },
            onHistoryBounds = onHistoryBounds,
        )
    }
}

private fun summaryAnnotated(text: String, state: UsageCardState, total: Long): AnnotatedString {
    // The amount (for example "3.71 GB") is bold; the rest is regular.
    val amount = formatBytes(total, state.units)
    val boldLength = "${amount.value} ${amount.unit}".length.coerceAtMost(text.length)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(0, boldLength)) }
        append(text.substring(boldLength))
    }
}

@Composable
private fun FilterChipItem(label: String, onRemove: () -> Unit) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(CircleShape)
            .background(palette.primary.copy(alpha = 0.16f))
            .clickable(role = Role.Button, onClickLabel = "Remove filter", onClick = onRemove)
            .padding(start = 12.dp, end = 8.dp)
            .semantics { contentDescription = "$label, remove filter" }
            .testTag("usage_chip"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = EmberbyteType.grotesk(UsageType.Chip, 600), color = palette.linkText)
        Icon(Icons.Rounded.Close, contentDescription = null, tint = palette.linkText, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun EmptyRanking(filtersActive: Boolean, onReset: () -> Unit) {
    val palette = LocalEmberbytePalette.current
    Column(
        Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("usage_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (filtersActive) "No apps match these filters" else "No app usage on this day",
            style = EmberbyteType.grotesk(UsageType.Summary, 600),
            color = palette.text,
        )
        if (filtersActive) {
            TextButton(onClick = onReset, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Reset", style = EmberbyteType.grotesk(UsageType.Button, 700), color = palette.linkText)
            }
        }
    }
}

/** The ranking: rows keyed by package glide to their new place when the order changes. */
@Composable
private fun RankedList(
    state: UsageCardState,
    dayWord: String,
    expanded: String?,
    onToggle: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    icons: (String) -> ImageBitmap?,
) {
    val reduce = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val first = state.rows.firstOrNull()?.amountBytes ?: 0L
    LookaheadScope {
        val transform = remember(reduce) {
            BoundsTransform { _, _ -> if (reduce) tween(0) else spring(dampingRatio = 0.8f, stiffness = 300f) }
        }
        Column(Modifier.fillMaxWidth().testTag("usage_ranking"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.rows.forEachIndexed { index, ranked ->
                androidx.compose.runtime.key(ranked.row.packageName) {
                    val enter = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        if (reduce) enter.snapTo(1f) else enter.animateTo(1f, tween(420))
                    }
                    AppUsageRowItem(
                        ranked = ranked,
                        first = first,
                        featured = index == 0,
                        expanded = expanded == ranked.row.packageName,
                        dayWord = dayWord,
                        units = state.units,
                        icon = icons(ranked.row.packageName),
                        onToggle = { onToggle(ranked.row.packageName) },
                        onOpenApp = { onOpenApp(ranked.row.packageName) },
                        modifier = Modifier
                            .animateBounds(this@LookaheadScope, boundsTransform = transform)
                            .graphicsLayer {
                                val p = enter.value
                                val scale = 0.9f + 0.1f * p
                                scaleX = scale
                                scaleY = scale
                                alpha = p
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedButtons(
    activeFilters: Int,
    onAllApps: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenHistory: () -> Unit,
    onHistoryBounds: (androidx.compose.ui.geometry.Rect) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val start = UsageShapes.SegmentStart
    val middle = UsageShapes.SegmentMiddle
    val end = UsageShapes.SegmentEnd
    val filtersActive = activeFilters > 0
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Segment(
            label = "All apps",
            shape = start,
            weight = 1f,
            container = palette.primary.copy(alpha = 0.16f),
            content = palette.linkText,
            onClick = onAllApps,
            tag = "usage_btn_all_apps",
        )
        Segment(
            label = "Filters",
            icon = Icons.Rounded.FilterList,
            badge = if (filtersActive) activeFilters else null,
            description = if (filtersActive) "Filters, $activeFilters active" else "Filters",
            shape = middle,
            weight = 1.1f,
            container = if (filtersActive) palette.primary.copy(alpha = 0.32f) else palette.primary.copy(alpha = 0.16f),
            content = palette.linkText,
            onClick = onOpenFilters,
            tag = "usage_btn_filters",
        )
        Segment(
            label = "History",
            icon = Icons.Rounded.Schedule,
            shape = end,
            weight = 1.15f,
            container = palette.primary,
            content = palette.onPrimary,
            onClick = onOpenHistory,
            tag = "usage_btn_history",
            onBounds = onHistoryBounds,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Segment(
    label: String,
    shape: androidx.compose.ui.graphics.Shape,
    weight: Float,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    badge: Int? = null,
    description: String = label,
    onBounds: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = Modifier
            .weight(weight)
            .then(if (onBounds != null) Modifier.onGloballyPositioned { onBounds(it.boundsInRoot()) } else Modifier)
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp)
            .semantics { contentDescription = description }
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = EmberbyteType.grotesk(UsageType.Button, 700),
            color = content,
            maxLines = 1,
        )
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.defaultMinSize(minWidth = 20.dp, minHeight = 20.dp).clip(CircleShape).background(palette.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(badge.toString(), style = EmberbyteType.grotesk(UsageType.RowSub, 700), color = palette.onPrimary, modifier = Modifier.testTag("usage_filters_badge"))
            }
        }
    }
}
