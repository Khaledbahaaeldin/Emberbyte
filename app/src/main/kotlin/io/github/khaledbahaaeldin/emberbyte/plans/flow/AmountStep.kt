package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.RolloverKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.GhostRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UnitBadge
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import java.math.BigDecimal
import java.text.DecimalFormatSymbols

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AmountStep(
    state: PlanFlowState,
    reveal: (Int) -> Float,
    onSetCap: (String) -> Unit,
    onSetCapUnit: (CapUnit) -> Unit,
    onSetRollover: (RolloverKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0]
    val decimalSeparator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
    val capError = if (state.fieldError == PlanFlowError.CapPositive || state.fieldError == PlanFlowError.CapMax) {
        flowErrorMessage(state.fieldError)
    } else {
        null
    }

    var editingExact by remember { mutableStateOf(false) }
    var rolloverExpanded by remember { mutableStateOf(false) }

    val unitLabel = when (state.capUnit) {
        CapUnit.MB -> if (state.unitSystem == UnitSystem.BINARY) "MiB" else "MB"
        CapUnit.GB -> if (state.unitSystem == UnitSystem.BINARY) "GiB" else "GB"
        CapUnit.TB -> if (state.unitSystem == UnitSystem.BINARY) "TiB" else "TB"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.plan_flow_amount_title),
            style = EmberbyteType.title(PlanFlowType.Title).copy(letterSpacing = (-0.01).em),
            color = palette.text,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                },
        )

        val r1 = reveal(1)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = r1
                    translationY = (1f - r1) * 14.dp.toPx()
                }
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable { editingExact = !editingExact }
                .testTag("amount_wavy_ring"),
            contentAlignment = Alignment.Center,
        ) {
            WavyRing(
                fraction = 0f,
                state = BudgetStateUi.Ok,
                size = 236.dp,
                label = "",
                ghost = GhostRing(fraction = 0f),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.capText.ifBlank { "0" },
                    style = EmberbyteType.hero(
                        if (state.capText.length > 4) PlanFlowType.RingHeroCompact else PlanFlowType.RingHero,
                    ).copy(fontFeatureSettings = "tnum"),
                    color = if (capError != null) palette.danger else palette.text,
                )
                UnitBadge(unit = unitLabel)
            }
        }

        if (editingExact || capError != null) {
            OutlinedTextField(
                value = state.capText,
                onValueChange = onSetCap,
                label = { Text(unitLabel) },
                singleLine = true,
                isError = capError != null,
                supportingText = capError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_exact_field"),
            )
        }

        val r2 = reveal(2)
        val unitItems = if (state.unitSystem == UnitSystem.BINARY) {
            listOf("MiB", "GiB", "TiB")
        } else {
            listOf("MB", "GB", "TB")
        }
        ConnectedButtonGroup(
            options = unitItems,
            selectedIndex = state.capUnit.ordinal,
            onSelect = { idx -> onSetCapUnit(CapUnit.entries[idx]) },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r2
                    translationY = (1f - r2) * 14.dp.toPx()
                },
        )

        val r3 = reveal(3)
        val deltas = listOf(-5, -1, 1, 5, 10)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r3
                    translationY = (1f - r3) * 14.dp.toPx()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            deltas.forEach { delta ->
                val label = if (delta > 0) "+$delta" else "$delta"
                FilterChip(
                    selected = false,
                    onClick = {
                        val current = state.capText.trim().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val next = (current + BigDecimal.valueOf(delta.toLong())).max(BigDecimal.ONE)
                        val formatted = next.stripTrailingZeros().toPlainString().replace('.', decimalSeparator)
                        onSetCap(formatted)
                    },
                    label = { Text(label) },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                )
            }
        }

        Text(
            text = stringResource(
                if (state.unitSystem == UnitSystem.BINARY) {
                    R.string.plan_flow_units_binary
                } else {
                    R.string.plan_flow_units_decimal
                },
            ),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
            color = palette.muted,
            modifier = Modifier.fillMaxWidth(),
        )

        val rolloverTitle = when (state.rollover) {
            RolloverKind.None -> stringResource(R.string.plan_flow_rollover_lost)
            RolloverKind.Full -> stringResource(R.string.plan_flow_rollover_full)
            RolloverKind.Capped -> stringResource(R.string.plan_flow_rollover_capped)
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable { rolloverExpanded = !rolloverExpanded },
            shape = MaterialTheme.shapes.large,
            color = palette.surface2,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = rolloverTitle,
                        style = EmberbyteType.heading(PlanFlowType.Body),
                        color = palette.text,
                    )
                    Icon(
                        imageVector = if (rolloverExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = palette.muted,
                    )
                }
                AnimatedVisibility(visible = rolloverExpanded) {
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val options = listOf(
                            RolloverKind.None to stringResource(R.string.plan_editor_rollover_lost),
                            RolloverKind.Full to stringResource(R.string.plan_editor_rollover_full),
                            RolloverKind.Capped to stringResource(R.string.plan_flow_rollover_capped_label),
                        )
                        options.forEach { (kind, label) ->
                            FilterChip(
                                selected = state.rollover == kind,
                                onClick = { onSetRollover(kind) },
                                label = { Text(label) },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
