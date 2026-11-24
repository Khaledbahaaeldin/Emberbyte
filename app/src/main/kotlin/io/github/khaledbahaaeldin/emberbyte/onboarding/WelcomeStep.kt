@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.rememberAmbientSeconds
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType

@Composable
internal fun WelcomeStep(
    reveal: (Int) -> Float,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.height(40.dp))

        // The one expressive moment: the cookie turns slowly. The loop is read in the layer and stops under reduced motion.
        val seconds = rememberAmbientSeconds(enabled = true)
        Box(
            modifier = Modifier
                .size(112.dp)
                .testTag("welcome_mark"),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .graphicsLayer { rotationZ = seconds.value * 8f }
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(palette.primary),
            )
            Text(
                text = "E",
                style = onboardingTitleStyle(OnboardingType.MarkGlyph),
                color = palette.background,
            )
        }

        Spacer(Modifier.height(36.dp))

        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.onb_welcome_title),
            style = onboardingTitleStyle(OnboardingType.WelcomeTitle),
            color = palette.text,
            modifier = Modifier
                .semantics { heading() }
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                },
        )

        Spacer(Modifier.height(14.dp))

        val r1 = reveal(1)
        Text(
            text = stringResource(R.string.onb_welcome_lead),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = OnboardingType.LeadBody),
            color = palette.muted,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .graphicsLayer {
                    alpha = r1
                    translationY = (1f - r1) * 14.dp.toPx()
                },
        )

        Spacer(Modifier.height(28.dp))

        val r2 = reveal(2)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.graphicsLayer {
                alpha = r2
                translationY = (1f - r2) * 14.dp.toPx()
            },
        ) {
            Fact(Icons.Rounded.PersonOff, stringResource(R.string.onb_fact_account))
            Fact(Icons.Rounded.CloudOff, stringResource(R.string.onb_fact_cloud))
            Fact(Icons.Rounded.Code, stringResource(R.string.onb_fact_open))
        }
    }
}

@Composable
private fun Fact(icon: ImageVector, text: String) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(palette.surface)
            .padding(start = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
        Text(
            text = text,
            style = EmberbyteType.heading(OnboardingType.Action),
            color = palette.text,
        )
    }
}
