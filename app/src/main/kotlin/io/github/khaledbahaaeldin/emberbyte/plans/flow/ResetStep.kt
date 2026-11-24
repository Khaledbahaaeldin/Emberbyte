package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import java.time.Clock
import java.time.DayOfWeek
import java.time.format.TextStyle

private enum class ResetTab { Monthly, Weekly, Custom }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResetStep(
    state: PlanFlowState,
    clock: Clock,
    reveal: (Int) -> Float,
    onSetCycleKind: (CycleKind) -> Unit,
    onSetDayOfMonth: (Int) -> Unit,
    onSetEveryDaysText: (String) -> Unit,
    onSetEveryDays: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0]
    var tab by remember(state.cycleKind, state.everyDays) {
        mutableStateOf(
            when {
                state.cycleKind == CycleKind.Monthly -> ResetTab.Monthly
                state.everyDays == 7 -> ResetTab.Weekly
                else -> ResetTab.Custom
            },
        )
    }

    val cycle = when (state.cycleKind) {
        CycleKind.Monthly -> Cycle.MonthlyOnDay(state.dayOfMonth.coerceIn(1, 31), state.renewalTime, clock.zone)
        CycleKind.EveryNDays -> Cycle.EveryNDays(state.everyDays.coerceIn(1, 365), state.anchor, state.renewalTime, clock.zone)
    }
    val daysError = if (state.fieldError == PlanFlowError.DaysRange) flowErrorMessage(state.fieldError) else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.plan_flow_reset_title),
            style = EmberbyteType.title(PlanFlowType.Title).copy(letterSpacing = (-0.035).em),
            color = palette.text,
            modifier = Modifier
                .semantics { heading() }
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                },
        )

        val r1 = reveal(1)
        ConnectedButtonGroup(
            options = listOf(
                stringResource(R.string.plan_flow_cycle_monthly),
                stringResource(R.string.plan_flow_cycle_weekly),
                stringResource(R.string.plan_flow_cycle_custom),
            ),
            selectedIndex = tab.ordinal,
            onSelect = { idx ->
                val picked = ResetTab.entries[idx]
                tab = picked
                when (picked) {
                    ResetTab.Monthly -> onSetCycleKind(CycleKind.Monthly)
                    ResetTab.Weekly -> {
                        onSetCycleKind(CycleKind.EveryNDays)
                        onSetEveryDays(7)
                    }
                    ResetTab.Custom -> {
                        onSetCycleKind(CycleKind.EveryNDays)
                        if (state.everyDays == 7) onSetEveryDays(30)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r1
                    translationY = (1f - r1) * 14.dp.toPx()
                },
        )

        val r2 = reveal(2)
        when (tab) {
            ResetTab.Monthly -> {
                MonthlyDayGrid(
                    selectedDay = state.dayOfMonth,
                    onSelectDay = onSetDayOfMonth,
                    modifier = Modifier.graphicsLayer {
                        alpha = r2
                        translationY = (1f - r2) * 14.dp.toPx()
                    },
                )
            }
            ResetTab.Weekly -> {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = r2
                            translationY = (1f - r2) * 14.dp.toPx()
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DayOfWeek.entries.forEach { dow ->
                        val selected = state.anchor.dayOfWeek == dow
                        FilterChip(
                            selected = selected,
                            onClick = { onSetEveryDays(7) },
                            label = { Text(dow.getDisplayName(TextStyle.SHORT, locale)) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        )
                    }
                }
            }
            ResetTab.Custom -> {
                OutlinedTextField(
                    value = state.everyDaysText,
                    onValueChange = onSetEveryDaysText,
                    label = { Text(stringResource(R.string.plan_flow_custom_days_label)) },
                    singleLine = true,
                    isError = daysError != null,
                    supportingText = daysError?.let { msg -> { Text(msg) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = r2
                            translationY = (1f - r2) * 14.dp.toPx()
                        }
                        .testTag("reset_custom_days_field"),
                )
            }
        }

        val r3 = reveal(3)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r3
                    translationY = (1f - r3) * 14.dp.toPx()
                },
            shape = mixedCorners(large = 30.dp, small = 10.dp, smallCorner = Corner.BottomEnd),
            color = palette.surface2,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = cycleSummaryLine(cycle, locale),
                    style = EmberbyteType.heading(PlanFlowType.CardTitle),
                    color = palette.text,
                )
                Text(
                    text = nextResetDescription(cycle, clock.instant(), clock.zone, locale),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
internal fun MonthlyDayGrid(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                for (col in 1..7) {
                    val day = row * 7 + col
                    val selected = selectedDay == day
                    val desc = if (selected) {
                        "${ordinalDay(day)} of the month, selected"
                    } else {
                        "${ordinalDay(day)} of the month"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics { contentDescription = desc }
                            .clickable { onSelectDay(day) }
                            .testTag("day_grid_$day")
                            .padding(3.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (selected) palette.primary else palette.surface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day.toString(),
                            style = EmberbyteType.heading(PlanFlowType.Caption),
                            color = if (selected) palette.background else palette.text,
                        )
                    }
                }
            }
        }
        FilterChip(
            selected = selectedDay >= 31,
            onClick = { onSelectDay(31) },
            label = { Text(stringResource(R.string.plan_flow_last_day_chip)) },
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .testTag("day_grid_last"),
        )
    }
}
