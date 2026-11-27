package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExtrasSection(
    addOns: List<AddOnDraft>,
    freeRules: List<FreeRuleDraft>,
    availableApps: List<AppChoiceUi>,
    unitSystem: UnitSystem,
    errors: Map<String, String>,
    onAddAddOn: () -> Unit,
    onUpdateAddOn: (Int, AddOnDraft) -> Unit,
    onRemoveAddOn: (Int) -> Unit,
    onAddFreeRule: () -> Unit,
    onUpdateFreeRule: (Int, FreeRuleDraft) -> Unit,
    onRemoveFreeRule: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0] ?: Locale.ENGLISH
    val totalCount = addOns.size + freeRules.size
    var expanded by remember(totalCount > 0) { mutableStateOf(totalCount > 0) }

    EditorSectionCard(title = stringResource(R.string.plan_editor_extras), modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.plan_editor_extras_count, totalCount),
                style = EmberbyteType.heading(PlanFlowType.Body),
                color = palette.text,
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) stringResource(R.string.plan_editor_collapse) else stringResource(R.string.plan_editor_expand),
                tint = palette.muted,
            )
        }

        if (expanded) {
            Text(
                text = stringResource(R.string.plan_editor_addons),
                style = EmberbyteType.heading(PlanFlowType.Caption),
                color = palette.muted,
            )
            addOns.forEachIndexed { index, draft ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = palette.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = draft.label,
                                onValueChange = { onUpdateAddOn(index, draft.copy(label = it)) },
                                label = { Text(stringResource(R.string.plan_editor_addon_label)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { onRemoveAddOn(index) },
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.plan_editor_remove_addon))
                            }
                        }
                        OutlinedTextField(
                            value = draft.amountText,
                            onValueChange = { onUpdateAddOn(index, draft.copy(amountText = it)) },
                            label = { Text(stringResource(R.string.plan_editor_amount)) },
                            singleLine = true,
                            isError = errors.containsKey("addOn.$index.bytes"),
                            supportingText = errors["addOn.$index.bytes"]?.let { msg -> { Text(msg) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        val units = if (unitSystem == UnitSystem.BINARY) listOf("MiB", "GiB", "TiB") else listOf("MB", "GB", "TB")
                        ConnectedButtonGroup(
                            options = units,
                            selectedIndex = draft.unit.ordinal,
                            onSelect = { u -> onUpdateAddOn(index, draft.copy(unit = CapUnit.entries[u])) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DateField(
                            label = stringResource(R.string.plan_editor_valid_from),
                            date = draft.validFrom,
                            onChange = { onUpdateAddOn(index, draft.copy(validFrom = it)) },
                        )
                        DateField(
                            label = stringResource(R.string.plan_editor_valid_until),
                            date = draft.validUntil,
                            onChange = { onUpdateAddOn(index, draft.copy(validUntil = it)) },
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = onAddAddOn,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text(stringResource(R.string.plan_editor_add_addon), modifier = Modifier.padding(start = 6.dp))
            }

            Text(
                text = stringResource(R.string.plan_editor_free_windows),
                style = EmberbyteType.heading(PlanFlowType.Caption),
                color = palette.muted,
            )
            freeRules.forEachIndexed { index, draft ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = palette.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = draft.label,
                                onValueChange = { onUpdateFreeRule(index, draft.copy(label = it)) },
                                label = { Text(stringResource(R.string.plan_editor_window_label)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { onRemoveFreeRule(index) },
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.plan_editor_remove_window))
                            }
                        }
                        FlowRow(
                            modifier = Modifier.testTag("free_window_days_multi_select"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            DayOfWeek.entries.forEach { day ->
                                FilterChip(
                                    selected = day in draft.days,
                                    onClick = {
                                        val next = if (day in draft.days) draft.days - day else draft.days + day
                                        onUpdateFreeRule(index, draft.copy(days = next))
                                    },
                                    label = { Text(day.getDisplayName(TextStyle.SHORT, locale)) },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                                )
                            }
                        }
                        TimeField(
                            label = stringResource(R.string.plan_editor_start_time),
                            time = draft.start,
                            onChange = { onUpdateFreeRule(index, draft.copy(start = it)) },
                        )
                        TimeField(
                            label = stringResource(R.string.plan_editor_end_time),
                            time = draft.end,
                            onChange = { onUpdateFreeRule(index, draft.copy(end = it)) },
                        )
                        if (availableApps.isNotEmpty()) {
                            var appMenuOpen by remember { mutableStateOf(false) }
                            val currentApp = availableApps.firstOrNull { it.packageName == draft.packageName }?.label ?: stringResource(R.string.plan_editor_all_apps)
                            androidx.compose.foundation.layout.Box {
                                OutlinedButton(
                                    onClick = { appMenuOpen = true },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                ) {
                                    Text(stringResource(R.string.plan_editor_app_choice, currentApp))
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = appMenuOpen,
                                    onDismissRequest = { appMenuOpen = false },
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(stringResource(R.string.plan_editor_all_apps)) },
                                        onClick = {
                                            appMenuOpen = false
                                            onUpdateFreeRule(index, draft.copy(packageName = null))
                                        },
                                    )
                                    availableApps.forEach { app ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(app.label) },
                                            onClick = {
                                                appMenuOpen = false
                                                onUpdateFreeRule(index, draft.copy(packageName = app.packageName))
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onAddFreeRule,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text(stringResource(R.string.plan_editor_add_window), modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}
