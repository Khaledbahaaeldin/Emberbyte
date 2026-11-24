package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThisCycleSection(
    countedSoFarText: String,
    firstCycleChoice: FirstCycleChoice,
    enteredAmount: String,
    enteredUnit: CapUnit,
    unitSystem: UnitSystem,
    enteredError: String?,
    onSelectChoice: (FirstCycleChoice) -> Unit,
    onSetEnteredAmount: (String) -> Unit,
    onSetEnteredUnit: (CapUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    EditorSectionCard(title = stringResource(R.string.plan_editor_this_cycle), modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.plan_editor_counted_so_far, countedSoFarText),
                style = EmberbyteType.heading(PlanFlowType.Body),
                color = palette.text,
            )
            if (firstCycleChoice == FirstCycleChoice.PhoneRecord) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = palette.primary.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = stringResource(R.string.plan_flow_choice_phone_tag),
                        style = EmberbyteType.heading(PlanFlowType.Micro),
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.plan_editor_first_cycle_note),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
            color = palette.muted,
        )

        val choices = listOf(
            FirstCycleChoice.PhoneRecord to stringResource(R.string.plan_editor_choice_phone),
            FirstCycleChoice.Entered to stringResource(R.string.plan_editor_choice_entered),
            FirstCycleChoice.StartNextCycle to stringResource(R.string.plan_editor_choice_next),
        )
        ConnectedButtonGroup(
            options = choices.map { it.second },
            selectedIndex = choices.indexOfFirst { it.first == firstCycleChoice }.coerceAtLeast(0),
            onSelect = { idx -> onSelectChoice(choices[idx].first) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (firstCycleChoice == FirstCycleChoice.Entered) {
            OutlinedTextField(
                value = enteredAmount,
                onValueChange = onSetEnteredAmount,
                label = { Text(stringResource(R.string.plan_editor_used_so_far)) },
                singleLine = true,
                isError = enteredError != null,
                supportingText = enteredError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_entered_amount_field"),
            )
            val unitLabels = if (unitSystem == UnitSystem.BINARY) listOf("MiB", "GiB", "TiB") else listOf("MB", "GB", "TB")
            ConnectedButtonGroup(
                options = unitLabels,
                selectedIndex = enteredUnit.ordinal,
                onSelect = { idx -> onSetEnteredUnit(CapUnit.entries[idx]) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
