package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

@Composable
fun Step1NameAndSim(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "What is the plan called?",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = { onEvent(PlanFlowEvent.SetName(it)) },
            label = { Text("Plan name") },
            placeholder = { Text("e.g. Main SIM, Vodafone, Unlimited") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.sims.isNotEmpty()) {
            Text(
                text = "Which SIM does it track?",
                style = MaterialTheme.typography.titleMedium,
                color = palette.text,
            )
            state.sims.forEach { sim ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(PlanFlowEvent.SetSim(sim.subscriptionId)) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = state.subscriptionId == sim.subscriptionId,
                        onClick = { onEvent(PlanFlowEvent.SetSim(sim.subscriptionId)) },
                    )
                    Text(
                        text = sim.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.text,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun Step2Cap(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "How much data do you get?",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        OutlinedTextField(
            value = state.capText,
            onValueChange = { onEvent(PlanFlowEvent.SetCap(it)) },
            label = { Text("Data allowance") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        val unitOptions = listOf("MB", "GB", "TB")
        val selectedIdx = when (state.capUnit) {
            CapUnit.MB -> 0
            CapUnit.GB -> 1
            CapUnit.TB -> 2
        }
        ConnectedButtonGroup(
            options = unitOptions,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                val u = when (idx) {
                    0 -> CapUnit.MB
                    1 -> CapUnit.GB
                    else -> CapUnit.TB
                }
                onEvent(PlanFlowEvent.SetCapUnit(u))
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("5", "10", "20", "50").forEach { quickValue ->
                AssistChip(
                    onClick = {
                        onEvent(PlanFlowEvent.SetCap(quickValue))
                        onEvent(PlanFlowEvent.SetCapUnit(CapUnit.GB))
                    },
                    label = { Text("$quickValue GB") },
                )
            }
        }
    }
}

@Composable
fun Step3Cycle(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "How often does it renew?",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        val cycleOptions = listOf("Monthly", "Every N days")
        val cycleIdx = if (state.cycleKind == CycleKind.Monthly) 0 else 1
        ConnectedButtonGroup(
            options = cycleOptions,
            selectedIndex = cycleIdx,
            onSelect = { idx ->
                val k = if (idx == 0) CycleKind.Monthly else CycleKind.EveryNDays
                onEvent(PlanFlowEvent.SetCycleKind(k))
            },
        )

        if (state.cycleKind == CycleKind.Monthly) {
            OutlinedTextField(
                value = state.dayOfMonth.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { day -> onEvent(PlanFlowEvent.SetDayOfMonth(day)) }
                },
                label = { Text("Day of month (1-31)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = state.everyDays.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { days -> onEvent(PlanFlowEvent.SetEveryDays(days)) }
                },
                label = { Text("Days per cycle") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "Renews next on ${state.nextCycleStartText}",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
        )
    }
}

@Composable
fun Step4ThisCycle(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "This cycle is already under way.",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "It started on ${state.currentCycleStartText}. How should Emberbyte count the first cycle?",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
        )

        // Option 1: Phone record
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.PhoneRecord)) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = state.firstCycleChoice == FirstCycleChoice.PhoneRecord,
                onClick = { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.PhoneRecord)) },
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = "Use what my phone recorded",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                )
                Text(
                    text = "Might miss use from before Emberbyte could measure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }

        // Option 2: Entered
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            RadioButton(
                selected = state.firstCycleChoice == FirstCycleChoice.Entered,
                onClick = { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.Entered)) },
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.Entered)) },
                ) {
                    Text(
                        text = "I've used some already",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.text,
                    )
                    Text(
                        text = "Copy it from your carrier's app. Emberbyte adds what the phone measures from now on.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                    )
                }
                if (state.firstCycleChoice == FirstCycleChoice.Entered) {
                    OutlinedTextField(
                        value = state.enteredAmount,
                        onValueChange = { onEvent(PlanFlowEvent.SetEnteredAmount(it)) },
                        label = { Text("Used so far") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    val unitOptions = listOf("MB", "GB", "TB")
                    val selectedIdx = when (state.enteredUnit) {
                        CapUnit.MB -> 0
                        CapUnit.GB -> 1
                        CapUnit.TB -> 2
                    }
                    ConnectedButtonGroup(
                        options = unitOptions,
                        selectedIndex = selectedIdx,
                        onSelect = { idx ->
                            val u = when (idx) {
                                0 -> CapUnit.MB
                                1 -> CapUnit.GB
                                else -> CapUnit.TB
                            }
                            onEvent(PlanFlowEvent.SetEnteredUnit(u))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    if (state.isEnteredOverCap) {
                        Text(
                            text = "This is already over your cap",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.warn,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Option 3: Start next cycle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.StartNextCycle)) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = state.firstCycleChoice == FirstCycleChoice.StartNextCycle,
                onClick = { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.StartNextCycle)) },
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = "Start next cycle",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                )
                Text(
                    text = "Starts ${state.nextCycleStartText}. No alerts until then.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
fun StepReview(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Review your plan",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Name: ${state.name}", style = MaterialTheme.typography.titleMedium, color = palette.text)
            Text("Cap: ${state.capText} ${state.capUnit.name}", style = MaterialTheme.typography.bodyLarge, color = palette.text)
            val cycleDesc = if (state.cycleKind == CycleKind.Monthly) "Monthly on day ${state.dayOfMonth}" else "Every ${state.everyDays} days"
            Text("Cycle: $cycleDesc", style = MaterialTheme.typography.bodyLarge, color = palette.text)
            if (state.isMidCycleApplicable) {
                val modeDesc = when (state.firstCycleChoice) {
                    FirstCycleChoice.PhoneRecord -> "Phone's record (Estimated)"
                    FirstCycleChoice.Entered -> "Entered: ${state.enteredAmount} ${state.enteredUnit.name}"
                    FirstCycleChoice.StartNextCycle -> "Starts next cycle on ${state.nextCycleStartText}"
                }
                Text("This cycle: $modeDesc", style = MaterialTheme.typography.bodyLarge, color = palette.text)
            }
        }
    }
}
