package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanCard
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanTile

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlansBento(
    cards: List<PlanCardUi>,
    onOpenPlan: (Long) -> Unit,
    onPinPlan: (Long, Boolean) -> Unit,
    onArchivePlan: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return

    val fontScale = LocalConfiguration.current.fontScale
    val singleColumn = fontScale >= 1.5f

    val frontPlan = cards.first()
    val otherPlans = cards.drop(1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Front Plan: Large PlanCard
        PlanCardWithMenu(
            model = frontPlan,
            onClick = { onOpenPlan(frontPlan.id) },
            onPinToggle = { onPinPlan(frontPlan.id, !frontPlan.pinned) },
            onArchive = { onArchivePlan(frontPlan.id) },
        )

        // Other plans as 2-column grid or single column at large font scale
        if (otherPlans.isNotEmpty()) {
            if (singleColumn) {
                otherPlans.forEach { plan ->
                    PlanTileWithMenu(
                        model = plan,
                        onClick = { onOpenPlan(plan.id) },
                        onPinToggle = { onPinPlan(plan.id, !plan.pinned) },
                        onArchive = { onArchivePlan(plan.id) },
                    )
                }
            } else {
                val chunked = otherPlans.chunked(2)
                chunked.forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PlanTileWithMenu(
                                model = rowPair[0],
                                onClick = { onOpenPlan(rowPair[0].id) },
                                onPinToggle = { onPinPlan(rowPair[0].id, !rowPair[0].pinned) },
                                onArchive = { onArchivePlan(rowPair[0].id) },
                            )
                        }
                        if (rowPair.size > 1) {
                            Box(modifier = Modifier.weight(1f)) {
                                PlanTileWithMenu(
                                    model = rowPair[1],
                                    onClick = { onOpenPlan(rowPair[1].id) },
                                    onPinToggle = { onPinPlan(rowPair[1].id, !rowPair[1].pinned) },
                                    onArchive = { onArchivePlan(rowPair[1].id) },
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlanCardWithMenu(
    model: PlanCardUi,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val pinLabel = if (model.pinned) "Unpin" else "Pin"
    val menuActions = listOf(
        CustomAccessibilityAction(pinLabel) { onPinToggle(); true },
        CustomAccessibilityAction("Archive") { onArchive(); true },
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuOpen = true },
            ),
    ) {
        PlanCard(
            model = model,
            onClick = onClick,
            modifier = Modifier.semantics {
                customActions = menuActions
            },
        )

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Plan options for ${model.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text(pinLabel) },
                    onClick = {
                        menuOpen = false
                        onPinToggle()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuOpen = false
                        onClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Archive") },
                    onClick = {
                        menuOpen = false
                        onArchive()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlanTileWithMenu(
    model: PlanCardUi,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val pinLabel = if (model.pinned) "Unpin" else "Pin"
    val menuActions = listOf(
        CustomAccessibilityAction(pinLabel) { onPinToggle(); true },
        CustomAccessibilityAction("Archive") { onArchive(); true },
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuOpen = true },
            ),
    ) {
        PlanTile(
            model = model,
            onClick = onClick,
            modifier = Modifier.semantics {
                customActions = menuActions
            },
        )

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp)) {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Plan options for ${model.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text(pinLabel) },
                    onClick = {
                        menuOpen = false
                        onPinToggle()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuOpen = false
                        onClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Archive") },
                    onClick = {
                        menuOpen = false
                        onArchive()
                    },
                )
            }
        }
    }
}
