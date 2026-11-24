package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanCard
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

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
        PlanCardWithMenu(
            model = frontPlan,
            onClick = { onOpenPlan(frontPlan.id) },
            onPinToggle = { onPinPlan(frontPlan.id, !frontPlan.pinned) },
            onArchive = { onArchivePlan(frontPlan.id) },
            delayMillis = 0,
        )

        if (otherPlans.isNotEmpty()) {
            if (singleColumn) {
                otherPlans.forEachIndexed { index, plan ->
                    PlanTileWithMenu(
                        model = plan,
                        onClick = { onOpenPlan(plan.id) },
                        onPinToggle = { onPinPlan(plan.id, !plan.pinned) },
                        onArchive = { onArchivePlan(plan.id) },
                        delayMillis = (index + 1) * 40,
                    )
                }
            } else {
                val chunked = otherPlans.chunked(2)
                chunked.forEachIndexed { rowIndex, rowPair ->
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
                                delayMillis = (rowIndex * 2 + 1) * 40,
                            )
                        }
                        if (rowPair.size > 1) {
                            Box(modifier = Modifier.weight(1f)) {
                                PlanTileWithMenu(
                                    model = rowPair[1],
                                    onClick = { onOpenPlan(rowPair[1].id) },
                                    onPinToggle = { onPinPlan(rowPair[1].id, !rowPair[1].pinned) },
                                    onArchive = { onArchivePlan(rowPair[1].id) },
                                    delayMillis = (rowIndex * 2 + 2) * 40,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanCardWithMenu(
    model: PlanCardUi,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
) {
    val palette = LocalEmberbytePalette.current
    var menuOpen by remember { mutableStateOf(false) }
    val pinLabel = stringResource(if (model.pinned) R.string.plan_menu_unpin else R.string.plan_menu_pin)
    val archiveLabel = stringResource(R.string.plan_menu_archive)
    val menuActions = listOf(
        CustomAccessibilityAction(pinLabel) { onPinToggle(); true },
        CustomAccessibilityAction(archiveLabel) { onArchive(); true },
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
            delayMillis = delayMillis,
        )

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
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
            ActivePlanDropdownMenu(
                expanded = menuOpen,
                pinLabel = pinLabel,
                onDismiss = { menuOpen = false },
                onPinToggle = onPinToggle,
                onEdit = onClick,
                onArchive = onArchive,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanTileWithMenu(
    model: PlanCardUi,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
) {
    val palette = LocalEmberbytePalette.current
    var menuOpen by remember { mutableStateOf(false) }
    val pinLabel = stringResource(if (model.pinned) R.string.plan_menu_unpin else R.string.plan_menu_pin)
    val archiveLabel = stringResource(R.string.plan_menu_archive)
    val menuActions = listOf(
        CustomAccessibilityAction(pinLabel) { onPinToggle(); true },
        CustomAccessibilityAction(archiveLabel) { onArchive(); true },
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
            delayMillis = delayMillis,
        )

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp)) {
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
            ActivePlanDropdownMenu(
                expanded = menuOpen,
                pinLabel = pinLabel,
                onDismiss = { menuOpen = false },
                onPinToggle = onPinToggle,
                onEdit = onClick,
                onArchive = onArchive,
            )
        }
    }
}

internal val PlanDropdownOffset = DpOffset(0.dp, 4.dp)

@Composable
private fun ActivePlanDropdownMenu(
    expanded: Boolean,
    pinLabel: String,
    onDismiss: () -> Unit,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = PlanDropdownOffset,
        containerColor = palette.surface,
    ) {
        DropdownMenuItem(
            text = { Text(pinLabel) },
            leadingIcon = { Icon(Icons.Rounded.PushPin, contentDescription = null) },
            onClick = {
                onDismiss()
                onPinToggle()
            },
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.plan_menu_edit)) },
            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            onClick = {
                onDismiss()
                onEdit()
            },
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.plan_menu_archive)) },
            leadingIcon = { Icon(Icons.Rounded.Archive, contentDescription = null) },
            colors = MenuDefaults.itemColors(
                textColor = palette.danger,
                leadingIconColor = palette.danger,
            ),
            onClick = {
                onDismiss()
                onArchive()
            },
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ArchivedPlanCardWithMenu(
    model: PlanCardUi,
    onClick: () -> Unit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var menuOpen by remember { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val unarchiveLabel = stringResource(R.string.plan_menu_unarchive)
    val deleteLabel = stringResource(R.string.plan_menu_delete)

    if (showDeleteConfirm) {
        EmberbyteDialog(
            onDismiss = { showDeleteConfirm = false },
            title = stringResource(R.string.plan_delete_dialog_title),
            icon = Icons.Rounded.Delete,
            destructive = true,
            confirm = DialogAction(
                label = stringResource(R.string.plan_delete_dialog_confirm),
                onClick = {
                    showDeleteConfirm = false
                    onDelete()
                },
            ),
            dismiss = DialogAction(
                label = stringResource(R.string.dialog_cancel),
                onClick = { showDeleteConfirm = false },
            ),
        ) {
            Text(
                text = stringResource(R.string.plan_delete_dialog_body),
                color = palette.muted,
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuOpen = true },
            ),
    ) {
        PlanCard(
            model = model.copy(forecastText = null),
            onClick = onClick,
            modifier = Modifier
                .alpha(0.6f)
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(unarchiveLabel) { onUnarchive(); true },
                        CustomAccessibilityAction(deleteLabel) { showDeleteConfirm = true; true },
                    )
                },
        )

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
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
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                offset = DpOffset(0.dp, 4.dp),
                containerColor = palette.surface,
            ) {
                DropdownMenuItem(
                    text = { Text(unarchiveLabel) },
                    leadingIcon = { Icon(Icons.Rounded.Unarchive, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onUnarchive()
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(deleteLabel) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                    colors = MenuDefaults.itemColors(
                        textColor = palette.danger,
                        leadingIconColor = palette.danger,
                    ),
                    onClick = {
                        menuOpen = false
                        showDeleteConfirm = true
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                )
            }
        }
    }
}
