package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AlertsSection(
    thresholds: Set<Int>,
    onToggleThreshold: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val levels = listOf(50, 80, 90, 100)
    val enabled = thresholds.isNotEmpty()

    EditorSectionCard(title = stringResource(R.string.plan_editor_alerts), modifier = modifier) {
        val toggleAll = { on: Boolean ->
            if (on && thresholds.isEmpty()) {
                onToggleThreshold(80)
                onToggleThreshold(100)
            } else if (!on) {
                thresholds.toList().forEach(onToggleThreshold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .toggleable(value = enabled, role = Role.Switch, onValueChange = toggleAll),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.plan_editor_notify_me),
                style = EmberbyteType.heading(PlanFlowType.Body),
                color = palette.text,
            )
            Switch(
                checked = enabled,
                onCheckedChange = null,
            )
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("alerts_multi_select"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            levels.forEach { pct ->
                FilterChip(
                    selected = pct in thresholds,
                    onClick = { onToggleThreshold(pct) },
                    label = { Text("$pct%") },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                )
            }
        }

        Text(
            text = stringResource(R.string.plan_editor_alerts_note),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
            color = palette.muted,
        )
    }
}

@Composable
internal fun OnHomeSection(
    pinned: Boolean,
    onSetPinned: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    EditorSectionCard(title = stringResource(R.string.plan_editor_on_home), modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .toggleable(value = pinned, role = Role.Switch, onValueChange = onSetPinned),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.plan_editor_pin_title),
                    style = EmberbyteType.heading(PlanFlowType.Body),
                    color = palette.text,
                )
                Text(
                    text = stringResource(R.string.plan_editor_pin_body),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                    color = palette.muted,
                )
            }
            Switch(
                checked = pinned,
                onCheckedChange = null,
            )
        }
    }
}
