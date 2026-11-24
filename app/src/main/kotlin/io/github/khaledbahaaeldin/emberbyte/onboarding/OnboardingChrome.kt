package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType
import kotlin.math.abs
import kotlin.math.roundToInt

/** Page titles: Space Grotesk 600 (never heavier), tracking no tighter than -0.02 em. */
internal fun onboardingTitleStyle(size: TextUnit): TextStyle =
    EmberbyteType.grotesk(size, 600).copy(
        fontFeatureSettings = null,
        lineHeight = size * 1.1f,
        letterSpacing = (-0.02).em,
    )

@Composable
internal fun OnboardingProgressPill(
    position: Float,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val effectivePos = if (reduceMotion) position.roundToInt().toFloat() else position
    val stepNum = (position.roundToInt() + 1).coerceIn(1, ONBOARDING_PAGE_COUNT)
    val description = stringResource(R.string.onb_step_desc, stepNum, ONBOARDING_PAGE_COUNT)

    Row(
        modifier = modifier
            .semantics { contentDescription = description }
            .testTag("onboarding_progress_pill"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(ONBOARDING_PAGE_COUNT) { index ->
            val width = pillWidthDp(index, effectivePos).dp
            val activeFraction = (1f - abs(effectivePos - index)).coerceIn(0f, 1f)
            val color = lerp(palette.surface2, palette.primary, activeFraction)
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
internal fun OnboardingTopBar(
    position: Float,
    showSkip: Boolean,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 8.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OnboardingProgressPill(position = position)
        if (showSkip) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("onboarding_skip_button"),
            ) {
                Text(
                    text = stringResource(R.string.onb_skip),
                    style = EmberbyteType.heading(OnboardingType.Quiet),
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
internal fun OnboardingActionBar(
    cta: OnboardingCta,
    quiet: OnboardingQuiet?,
    onPrimaryClick: () -> Unit,
    onQuietClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            onClick = onPrimaryClick,
            shape = mixedCorners(large = 32.dp, small = 12.dp, smallCorner = Corner.BottomStart),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = palette.background,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("onboarding_primary_button"),
        ) {
            Text(
                text = stringResource(ctaLabelRes(cta)),
                style = EmberbyteType.heading(OnboardingType.Button),
            )
        }

        // A fixed slot, so the big button never jumps when a page has no quiet action.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (quiet != null) {
                TextButton(
                    onClick = onQuietClick,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag("onboarding_quiet_button"),
                ) {
                    Text(
                        text = stringResource(quietLabelRes(quiet)),
                        style = EmberbyteType.heading(OnboardingType.Quiet),
                        color = palette.muted,
                    )
                }
            }
        }
    }
}
