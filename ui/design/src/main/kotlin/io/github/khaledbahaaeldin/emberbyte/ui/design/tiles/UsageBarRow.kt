package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.spokenUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

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
) {
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = summary },
    ) {

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
                Column(
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Box(
                        Modifier
                            .widthIn(max = 24.dp)
                            .fillMaxWidth()
                            .height((8 + 56 * (bar.bytes.toFloat() / max)).dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            )
                            .then(
                                if (!isSelected) {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
                                } else {
                                    Modifier
                                }
                            ),
                    )
                    ForceLtr {
                        Text(
                            bar.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
                val fillWidth = (totalWidth * clampedFraction).coerceAtLeast(4.dp)
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


