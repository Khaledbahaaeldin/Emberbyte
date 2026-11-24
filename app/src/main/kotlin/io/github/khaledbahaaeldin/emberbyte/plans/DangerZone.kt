package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType

@Composable
internal fun DangerZone(
    planName: String,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var showConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = mixedCorners(large = 28.dp, small = 10.dp, smallCorner = Corner.BottomEnd),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.danger.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.plan_editor_danger_zone),
                style = EmberbyteType.heading(PlanFlowType.Micro).copy(letterSpacing = 0.08.em),
                color = palette.danger,
                modifier = Modifier.semantics { heading() },
            )
            OutlinedButton(
                onClick = { showConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.danger),
                border = BorderStroke(1.dp, palette.danger.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Text(stringResource(R.string.plan_editor_delete_plan))
            }
        }
    }

    if (showConfirm) {
        val displayName = planName.trim().ifEmpty { stringResource(R.string.plan_editor_this_plan) }
        EmberbyteDialog(
            onDismiss = { showConfirm = false },
            title = stringResource(R.string.plan_editor_delete_title, displayName),
            icon = Icons.Rounded.DeleteOutline,
            destructive = true,
            confirm = DialogAction(
                label = stringResource(R.string.plan_editor_delete),
                onClick = {
                    showConfirm = false
                    onDeleteConfirmed()
                },
            ),
            dismiss = DialogAction(
                label = stringResource(R.string.plan_editor_cancel),
                onClick = { showConfirm = false },
            ),
        ) {
            Text(stringResource(R.string.plan_editor_delete_body))
        }
    }
}
