package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.graphics.graphicsLayer
import io.github.khaledbahaaeldin.emberbyte.nav.GuardedBack
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.nav.BackTransitions
import io.github.khaledbahaaeldin.emberbyte.nav.NavDiagnostics
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    // Going back a step follows the finger: the step content shrinks and rounds like any other screen being closed,
    // springs back when the gesture is cancelled, and goes to the previous step when it is committed.
    var stepBackProgress by remember { mutableFloatStateOf(0f) }
    var stepBackDragging by remember { mutableStateOf(false) }
    PredictiveBackHandler(enabled = state.step > 1) { progress ->
        NavDiagnostics.record("BACK_START", "plan-step")
        stepBackDragging = true
        try {
            progress.collect { event -> stepBackProgress = event.progress.coerceIn(0f, 1f) }
            NavDiagnostics.record("BACK_COMMIT", "plan-step")
            stepBackDragging = false
            stepBackProgress = 0f
            onEvent(PlanFlowEvent.PreviousStep)
        } catch (_: CancellationException) {
            NavDiagnostics.record("BACK_CANCEL", "plan-step")
            stepBackDragging = false
            stepBackProgress = 0f
        }
    }
    val shownBackProgress by animateFloatAsState(
        targetValue = stepBackProgress,
        animationSpec = if (stepBackDragging || reduceMotion) snap() else BackTransitions.cancelSpring,
        label = "PlanFlowStepBackProgress",
    )
    val layoutDirection = LocalLayoutDirection.current
    val rtl = layoutDirection == LayoutDirection.Rtl
    val motion = MaterialTheme.motionScheme

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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        if (!reduceMotion) {
                            val scale = BackTransitions.shrinkFor(shownBackProgress)
                            val radius = BackTransitions.cornerRadiusFor(shownBackProgress)
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = BackTransitions.transformOriginFor(layoutDirection)
                            shape = RoundedCornerShape(radius)
                            clip = radius > 0.dp
                        }
                    },
            ) {
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
                        transitionSpec = {
                            val direction = stepDirection(initialState, targetState, rtl)
                            (
                                slideInHorizontally(motion.defaultSpatialSpec()) {
                                    (it * STEP_SLIDE_FRACTION * direction).roundToInt()
                                } + fadeIn(motion.defaultEffectsSpec())
                                ) togetherWith (
                                slideOutHorizontally(motion.defaultSpatialSpec()) {
                                    (-it * STEP_SLIDE_FRACTION * direction).roundToInt()
                                } + fadeOut(motion.fastEffectsSpec())
                                ) using SizeTransform(clip = false)
                        },
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
