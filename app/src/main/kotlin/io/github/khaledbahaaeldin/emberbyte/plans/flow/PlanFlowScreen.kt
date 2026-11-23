package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.nav.BackTransitions
import io.github.khaledbahaaeldin.emberbyte.nav.GuardedBack
import io.github.khaledbahaaeldin.emberbyte.nav.NavDiagnostics
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException

internal const val PLAN_FLOW_PROGRESS_TAG = "plan_flow_wavy_progress"
internal const val PLAN_FLOW_FILLED_CONTINUE_TAG = "plan_flow_filled_continue"
internal const val PLAN_FLOW_TEXT_BACK_TAG = "plan_flow_text_back"

internal val PlanFlowAnimatedProgressKey = SemanticsPropertyKey<Float>("PlanFlowAnimatedProgress")

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
    val emberbyteMotion = rememberEmberbyteMotion()
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }

    val hasEnteredData = state.name.isNotBlank() || state.capText.isNotBlank() || state.enteredAmount.isNotBlank()

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
    val stepBackShape = remember { PlanStepBackShape { shownBackProgress } }

    GuardedBack(
        dirty = state.step == 1 && hasEnteredData,
        onConfirmLeave = onBack,
    ) { shrink ->
        if (showDiscardConfirm) {
            EmberbyteDialog(
                onDismiss = { showDiscardConfirm = false },
                title = stringResource(R.string.plan_flow_discard_title),
                icon = Icons.Rounded.Delete,
                destructive = true,
                confirm = DialogAction(
                    label = stringResource(R.string.plan_flow_discard_confirm),
                    onClick = {
                        showDiscardConfirm = false
                        onBack()
                    },
                ),
                dismiss = DialogAction(
                    label = stringResource(R.string.plan_flow_discard_cancel),
                    onClick = { showDiscardConfirm = false },
                ),
            ) {
                Text(
                    text = stringResource(R.string.plan_flow_discard_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
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
                    TextButton(
                        onClick = {
                            if (state.step > 1) {
                                onEvent(PlanFlowEvent.PreviousStep)
                            } else if (hasEnteredData) {
                                showDiscardConfirm = true
                            } else {
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = palette.linkText),
                        modifier = Modifier
                            .weight(1f)
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag(PLAN_FLOW_TEXT_BACK_TAG),
                    ) {
                        Text(stringResource(R.string.plan_flow_back))
                    }

                    val ctaEnabled = if (state.isReviewStep) {
                        !state.isSaving && state.createdPlanId == null
                    } else {
                        true
                    }

                    Button(
                        onClick = {
                            if (state.isReviewStep) {
                                onEvent(PlanFlowEvent.CreatePlan)
                            } else {
                                onEvent(PlanFlowEvent.NextStep)
                            }
                        },
                        enabled = ctaEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = palette.onPrimary,
                            disabledContainerColor = palette.surface2,
                            disabledContentColor = palette.muted,
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag(PLAN_FLOW_FILLED_CONTINUE_TAG),
                    ) {
                        EmberbyteCrossfade(targetState = state.isReviewStep) { isReview ->
                            Text(
                                text = stringResource(
                                    if (isReview) R.string.plan_flow_create else R.string.plan_flow_continue,
                                ),
                            )
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
                EmberbyteCrossfade(
                    targetState = state.step to state.totalSteps.coerceAtLeast(1),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                ) { (step, total) ->
                    Text(
                        text = stringResource(R.string.plan_flow_step_counter, step, total),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.muted,
                    )
                }

                val targetProgress = (state.step.toFloat() / state.totalSteps.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                val animatedProgressState = remember { mutableFloatStateOf(if (reduceMotion) targetProgress else 0f) }

                WavyBar(
                    fraction = targetProgress,
                    color = palette.primary,
                    onAnimatedFractionChanged = { animatedProgressState.floatValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag(PLAN_FLOW_PROGRESS_TAG)
                        .semantics {
                            set(PlanFlowAnimatedProgressKey, animatedProgressState.floatValue)
                        },
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            if (!reduceMotion) {
                                val currentProgress = shownBackProgress
                                val scale = BackTransitions.shrinkFor(currentProgress)
                                val radius = BackTransitions.cornerRadiusFor(currentProgress)
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = BackTransitions.transformOriginFor(layoutDirection)
                                shape = stepBackShape
                                clip = radius > 0.dp
                            }
                        },
                ) {
                    if (reduceMotion) {
                        PlanFlowStepBody(step = state.step, state = state, onEvent = onEvent)
                    } else {
                        AnimatedContent(
                            targetState = state.step,
                            transitionSpec = {
                                val direction = stepDirection(initialState, targetState, rtl)
                                (
                                    slideInHorizontally(emberbyteMotion.spatial()) {
                                        (it * STEP_SLIDE_FRACTION * direction).roundToInt()
                                    } + fadeIn(emberbyteMotion.effects())
                                    ) togetherWith (
                                    slideOutHorizontally(emberbyteMotion.spatial()) {
                                        (-it * STEP_SLIDE_FRACTION * direction).roundToInt()
                                    } + fadeOut(emberbyteMotion.fastEffects())
                                    ) using SizeTransform(clip = false)
                            },
                            label = "PlanFlowStepTransition",
                        ) { step ->
                            PlanFlowStepBody(step = step, state = state, onEvent = onEvent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanFlowStepBody(
    step: Int,
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
) {
    when {
        step == 1 -> Step1NameAndSim(state = state, onEvent = onEvent)
        step == 2 -> Step2Cap(state = state, onEvent = onEvent)
        step == 3 -> Step3Cycle(state = state, onEvent = onEvent)
        state.isMidCycleApplicable && step == 4 -> Step4ThisCycle(state = state, onEvent = onEvent)
        state.isReviewStep -> StepReview(state = state, onEvent = onEvent)
    }
}

private class PlanStepBackShape(private val progressProvider: () -> Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radiusPx = with(density) { BackTransitions.cornerRadiusFor(progressProvider()).toPx() }
        if (radiusPx <= 0f) {
            return Outline.Rectangle(Rect(Offset.Zero, size))
        }
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
            ),
        )
    }

    override fun equals(other: Any?): Boolean = other is PlanStepBackShape
    override fun hashCode(): Int = PlanStepBackShape::class.hashCode()
}
