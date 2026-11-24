@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.common.EmberbyteScreenScaffold
import io.github.khaledbahaaeldin.emberbyte.nav.GuardedBack
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import io.github.khaledbahaaeldin.emberbyte.ui.design.chips.EmberbyteFilterChip
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PermissionPromptUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.PlanCard
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.PermissionPrompt
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer
import java.time.DayOfWeek

@Composable
fun PlanEditorScreen(
    viewModel: PlanEditorViewModel,
    onBack: () -> Unit,
    onPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }
    PlanEditorContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onPrompt = onPrompt,
        modifier = modifier,
    )
}

@Composable
internal fun PlanEditorContent(
    state: PlanEditorUiState,
    onEvent: (PlanEditorEvent) -> Unit,
    onBack: () -> Unit,
    onPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val backRequester = remember { FocusRequester() }

    val handleBack: () -> Unit = {
        if (state.isDirty) {
            backDispatcher?.onBackPressed() ?: onBack()
        } else {
            onBack()
        }
    }

    GuardedBack(
        dirty = state.isDirty,
        onConfirmLeave = onBack,
        onDismiss = { runCatching { backRequester.requestFocus() } },
    ) { shrink ->
        var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

        if (showDeleteDialog) {
            EmberbyteDialog(
                onDismiss = { showDeleteDialog = false },
                title = stringResource(R.string.plan_delete_dialog_title),
                icon = Icons.Rounded.Delete,
                destructive = true,
                confirm = DialogAction(
                    label = stringResource(R.string.plan_delete_dialog_confirm),
                    onClick = {
                        showDeleteDialog = false
                        onEvent(PlanEditorEvent.Delete)
                    },
                ),
                dismiss = DialogAction(
                    label = stringResource(R.string.dialog_cancel),
                    onClick = { showDeleteDialog = false },
                ),
            ) {
                Text(
                    text = stringResource(R.string.plan_delete_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }
        }

        val showStickyBar = !state.loading && !state.notFound
        EmberbyteScreenScaffold(
            title = stringResource(if (state.isNew) R.string.plan_editor_title_new else R.string.plan_editor_title_edit),
            onBack = handleBack,
            backModifier = Modifier.focusRequester(backRequester),
            bottomBar = if (showStickyBar) {
                {
                    Surface(
                        color = palette.surface,
                        tonalElevation = 2.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = handleBack,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            ) {
                                Text(stringResource(R.string.plan_editor_discard_changes))
                            }
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = { onEvent(PlanEditorEvent.Save) },
                                enabled = !state.saving,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 112.dp, minHeight = 48.dp),
                            ) {
                                Text(stringResource(R.string.plan_editor_save))
                            }
                        }
                    }
                }
            } else {
                null
            },
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = shrink
                    scaleY = shrink
                },
        ) { scaffoldPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    state.loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingState(label = stringResource(R.string.plan_editor_loading))
                        }
                    }

                    state.notFound -> {
                        Text(stringResource(R.string.plan_editor_not_found))
                        Button(
                            onClick = onBack,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Text(stringResource(R.string.plan_flow_back))
                        }
                    }

                    else -> {
                        // Preview section
                        if (state.preview != null) {
                            BentoTile(
                                title = stringResource(R.string.plan_section_preview),
                                container = TileContainer.Standard,
                                headingTitle = true,
                            ) {
                                Box(Modifier.padding(top = 8.dp)) {
                                    PlanCard(model = state.preview, onClick = {})
                                }
                            }
                        }

                        // What-if section (excluded from dirty state, inline result chip + full date)
                        if (state.whatIf != null) {
                            WhatIfCard(
                                whatIf = state.whatIf,
                                onAmountChange = { onEvent(PlanEditorEvent.SetWhatIfAmount(it)) },
                                onUnitChange = { onEvent(PlanEditorEvent.SetWhatIfUnit(it)) },
                            )
                        }

                        // Basics section
                        BentoTile(
                            title = stringResource(R.string.plan_section_basics),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val nameError = state.errors["name"]
                                OutlinedTextField(
                                    value = state.form.name,
                                    onValueChange = { onEvent(PlanEditorEvent.SetName(it)) },
                                    label = { Text(stringResource(R.string.plan_field_name)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            if (nameError != null) error(nameError)
                                        },
                                    singleLine = true,
                                    isError = nameError != null,
                                    supportingText = nameError?.let { msg -> { Text(msg, color = palette.danger) } },
                                )

                                Text(
                                    text = stringResource(R.string.plan_field_sim),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.text,
                                )
                                if (state.needsPhonePermission) {
                                    PermissionPrompt(
                                        model = PermissionPromptUi(
                                            id = "phone_state",
                                            title = stringResource(R.string.plan_phone_perm_title),
                                            body = stringResource(R.string.plan_phone_perm_body),
                                            actionLabel = stringResource(R.string.plan_phone_perm_allow),
                                        ),
                                        onAction = { onPrompt("phone_state") },
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sim_radio_list"),
                                ) {
                                    for (sim in state.sims) {
                                        val isSelected = state.form.subscriptionId == sim.subscriptionId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .selectable(
                                                    selected = isSelected,
                                                    role = Role.RadioButton,
                                                    onClick = { onEvent(PlanEditorEvent.SetSim(sim.subscriptionId)) },
                                                )
                                                .semantics(mergeDescendants = true) {}
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = null,
                                            )
                                            Text(sim.label, modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Cap and cycle section (animates conditional height on spatial spring, snaps when reduced)
                        val sizeAnimSpec = if (reduceMotion) snap() else motion.spatial<androidx.compose.ui.unit.IntSize>()
                        BentoTile(
                            title = stringResource(R.string.plan_section_cap_cycle),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cap_and_cycle_card")
                                    .animateContentSize(animationSpec = sizeAnimSpec)
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val capError = state.errors["capBytes"]
                                OutlinedTextField(
                                    value = state.form.capText,
                                    onValueChange = { onEvent(PlanEditorEvent.SetCap(it)) },
                                    label = { Text(stringResource(R.string.plan_field_cap)) },
                                    trailingIcon = {
                                        Text(
                                            text = state.form.capUnit.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.muted,
                                            modifier = Modifier.padding(end = 8.dp),
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            if (capError != null) error(capError)
                                        },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    isError = capError != null,
                                    supportingText = capError?.let { msg -> { Text(msg, color = palette.danger) } },
                                )
                                CapUnitButtonGroup(
                                    selected = state.form.capUnit,
                                    units = CapUnit.entries,
                                    onSelect = { onEvent(PlanEditorEvent.SetCapUnit(it)) },
                                )

                                Text(
                                    text = stringResource(R.string.plan_field_cycle),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.text,
                                )
                                val cycleOptions = listOf(
                                    stringResource(R.string.plan_cycle_monthly),
                                    stringResource(R.string.plan_cycle_every_n_days),
                                )
                                ConnectedButtonGroup(
                                    options = cycleOptions,
                                    selectedIndex = if (state.form.cycleKind == CycleKind.Monthly) 0 else 1,
                                    onSelect = { index ->
                                        val next = if (index == 0) CycleKind.Monthly else CycleKind.EveryNDays
                                        onEvent(PlanEditorEvent.SetCycleKind(next))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                EmberbyteAnimatedVisibility(visible = state.form.cycleKind == CycleKind.Monthly) {
                                    val dayError = state.errors["cycle.dayOfMonth"]
                                    OutlinedTextField(
                                        value = if (state.form.dayOfMonth == 0) "" else state.form.dayOfMonth.toString(),
                                        onValueChange = { input ->
                                            val day = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                                            onEvent(PlanEditorEvent.SetDayOfMonth(day))
                                        },
                                        label = { Text(stringResource(R.string.plan_field_day_of_month)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                if (dayError != null) error(dayError)
                                            },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = dayError != null,
                                        supportingText = dayError?.let { msg -> { Text(msg, color = palette.danger) } },
                                    )
                                }

                                EmberbyteAnimatedVisibility(visible = state.form.cycleKind == CycleKind.EveryNDays) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        val daysError = state.errors["cycle.days"]
                                        OutlinedTextField(
                                            value = if (state.form.everyDays == 0) "" else state.form.everyDays.toString(),
                                            onValueChange = { input ->
                                                val days = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                onEvent(PlanEditorEvent.SetEveryDays(days))
                                            },
                                            label = { Text(stringResource(R.string.plan_field_every_days)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    if (daysError != null) error(daysError)
                                                },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = daysError != null,
                                            supportingText = daysError?.let { msg -> { Text(msg, color = palette.danger) } },
                                        )
                                        DateField(
                                            label = stringResource(R.string.plan_field_starts_on),
                                            date = state.form.anchor,
                                            onChange = { onEvent(PlanEditorEvent.SetAnchor(it)) },
                                        )
                                    }
                                }

                                TimeField(
                                    label = stringResource(R.string.plan_field_renews_at),
                                    time = state.form.renewalTime,
                                    onChange = { onEvent(PlanEditorEvent.SetRenewalTime(it)) },
                                )
                            }
                        }

                        // Rollover section
                        BentoTile(
                            title = stringResource(R.string.plan_section_rollover),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = sizeAnimSpec)
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val rolloverKinds = RolloverKind.entries
                                val rolloverLabels = listOf(
                                    stringResource(R.string.plan_rollover_none),
                                    stringResource(R.string.plan_rollover_full),
                                    stringResource(R.string.plan_rollover_capped),
                                )
                                ConnectedButtonGroup(
                                    options = rolloverLabels,
                                    selectedIndex = rolloverKinds.indexOf(state.form.rolloverKind).coerceAtLeast(0),
                                    onSelect = { idx -> onEvent(PlanEditorEvent.SetRolloverKind(rolloverKinds[idx])) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                EmberbyteAnimatedVisibility(visible = state.form.rolloverKind == RolloverKind.Capped) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        val rolloverError = state.errors["rollover.maxBytes"]
                                        OutlinedTextField(
                                            value = state.form.rolloverCapText,
                                            onValueChange = { onEvent(PlanEditorEvent.SetRolloverCap(it)) },
                                            label = { Text(stringResource(R.string.plan_field_max_rollover)) },
                                            trailingIcon = {
                                                Text(
                                                    text = state.form.rolloverCapUnit.label,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = palette.muted,
                                                    modifier = Modifier.padding(end = 8.dp),
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    if (rolloverError != null) error(rolloverError)
                                                },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = rolloverError != null,
                                            supportingText = rolloverError?.let { msg -> { Text(msg, color = palette.danger) } },
                                        )
                                        CapUnitButtonGroup(
                                            selected = state.form.rolloverCapUnit,
                                            units = CapUnit.entries,
                                            onSelect = { onEvent(PlanEditorEvent.SetRolloverUnit(it)) },
                                        )
                                    }
                                }
                            }
                        }

                        // Add-on packs section
                        BentoTile(
                            title = stringResource(R.string.plan_section_addons),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = sizeAnimSpec)
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                state.form.addOns.forEachIndexed { index, addOn ->
                                    AddOnItemCard(
                                        index = index,
                                        addOn = addOn,
                                        errors = state.errors,
                                        onUpdate = { onEvent(PlanEditorEvent.UpdateAddOn(index, it)) },
                                        onRemove = { onEvent(PlanEditorEvent.RemoveAddOn(index)) },
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onEvent(PlanEditorEvent.AddAddOn) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                ) {
                                    Text(stringResource(R.string.plan_addon_add))
                                }
                            }
                        }

                        // Free windows section
                        BentoTile(
                            title = stringResource(R.string.plan_section_free_windows),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = sizeAnimSpec)
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.plan_free_window_helper),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.muted,
                                )
                                state.form.freeRules.forEachIndexed { index, rule ->
                                    FreeWindowItemCard(
                                        index = index,
                                        rule = rule,
                                        availableApps = state.availableApps,
                                        errors = state.errors,
                                        onUpdate = { onEvent(PlanEditorEvent.UpdateFreeRule(index, it)) },
                                        onRemove = { onEvent(PlanEditorEvent.RemoveFreeRule(index)) },
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onEvent(PlanEditorEvent.AddFreeRule) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                ) {
                                    Text(stringResource(R.string.plan_free_window_add))
                                }
                            }
                        }

                        // This cycle section (ConnectedButtonGroup, zero stock RadioButtons!)
                        BentoTile(
                            title = stringResource(R.string.plan_section_this_cycle),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = sizeAnimSpec)
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val choices = FirstCycleChoice.entries
                                val choiceTitles = listOf(
                                    stringResource(R.string.plan_this_cycle_phone),
                                    stringResource(R.string.plan_this_cycle_entered),
                                    stringResource(R.string.plan_this_cycle_next),
                                )
                                val selectedIdx = choices.indexOf(state.form.firstCycleChoice).coerceAtLeast(0)
                                ConnectedButtonGroup(
                                    options = choiceTitles,
                                    selectedIndex = selectedIdx,
                                    onSelect = { idx -> onEvent(PlanEditorEvent.SetFirstCycleChoice(choices[idx])) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                val helperText = when (state.form.firstCycleChoice) {
                                    FirstCycleChoice.PhoneRecord -> stringResource(R.string.plan_this_cycle_phone_helper)
                                    FirstCycleChoice.Entered -> stringResource(R.string.plan_this_cycle_entered_helper)
                                    FirstCycleChoice.StartNextCycle -> stringResource(R.string.plan_this_cycle_next_helper)
                                }
                                Text(
                                    text = helperText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.muted,
                                )

                                EmberbyteAnimatedVisibility(visible = state.form.firstCycleChoice == FirstCycleChoice.Entered) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        OutlinedTextField(
                                            value = state.form.enteredAmount,
                                            onValueChange = { onEvent(PlanEditorEvent.SetEnteredAmount(it)) },
                                            label = { Text(stringResource(R.string.plan_field_entered_usage)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        )
                                        CapUnitButtonGroup(
                                            selected = state.form.enteredUnit,
                                            units = listOf(CapUnit.MB, CapUnit.GB),
                                            onSelect = { onEvent(PlanEditorEvent.SetEnteredUnit(it)) },
                                        )
                                    }
                                }
                            }
                        }

                        // Alerts section (multi-select with helper copy)
                        BentoTile(
                            title = stringResource(R.string.plan_section_alerts),
                            container = TileContainer.Standard,
                            headingTitle = true,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.plan_alerts_helper),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.text,
                                )
                                FlowRow(
                                    modifier = Modifier.testTag("alerts_multi_select"),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    for (percent in listOf(50, 80, 90, 100)) {
                                        EmberbyteFilterChip(
                                            selected = percent in state.form.alertThresholds,
                                            onClick = { onEvent(PlanEditorEvent.ToggleThreshold(percent)) },
                                            label = { Text("$percent%", softWrap = false, maxLines = 1) },
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.plan_alerts_none_helper),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.muted,
                                )
                            }
                        }

                        val consumedKeys = buildSet {
                            add("name")
                            add("capBytes")
                            if (state.form.cycleKind == CycleKind.Monthly) add("cycle.dayOfMonth")
                            if (state.form.cycleKind == CycleKind.EveryNDays) add("cycle.days")
                            if (state.form.rolloverKind == RolloverKind.Capped) add("rollover.maxBytes")
                            state.form.addOns.indices.forEach { index ->
                                add("addOn.$index.label")
                                add("addOn.$index.bytes")
                                add("addOn.$index.validUntil")
                            }
                            state.form.freeRules.indices.forEach { index ->
                                add("freeRule.$index.label")
                                add("freeRule.$index.days")
                                add("freeRule.$index.time")
                                add("freeRule.$index.packageName")
                            }
                        }
                        val unconsumedErrors = state.errors.filterKeys { it !in consumedKeys }.toSortedMap()
                        for ((_, unconsumedMessage) in unconsumedErrors) {
                            Text(
                                text = unconsumedMessage,
                                color = palette.danger,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        // Danger zone (existing plans only)
                        if (!state.isNew) {
                            BentoTile(
                                title = stringResource(R.string.plan_section_danger),
                                container = TileContainer.Standard,
                                headingTitle = true,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (state.isArchived) {
                                        OutlinedButton(
                                            onClick = { onEvent(PlanEditorEvent.Unarchive) },
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        ) {
                                            Text(stringResource(R.string.plan_unarchive_action))
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onEvent(PlanEditorEvent.Archive) },
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        ) {
                                            Text(stringResource(R.string.plan_archive_action))
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = { showDeleteDialog = true },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                        ),
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.plan_delete_action))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapUnitButtonGroup(
    selected: CapUnit,
    units: List<CapUnit>,
    onSelect: (CapUnit) -> Unit,
) {
    val selectedIndex = units.indexOf(selected).coerceAtLeast(0)
    ConnectedButtonGroup(
        options = units.map { it.label },
        selectedIndex = selectedIndex,
        onSelect = { idx -> onSelect(units[idx]) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun WhatIfCard(
    whatIf: WhatIfUi,
    onAmountChange: (String) -> Unit,
    onUnitChange: (CapUnit) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val whatIfError = whatIf.error
    BentoTile(
        title = stringResource(R.string.plan_section_what_if),
        container = TileContainer.Standard,
        headingTitle = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = whatIf.amountText,
                onValueChange = onAmountChange,
                label = { Text(stringResource(R.string.plan_what_if_extra_label)) },
                trailingIcon = {
                    Text(
                        text = whatIf.unit.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.muted,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (whatIfError != null) error(whatIfError)
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = whatIfError != null,
                supportingText = whatIfError?.let { msg -> { Text(msg, color = palette.danger) } },
            )
            CapUnitButtonGroup(
                selected = whatIf.unit,
                units = listOf(CapUnit.MB, CapUnit.GB),
                onSelect = onUnitChange,
            )
            ForceLtr {
                Text(
                    text = stringResource(R.string.plan_what_if_now, whatIf.currentText),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.text,
                )
            }
            if (whatIf.whatIfText != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = palette.primary.copy(alpha = 0.14f),
                    modifier = Modifier.testTag("what_if_result_chip"),
                ) {
                    ForceLtr {
                        Text(
                            text = stringResource(R.string.plan_what_if_with_extra, whatIf.whatIfText),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddOnItemCard(
    index: Int,
    addOn: AddOnDraft,
    errors: Map<String, String>,
    onUpdate: (AddOnDraft) -> Unit,
    onRemove: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = addOn.label.ifBlank { stringResource(R.string.plan_addon_card_title, index + 1) },
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.text,
                )
                IconButton(
                    onClick = onRemove,
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = palette.muted),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.plan_addon_remove_desc),
                    )
                }
            }

            val labelError = errors["addOn.$index.label"]
            OutlinedTextField(
                value = addOn.label,
                onValueChange = { onUpdate(addOn.copy(label = it)) },
                label = { Text(stringResource(R.string.plan_field_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (labelError != null) error(labelError)
                    },
                singleLine = true,
                isError = labelError != null,
                supportingText = labelError?.let { msg -> { Text(msg, color = palette.danger) } },
            )

            val amountError = errors["addOn.$index.bytes"]
            OutlinedTextField(
                value = addOn.amountText,
                onValueChange = { onUpdate(addOn.copy(amountText = it)) },
                label = { Text(stringResource(R.string.plan_field_amount)) },
                trailingIcon = {
                    Text(
                        text = addOn.unit.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.muted,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (amountError != null) error(amountError)
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = amountError?.let { msg -> { Text(msg, color = palette.danger) } },
            )
            CapUnitButtonGroup(
                selected = addOn.unit,
                units = CapUnit.entries,
                onSelect = { onUpdate(addOn.copy(unit = it)) },
            )
            DateField(
                label = stringResource(R.string.plan_field_valid_from),
                date = addOn.validFrom,
                onChange = { onUpdate(addOn.copy(validFrom = it)) },
            )
            DateField(
                label = stringResource(R.string.plan_field_valid_until),
                date = addOn.validUntil,
                onChange = { onUpdate(addOn.copy(validUntil = it)) },
            )
            errors["addOn.$index.validUntil"]?.let { msg ->
                Text(
                    text = msg,
                    color = palette.danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FreeWindowItemCard(
    index: Int,
    rule: FreeRuleDraft,
    availableApps: List<AppChoiceUi>,
    errors: Map<String, String>,
    onUpdate: (FreeRuleDraft) -> Unit,
    onRemove: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    var showAppPicker by rememberSaveable { mutableStateOf(false) }

    if (showAppPicker) {
        FreeWindowAppPickerDialog(
            selectedPackage = rule.packageName,
            availableApps = availableApps,
            onDismiss = { showAppPicker = false },
            onSelect = { pkg ->
                showAppPicker = false
                onUpdate(rule.copy(packageName = pkg))
            },
        )
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rule.label.ifBlank { stringResource(R.string.plan_free_window_card_title, index + 1) },
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.text,
                )
                IconButton(
                    onClick = onRemove,
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = palette.muted),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.plan_free_window_remove_desc),
                    )
                }
            }

            val ruleLabelError = errors["freeRule.$index.label"]
            OutlinedTextField(
                value = rule.label,
                onValueChange = { onUpdate(rule.copy(label = it)) },
                label = { Text(stringResource(R.string.plan_field_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (ruleLabelError != null) error(ruleLabelError)
                    },
                singleLine = true,
                isError = ruleLabelError != null,
                supportingText = ruleLabelError?.let { msg -> { Text(msg, color = palette.danger) } },
            )

            val allDays = DayOfWeek.entries.toSet()
            val allSelected = rule.days.size == allDays.size
            val dayLabels = listOf(
                DayOfWeek.MONDAY to stringResource(R.string.plan_day_mon),
                DayOfWeek.TUESDAY to stringResource(R.string.plan_day_tue),
                DayOfWeek.WEDNESDAY to stringResource(R.string.plan_day_wed),
                DayOfWeek.THURSDAY to stringResource(R.string.plan_day_thu),
                DayOfWeek.FRIDAY to stringResource(R.string.plan_day_fri),
                DayOfWeek.SATURDAY to stringResource(R.string.plan_day_sat),
                DayOfWeek.SUNDAY to stringResource(R.string.plan_day_sun),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("free_window_days_multi_select"),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EmberbyteFilterChip(
                    selected = allSelected,
                    onClick = {
                        val nextDays = if (allSelected) emptySet() else allDays
                        onUpdate(rule.copy(days = nextDays))
                    },
                    label = { Text(stringResource(R.string.plan_free_window_all_days), softWrap = false) },
                )
                for ((day, label) in dayLabels) {
                    EmberbyteFilterChip(
                        selected = day in rule.days,
                        onClick = {
                            val nextDays = if (day in rule.days) rule.days - day else rule.days + day
                            onUpdate(rule.copy(days = nextDays))
                        },
                        label = { Text(label, softWrap = false) },
                    )
                }
            }
            errors["freeRule.$index.days"]?.let { msg ->
                Text(
                    text = msg,
                    color = palette.danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            TimeField(
                label = stringResource(R.string.plan_field_from),
                time = rule.start,
                onChange = { onUpdate(rule.copy(start = it)) },
            )
            TimeField(
                label = stringResource(R.string.plan_field_to),
                time = rule.end,
                onChange = { onUpdate(rule.copy(end = it)) },
            )
            errors["freeRule.$index.time"]?.let { msg ->
                Text(
                    text = msg,
                    color = palette.danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            val selectedAppLabel = if (rule.packageName == null) {
                stringResource(R.string.plan_free_window_all_apps)
            } else {
                availableApps.firstOrNull { it.packageName == rule.packageName }?.label ?: rule.packageName
            }
            OutlinedButton(
                onClick = { showAppPicker = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.plan_free_window_app_label, selectedAppLabel))
            }
            errors["freeRule.$index.packageName"]?.let { msg ->
                Text(
                    text = msg,
                    color = palette.danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FreeWindowAppPickerDialog(
    selectedPackage: String?,
    availableApps: List<AppChoiceUi>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    var search by rememberSaveable { mutableStateOf("") }
    val filtered = if (search.isBlank()) {
        availableApps
    } else {
        availableApps.filter {
            it.label.contains(search, ignoreCase = true) || it.packageName.contains(search, ignoreCase = true)
        }
    }

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.plan_free_window_choose_app),
        icon = Icons.Rounded.Apps,
        confirm = DialogAction(
            label = stringResource(R.string.plan_dialog_ok),
            onClick = onDismiss,
        ),
        dismiss = DialogAction(
            label = stringResource(R.string.dialog_cancel),
            onClick = onDismiss,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (availableApps.size > 4) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text(stringResource(R.string.plan_free_window_search_apps)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .selectable(
                        selected = selectedPackage == null,
                        role = Role.RadioButton,
                        onClick = { onSelect(null) },
                    )
                    .semantics(mergeDescendants = true) {}
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selectedPackage == null, onClick = null)
                Text(
                    text = stringResource(R.string.plan_free_window_all_apps),
                    color = palette.text,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            for (app in filtered) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp)
                        .selectable(
                            selected = selectedPackage == app.packageName,
                            role = Role.RadioButton,
                            onClick = { onSelect(app.packageName) },
                        )
                        .semantics(mergeDescendants = true) {}
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selectedPackage == app.packageName, onClick = null)
                    Text(
                        text = app.label,
                        color = palette.text,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
