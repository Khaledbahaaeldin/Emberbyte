package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RenewalTimelineCard(
    events: List<RenewalEvent>,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) return
    val palette = LocalEmberbytePalette.current
    val density = LocalDensity.current
    val locale: Locale = LocalConfiguration.current.locales[0] ?: Locale.ENGLISH
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM", locale) }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface2,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.plan_renewals_title),
                style = MaterialTheme.typography.titleMedium,
                color = palette.text,
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val widthPx = with(density) { maxWidth.toPx() }
                val labelWidthDp = 56.dp
                val labelPx = with(density) { labelWidthDp.toPx() }
                val slots = remember(events, widthPx, labelPx) {
                    layoutRenewalLabels(events, widthPx, labelPx)
                }
                val rowCount = (slots.maxOfOrNull { it.row } ?: 0) + 1
                val rowHeightDp = 34.dp
                val lineYDp = 10.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineYDp + 16.dp + rowHeightDp * rowCount),
                ) {
                    // Horizontal track line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = lineYDp)
                            .height(2.dp)
                            .background(palette.text.copy(alpha = 0.2f)),
                    )

                    slots.forEach { slot ->
                        val maxOffsetPx = (widthPx - labelPx).coerceAtLeast(0f)
                        val xPx = (slot.xFraction * maxOffsetPx).roundToInt()
                        val dotSize = 10.dp
                        val dotCenterOffsetPx = (xPx + labelPx / 2f - with(density) { dotSize.toPx() } / 2f)
                            .roundToInt()
                            .coerceIn(0, (widthPx - with(density) { dotSize.toPx() }).roundToInt().coerceAtLeast(0))

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(dotCenterOffsetPx, with(density) { (lineYDp - 4.dp).roundToPx() }) }
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        slot.event.isToday -> palette.text
                                        slot.event.isUpcoming -> palette.muted
                                        else -> palette.primary
                                    },
                                ),
                        )

                        Column(
                            modifier = Modifier
                                .offset {
                                    val yPx = with(density) { (lineYDp + 12.dp + rowHeightDp * slot.row).roundToPx() }
                                    IntOffset(xPx, yPx)
                                }
                                .width(labelWidthDp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = slot.event.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = dateFormatter.format(slot.event.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.muted,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
