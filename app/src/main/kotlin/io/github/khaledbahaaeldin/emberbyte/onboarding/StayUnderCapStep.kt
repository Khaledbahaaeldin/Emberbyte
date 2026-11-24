package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType

@Composable
internal fun StayUnderCapStep(
    hasPlan: Boolean,
    reveal: (Int) -> Float,
    layerHiddenByShared: Boolean,
    onAnchorRect: (LayerRect) -> Unit,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            PlanCapDisc(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()
                        onAnchorRect(
                            LayerRect(
                                x = pos.x,
                                y = pos.y,
                                w = coords.size.width.toFloat(),
                                h = coords.size.height.toFloat(),
                            ),
                        )
                    }
                    .alpha(if (layerHiddenByShared) 0f else 1f)
                    .testTag("stay_cap_disc"),
            )
        }

        val r0 = reveal(0)
        Text(
            text = "Stay under your cap",
            style = EmberbyteType.title(OnboardingType.StepTitle).copy(letterSpacing = (-0.01).em),
            color = palette.text,
            modifier = Modifier
                .semantics { heading() }
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                },
        )

        val r1 = reveal(1)
        Text(
            text = "Add your cap and renewal day to see what is left and when it runs out.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = OnboardingType.LeadBody),
            color = palette.muted,
            modifier = Modifier.graphicsLayer {
                alpha = r1
                translationY = (1f - r1) * 14.dp.toPx()
            },
        )

        val r2 = reveal(2)
        val containerColor = if (hasPlan) lerp(palette.surface2, palette.primary, 0.22f) else palette.surface2
        FilledTonalButton(
            onClick = onSetUpPlan,
            shape = mixedCorners(large = 28.dp, small = 10.dp, smallCorner = Corner.BottomStart),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = containerColor,
                contentColor = palette.text,
            ),
            modifier = Modifier
                .height(56.dp)
                .graphicsLayer {
                    alpha = r2
                    translationY = (1f - r2) * 14.dp.toPx()
                }
                .testTag("onboarding_set_up_plan_button"),
        ) {
            Icon(
                imageVector = if (hasPlan) Icons.Rounded.Check else Icons.Rounded.Add,
                contentDescription = null,
                tint = palette.primary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (hasPlan) "Plan added" else "Set up a plan",
                style = EmberbyteType.heading(OnboardingType.Action),
                color = palette.text,
            )
        }
    }
}

@Composable
internal fun PlanCapDisc(modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    Box(
        modifier = modifier
            .size(216.dp)
            .clip(CircleShape)
            .background(palette.surface2),
        contentAlignment = Alignment.Center,
    ) {
        WavyRing(
            fraction = 0.62f,
            state = BudgetStateUi.Ok,
            size = 200.dp,
            label = "",
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "12.4",
                style = EmberbyteType.hero(OnboardingType.DiscValue).copy(
                    letterSpacing = (-0.04).em,
                    fontFeatureSettings = "tnum",
                ),
                color = palette.text,
            )
            Text(
                text = "GB left",
                style = EmberbyteType.heading(OnboardingType.LeadBody),
                color = palette.muted,
            )
            Text(
                text = "18 days to go",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.Caption),
                color = palette.muted,
            )
        }
    }
}
