package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.GhostRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType

@Composable
internal fun flowErrorMessage(error: PlanFlowError?): String? = when (error) {
    PlanFlowError.NameRequired -> stringResource(R.string.plan_error_name_required)
    PlanFlowError.NameTooLong -> stringResource(R.string.plan_error_name_length)
    PlanFlowError.CapPositive, PlanFlowError.EnteredPositive -> stringResource(R.string.plan_error_cap_positive)
    PlanFlowError.CapMax, PlanFlowError.EnteredMax -> stringResource(R.string.plan_error_cap_max)
    PlanFlowError.DayRange -> stringResource(R.string.plan_error_day_range)
    PlanFlowError.DaysRange -> stringResource(R.string.plan_error_days_range)
    null -> null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NameStep(
    state: PlanFlowState,
    hasPhonePermission: Boolean,
    reveal: (Int) -> Float,
    onSetName: (String) -> Unit,
    onSetSim: (Int?) -> Unit,
    onRequestPhonePermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val nameError = if (state.fieldError == PlanFlowError.NameRequired || state.fieldError == PlanFlowError.NameTooLong) {
        flowErrorMessage(state.fieldError)
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.plan_flow_name_title),
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
        OutlinedTextField(
            value = state.name,
            onValueChange = onSetName,
            label = { Text(stringResource(R.string.plan_flow_name_hint)) },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.primary,
                unfocusedBorderColor = palette.surface2,
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                focusedTextColor = palette.text,
                unfocusedTextColor = palette.text,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .graphicsLayer {
                    alpha = r1
                    translationY = (1f - r1) * 14.dp.toPx()
                }
                .testTag("plan_flow_name_field"),
        )

        val r2 = reveal(2)
        val suggestions = listOf("Home SIM", "Work", "Travel eSIM", "Hotspot")
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
            suggestions.forEach { chip ->
                FilterChip(
                    selected = state.name.trim() == chip,
                    onClick = { onSetName(chip) },
                    label = { Text(chip) },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                )
            }
        }

        if (state.sims.size >= 2) {
            val r3 = reveal(3)
            val simLabels = listOf("All mobile") + state.sims.map { it.label }
            val selectedIndex = if (state.subscriptionId == null) {
                0
            } else {
                (state.sims.indexOfFirst { it.subscriptionId == state.subscriptionId } + 1).coerceAtLeast(0)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = r3
                        translationY = (1f - r3) * 14.dp.toPx()
                    }
                    .testTag("name_sim_group"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.plan_flow_step1_sim_prompt),
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.muted,
                )
                ConnectedButtonGroup(
                    options = simLabels,
                    selectedIndex = selectedIndex,
                    onSelect = { idx ->
                        val subId = if (idx == 0) null else state.sims.getOrNull(idx - 1)?.subscriptionId
                        onSetSim(subId)
                    },
                )
            }
        } else if (!hasPhonePermission) {
            val r3 = reveal(3)
            FilterChip(
                selected = false,
                onClick = onRequestPhonePermission,
                leadingIcon = {
                    Icon(Icons.Rounded.SimCard, contentDescription = null)
                },
                label = { Text(stringResource(R.string.plan_flow_sim_permission_chip)) },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .graphicsLayer {
                        alpha = r3
                        translationY = (1f - r3) * 14.dp.toPx()
                    }
                    .testTag("name_sim_permission_chip"),
            )
        }

        Spacer(Modifier.weight(1f, fill = true))

        val r4 = reveal(4)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r4
                    translationY = (1f - r4) * 14.dp.toPx()
                },
            shape = mixedCorners(large = 30.dp, small = 10.dp, smallCorner = Corner.BottomEnd),
            color = palette.surface2,
            border = BorderStroke(1.dp, palette.surface),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WavyRing(
                    fraction = 0f,
                    state = BudgetStateUi.Ok,
                    size = 56.dp,
                    label = "",
                    ghost = GhostRing(fraction = 0f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = state.name.trim().ifEmpty { stringResource(R.string.plan_flow_name_hint) },
                        style = EmberbyteType.heading(PlanFlowType.CardTitle),
                        color = palette.text,
                    )
                    Text(
                        text = stringResource(R.string.plan_flow_name_preview_sub),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                        color = palette.muted,
                    )
                }
            }
        }
    }
}
