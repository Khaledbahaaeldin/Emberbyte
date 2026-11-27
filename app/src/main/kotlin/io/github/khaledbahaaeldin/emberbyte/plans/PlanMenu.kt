package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

internal val PlanMenuDropdownOffset = DpOffset(0.dp, 4.dp)

@Composable
fun PlanMenu(
    expanded: Boolean,
    pinned: Boolean,
    showPin: Boolean,
    onDismiss: () -> Unit,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val pinLabel = stringResource(if (pinned) R.string.plan_menu_unpin else R.string.plan_menu_pin_front)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = PlanMenuDropdownOffset,
        containerColor = palette.surface,
    ) {
        if (showPin) {
            DropdownMenuItem(
                text = { Text(pinLabel) },
                leadingIcon = { Icon(Icons.Rounded.PushPin, contentDescription = null) },
                onClick = {
                    onDismiss()
                    onPinToggle()
                },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            )
        }
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
            text = { Text(stringResource(R.string.plan_menu_delete)) },
            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            colors = MenuDefaults.itemColors(
                textColor = palette.danger,
                leadingIconColor = palette.danger,
            ),
            onClick = {
                onDismiss()
                onDelete()
            },
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        )
    }
}
