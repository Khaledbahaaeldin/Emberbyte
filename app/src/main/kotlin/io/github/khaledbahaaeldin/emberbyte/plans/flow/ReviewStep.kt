package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReviewStep(
    state: PlanFlowState,
    reveal: (Int) -> Float,
    onToggleAlert: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0]
    val levels = listOf(50, 80, 90, 100)

    val unitLabel = when (state.capUnit) {
        CapUnit.MB -> if (state.unitSystem == UnitSystem.BINARY) "MiB" else "MB"
        CapUnit.GB -> if (state.unitSystem == UnitSystem.BINARY) "GiB" else "GB"
        CapUnit.TB -> if (state.unitSystem == UnitSystem.BINARY) "TiB" else "TB"
    }
    val cycle = when (state.cycleKind) {
        CycleKind.Monthly -> Cycle.MonthlyOnDay(state.dayOfMonth.coerceIn(1, 31), state.renewalTime, ZoneId.systemDefault())
        CycleKind.EveryNDays -> Cycle.EveryNDays(state.everyDays.coerceIn(1, 365), state.anchor, state.renewalTime, ZoneId.systemDefault())
    }
    val countingText = when {
        !state.isMidCycleApplicable || state.firstCycleChoice == FirstCycleChoice.PhoneRecord ->
            stringResource(R.string.plan_flow_choice_phone)
        state.firstCycleChoice == FirstCycleChoice.Entered ->
            "${state.enteredAmount} ${state.enteredUnit.name} already used"
        else -> stringResource(R.string.plan_flow_choice_next)
    }
    val alertsText = if (state.alertThresholds.isEmpty()) {
        stringResource(R.string.plan_flow_alerts_off)
    } else {
        val sorted = state.alertThresholds.sorted().joinToString(", ") { "$it%" }
        stringResource(R.string.plan_flow_alerts_at, sorted)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.plan_flow_warn_title),
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
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r1
                    translationY = (1f - r1) * 14.dp.toPx()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            levels.forEach { pct ->
                FilterChip(
                    selected = pct in state.alertThresholds,
                    onClick = { onToggleAlert(pct) },
                    label = { Text("$pct%") },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("alert_chip_$pct"),
                )
            }
        }

        val r2 = reveal(2)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r2
                    translationY = (1f - r2) * 14.dp.toPx()
                },
            shape = mixedCorners(large = 34.dp, small = 12.dp, smallCorner = Corner.BottomStart),
            color = palette.surface2,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.plan_flow_looks_right),
                    style = EmberbyteType.heading(PlanFlowType.SectionTitle),
                    color = palette.muted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WavyRing(
                        fraction = 0f,
                        state = BudgetStateUi.Ok,
                        size = 96.dp,
                        label = "${state.capText} $unitLabel",
                        modifier = Modifier.testTag("review_summary_ring"),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = state.name.trim(),
                            style = EmberbyteType.heading(PlanFlowType.SectionTitle),
                            color = palette.text,
                        )
                        Text(
                            text = "${state.capText} $unitLabel",
                            style = EmberbyteType.heading(PlanFlowType.Body),
                            color = palette.primary,
                        )
                        Text(
                            text = cycleSummaryText(cycle, locale),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                            color = palette.muted,
                        )
                        Text(
                            text = countingText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                            color = palette.muted,
                        )
                        Text(
                            text = alertsText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                            color = palette.muted,
                        )
                    }
                }
            }
        }

        if (state.saveFailed) {
            Text(
                text = stringResource(R.string.plan_flow_save_failed),
                style = EmberbyteType.heading(PlanFlowType.Body),
                color = palette.danger,
            )
        }
    }
}
