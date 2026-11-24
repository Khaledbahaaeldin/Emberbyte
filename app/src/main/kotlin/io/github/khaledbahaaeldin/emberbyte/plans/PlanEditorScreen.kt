package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.nav.GuardedBack
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import kotlinx.coroutines.launch

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
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val backRequester = remember { FocusRequester() }

    val showSlimPreview by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val handleBack: () -> Unit = {
        if (state.isDirty) {
            backDispatcher?.onBackPressed() ?: onBack()
        } else {
            onBack()
        }
    }

    val canSave = !state.saving &&
        state.isDirty &&
        state.errors.isEmpty() &&
        state.form.name.isNotBlank() &&
        state.form.capText.isNotBlank()

    GuardedBack(
        dirty = state.isDirty,
        onConfirmLeave = onBack,
        onDismiss = { runCatching { backRequester.requestFocus() } },
    ) { shrink ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.background)
                .graphicsLayer {
                    scaleX = shrink
                    scaleY = shrink
                },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                EditorHeader(
                    title = if (state.isNew) "New plan" else "Edit plan",
                    canDelete = !state.isNew,
                    showSlimPreview = showSlimPreview,
                    preview = state.preview,
                    onBack = handleBack,
                    onJumpToDelete = {
                        scope.launch {
                            listState.animateScrollToItem(9)
                        }
                    },
                    backFocusRequester = backRequester,
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("editor_lazy_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (state.loading) {
                        item(key = "loading") {
                            LoadingState(
                                label = "Loading plan",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 48.dp),
                            )
                        }
                        return@LazyColumn
                    }

                    if (state.notFound) {
                        item(key = "not_found") {
                            EditorSectionCard(title = "Plan") {
                                Text(
                                    text = "This plan no longer exists",
                                    style = EmberbyteType.heading(PlanFlowType.Body),
                                    color = palette.text,
                                )
                            }
                        }
                        return@LazyColumn
                    }

                    if (state.preview != null) {
                        item(key = "preview") {
                            EditorPreviewCard(preview = state.preview)
                        }
                    }

                    item(key = "plan") {
                        PlanSection(
                            name = state.form.name,
                            nameError = state.errors["name"],
                            subscriptionId = state.form.subscriptionId,
                            sims = state.sims,
                            needsPhonePermission = state.needsPhonePermission,
                            onSetName = { onEvent(PlanEditorEvent.SetName(it)) },
                            onSetSim = { onEvent(PlanEditorEvent.SetSim(it)) },
                            onRequestPhonePermission = { onPrompt("phone_state") },
                        )
                    }

                    item(key = "data") {
                        DataSection(
                            capText = state.form.capText,
                            capUnit = state.form.capUnit,
                            unitSystem = state.unitSystem,
                            capError = state.errors["capBytes"],
                            onSetCap = { onEvent(PlanEditorEvent.SetCap(it)) },
                            onSetCapUnit = { onEvent(PlanEditorEvent.SetCapUnit(it)) },
                        )
                    }

                    item(key = "cycle") {
                        CycleSection(
                            cycleKind = state.form.cycleKind,
                            dayOfMonth = state.form.dayOfMonth,
                            everyDays = state.form.everyDays,
                            anchor = state.form.anchor,
                            renewalTime = state.form.renewalTime,
                            rolloverKind = state.form.rolloverKind,
                            rolloverCapText = state.form.rolloverCapText,
                            rolloverCapUnit = state.form.rolloverCapUnit,
                            unitSystem = state.unitSystem,
                            daysError = state.errors["cycle.days"] ?: state.errors["everyDays"],
                            rolloverError = state.errors["rollover.maxBytes"],
                            onSetCycleKind = { onEvent(PlanEditorEvent.SetCycleKind(it)) },
                            onSetDayOfMonth = { onEvent(PlanEditorEvent.SetDayOfMonth(it)) },
                            onSetEveryDays = { onEvent(PlanEditorEvent.SetEveryDays(it)) },
                            onSetAnchor = { onEvent(PlanEditorEvent.SetAnchor(it)) },
                            onSetRenewalTime = { onEvent(PlanEditorEvent.SetRenewalTime(it)) },
                            onSetRolloverKind = { onEvent(PlanEditorEvent.SetRolloverKind(it)) },
                            onSetRolloverCap = { onEvent(PlanEditorEvent.SetRolloverCap(it)) },
                            onSetRolloverUnit = { onEvent(PlanEditorEvent.SetRolloverUnit(it)) },
                            modifier = Modifier.testTag("editor_cycle_section"),
                        )
                    }

                    val showFirstCycle = state.isNew ||
                        state.form.firstCycle != FirstCycle.PhoneRecord ||
                        state.form.firstCycleChoice != FirstCycleChoice.PhoneRecord ||
                        (state.preview?.isEstimated == true)
                    if (showFirstCycle) {
                        item(key = "this_cycle") {
                            ThisCycleSection(
                                countedSoFarText = state.preview?.usedText?.substringBefore(" of ") ?: "0 GB",
                                firstCycleChoice = state.form.firstCycleChoice,
                                enteredAmount = state.form.enteredAmount,
                                enteredUnit = state.form.enteredUnit,
                                unitSystem = state.unitSystem,
                                enteredError = state.errors["enteredAmount"],
                                onSelectChoice = { onEvent(PlanEditorEvent.SetFirstCycleChoice(it)) },
                                onSetEnteredAmount = { onEvent(PlanEditorEvent.SetEnteredAmount(it)) },
                                onSetEnteredUnit = { onEvent(PlanEditorEvent.SetEnteredUnit(it)) },
                            )
                        }
                    }

                    if (state.whatIf != null) {
                        item(key = "what_if") {
                            val capVal = state.form.capText.trim().replace(',', '.').toDoubleOrNull()?.toInt()?.coerceAtLeast(1) ?: 10
                            val extraVal = state.whatIf.amountText.trim().replace(',', '.').toDoubleOrNull()?.toInt()?.coerceAtLeast(0) ?: 0
                            WhatIfCard(
                                capGb = capVal,
                                extraGb = extraVal,
                                nowText = "Now: ${state.whatIf.currentText}",
                                withExtraText = "With +$extraVal GB: ${state.whatIf.whatIfText}",
                                unitSystem = state.unitSystem,
                                onChangeExtraGb = { gb ->
                                    onEvent(PlanEditorEvent.SetWhatIfUnit(CapUnit.GB))
                                    onEvent(PlanEditorEvent.SetWhatIfAmount(gb.toString()))
                                },
                            )
                        }
                    }

                    item(key = "extras") {
                        ExtrasSection(
                            addOns = state.form.addOns,
                            freeRules = state.form.freeRules,
                            availableApps = state.availableApps,
                            unitSystem = state.unitSystem,
                            errors = state.errors,
                            onAddAddOn = { onEvent(PlanEditorEvent.AddAddOn) },
                            onUpdateAddOn = { idx, draft -> onEvent(PlanEditorEvent.UpdateAddOn(idx, draft)) },
                            onRemoveAddOn = { idx -> onEvent(PlanEditorEvent.RemoveAddOn(idx)) },
                            onAddFreeRule = { onEvent(PlanEditorEvent.AddFreeRule) },
                            onUpdateFreeRule = { idx, draft -> onEvent(PlanEditorEvent.UpdateFreeRule(idx, draft)) },
                            onRemoveFreeRule = { idx -> onEvent(PlanEditorEvent.RemoveFreeRule(idx)) },
                        )
                    }

                    item(key = "alerts") {
                        AlertsSection(
                            thresholds = state.form.alertThresholds,
                            onToggleThreshold = { onEvent(PlanEditorEvent.ToggleThreshold(it)) },
                        )
                    }

                    item(key = "on_home") {
                        OnHomeSection(
                            pinned = state.form.pinned,
                            onSetPinned = { onEvent(PlanEditorEvent.SetPinned(it)) },
                        )
                    }

                    if (!state.isNew) {
                        item(key = "danger") {
                            DangerZone(
                                planName = state.form.name,
                                onDeleteConfirmed = { onEvent(PlanEditorEvent.Delete) },
                            )
                        }
                    }

                    val extraErrors = state.errors.filterKeys { key ->
                        key != "name" &&
                            key != "capBytes" &&
                            key != "cycle.days" &&
                            key != "everyDays" &&
                            key != "rollover.maxBytes" &&
                            key != "enteredAmount" &&
                            !(key.startsWith("addOn.") && key.endsWith(".bytes"))
                    }
                    if (extraErrors.isNotEmpty()) {
                        item(key = "extra_errors") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                extraErrors.values.forEach { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.danger,
                                    )
                                }
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }

            Button(
                onClick = { onEvent(PlanEditorEvent.Save) },
                enabled = canSave,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.background,
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 52.dp)
                    .testTag("editor_save_pill"),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Text(
                    text = "Save",
                    style = EmberbyteType.heading(PlanFlowType.Button),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }

    val otherPinned = state.pendingPinReplacement
    if (otherPinned != null) {
        val thisName = state.form.name.trim().ifEmpty { "this plan" }
        EmberbyteDialog(
            onDismiss = { onEvent(PlanEditorEvent.DismissPinReplacement) },
            title = "Unpin $otherPinned and pin $thisName?",
            icon = Icons.Rounded.PushPin,
            confirm = DialogAction(
                label = "Pin",
                onClick = { onEvent(PlanEditorEvent.ConfirmPinReplacement) },
            ),
            dismiss = DialogAction(
                label = "Cancel",
                onClick = { onEvent(PlanEditorEvent.DismissPinReplacement) },
            ),
        ) {
            Text("Only one plan can be pinned to the front on Home.")
        }
    }
}
