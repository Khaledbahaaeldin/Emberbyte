package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.chips.EmberbyteFilterChip
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.spokenUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette

@Composable
fun UsageBarRow(
    points: List<BarUi>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollKey: Any? = null,
    openAtEnd: Boolean = true,
    summaryDescription: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    barColor: ((index: Int, isSelected: Boolean) -> Color)? = null,
    showValues: Boolean = true,
    todayIndex: Int? = null,
    averageBytes: Long? = null,
) {
    val palette = LocalEmberbytePalette.current
    val density = LocalDensity.current
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    val isCrowded = points.size > 7 || density.fontScale > 1.3f
    var showAllValues by rememberSaveable(scrollKey) { mutableStateOf(false) }
    val highestIndex = points.indices.maxByOrNull { points[it].bytes }
    val currentPeriodIndex = points.lastIndex
    val hasSelection = selectedIndex != null

    val max = (points.maxOfOrNull { it.bytes } ?: 0L).coerceAtLeast(1L)
    val summary = summaryDescription ?: run {
        if (points.isNotEmpty()) {
            if (points.size == 7) {
                val highest = points.maxByOrNull { it.bytes }
                if (highest != null) "Daily usage, last 7 days, highest ${highest.description}"
                else "Daily usage, last 7 days"
            } else {
                "Daily usage, last ${points.size} periods"
            }
        } else "Daily usage"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (isCrowded && showValues) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                EmberbyteFilterChip(
                    selected = showAllValues,
                    onClick = { showAllValues = !showAllValues },
                    label = { Text(if (showAllValues) "Hide values" else "Show values") },
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { this.contentDescription = summary },
        ) {
            var shown by remember(reduceMotion) { mutableStateOf<Boolean>(reduceMotion) }
            LaunchedEffect(reduceMotion) { shown = true }
            val count = points.size
            val totalNeeded = (count * 48).dp + ((count - 1).coerceAtLeast(0) * 8).dp
            val scrollable = totalNeeded > maxWidth
            val scrollState = rememberScrollState()

            if (scrollable && openAtEnd) {
                LaunchedEffect(points.size, scrollKey) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }

            Row(
                modifier = if (scrollable) {
                    Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(contentPadding)
                } else {
                    Modifier.fillMaxWidth().padding(contentPadding)
                },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                points.forEachIndexed { index, bar ->
                    val isSelected = index == selectedIndex
                    val isToday = todayIndex != null && index == todayIndex
                    val shouldShowValue = showValues && bar.valueText.isNotEmpty() && (
                        !isCrowded || showAllValues || isSelected || index == highestIndex || index == currentPeriodIndex
                    )

                    if (bar.isBoundary && index > 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(palette.surface2)
                        )
                    }

                    UsageBarColumn(
                        modifier = if (scrollable) {
                            Modifier
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable(role = Role.Tab) { onSelect(index) }
                                .semantics {
                                    contentDescription = bar.description
                                    selected = isSelected
                                }
                        } else {
                            Modifier
                                .weight(1f)
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable(role = Role.Tab) { onSelect(index) }
                                .semantics {
                                    contentDescription = bar.description
                                    selected = isSelected
                                }
                        },
                        bar = bar,
                        index = index,
                        isSelected = isSelected,
                        shouldShowValue = shouldShowValue,
                        isToday = isToday,
                        hasSelection = hasSelection,
                        isCrowded = isCrowded,
                        shown = shown,
                        reduceMotion = reduceMotion,
                        max = max,
                        barColor = barColor,
                        palette = palette,
                        motion = motion,
                        averageBytes = averageBytes,
                    )
                }
            }

            // Trailing edge fade scrim indicating more scrollable content
            if (scrollable && scrollState.value < scrollState.maxValue) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(24.dp)
                        .height(100.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, palette.background)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun UsageBarRow(
    label: String,
    bytes: Long,
    fraction: Float,
    color: Color,
    valueText: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val percent = kotlin.math.round(clampedFraction * 100f).toInt()
    val formattedBytes = formatBytes(bytes, ByteUnits.DECIMAL)
    val displayVal = if (formattedBytes.value.contains(".") && formattedBytes.value.endsWith("0")) {
        formattedBytes.value.trimEnd('0').removeSuffix(".")
    } else {
        formattedBytes.value
    }
    val spokenBytes = "$displayVal ${spokenUnit(formattedBytes.unit)}"
    val rowDescription = "$label, $spokenBytes, $percent percent of the total"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {
                this.contentDescription = rowDescription
                this.progressBarRangeInfo = ProgressBarRangeInfo(
                    current = clampedFraction,
                    range = 0f..1f,
                    steps = 0,
                )
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Flat rounded bar with stub
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(palette.track),
            ) {
                val totalWidth = maxWidth
                val fillWidth = (totalWidth * clampedFraction).coerceIn(8.dp, totalWidth)
                Box(
                    modifier = Modifier
                        .testTag("usage_bar_row_stub")
                        .width(fillWidth)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }

        // Value bubble in surface2 with EmberbyteType.value and ForceLtr
        Box(
            modifier = Modifier
                .testTag("usage_bar_row_bubble")
                .clip(CircleShape)
                .background(palette.surface2)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            ForceLtr {
                Text(
                    text = valueText,
                    style = EmberbyteType.value(12.sp),
                    color = palette.text,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun UsageBarColumn(
    modifier: Modifier,
    bar: BarUi,
    index: Int,
    isSelected: Boolean,
    shouldShowValue: Boolean,
    isToday: Boolean,
    hasSelection: Boolean,
    isCrowded: Boolean,
    shown: Boolean,
    reduceMotion: Boolean,
    max: Long,
    barColor: ((index: Int, isSelected: Boolean) -> Color)?,
    palette: EmberbytePalette,
    motion: io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteMotion,
    averageBytes: Long? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        // Value label above bar
        Box(
            modifier = Modifier
                .height(18.dp)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            if (shouldShowValue) {
                ForceLtr {
                    Text(
                        text = bar.valueText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) palette.text else palette.muted,
                        maxLines = 1,
                    )
                }
            }
        }

        // Bar fill & styling
        val resolvedColor = barColor?.invoke(index, isSelected)
            ?: when {
                isSelected -> palette.primary
                hasSelection -> palette.surface.copy(alpha = 0.38f)
                else -> palette.surface
            }
        val targetHeight = (8 + 56 * (bar.bytes.toFloat() / max)).dp
        // With an average the cell is tall enough to hold the dashed line at its level, even above a short bar.
        val averageHeight = averageBytes?.let { (8 + 56 * (it.toFloat() / max)).dp }
        val animatedHeight = animateDpAsState(
            targetValue = if (shown || reduceMotion) targetHeight else 8.dp,
            animationSpec = motion.spatial(),
            label = "bar_height_$index",
        )
        val animatedColor = animateColorAsState(
            targetValue = resolvedColor,
            animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else motion.effects(),
            label = "bar_color_$index",
        )

        val borderColor = when {
            isToday && isSelected -> palette.accent
            isToday && !isSelected -> palette.primary
            !isSelected && barColor == null -> palette.surface2
            else -> null
        }
        val borderWidth = if (isToday) 1.5.dp else 1.dp
        val density = LocalDensity.current
        val borderStroke = remember(borderWidth, density) {
            Stroke(width = with(density) { borderWidth.toPx() })
        }

        Box(
            Modifier
                .testTag("usage_bar_$index")
                .widthIn(max = 24.dp)
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val h = (averageHeight?.let { maxOf(animatedHeight.value, it) } ?: animatedHeight.value).roundToPx()
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = h, maxHeight = h)
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                }
                .drawWithCache {
                    val defaultRadius = 8.dp.toPx()
                    val mobileShare = if (bar.bytes > 0L) (bar.mobileBytes.toFloat() / bar.bytes).coerceIn(0f, 1f) else 0f
                    onDrawBehind {
                        val barHeight = animatedHeight.value.toPx().coerceAtMost(size.height)
                        val top = size.height - barHeight
                        val barSize = androidx.compose.ui.geometry.Size(size.width, barHeight)
                        val cornerRadius = CornerRadius(if (isSelected) size.minDimension / 2f else defaultRadius)
                        drawRoundRect(
                            color = animatedColor.value,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                            size = barSize,
                            cornerRadius = cornerRadius,
                        )
                        if (mobileShare > 0f) {
                            // Mobile data stacks above Wi-Fi, with a 2 dp gap.
                            val path = androidx.compose.ui.graphics.Path().apply {
                                addRoundRect(androidx.compose.ui.geometry.RoundRect(androidx.compose.ui.geometry.Rect(androidx.compose.ui.geometry.Offset(0f, top), barSize), cornerRadius))
                            }
                            clipPath(path) {
                                val mobileHeight = barHeight * mobileShare
                                drawRect(
                                    color = if (hasSelection && !isSelected) palette.accent.copy(alpha = 0.38f) else palette.accent,
                                    topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                                    size = androidx.compose.ui.geometry.Size(size.width, mobileHeight),
                                )
                                drawLine(
                                    color = palette.surface2,
                                    start = androidx.compose.ui.geometry.Offset(0f, top + mobileHeight),
                                    end = androidx.compose.ui.geometry.Offset(size.width, top + mobileHeight),
                                    strokeWidth = 2.dp.toPx(),
                                )
                            }
                        }
                        if (borderColor != null) {
                            drawRoundRect(
                                color = borderColor,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                                size = barSize,
                                cornerRadius = cornerRadius,
                                style = borderStroke,
                            )
                        }
                        if (averageHeight != null) {
                            val y = size.height - averageHeight.toPx()
                            drawLine(
                                color = palette.muted,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                            )
                        }
                    }
                },
        )

        // Label with tonal pill for selection
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .background(palette.surface2, shape = CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    } else {
                        Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            ForceLtr {
                val labelText = if (isCrowded && bar.shortLabel != null) bar.shortLabel else bar.label
                Text(
                    labelText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) palette.text else palette.muted,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Marker dot under selected label (fixed height so layout never shifts)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(palette.primary, CircleShape),
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}



