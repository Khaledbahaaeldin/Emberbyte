package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.engine.model.BudgetPeriod
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.flow.filterDecimalInput
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun BudgetEditorDialog(
    editor: BudgetEditorUi,
    planOptions: List<PlanOptionUi>,
    onEvent: (AppDetailEvent) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0] ?: Locale.ENGLISH
    val decimalSeparator = remember(locale) { DecimalFormatSymbols.getInstance(locale).decimalSeparator }

    EmberbyteDialog(
        onDismiss = { onEvent(AppDetailEvent.CloseBudgetEditor) },
        title = stringResource(
            if (editor.isEdit) R.string.dialog_budget_edit_title else R.string.dialog_budget_set_title,
        ),
        icon = Icons.Rounded.Tune,
        confirm = DialogAction(
            label = stringResource(R.string.dialog_budget_save),
            onClick = { onEvent(AppDetailEvent.SaveBudget) },
            enabled = !editor.saving,
        ),
        dismiss = DialogAction(
            label = stringResource(R.string.dialog_cancel),
            onClick = { onEvent(AppDetailEvent.CloseBudgetEditor) },
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val limitError = editor.errors["limitBytes"]
            OutlinedTextField(
                value = editor.limitText,
                onValueChange = { raw ->
                    val filtered = filterDecimalInput(raw, decimalSeparator)
                    if (filtered != null) {
                        onEvent(AppDetailEvent.SetBudgetLimit(filtered))
                    }
                },
                label = { Text(stringResource(R.string.dialog_budget_limit_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                isError = limitError != null,
                supportingText = {
                    Box(modifier = Modifier.defaultMinSize(minHeight = 20.dp)) {
                        if (limitError != null) {
                            Text(
                                text = limitError,
                                color = palette.danger,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (limitError != null) {
                            error(limitError)
                        }
                    },
            )

            // Unit group (labelled, ConnectedButtonGroup)
            Text(
                text = stringResource(R.string.dialog_budget_unit_label),
                style = MaterialTheme.typography.labelLarge,
                color = palette.text,
            )
            val units = listOf(CapUnit.MB, CapUnit.GB)
            val unitIndex = if (editor.unit == CapUnit.MB) 0 else 1
            ConnectedButtonGroup(
                options = units.map { it.label },
                selectedIndex = unitIndex,
                onSelect = { index -> onEvent(AppDetailEvent.SetBudgetUnit(units[index])) },
                modifier = Modifier.fillMaxWidth(),
            )

            // Period group: Daily / Plan cycle
            Text(
                text = stringResource(R.string.dialog_budget_period_label),
                style = MaterialTheme.typography.labelLarge,
                color = palette.text,
            )
            val periodOptions = listOf(
                stringResource(R.string.dialog_budget_period_daily),
                stringResource(R.string.dialog_budget_period_cycle),
            )
            val periodIndex = if (editor.period == BudgetPeriod.DAILY) 0 else 1
            ConnectedButtonGroup(
                options = periodOptions,
                selectedIndex = periodIndex,
                onSelect = { index ->
                    val nextPeriod = if (index == 0) BudgetPeriod.DAILY else BudgetPeriod.CYCLE
                    onEvent(AppDetailEvent.SetBudgetPeriod(nextPeriod))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            EmberbyteAnimatedVisibility(visible = editor.period == BudgetPeriod.CYCLE) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                            .selectable(
                                selected = editor.planId == null,
                                role = Role.RadioButton,
                                onClick = { onEvent(AppDetailEvent.SetBudgetPlan(null)) },
                            )
                            .semantics(mergeDescendants = true) {}
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = editor.planId == null,
                            onClick = null,
                        )
                        Text(
                            text = stringResource(R.string.dialog_budget_calendar_month),
                            color = palette.text,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    planOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .selectable(
                                    selected = editor.planId == opt.id,
                                    role = Role.RadioButton,
                                    onClick = { onEvent(AppDetailEvent.SetBudgetPlan(opt.id)) },
                                )
                                .semantics(mergeDescendants = true) {}
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = editor.planId == opt.id,
                                onClick = null,
                            )
                            Text(
                                text = opt.name,
                                color = palette.text,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }

                    editor.errors["planId"]?.let {
                        Text(it, color = palette.danger, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Warn group: 50%, 80%, 90%
            Text(
                text = stringResource(R.string.dialog_budget_warn_label),
                style = MaterialTheme.typography.labelLarge,
                color = palette.text,
            )
            val warnPercents = listOf(50, 80, 90)
            val warnIndex = warnPercents.indexOf(editor.warnAtPercent).coerceAtLeast(0)
            ConnectedButtonGroup(
                options = warnPercents.map { "$it%" },
                selectedIndex = warnIndex,
                onSelect = { index -> onEvent(AppDetailEvent.SetBudgetWarn(warnPercents[index])) },
                modifier = Modifier.fillMaxWidth(),
            )
            editor.errors["warnAtPercent"]?.let {
                Text(it, color = palette.danger, style = MaterialTheme.typography.bodySmall)
            }

            editor.errors["form"]?.let {
                Text(it, color = palette.danger, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
