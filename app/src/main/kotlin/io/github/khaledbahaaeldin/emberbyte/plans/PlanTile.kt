@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.accentText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.warnText

@Composable
fun PlanTileCard(
    model: PlanCardUi,
    index: Int,
    onPinToFront: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var menuOpen by remember { mutableStateOf(false) }

    val smallCorner = if (index % 2 == 0) Corner.TopEnd else Corner.BottomStart
    val shape = mixedCorners(large = 28.dp, small = 12.dp, smallCorner = smallCorner)
    val baseContainer = lerp(palette.surface, palette.text, 0.08f)
    val containerColor = when (model.statusKind) {
        PlanStatusKind.RunsOutEarly -> lerp(baseContainer, palette.warn, 0.20f)
        PlanStatusKind.OverCap -> lerp(baseContainer, palette.danger, 0.22f)
        else -> baseContainer
    }
    val statusColor = when (model.statusKind) {
        PlanStatusKind.OverCap -> palette.danger
        PlanStatusKind.RunsOutEarly -> palette.warnText
        else -> palette.accentText
    }
    val statusLine = when (model.statusKind) {
        PlanStatusKind.OverCap -> stringResource(R.string.plan_status_over_cap)
        PlanStatusKind.RunsOutEarly -> stringResource(R.string.plan_status_runs_out_date, model.runOutDateText ?: "")
        else -> stringResource(R.string.plan_status_on_track)
    }

    val tapLabel = stringResource(R.string.plan_tile_pin_a11y)
    val tileDescription = "${model.name}, ${model.remainingText}, ${model.daysLeftText}, $statusLine. $tapLabel"

    Surface(
        onClick = onPinToFront,
        shape = shape,
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 176.dp)
            .pressMorph(shape)
            .semantics(mergeDescendants = false) {
                contentDescription = tileDescription
                customActions = listOf(
                    CustomAccessibilityAction(tapLabel) { onPinToFront(); true },
                )
            },
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Name + 40-48 dp kebab ABOVE and to the side of the ring (never overlapping the ring, B5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
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
                        onPinToggle = onPinToFront,
                        onEdit = onEdit,
                        onDelete = onDelete,
                    )
                }
            }

            // 78 dp wavy ring with percent in the centre
            WavyRing(
                fraction = model.fraction,
                state = model.ringState,
                size = 78.dp,
                label = model.percentText,
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ForceLtr {
                    Text(
                        text = model.remainingText,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ForceLtr {
                    Text(
                        text = model.daysLeftText,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.muted,
                        maxLines = 1,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                )
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
