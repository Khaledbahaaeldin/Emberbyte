package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.graphics.graphicsLayer
import io.github.khaledbahaaeldin.emberbyte.nav.GuardedBack
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

@Composable
fun PlanFlowScreen(
    viewModel: PlanFlowViewModel,
    onPlanCreated: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.createdPlanId) {
        state.createdPlanId?.let { id ->
            onPlanCreated(id)
        }
    }

    PlanFlowContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onCancel,
        modifier = modifier,
    )
}

@Composable
fun PlanFlowContent(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val hasEnteredData = state.name.isNotBlank() || state.capText.isNotBlank() || state.enteredAmount.isNotBlank()
    val isDirty = state.step > 1 || hasEnteredData
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler(enabled = state.step > 1) {
        onEvent(PlanFlowEvent.PreviousStep)
    }

    GuardedBack(
        dirty = state.step == 1 && hasEnteredData,
        onConfirmLeave = onBack,
    ) { shrink ->
        if (showDiscardConfirm) {
            EmberbyteDialog(
                onDismiss = { showDiscardConfirm = false },
                title = "Discard plan?",
                icon = Icons.Rounded.Delete,
                destructive = true,
                confirm = DialogAction(
                    label = "Discard",
                    onClick = {
                        showDiscardConfirm = false
                        onBack()
                    },
                ),
                dismiss = DialogAction(
                    label = "Cancel",
                    onClick = { showDiscardConfirm = false },
                ),
            ) {
                Text(
                    text = "You have entered details for this plan. Are you sure you want to exit without creating it?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = shrink
                    scaleY = shrink
                }
                .imePadding(),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        if (state.step > 1) {
                            onEvent(PlanFlowEvent.PreviousStep)
                        } else if (hasEnteredData) {
                            showDiscardConfirm = true
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Text("Back")
                }

                if (state.isReviewStep) {
                    Button(
                        onClick = { onEvent(PlanFlowEvent.CreatePlan) },
                        enabled = !state.isSaving && state.createdPlanId == null,
                        modifier = Modifier
                            .weight(2f)
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Text("Create plan")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onEvent(PlanFlowEvent.NextStep) },
                        enabled = state.canContinue(),
                        modifier = Modifier
                            .weight(2f)
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Text("Continue")
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Step ${state.step} of ${state.totalSteps}",
                style = MaterialTheme.typography.labelMedium,
                color = palette.muted,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            LinearProgressIndicator(
                progress = { (state.step.toFloat() / state.totalSteps.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )

            Box(modifier = Modifier.weight(1f)) {
                if (reduceMotion) {
                    when {
                        state.step == 1 -> Step1NameAndSim(state = state, onEvent = onEvent)
                        state.step == 2 -> Step2Cap(state = state, onEvent = onEvent)
                        state.step == 3 -> Step3Cycle(state = state, onEvent = onEvent)
                        state.isMidCycleApplicable && state.step == 4 -> Step4ThisCycle(state = state, onEvent = onEvent)
                        state.isReviewStep -> StepReview(state = state, onEvent = onEvent)
                    }
                } else {
                    AnimatedContent(
                        targetState = state.step,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "PlanFlowStepTransition",
                    ) { step ->
                        when {
                            step == 1 -> Step1NameAndSim(state = state, onEvent = onEvent)
                            step == 2 -> Step2Cap(state = state, onEvent = onEvent)
                            step == 3 -> Step3Cycle(state = state, onEvent = onEvent)
                            state.isMidCycleApplicable && step == 4 -> Step4ThisCycle(state = state, onEvent = onEvent)
                            state.isReviewStep -> StepReview(state = state, onEvent = onEvent)
                        }
                    }
                }
            }
        }
    }
}
}
