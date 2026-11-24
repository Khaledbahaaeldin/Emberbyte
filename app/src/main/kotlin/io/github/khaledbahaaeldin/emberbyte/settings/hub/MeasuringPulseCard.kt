package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.settings.HealthAction
import io.github.khaledbahaaeldin.emberbyte.settings.MeasuringHealth
import io.github.khaledbahaaeldin.emberbyte.settings.SampleHour
import io.github.khaledbahaaeldin.emberbyte.settings.missedHours
import io.github.khaledbahaaeldin.emberbyte.settings.missedRange
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
internal fun MeasuringPulseCard(
    health: MeasuringHealth,
    strip: List<SampleHour>,
    lastSampleAt: Instant?,
    now: Instant,
    zone: ZoneId,
    amoled: Boolean,
    onOpenKeepMeasuring: () -> Unit,
    onAction: (HealthAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val isWarning = health.primary != null
    val baseSurface = if (amoled) palette.surface else palette.surface2
    val tintTarget = if (isWarning) palette.warn else palette.primary
    val topColor = lerp(baseSurface, tintTarget, if (amoled) 0.18f else if (isWarning) 0.34f else 0.30f)
    val bottomColor = lerp(baseSurface, tintTarget, if (amoled) 0.08f else 0.14f)

    val (headline, body, ctaLabel) = pulseCopy(health, missedHours(strip))
    val lastSampleText = formatLastSample(lastSampleAt, now)
    val missedCount = missedHours(strip)
    val rangeText = missedRange(strip, zone)
    val stripDescription = if (missedCount > 0 && rangeText != null) {
        "$missedCount hours missed between $rangeText"
    } else if (strip.isEmpty()) {
        "No samples yet"
    } else {
        "All 24 hours sampled"
    }

    val shape = mixedCorners(large = 40.dp, small = 14.dp, smallCorner = Corner.BottomStart)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pulse_card")
            .semantics {
                contentDescription = "Measuring, ${health.score} of 4 checks pass. $headline"
            }
            .clip(shape)
            .clickable(onClick = onOpenKeepMeasuring),
        shape = shape,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WavyRing(
                    fraction = (health.score / 4f).coerceIn(0f, 1f),
                    state = if (isWarning) BudgetStateUi.Warning else BudgetStateUi.Ok,
                    size = 118.dp,
                    label = "${health.score}/4",
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isWarning) palette.warn else palette.primary),
                        )
                        Text(
                            text = lastSampleText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Caption),
                            color = palette.muted,
                        )
                    }
                    Text(
                        text = headline,
                        style = EmberbyteType.heading(SettingsType.PulseHeadline),
                        color = palette.text,
                    )
                    Text(
                        text = body,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.PulseBody),
                        color = palette.muted,
                    )
                    val action = health.primary
                    if (action != null && ctaLabel != null) {
                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = { onAction(action) },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("pulse_action_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWarning) palette.warn else palette.primary,
                                contentColor = palette.background,
                            ),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(ctaLabel, style = EmberbyteType.heading(SettingsType.PulseBody))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = stripDescription },
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Samples taken, last 24 h",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro),
                        color = palette.muted,
                    )
                    if (missedCount > 0) {
                        Text(
                            text = "$missedCount h missed",
                            style = EmberbyteType.heading(SettingsType.Micro),
                            color = palette.warn,
                        )
                    }
                }

                if (strip.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(palette.surface.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = "No samples recorded yet",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro),
                            color = palette.muted,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }
                } else {
                    SampleStripCanvas(
                        strip = strip,
                        normalColor = palette.primary,
                        gapColor = palette.warn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("24 h ago", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro), color = palette.muted)
                        Text("12 h", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro), color = palette.muted)
                        Text("now", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro), color = palette.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleStripCanvas(
    strip: List<SampleHour>,
    normalColor: Color,
    gapColor: Color,
    modifier: Modifier = Modifier,
) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
    Canvas(modifier = modifier) {
        if (strip.isEmpty()) return@Canvas
        val gapPx = 3.dp.toPx()
        val barWidth = ((size.width - gapPx * (strip.size - 1)) / strip.size).coerceAtLeast(2f)
        val radius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        strip.forEachIndexed { index, hour ->
            val x = index * (barWidth + gapPx)
            val h = (size.height * hour.height).coerceAtLeast(4.dp.toPx())
            val y = size.height - h
            if (hour.isGap) {
                drawRoundRect(
                    color = gapColor.copy(alpha = 0.22f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, h),
                    cornerRadius = radius,
                )
                drawRoundRect(
                    color = gapColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, h),
                    cornerRadius = radius,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = dash),
                )
            } else {
                drawRoundRect(
                    color = normalColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, h),
                    cornerRadius = radius,
                )
            }
        }
    }
}

private fun pulseCopy(health: MeasuringHealth, missedHours: Int): Triple<String, String, String?> =
    when {
        health.pausedWithGap -> Triple(
            "Android paused you last night",
            "$missedHours hours of usage were not counted. Allow background use to stop that.",
            "Fix battery",
        )
        health.primary == HealthAction.TurnOnLive -> Triple(
            "Numbers are stale",
            "Turn on the live notification so Emberbyte keeps sampling in the background.",
            "Turn on",
        )
        health.primary == HealthAction.AllowUsageAccess -> Triple(
            "Usage access is off",
            "Allow usage access so Emberbyte can read per-app mobile and Wi-Fi totals.",
            "Allow",
        )
        health.primary == HealthAction.FixBattery -> Triple(
            "Battery restriction active",
            "Allow unrestricted background use so Android does not pause counting.",
            "Fix battery",
        )
        else -> Triple(
            "Counting everything",
            "Sampling every 15 minutes, even with the screen off.",
            null,
        )
    }

private fun formatLastSample(lastSampleAt: Instant?, now: Instant): String {
    if (lastSampleAt == null) return "Waiting for first sample"
    val mins = ChronoUnit.MINUTES.between(lastSampleAt, now).coerceAtLeast(0L)
    return when {
        mins < 1L -> "Last sample just now"
        mins < 60L -> "Last sample $mins min ago"
        else -> "Last sample ${mins / 60L} h ago"
    }
}
