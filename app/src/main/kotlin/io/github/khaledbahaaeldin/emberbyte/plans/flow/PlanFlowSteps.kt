package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.home.toByteUnits
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.plans.bytesOf
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanCard
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.warnText
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import java.text.DecimalFormatSymbols
import java.util.Locale

internal val FieldKeyboardTypeKey = SemanticsPropertyKey<KeyboardType>("FieldKeyboardType")

private fun Modifier.keyboardSemantics(keyboardType: KeyboardType, errorText: String?): Modifier =
    semantics {
        set(FieldKeyboardTypeKey, keyboardType)
        if (errorText != null) {
            error(errorText)
        }
    }

@Composable
private fun rememberLocaleDecimalSeparator(): Char {
    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0] ?: Locale.ENGLISH
    return remember(locale) { DecimalFormatSymbols.getInstance(locale).decimalSeparator }
}

@Composable
private fun errorMessageFor(error: PlanFlowError?): String? = when (error) {
    PlanFlowError.NameRequired -> stringResource(R.string.plan_error_name_required)
    PlanFlowError.NameTooLong -> stringResource(R.string.plan_error_name_length)
    PlanFlowError.CapPositive, PlanFlowError.EnteredPositive -> stringResource(R.string.plan_error_cap_positive)
    PlanFlowError.CapMax, PlanFlowError.EnteredMax -> stringResource(R.string.plan_error_cap_max)
    PlanFlowError.DayRange -> stringResource(R.string.plan_error_day_range)
    PlanFlowError.DaysRange -> stringResource(R.string.plan_error_days_range)
    null -> null
}

