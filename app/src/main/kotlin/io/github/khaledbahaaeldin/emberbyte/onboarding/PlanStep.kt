package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.GhostRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType

@Composable
internal fun PlanStep(
    hasPlan: Boolean,
    reveal: (Int) -> Float,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        val r0 = reveal(0)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                }
                .clearAndSetSemantics {}
                .testTag("plan_ring"),
            contentAlignment = Alignment.Center,
        ) {
            // The ring is drawn by the shared ghost-ring code: it draws once, and stands still under reduced motion.
            WavyRing(
                fraction = 0f,
                state = BudgetStateUi.Ok,
                size = 220.dp,
                label = "",
                ghost = GhostRing(fraction = if (hasPlan) 1f else 0f),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasPlan) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(56.dp),
                    )
                    Text(
                        text = stringResource(R.string.onb_plan_ring_added),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.Caption),
                        color = palette.muted,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = "0",
                        style = onboardingTitleStyle(OnboardingType.RingValue),
                        color = palette.text,
                    )
                    Text(
                        text = stringResource(R.string.onb_plan_ring_caption),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.Caption),
                        color = palette.muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        val r1 = reveal(1)
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.graphicsLayer {
                alpha = r1
                translationY = (1f - r1) * 14.dp.toPx()
            },
        ) {
            Text(
                text = stringResource(if (hasPlan) R.string.onb_plan_added_title else R.string.onb_plan_title),
                style = onboardingTitleStyle(OnboardingType.StepTitle),
                color = palette.text,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(if (hasPlan) R.string.onb_plan_added_lead else R.string.onb_plan_lead),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = OnboardingType.LeadBody),
                color = palette.muted,
            )
        }
    }
}
