package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
internal fun OnboardingProgressPill(
    position: Float,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val effectivePos = if (reduceMotion) position.roundToInt().toFloat() else position
    val stepNum = (position.roundToInt() + 1).coerceIn(1, 4)

    Row(
        modifier = modifier
            .semantics { contentDescription = "Step $stepNum of 4" }
            .testTag("onboarding_progress_pill"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(4) { index ->
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
internal fun OnboardingActionBar(
    position: Float,
    usageAccess: Boolean,
    notifications: Boolean,
    onPrimaryClick: () -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val p = position.coerceIn(0f, 3f)
    val lowPage = floor(p).toInt().coerceIn(0, 3)
    val highPage = ceil(p).toInt().coerceIn(0, 3)
    val frac = (p - lowPage).coerceIn(0f, 1f)

    val lowLabel = ctaLabel(lowPage, usageAccess, notifications)
    val highLabel = ctaLabel(highPage, usageAccess, notifications)
    val showHigh = !reduceMotion && lowPage != highPage && frac >= 0.5f
    val primaryText = if (showHigh) highLabel else lowLabel
    val primaryAlpha = when {
        reduceMotion || lowPage == highPage -> 1f
        frac < 0.5f -> (1f - frac * 2f).coerceIn(0f, 1f)
        else -> ((frac - 0.5f) * 2f).coerceIn(0f, 1f)
    }

    val backAlphaVal = backAlpha(p)
    val backReachable = p >= 0.5f
    val skipText = skipLabel(p)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                .height(64.dp)
                .testTag("onboarding_primary_button"),
        ) {
            Text(
                text = primaryText,
                style = EmberbyteType.heading(OnboardingType.Button),
                modifier = Modifier.alpha(primaryAlpha),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (backReachable) {
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .alpha(backAlphaVal)
                        .testTag("onboarding_back_button"),
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.muted,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clearAndSetSemantics {},
                )
            }

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = onSkipClick,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("onboarding_skip_button"),
            ) {
                Text(
                    text = skipText,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.muted,
                )
            }
        }
    }
}
