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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.plans.flow.MonthlyDayGrid
import io.github.khaledbahaaeldin.emberbyte.plans.flow.cycleSummaryText
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private enum class EditorCycleTab { Monthly, Weekly, Custom }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CycleSection(
    cycleKind: CycleKind,
    dayOfMonth: Int,
    everyDays: Int,
    anchor: LocalDate,
    renewalTime: LocalTime,
    rolloverKind: RolloverKind,
    rolloverCapText: String,
    rolloverCapUnit: CapUnit,
    unitSystem: UnitSystem,
    daysError: String?,
    rolloverError: String?,
    onSetCycleKind: (CycleKind) -> Unit,
    onSetDayOfMonth: (Int) -> Unit,
    onSetEveryDays: (Int) -> Unit,
    onSetAnchor: (LocalDate) -> Unit,
    onSetRenewalTime: (LocalTime) -> Unit = {},
    onSetRolloverKind: (RolloverKind) -> Unit,
    onSetRolloverCap: (String) -> Unit,
    onSetRolloverUnit: (CapUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0]
    var showDaySheet by remember { mutableStateOf(false) }

    val selectedTab = when (cycleKind) {
        CycleKind.Monthly -> EditorCycleTab.Monthly
        CycleKind.EveryNDays -> if (everyDays == 7) EditorCycleTab.Weekly else EditorCycleTab.Custom
    }

    val cycle = when (cycleKind) {
        CycleKind.Monthly -> Cycle.MonthlyOnDay(dayOfMonth.coerceIn(1, 31), renewalTime, ZoneId.systemDefault())
        CycleKind.EveryNDays -> Cycle.EveryNDays(everyDays.coerceIn(1, 365), anchor, renewalTime, ZoneId.systemDefault())
    }

    EditorSectionCard(title = stringResource(R.string.plan_editor_cycle), modifier = modifier) {
        ConnectedButtonGroup(
            options = listOf(stringResource(R.string.plan_flow_cycle_monthly), stringResource(R.string.plan_flow_cycle_weekly), stringResource(R.string.plan_flow_cycle_custom)),
            selectedIndex = selectedTab.ordinal,
            onSelect = { idx ->
                when (EditorCycleTab.entries[idx]) {
                    EditorCycleTab.Monthly -> onSetCycleKind(CycleKind.Monthly)
                    EditorCycleTab.Weekly -> {
                        onSetCycleKind(CycleKind.EveryNDays)
                        onSetEveryDays(7)
                    }
                    EditorCycleTab.Custom -> {
                        onSetCycleKind(CycleKind.EveryNDays)
                        if (everyDays == 7) onSetEveryDays(30)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.plan_editor_resets_on),
                    style = EmberbyteType.heading(PlanFlowType.Caption),
                    color = palette.muted,
                )
                Text(
                    text = cycleSummaryText(cycle, locale),
                    style = EmberbyteType.heading(PlanFlowType.Body),
                    color = palette.text,
                )
            }
            if (selectedTab == EditorCycleTab.Monthly) {
                AssistChip(
                    onClick = { showDaySheet = true },
                    label = { Text(stringResource(R.string.plan_editor_change)) },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                )
            }
        }

        if (selectedTab == EditorCycleTab.Custom) {
            OutlinedTextField(
                value = everyDays.toString(),
                onValueChange = { text ->
                    text.filter { it.isDigit() }.toIntOrNull()?.let(onSetEveryDays)
                },
                label = { Text(stringResource(R.string.plan_editor_days_per_cycle)) },
                singleLine = true,
                isError = daysError != null,
                supportingText = daysError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_days_field"),
            )
            DateField(
                label = stringResource(R.string.plan_editor_cycle_anchor),
                date = anchor,
                onChange = onSetAnchor,
            )
        }

        TimeField(
            label = stringResource(R.string.plan_editor_renews_at),
            time = renewalTime,
            onChange = onSetRenewalTime,
        )

        Text(
            text = stringResource(R.string.plan_editor_unused_data),
            style = EmberbyteType.heading(PlanFlowType.Caption),
            color = palette.muted,
        )
        ConnectedButtonGroup(
            options = listOf(stringResource(R.string.plan_editor_rollover_lost), stringResource(R.string.plan_editor_rollover_full), stringResource(R.string.plan_editor_rollover_capped)),
            selectedIndex = rolloverKind.ordinal,
            onSelect = { idx -> onSetRolloverKind(RolloverKind.entries[idx]) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (rolloverKind == RolloverKind.Capped) {
            OutlinedTextField(
                value = rolloverCapText,
                onValueChange = onSetRolloverCap,
                label = { Text(stringResource(R.string.plan_editor_rollover_cap)) },
                singleLine = true,
                isError = rolloverError != null,
                supportingText = rolloverError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            )
            val unitLabels = if (unitSystem == UnitSystem.BINARY) listOf("MiB", "GiB", "TiB") else listOf("MB", "GB", "TB")
            ConnectedButtonGroup(
                options = unitLabels,
                selectedIndex = rolloverCapUnit.ordinal,
                onSelect = { idx -> onSetRolloverUnit(CapUnit.entries[idx]) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDaySheet) {
        ModalBottomSheet(onDismissRequest = { showDaySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.plan_editor_resets_on),
                    style = EmberbyteType.heading(PlanFlowType.SectionTitle),
                    color = palette.text,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                MonthlyDayGrid(
                    selectedDay = dayOfMonth,
                    onSelectDay = {
                        onSetDayOfMonth(it)
                        showDaySheet = false
                    },
                )
            }
        }
    }
}
