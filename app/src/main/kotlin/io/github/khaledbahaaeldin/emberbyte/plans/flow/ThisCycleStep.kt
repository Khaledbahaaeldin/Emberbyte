package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import java.time.Clock
import java.time.LocalDate

@Composable
internal fun ThisCycleStep(
    state: PlanFlowState,
    clock: Clock,
    reveal: (Int) -> Float,
    onSelectChoice: (FirstCycleChoice) -> Unit,
    onSetEnteredAmount: (String) -> Unit,
    onSetEnteredUnit: (CapUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val today = LocalDate.now(clock).dayOfMonth
    val enteredError = if (state.fieldError == PlanFlowError.EnteredPositive || state.fieldError == PlanFlowError.EnteredMax) {
        flowErrorMessage(state.fieldError)
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.plan_flow_this_cycle_title, ordinalDay(today)),
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
        CycleChoiceCard(
            title = stringResource(R.string.plan_flow_choice_phone),
            subtitle = stringResource(R.string.plan_flow_v2_choice_phone_sub),
            tag = stringResource(R.string.plan_flow_choice_phone_tag),
            selected = state.firstCycleChoice == FirstCycleChoice.PhoneRecord,
            smallCorner = Corner.BottomEnd,
            onClick = { onSelectChoice(FirstCycleChoice.PhoneRecord) },
            modifier = Modifier
                .graphicsLayer {
                    alpha = r1
                    translationY = (1f - r1) * 14.dp.toPx()
                }
                .testTag("this_cycle_phone_card"),
        )

        val r2 = reveal(2)
        CycleChoiceCard(
            title = stringResource(R.string.plan_flow_choice_entered),
            subtitle = stringResource(R.string.plan_flow_v2_choice_entered_sub),
            tag = null,
            selected = state.firstCycleChoice == FirstCycleChoice.Entered,
            smallCorner = Corner.TopStart,
            onClick = { onSelectChoice(FirstCycleChoice.Entered) },
            modifier = Modifier
                .graphicsLayer {
                    alpha = r2
                    translationY = (1f - r2) * 14.dp.toPx()
                }
                .testTag("this_cycle_entered_card"),
        ) {
            AnimatedVisibility(visible = state.firstCycleChoice == FirstCycleChoice.Entered) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = state.enteredAmount,
                        onValueChange = onSetEnteredAmount,
                        label = { Text(state.enteredUnit.name) },
                        singleLine = true,
                        isError = enteredError != null,
                        supportingText = enteredError?.let { msg -> { Text(msg) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("this_cycle_entered_field"),
                    )
                    val unitItems = if (state.unitSystem == UnitSystem.BINARY) {
                        listOf("MiB", "GiB", "TiB")
                    } else {
                        listOf("MB", "GB", "TB")
                    }
                    ConnectedButtonGroup(
                        options = unitItems,
                        selectedIndex = state.enteredUnit.ordinal,
                        onSelect = { idx -> onSetEnteredUnit(CapUnit.entries[idx]) },
                    )
                    if (state.isEnteredOverCap) {
                        Text(
                            text = stringResource(R.string.plan_flow_over_cap_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.warn,
                        )
                    }
                }
            }
        }

        val r3 = reveal(3)
        CycleChoiceCard(
            title = stringResource(R.string.plan_flow_choice_next),
            subtitle = stringResource(R.string.plan_flow_v2_choice_next_sub, state.nextCycleStartText),
            tag = null,
            selected = state.firstCycleChoice == FirstCycleChoice.StartNextCycle,
            smallCorner = Corner.BottomStart,
            onClick = { onSelectChoice(FirstCycleChoice.StartNextCycle) },
            modifier = Modifier
                .graphicsLayer {
                    alpha = r3
                    translationY = (1f - r3) * 14.dp.toPx()
                }
                .testTag("this_cycle_next_card"),
        )

        val r4 = reveal(4)
        Text(
            text = stringResource(R.string.plan_flow_this_cycle_footnote),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
            color = palette.muted,
            modifier = Modifier.graphicsLayer {
                alpha = r4
                translationY = (1f - r4) * 14.dp.toPx()
            },
        )
    }
}

@Composable
private fun CycleChoiceCard(
    title: String,
    subtitle: String,
    tag: String?,
    selected: Boolean,
    smallCorner: Corner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    val shape = mixedCorners(large = 28.dp, small = 10.dp, smallCorner = smallCorner)
    val desc = buildString {
        append(title)
        if (tag != null) append(", ").append(tag)
        if (selected) append(", selected")
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .semantics { contentDescription = desc }
            .clickable(role = Role.RadioButton, onClick = onClick),
        shape = shape,
        color = if (selected) palette.primary.copy(alpha = 0.14f) else palette.surface2,
        border = if (selected) BorderStroke(1.5.dp, palette.primary) else null,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = title,
                            style = EmberbyteType.heading(PlanFlowType.CardTitle),
                            color = palette.text,
                        )
                        if (tag != null) {
                            Surface(
                                shape = CircleShape,
                                color = palette.surface,
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = PlanFlowType.Micro),
                                    color = palette.muted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                        color = palette.muted,
                    )
                }
            }
            extra()
        }
    }
}
