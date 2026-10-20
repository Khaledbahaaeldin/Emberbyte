package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.nav.PermissionActions
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import kotlinx.coroutines.launch

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
        onSetUpPlan = onSetUpPlan,
        onFinish = onFinish,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OnboardingContent(
    state: OnboardingUiState,
    onOpenUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    onSetUpPlan: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()

    var hasFinished by remember { mutableStateOf(false) }
    var previousUsageAccess by remember { mutableStateOf(state.usageAccess) }

    fun completeOnce() {
        if (!hasFinished) {
            hasFinished = true
            onFinish()
        }
    }

    LaunchedEffect(state.usageAccess) {
        if (!previousUsageAccess && state.usageAccess) {
            completeOnce()
        }
        previousUsageAccess = state.usageAccess
    }

    fun moveToPage(targetPage: Int) {
        scope.launch {
            if (reduceMotion) {
                pagerState.scrollToPage(targetPage)
            } else {
                pagerState.animateScrollToPage(targetPage)
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage > 0) {
        moveToPage((pagerState.currentPage - 1).coerceAtLeast(0))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("onboarding_pager"),
        ) { page ->
            when (page) {
                0 -> WelcomeOnboardingPage(modifier = Modifier.fillMaxSize())
                1 -> SeeYourDataOnboardingPage(modifier = Modifier.fillMaxSize())
                2 -> StayUnderYourCapOnboardingPage(
                    hasPlan = state.hasPlan,
                    onSetUpPlan = onSetUpPlan,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> AllowUsageAccessOnboardingPage(
                    state = state,
                    onOpenUsageAccess = onOpenUsageAccess,
                    onRequestNotifications = onRequestNotifications,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        OnboardingPageDots(
            pagerState = pagerState,
            reduceMotion = reduceMotion,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        OnboardingBottomBar(
            currentPage = pagerState.currentPage,
            usageAccessGranted = state.usageAccess,
            onSkipToPermission = { moveToPage(ONBOARDING_PAGE_COUNT - 1) },
            onNext = { moveToPage((pagerState.currentPage + 1).coerceAtMost(ONBOARDING_PAGE_COUNT - 1)) },
            onAllowAccess = onOpenUsageAccess,
            onFinish = { completeOnce() },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingPageDots(
    pagerState: PagerState,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val currentPage = pagerState.currentPage
    val description = "Page ${currentPage + 1} of $ONBOARDING_PAGE_COUNT"

    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clearAndSetSemantics {
                contentDescription = description
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in 0 until ONBOARDING_PAGE_COUNT) {
            val isSelected = index == currentPage
            val targetWidth: Dp = if (isSelected) 28.dp else 8.dp
            val dotWidth by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = if (reduceMotion) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "onboarding_dot_width_$index",
            )
            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) palette.primary else palette.track),
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    usageAccessGranted: Boolean,
    onSkipToPermission: () -> Unit,
    onNext: () -> Unit,
    onAllowAccess: () -> Unit,
    onFinish: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val isLastPage = currentPage == ONBOARDING_PAGE_COUNT - 1
    val fontScale = LocalDensity.current.fontScale
    val buttonFontSize = if (fontScale > 1.3f) (16f * (1.3f / fontScale)).sp else 16.sp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isLastPage) {
            TextButton(
                onClick = onSkipToPermission,
                colors = ButtonDefaults.textButtonColors(contentColor = palette.primary),
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(
                    text = "Skip",
                    style = EmberbyteType.heading(buttonFontSize),
                )
            }
            Button(
                onClick = onNext,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            ) {
                Text(
                    text = "Next",
                    style = EmberbyteType.heading(buttonFontSize),
                    maxLines = 1,
                )
            }
        } else {
            TextButton(
                onClick = onFinish,
                colors = ButtonDefaults.textButtonColors(contentColor = palette.primary),
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(
                    text = "Skip for now",
                    style = EmberbyteType.heading(buttonFontSize),
                )
            }
            Button(
                onClick = if (usageAccessGranted) onFinish else onAllowAccess,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            ) {
                Text(
                    text = if (usageAccessGranted) "Continue" else "Allow access",
                    style = EmberbyteType.heading(buttonFontSize),
                    maxLines = 1,
                )
            }
        }
    }
}
