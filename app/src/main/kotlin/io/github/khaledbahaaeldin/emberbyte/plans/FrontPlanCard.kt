@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.accentText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.warnText

@Composable
fun FrontPlanCard(
    model: PlanCardUi,
    expandedDetails: Boolean,
    whatIfExtraGb: Int,
    whatIfSentence: String?,
    onOpenPlan: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
    onToggleDetails: () -> Unit,
    onSelectWhatIf: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    var menuOpen by remember { mutableStateOf(false) }

    val cardShape = mixedCorners(large = 34.dp, small = 12.dp, smallCorner = Corner.BottomStart)
    val containerColor = when (model.statusKind) {
        PlanStatusKind.RunsOutEarly -> lerp(palette.surface2, palette.warn, 0.20f)
        PlanStatusKind.OverCap -> lerp(palette.surface2, palette.danger, 0.20f)
        else -> palette.surface2
    }
    val statusColor = when (model.statusKind) {
        PlanStatusKind.OverCap -> palette.danger
        PlanStatusKind.RunsOutEarly -> palette.warnText
        else -> palette.accentText
    }
    val statusIcon = when (model.statusKind) {
        PlanStatusKind.OverCap -> Icons.Rounded.Error
        PlanStatusKind.RunsOutEarly -> Icons.Rounded.Warning
        else -> Icons.Rounded.CheckCircle
    }
    val statusLabel = when (model.statusKind) {
        PlanStatusKind.OverCap -> stringResource(R.string.plan_status_over_cap)
        PlanStatusKind.RunsOutEarly -> {
            val date = model.runOutDateText ?: ""
            val early = model.earlyDaysText
            if (early != null) stringResource(R.string.plan_status_runs_out_early, date, early)
            else stringResource(R.string.plan_status_runs_out_date, date)
        }
        else -> stringResource(R.string.plan_status_will_last)
    }

    val pinLabel = stringResource(if (model.pinned) R.string.plan_menu_unpin else R.string.plan_menu_pin_front)
    val deleteLabel = stringResource(R.string.plan_menu_delete)
    val cardDescription = "${model.name} ${model.capText}, ${model.remainingText} of ${model.capText}, $statusLabel"

    val barProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(reduceMotion, model.id) {
        if (reduceMotion) {
            barProgress.snapTo(1f)
        } else {
            barProgress.animateTo(1f, motion.spatial())
        }
    }

    Surface(
        onClick = onOpenPlan,
        shape = cardShape,
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .pressMorph(cardShape)
            .semantics(mergeDescendants = false) {
                contentDescription = cardDescription
                customActions = listOf(
                    CustomAccessibilityAction(pinLabel) { onPinToggle(); true },
                    CustomAccessibilityAction(deleteLabel) { onDelete(); true },
                )
            },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Title row + Front plan chip
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    model.simLabel?.let { sim ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = palette.surface,
                            modifier = Modifier.padding(horizontal = 6.dp),
                        ) {
                            Text(
                                text = sim,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                    Box {
                        IconButton(
                            onClick = { menuOpen = true },
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = palette.muted),
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.plan_menu_options_desc, model.name),
                            )
                        }
                        PlanMenu(
                            expanded = menuOpen,
                            pinned = model.pinned,
                            showPin = true,
                            onDismiss = { menuOpen = false },
                            onPinToggle = onPinToggle,
                            onEdit = onOpenPlan,
                            onDelete = onDelete,
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = palette.primary.copy(alpha = 0.14f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.plan_front_chip),
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.primary,
                        )
                    }
                }
            }

            // Ring + remaining number + status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WavyRing(
                    fraction = model.fraction,
                    state = model.ringState,
                    size = 132.dp,
                    label = stringResource(R.string.plan_ring_used_format, model.percentText),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ForceLtr {
                        Text(
                            text = model.remainingText,
                            style = MaterialTheme.typography.displaySmallEmphasized.copy(fontWeight = FontWeight.Bold),
                            color = palette.text,
                        )
                    }
                    ForceLtr {
                        Text(
                            text = model.usedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted,
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = statusColor.copy(alpha = 0.14f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                            )
                        }
                    }
                }
            }

            // Time and Data tracks
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val p = barProgress.value
                TrackRow(
                    label = stringResource(R.string.plan_track_time),
                    detail = stringResource(R.string.plan_track_time_sub, model.dayOfCycle, model.cycleLength),
                    fraction = (model.timeFraction * p).coerceIn(0f, 1f),
                    color = palette.muted,
                )
                TrackRow(
                    label = stringResource(R.string.plan_track_data),
                    detail = model.percentText,
                    fraction = (model.fraction * p).coerceIn(0f, 1f),
                    color = statusColor,
                )
            }

            // Seven-day mini chart (44 dp tall)
            if (model.weekBars.isNotEmpty()) {
                val p = barProgress.value
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        val bars = model.weekBars.takeLast(7)
                        bars.forEachIndexed { idx, value ->
                            val isToday = idx == bars.lastIndex
                            val barFraction = (value * p).coerceIn(0.08f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(barFraction)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (isToday) palette.primary else palette.primary.copy(alpha = 0.55f),
                                    ),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.plan_chart_7_days_ago),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.muted,
                        )
                        Text(
                            text = stringResource(R.string.plan_chart_today),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.muted,
                        )
                    }
                }
            }

            // Fact chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FactChip(model.daysLeftText)
                if (model.renewsText.isNotEmpty()) {
                    FactChip(model.renewsText)
                }
                model.allowancePerDayText?.let { FactChip(it) }
            }

            // Action row: Details and what if | Edit
            val actionLabels = listOf(
                stringResource(R.string.plan_details_what_if),
                stringResource(R.string.plan_menu_edit),
            )
            ConnectedButtonGroup(
                options = actionLabels,
                selectedIndex = if (expandedDetails) 0 else 1,
                onSelect = { idx ->
                    if (idx == 0) onToggleDetails() else onOpenPlan()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Expandable inline What-if (never saved)
            EmberbyteAnimatedVisibility(visible = expandedDetails) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val options = listOf(0, 1, 5, 10)
                    val labels = listOf(
                        stringResource(R.string.plan_what_if_none),
                        stringResource(R.string.plan_what_if_plus_gb, 1),
                        stringResource(R.string.plan_what_if_plus_gb, 5),
                        stringResource(R.string.plan_what_if_plus_gb, 10),
                    )
                    ConnectedButtonGroup(
                        options = labels,
                        selectedIndex = options.indexOf(whatIfExtraGb).coerceAtLeast(0),
                        onSelect = { idx -> onSelectWhatIf(options[idx]) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (whatIfSentence != null) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = palette.surface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ForceLtr {
                                Text(
                                    text = whatIfSentence,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.text,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        model.rolloverText?.let { FactChip(it) }
                        model.chips.forEach { FactChip(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    label: String,
    detail: String,
    fraction: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    val palette = LocalEmberbytePalette.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = palette.text)
            Text(text = detail, style = MaterialTheme.typography.labelMedium, color = palette.muted)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(palette.text.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun FactChip(text: String) {
    val palette = LocalEmberbytePalette.current
    Surface(
        shape = MaterialTheme.shapes.small,
        color = palette.surface,
    ) {
        ForceLtr {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = palette.text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
