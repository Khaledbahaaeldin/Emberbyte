package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.nav.GuardedBack
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import java.time.Clock
import kotlin.math.abs

@Composable
fun PlanFlowScreen(
    viewModel: PlanFlowViewModel,
    onPlanCreated: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    hasPhonePermission: Boolean = true,
    onRequestPhonePermission: () -> Unit = {},
    clock: Clock = SystemZoneClock(),
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
        hasPhonePermission = hasPhonePermission,
        onRequestPhonePermission = onRequestPhonePermission,
        clock = clock,
        modifier = modifier,
    )
}

@Composable
internal fun PlanFlowContent(
    state: PlanFlowState,
    onEvent: (PlanFlowEvent) -> Unit,
    onBack: () -> Unit,
    hasPhonePermission: Boolean = true,
    onRequestPhonePermission: () -> Unit = {},
    clock: Clock = SystemZoneClock(),
    modifier: Modifier = Modifier,
) {
    val reduceMotion = LocalReduceMotion.current
    val pagerState = rememberPagerState(
        initialPage = (state.step - 1).coerceIn(0, state.totalSteps - 1),
        pageCount = { state.totalSteps },
    )
    var pagerWidthPx by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(state.step, reduceMotion) {
        val target = (state.step - 1).coerceIn(0, state.totalSteps - 1)
        if (!reduceMotion && pagerState.currentPage != target) {
            pagerState.animateScrollToPage(
                page = target,
                animationSpec = spring(dampingRatio = 1f, stiffness = 224f),
            )
        }
    }

    val position = if (reduceMotion) {
        (state.step - 1).toFloat()
    } else {
        (pagerState.currentPage + pagerState.currentPageOffsetFraction)
            .coerceIn(0f, (state.totalSteps - 1).toFloat())
    }

    val hasDirtyInput = state.name.isNotBlank() || state.capText.isNotBlank() || state.enteredAmount.isNotBlank()
    val isLastStep = state.isReviewStep || state.step >= state.totalSteps

    GuardedBack(
        dirty = state.step == 1 && hasDirtyInput,
        onConfirmLeave = onBack,
    ) { shrink ->
        PlanFlowScaffoldChrome(
            position = position,
            totalSteps = state.totalSteps,
            step = state.step,
            isLastStep = isLastStep,
            isSaving = state.isSaving,
            hasDirtyInput = hasDirtyInput,
            onBack = {
                if (state.step > 1) onEvent(PlanFlowEvent.PreviousStep) else onBack()
            },
            onClose = onBack,
            onPrimary = {
                if (isLastStep) onEvent(PlanFlowEvent.CreatePlan) else onEvent(PlanFlowEvent.NextStep)
            },
            modifier = modifier.graphicsLayer {
                scaleX = shrink
                scaleY = shrink
            },
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { pagerWidthPx = it.width.toFloat().coerceAtLeast(1f) },
        ) {
            if (reduceMotion) {
                Crossfade(
                    targetState = state.step,
                    animationSpec = tween(durationMillis = 200),
                    label = "plan_flow_reduced_crossfade",
                ) { stepNum ->
                    PlanFlowStepBody(
                        stepNum = stepNum,
                        state = state,
                        hasPhonePermission = hasPhonePermission,
                        clock = clock,
                        reveal = { 1f },
                        onEvent = onEvent,
                        onRequestPhonePermission = onRequestPhonePermission,
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    beyondViewportPageCount = 0,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("plan_flow_pager"),
                ) { page ->
                    val d = page - position
                    val q = (1f - abs(d)).coerceIn(0f, 1f)
                    val isOutgoing = abs(d) > 0.001f &&
                        ((d < 0f && position >= page) || (d > 0f && position <= page))
                    val alpha = flowPageAlpha(d)
                    val offsetPx = flowParallaxOffset(d, pagerWidthPx, isOutgoing)
                    val reveal: (Int) -> Float = { idx -> flowRevealFraction(q, idx) }

                    PlanFlowStepBody(
                        stepNum = page + 1,
                        state = state,
                        hasPhonePermission = hasPhonePermission,
                        clock = clock,
                        reveal = reveal,
                        onEvent = onEvent,
                        onRequestPhonePermission = onRequestPhonePermission,
                        modifier = Modifier.graphicsLayer {
                            this.alpha = alpha
                            translationX = offsetPx
                        },
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun PlanFlowStepBody(
    stepNum: Int,
    state: PlanFlowState,
    hasPhonePermission: Boolean,
    clock: Clock,
    reveal: (Int) -> Float,
    onEvent: (PlanFlowEvent) -> Unit,
    onRequestPhonePermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReview = (!state.isMidCycleApplicable && stepNum == 4) || (state.isMidCycleApplicable && stepNum == 5)
    when {
        stepNum == 1 -> NameStep(
            state = state,
            hasPhonePermission = hasPhonePermission,
            reveal = reveal,
            onSetName = { onEvent(PlanFlowEvent.SetName(it)) },
            onSetSim = { onEvent(PlanFlowEvent.SetSim(it)) },
            onRequestPhonePermission = onRequestPhonePermission,
            modifier = modifier,
        )
        stepNum == 2 -> AmountStep(
            state = state,
            reveal = reveal,
            onSetCap = { onEvent(PlanFlowEvent.SetCap(it)) },
            onSetCapUnit = { onEvent(PlanFlowEvent.SetCapUnit(it)) },
            onSetRollover = { onEvent(PlanFlowEvent.SetRollover(it)) },
            modifier = modifier,
        )
        stepNum == 3 -> ResetStep(
            state = state,
            clock = clock,
            reveal = reveal,
            onSetCycleKind = { onEvent(PlanFlowEvent.SetCycleKind(it)) },
            onSetDayOfMonth = { onEvent(PlanFlowEvent.SetDayOfMonth(it)) },
            onSetEveryDaysText = { onEvent(PlanFlowEvent.SetEveryDaysText(it)) },
            onSetEveryDays = { onEvent(PlanFlowEvent.SetEveryDays(it)) },
            modifier = modifier,
        )
        stepNum == 4 && !isReview -> ThisCycleStep(
            state = state,
            clock = clock,
            reveal = reveal,
            onSelectChoice = { onEvent(PlanFlowEvent.SetFirstCycleChoice(it)) },
            onSetEnteredAmount = { onEvent(PlanFlowEvent.SetEnteredAmount(it)) },
            onSetEnteredUnit = { onEvent(PlanFlowEvent.SetEnteredUnit(it)) },
            modifier = modifier,
        )
        else -> ReviewStep(
            state = state,
            reveal = reveal,
            onToggleAlert = { onEvent(PlanFlowEvent.ToggleAlert(it)) },
            modifier = modifier,
        )
    }
}
