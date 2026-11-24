@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.settings.StorageBreakdown
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.settings.hub.rememberSettingsAmbientSeconds
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import kotlin.math.PI
import kotlin.math.sin

@Composable
internal fun HubCardContainer(
    title: String,
    subtitle: String,
    tint: Color,
    smallCorner: Corner,
    amoled: Boolean,
    contentDescriptionText: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visual: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val base = if (amoled) palette.surface else palette.surface2
    val bg = lerp(base, tint, if (amoled) 0.14f else 0.18f)
    val shape = mixedCorners(large = 30.dp, small = 10.dp, smallCorner = smallCorner)
    val interaction = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { contentDescription = contentDescriptionText }
            .pressMorph(shape = shape, pressedCorner = 22.dp, interactionSource = interaction)
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = shape,
        color = bg,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            visual()
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = EmberbyteType.heading(SettingsType.CardTitle),
                    color = palette.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Caption),
                    color = palette.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun LookAndFeelCard(
    themeName: String,
    modeLabel: String,
    amoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val seconds by rememberSettingsAmbientSeconds(!reduceMotion)
    val rot = if (reduceMotion) 0f else (seconds * 12f) % 360f

    HubCardContainer(
        title = "Look and feel",
        subtitle = "$themeName · $modeLabel",
        tint = palette.primary,
        smallCorner = Corner.TopEnd,
        amoled = amoled,
        contentDescriptionText = "Look and feel, $themeName, $modeLabel",
        testTag = "hub_card_look_and_feel",
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .graphicsLayer { rotationZ = rot }
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(palette.primary.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = themeName,
                    style = EmberbyteType.heading(SettingsType.PulseBody),
                    color = palette.text,
                    modifier = Modifier.graphicsLayer { rotationZ = -rot },
                )
            }
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy((-6).dp),
            ) {
                Box(Modifier.size(16.dp).clip(CircleShape).background(palette.primary))
                Box(Modifier.size(16.dp).clip(CircleShape).background(palette.accent))
                Box(Modifier.size(16.dp).clip(CircleShape).background(palette.warn))
            }
        }
    }
}

@Composable
internal fun NotificationsCard(
    liveOn: Boolean,
    speedOn: Boolean,
    todayText: String,
    amoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val status = when {
        !liveOn -> "Off"
        speedOn -> "On, speed shown"
        else -> "On"
    }
    HubCardContainer(
        title = "Notifications",
        subtitle = status,
        tint = palette.accent,
        smallCorner = Corner.BottomStart,
        amoled = amoled,
        contentDescriptionText = "Notifications, $status",
        testTag = "hub_card_notifications",
        onClick = onClick,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (liveOn) 1f else 0.45f),
            shape = MaterialTheme.shapes.medium,
            color = palette.surface.copy(alpha = 0.72f),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = todayText,
                        style = EmberbyteType.heading(SettingsType.RowTitle),
                        color = palette.text,
                    )
                    if (speedOn && liveOn) {
                        Text(
                            text = "1.4 MB/s",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro),
                            color = palette.primary,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(palette.surface2),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(palette.primary),
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlertsCard(
    spikesOn: Boolean,
    amoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val status = if (spikesOn) "Spike alerts on" else "Off"
    HubCardContainer(
        title = "Alerts",
        subtitle = status,
        tint = palette.warn,
        smallCorner = Corner.BottomEnd,
        amoled = amoled,
        contentDescriptionText = "Alerts, $status",
        testTag = "hub_card_alerts",
        onClick = onClick,
        modifier = modifier,
    ) {
        val lineColor = if (spikesOn) palette.warn else palette.muted
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(if (spikesOn) 1f else 0.45f),
        ) {
            val pts = listOf(0.25f, 0.30f, 0.22f, 0.85f, 0.40f, 0.28f, 0.32f)
            val stepX = size.width / (pts.size - 1)
            val path = Path().apply {
                pts.forEachIndexed { idx, p ->
                    val x = idx * stepX
                    val y = size.height * (1f - p)
                    if (idx == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(3 * stepX, size.height * (1f - pts[3])),
            )
        }
    }
}

@Composable
internal fun UnitsCard(
    unitSystem: UnitSystem,
    amoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val big = if (unitSystem == UnitSystem.DECIMAL) "GB" else "GiB"
    val sub = if (unitSystem == UnitSystem.DECIMAL) "Decimal · 1000" else "Binary · 1024"
    HubCardContainer(
        title = "Units",
        subtitle = sub,
        tint = palette.primary,
        smallCorner = Corner.TopStart,
        amoled = amoled,
        contentDescriptionText = "Units, $sub",
        testTag = "hub_card_units",
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = big,
            style = EmberbyteType.hero(SettingsType.UnitsDisplay),
            color = palette.primary,
        )
    }
}

@Composable
internal fun DataAndBackupCard(
    storageText: String,
    sinceText: String,
    breakdown: StorageBreakdown,
    backupLabel: String,
    hasBackup: Boolean,
    amoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    HubCardContainer(
        title = "Data and backup",
        subtitle = "$storageText · $sinceText",
        tint = palette.accent,
        smallCorner = Corner.TopEnd,
        amoled = amoled,
        contentDescriptionText = "Data and backup, $storageText, $backupLabel",
        testTag = "hub_card_data",
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (hasBackup) palette.primary.copy(alpha = 0.16f) else palette.warn.copy(alpha = 0.22f),
            ) {
                Text(
                    text = backupLabel,
                    style = EmberbyteType.heading(SettingsType.Micro),
                    color = if (hasBackup) palette.primary else palette.warn,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        StorageMiniBar(breakdown = breakdown)
    }
}

@Composable
internal fun StorageMiniBar(
    breakdown: StorageBreakdown,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val total = breakdown.totalBytes.coerceAtLeast(1L).toFloat()
    val parts = listOf(
        (breakdown.hourlyBytes / total) to palette.primary,
        (breakdown.dailyBytes / total) to palette.accent,
        (breakdown.plansBytes / total) to palette.warn,
        (breakdown.otherBytes / total) to palette.muted.copy(alpha = 0.5f),
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
    ) {
        val gap = 2.dp.toPx()
        var x = 0f
        parts.forEach { (fraction, color) ->
            val w = (size.width * fraction).coerceAtLeast(if (fraction > 0f) 4.dp.toPx() else 0f)
            if (w > 0f && x < size.width) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, 0f),
                    size = Size(w.coerceAtMost(size.width - x), size.height),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
                x += w + gap
            }
        }
    }
}

@Composable
internal fun AboutCard(
    versionName: String,
    updateStatus: String,
    amoled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val seconds by rememberSettingsAmbientSeconds(!reduceMotion)
    val wobble = if (reduceMotion) 0f else sin(seconds * 2.0 * PI / 3.0).toFloat() * 5f

    HubCardContainer(
        title = "About",
        subtitle = "$versionName · $updateStatus",
        tint = palette.primary,
        smallCorner = Corner.BottomStart,
        amoled = amoled,
        contentDescriptionText = "About, $versionName, $updateStatus",
        testTag = "hub_card_about",
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { rotationZ = wobble }
                .clip(MaterialShapes.Sunny.toShape())
                .background(palette.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "E",
                style = EmberbyteType.title(SettingsType.PulseHeadline),
                color = palette.primary,
            )
        }
    }
}
