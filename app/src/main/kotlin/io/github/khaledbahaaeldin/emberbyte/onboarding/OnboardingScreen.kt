package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.nav.PermissionActions
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import kotlin.math.abs
import kotlinx.coroutines.launch

internal const val ONBOARDING_PAGE_COUNT = 4

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    actions: PermissionActions,
    onSetUpPlan: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingContent(
        state = state,
        onOpenUsageAccess = actions.openUsageAccess,
        onRequestNotifications = actions.requestNotifications,
        onRequestPhone = actions.requestPhoneState,
        onRequestBattery = actions.requestBatteryUnrestricted,
        onSetUpPlan = onSetUpPlan,
        onFinish = onFinish,
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingContent(
    state: OnboardingUiState,
    onOpenUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    onSetUpPlan: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    onRequestPhone: () -> Unit = {},
    onRequestBattery: () -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()
    // With reduced motion the pager is never composed, so its state cannot scroll: a plain index drives the page then.
    var reducedPage by remember { mutableIntStateOf(0) }
    val currentPage = if (reduceMotion) reducedPage else pagerState.currentPage

    var hasFinished by remember { mutableStateOf(false) }
    var pagerWidthPx by remember { mutableStateOf(1f) }
    val anchorRects = remember {
        mutableStateListOf(
            LayerRect(24f, 40f, 72f, 72f),
            LayerRect(24f, 40f, 360f, 220f),
            LayerRect(96f, 40f, 216f, 216f),
            LayerRect(24f, 140f, 360f, 160f),
        )
    }

    fun completeOnce() {
        if (!hasFinished) {
            hasFinished = true
            onFinish()
        }
    }

    fun moveToPage(targetPage: Int) {
        scope.launch {
            val clamped = targetPage.coerceIn(0, ONBOARDING_PAGE_COUNT - 1)
            if (reduceMotion) {
                reducedPage = clamped
            } else {
                pagerState.animateScrollToPage(
                    page = clamped,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 224f),
                )
            }
        }
    }

    BackHandler(enabled = currentPage > 0) {
        moveToPage(currentPage - 1)
    }

    val position = if (reduceMotion) reducedPage.toFloat() else (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 3f)
    val sharedActive = !reduceMotion && position < 2.999f && abs(position - position.toInt()) > 0.001f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingProgressPill(
                position = position,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { pagerWidthPx = it.width.toFloat().coerceAtLeast(1f) },
            ) {
                if (reduceMotion) {
                    Crossfade(
                        targetState = reducedPage,
                        animationSpec = tween(durationMillis = 200),
                        label = "onboarding_reduced_crossfade",
                    ) { page ->
                        OnboardingStepPage(
                            page = page,
                            state = state,
                            reveal = { 1f },
                            layerHiddenByShared = false,
                            onAnchorRect = { rect -> anchorRects[page] = rect },
                            onSetUpPlan = onSetUpPlan,
                            onOpenUsageAccess = onOpenUsageAccess,
                            onRequestNotifications = onRequestNotifications,
                            onRequestPhone = onRequestPhone,
                            onRequestBattery = onRequestBattery,
                        )
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("onboarding_pager"),
                    ) { page ->
                        val d = (page - position)
                        val q = (1f - abs(d)).coerceIn(0f, 1f)
                        val isOutgoing = abs(d) > 0.001f &&
                            ((d < 0f && position >= page) || (d > 0f && position <= page))
                        val alpha = pageAlpha(d)
                        val offsetPx = parallaxOffset(d, pagerWidthPx, isOutgoing) - d * pagerWidthPx
                        val reveal: (Int) -> Float = { idx -> revealFraction(q, idx) }

                        OnboardingStepPage(
                            page = page,
                            state = state,
                            reveal = reveal,
                            layerHiddenByShared = sharedActive && page < 3,
                            onAnchorRect = { rect -> anchorRects[page] = rect },
                            onSetUpPlan = onSetUpPlan,
                            onOpenUsageAccess = onOpenUsageAccess,
                            onRequestNotifications = onRequestNotifications,
                            onRequestPhone = onRequestPhone,
                            onRequestBattery = onRequestBattery,
                            modifier = Modifier.graphicsLayer {
                                this.alpha = alpha
                                translationX = offsetPx
                            },
                        )
                    }
                }
            }

            OnboardingActionBar(
                position = position,
                usageAccess = state.usageAccess,
                notifications = state.notifications,
                phoneState = state.phoneState,
                batteryUnrestricted = state.batteryUnrestricted,
                onPrimaryClick = {
                    val page = currentPage
                    if (page < ONBOARDING_PAGE_COUNT - 1) {
                        moveToPage(page + 1)
                    } else {
                        when (nextPermissionAction(state.usageAccess, state.notifications, state.phoneState, state.batteryUnrestricted)) {
                            PermissionAction.Usage -> onOpenUsageAccess()
                            PermissionAction.Notifications -> onRequestNotifications()
                            PermissionAction.Phone -> onRequestPhone()
                            PermissionAction.Battery -> onRequestBattery()
                            PermissionAction.Done -> completeOnce()
                        }
                    }
                },
                onBackClick = { moveToPage(currentPage - 1) },
                onSkipClick = { completeOnce() },
            )
        }

        if (sharedActive) {
            SharedOnboardingLayer(
                position = position,
                rects = anchorRects,
                usageAccess = state.usageAccess,
            )
        }
    }
}

@Composable
private fun OnboardingStepPage(
    page: Int,
    state: OnboardingUiState,
    reveal: (Int) -> Float,
    layerHiddenByShared: Boolean,
    onAnchorRect: (LayerRect) -> Unit,
    onSetUpPlan: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestPhone: () -> Unit,
    onRequestBattery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (page) {
        0 -> WelcomeStep(
            reveal = reveal,
            layerHiddenByShared = layerHiddenByShared,
            onAnchorRect = onAnchorRect,
            modifier = modifier,
        )
        1 -> SeeYourDataStep(
            reveal = reveal,
            layerHiddenByShared = layerHiddenByShared,
            onAnchorRect = onAnchorRect,
            modifier = modifier,
        )
        2 -> StayUnderCapStep(
            hasPlan = state.hasPlan,
            reveal = reveal,
            layerHiddenByShared = layerHiddenByShared,
            onAnchorRect = onAnchorRect,
            onSetUpPlan = onSetUpPlan,
            modifier = modifier,
        )
        else -> AllowAccessStep(
            usageAccess = state.usageAccess,
            notifications = state.notifications,
            reveal = reveal,
            firstCardHiddenByShared = layerHiddenByShared,
            onAnchorRect = onAnchorRect,
            onOpenUsageAccess = onOpenUsageAccess,
            onRequestNotifications = onRequestNotifications,
            phoneState = state.phoneState,
            batteryUnrestricted = state.batteryUnrestricted,
            onRequestPhone = onRequestPhone,
            onRequestBattery = onRequestBattery,
            modifier = modifier,
        )
    }
}