@Composable
fun Step1NameAndSim(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val focusRequester = remember { FocusRequester() }
    val nameError = if (state.fieldError == PlanFlowError.NameRequired || state.fieldError == PlanFlowError.NameTooLong) {
        errorMessageFor(state.fieldError)
    } else {
        null
    }

    LaunchedEffect(state.validationAttempt) {
        if (state.validationAttempt > 0 && nameError != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.plan_flow_step1_title),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = { onEvent(PlanFlowEvent.SetName(it)) },
            label = { Text(stringResource(R.string.plan_field_name)) },
            placeholder = { Text(stringResource(R.string.plan_field_name_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            isError = nameError != null,
            supportingText = nameError?.let { msg -> { Text(msg) } },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics {
                    if (nameError != null) error(nameError)
                },
        )

        if (state.sims.isNotEmpty()) {
            Text(
                text = stringResource(R.string.plan_flow_step1_sim_prompt),
                style = MaterialTheme.typography.titleMedium,
                color = palette.text,
            )
            state.sims.forEach { sim ->
                val selected = state.subscriptionId == sim.subscriptionId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp)
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onEvent(PlanFlowEvent.SetSim(sim.subscriptionId)) },
                        )
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = null,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step2Cap(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val decimalSeparator = rememberLocaleDecimalSeparator()
    val focusRequester = remember { FocusRequester() }
    val capError = if (state.fieldError == PlanFlowError.CapPositive || state.fieldError == PlanFlowError.CapMax) {
        errorMessageFor(state.fieldError)
    } else {
        null
    }

    LaunchedEffect(state.validationAttempt) {
        if (state.validationAttempt > 0 && capError != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.plan_flow_step2_title),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        OutlinedTextField(
            value = state.capText,
            onValueChange = { raw ->
                val filtered = filterDecimalInput(raw, decimalSeparator)
                if (filtered != null) {
                    onEvent(PlanFlowEvent.SetCap(filtered))
                } else {
                    onEvent(PlanFlowEvent.SetCap(raw))
                }
            },
            label = { Text(stringResource(R.string.plan_field_cap)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
            isError = capError != null,
            supportingText = capError?.let { msg -> { Text(msg) } },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .keyboardSemantics(KeyboardType.Decimal, capError),
        )

        val unitOptions = listOf(
            stringResource(R.string.plan_unit_mb),
            stringResource(R.string.plan_unit_gb),
            stringResource(R.string.plan_unit_tb),
        )
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

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("5", "10", "20", "50").forEach { quickValue ->
                AssistChip(
                    onClick = {
                        onEvent(PlanFlowEvent.SetCap(quickValue))
                        onEvent(PlanFlowEvent.SetCapUnit(CapUnit.GB))
                    },
                    label = { Text(stringResource(R.string.plan_quick_gb, quickValue)) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
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
    val focusRequester = remember { FocusRequester() }
    val cycleError = if (state.fieldError == PlanFlowError.DayRange || state.fieldError == PlanFlowError.DaysRange) {
        errorMessageFor(state.fieldError)
    } else {
        null
    }

    LaunchedEffect(state.validationAttempt) {
        if (state.validationAttempt > 0 && cycleError != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.plan_flow_step3_title),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        val cycleOptions = listOf(
            stringResource(R.string.plan_cycle_monthly),
            stringResource(R.string.plan_cycle_every_n_days),
        )
        val cycleIdx = if (state.cycleKind == CycleKind.Monthly) 0 else 1
        ConnectedButtonGroup(
            options = cycleOptions,
            selectedIndex = cycleIdx,
            onSelect = { idx ->
                val k = if (idx == 0) CycleKind.Monthly else CycleKind.EveryNDays
                onEvent(PlanFlowEvent.SetCycleKind(k))
            },
            modifier = Modifier.fillMaxWidth(),
        )

        EmberbyteCrossfade(targetState = state.cycleKind) { kind ->
            if (kind == CycleKind.Monthly) {
                val dayErr = if (state.fieldError == PlanFlowError.DayRange) errorMessageFor(state.fieldError) else null
                OutlinedTextField(
                    value = state.dayOfMonthText,
                    onValueChange = { onEvent(PlanFlowEvent.SetDayOfMonthText(it)) },
                    label = { Text(stringResource(R.string.plan_field_day_of_month)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    isError = dayErr != null,
                    supportingText = dayErr?.let { msg -> { Text(msg) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .keyboardSemantics(KeyboardType.Number, dayErr),
                )
            } else {
                val daysErr = if (state.fieldError == PlanFlowError.DaysRange) errorMessageFor(state.fieldError) else null
                OutlinedTextField(
                    value = state.everyDaysText,
                    onValueChange = { onEvent(PlanFlowEvent.SetEveryDaysText(it)) },
                    label = { Text(stringResource(R.string.plan_field_every_days)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    isError = daysErr != null,
                    supportingText = daysErr?.let { msg -> { Text(msg) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .keyboardSemantics(KeyboardType.Number, daysErr),
                )
            }
        }

        if (state.nextCycleStartText.isNotEmpty()) {
            Text(
                text = stringResource(R.string.plan_flow_renews_next, state.nextCycleStartText),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
        }
    }
}

@Composable
fun Step4ThisCycle(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val decimalSeparator = rememberLocaleDecimalSeparator()
    val focusRequester = remember { FocusRequester() }
    val enteredError = if (state.fieldError == PlanFlowError.EnteredPositive || state.fieldError == PlanFlowError.EnteredMax) {
        errorMessageFor(state.fieldError)
    } else {
        null
    }

    LaunchedEffect(state.validationAttempt) {
        if (state.validationAttempt > 0 && enteredError != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.plan_flow_step4_title),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = stringResource(R.string.plan_flow_step4_body, state.currentCycleStartText),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
        )

        // Option 1: Phone record
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .selectable(
                    selected = state.firstCycleChoice == FirstCycleChoice.PhoneRecord,
                    role = Role.RadioButton,
                    onClick = { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.PhoneRecord)) },
                )
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = state.firstCycleChoice == FirstCycleChoice.PhoneRecord,
                onClick = null,
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = stringResource(R.string.plan_flow_choice_phone_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                )
                Text(
                    text = stringResource(R.string.plan_flow_choice_phone_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }

        // Option 2: Entered
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .selectable(
                        selected = state.firstCycleChoice == FirstCycleChoice.Entered,
                        role = Role.RadioButton,
                        onClick = { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.Entered)) },
                    )
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = state.firstCycleChoice == FirstCycleChoice.Entered,
                    onClick = null,
                )
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = stringResource(R.string.plan_flow_choice_entered_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.text,
                    )
                    Text(
                        text = stringResource(R.string.plan_flow_choice_entered_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                    )
                }
            }
            EmberbyteAnimatedVisibility(visible = state.firstCycleChoice == FirstCycleChoice.Entered) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 32.dp, top = 8.dp)) {
                    OutlinedTextField(
                        value = state.enteredAmount,
                        onValueChange = { raw ->
                            val filtered = filterDecimalInput(raw, decimalSeparator)
                            if (filtered != null) {
                                onEvent(PlanFlowEvent.SetEnteredAmount(filtered))
                            } else {
                                onEvent(PlanFlowEvent.SetEnteredAmount(raw))
                            }
                        },
                        label = { Text(stringResource(R.string.plan_field_used_so_far)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                        isError = enteredError != null,
                        supportingText = enteredError?.let { msg -> { Text(msg) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .keyboardSemantics(KeyboardType.Decimal, enteredError),
                    )
                    val unitOptions = listOf(
                        stringResource(R.string.plan_unit_mb),
                        stringResource(R.string.plan_unit_gb),
                        stringResource(R.string.plan_unit_tb),
                    )
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
                    EmberbyteAnimatedVisibility(visible = state.isEnteredOverCap) {
                        Text(
                            text = stringResource(R.string.plan_flow_over_cap_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.warnText,
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
                .sizeIn(minHeight = 48.dp)
                .selectable(
                    selected = state.firstCycleChoice == FirstCycleChoice.StartNextCycle,
                    role = Role.RadioButton,
                    onClick = { onEvent(PlanFlowEvent.SetFirstCycleChoice(FirstCycleChoice.StartNextCycle)) },
                )
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = state.firstCycleChoice == FirstCycleChoice.StartNextCycle,
                onClick = null,
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = stringResource(R.string.plan_flow_choice_next_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                )
                Text(
                    text = stringResource(R.string.plan_flow_choice_next_sub, state.nextCycleStartText),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
    }
}

internal fun buildReviewPreviewCard(
    state: PlanFlowState,
    daysLeftText: String,
    startsAtText: String?,
): PlanCardUi {
    val units = state.unitSystem.toByteUnits()
    val capBytes = bytesOf(state.capText, state.capUnit, state.unitSystem)?.coerceAtLeast(1L) ?: 1L
    val isDormant = state.isMidCycleApplicable && state.firstCycleChoice == FirstCycleChoice.StartNextCycle
    val usedBytes = if (state.isMidCycleApplicable && state.firstCycleChoice == FirstCycleChoice.Entered) {
        bytesOf(state.enteredAmount, state.enteredUnit, state.unitSystem) ?: 0L
    } else {
        0L
    }
    val remainingBytes = capBytes - usedBytes
    val fraction = (usedBytes.toFloat() / capBytes.toFloat()).coerceIn(0f, 1f)
    val percent = ((usedBytes.toDouble() / capBytes.toDouble()) * 100.0).toInt().coerceAtLeast(0)
    val ringState = when {
        percent >= 100 -> BudgetStateUi.Exceeded
        percent >= 80 -> BudgetStateUi.Warning
        else -> BudgetStateUi.Ok
    }
    return PlanCardUi(
        id = 0L,
        name = state.name.trim(),
        simLabel = null,
        fraction = fraction,
        ringState = ringState,
        percentText = "$percent%",
        usedText = "${plainBytes(usedBytes, units)} of ${plainBytes(capBytes, units)}",
        remainingText = remainingText(remainingBytes, units),
        daysLeftText = daysLeftText,
        forecastText = null,
        isEstimated = state.isMidCycleApplicable && state.firstCycleChoice == FirstCycleChoice.PhoneRecord,
        chips = emptyList(),
        isDormant = isDormant,
        startsAtText = if (isDormant) startsAtText else null,
        enteredNote = null,
        pinned = false,
    )
}

@Composable
fun StepReview(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val daysLeftText = when (state.daysUntilNextRenewal) {
        0 -> stringResource(R.string.plan_preview_renews_today)
        1 -> stringResource(R.string.plan_preview_one_day_left)
        else -> stringResource(R.string.plan_preview_days_left, state.daysUntilNextRenewal)
    }
    val startsAtText = if (state.nextCycleStartText.isNotEmpty()) {
        stringResource(R.string.plan_preview_starts_at, state.nextCycleStartText)
    } else {
        null
    }
    val previewCard = remember(state, daysLeftText, startsAtText) {
        buildReviewPreviewCard(state, daysLeftText, startsAtText)
    }
    val simDisplay = state.sims.firstOrNull { it.subscriptionId == state.subscriptionId }?.label
        ?: stringResource(R.string.plan_sim_none)
    val capDisplay = "${state.capText} ${state.capUnit.label}".trim()
    val cycleDisplay = if (state.cycleKind == CycleKind.Monthly) {
        stringResource(R.string.plan_review_cycle_monthly, state.dayOfMonth)
    } else {
        stringResource(R.string.plan_review_cycle_every_days, state.everyDays)
    }
    val renewsDisplay = stringResource(R.string.plan_review_renews_value, state.nextCycleStartText)
    val startDisplay = if (!state.isMidCycleApplicable) {
        stringResource(R.string.plan_review_start_today)
    } else {
        when (state.firstCycleChoice) {
            FirstCycleChoice.PhoneRecord -> stringResource(R.string.plan_review_start_phone)
            FirstCycleChoice.Entered -> stringResource(R.string.plan_review_start_entered, state.enteredAmount, state.enteredUnit.label)
            FirstCycleChoice.StartNextCycle -> stringResource(R.string.plan_review_start_next, state.nextCycleStartText)
        }
    }
    val previewDesc = stringResource(R.string.plan_review_preview_desc, state.name, previewCard.remainingText)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.plan_flow_review_title),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        PlanCard(
            model = previewCard,
            onClick = {},
            modifier = Modifier.clearAndSetSemantics { contentDescription = previewDesc },
        )

        BentoTile(
            title = stringResource(R.string.plan_review_summary_title),
            container = TileContainer.Standard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewRow(label = stringResource(R.string.plan_field_name), value = state.name)
                ReviewRow(label = stringResource(R.string.plan_field_cap), value = capDisplay, ltrValue = true)
                ReviewRow(label = stringResource(R.string.plan_field_cycle), value = cycleDisplay)
                ReviewRow(label = stringResource(R.string.plan_field_sim), value = simDisplay)
                ReviewRow(label = stringResource(R.string.plan_review_next_renewal_label), value = renewsDisplay)
                ReviewRow(label = stringResource(R.string.plan_review_start_label), value = startDisplay)
            }
        }
    }
}

@Composable
private fun ReviewRow(
    label: String,
    value: String,
    ltrValue: Boolean = false,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
            modifier = Modifier.padding(end = 12.dp),
        )
        if (ltrValue) {
            ForceLtr {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.text,
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.text,
            )
        }
    }
}
