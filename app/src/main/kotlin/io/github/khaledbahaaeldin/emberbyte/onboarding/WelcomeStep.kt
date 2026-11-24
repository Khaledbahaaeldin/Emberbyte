@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType

@Composable
internal fun WelcomeStep(
    reveal: (Int) -> Float,
    layerHiddenByShared: Boolean,
    onAnchorRect: (LayerRect) -> Unit,
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
        Box(
            modifier = Modifier
                .size(72.dp)
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
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(palette.primary)
                .testTag("welcome_mark"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "E",
                style = EmberbyteType.title(OnboardingType.MarkGlyph),
                color = palette.background,
            )
        }

        Spacer(Modifier.height(24.dp))

        val r0 = reveal(0)
        Text(
            text = "Your data stays on your phone.",
            style = EmberbyteType.title(OnboardingType.WelcomeTitle).copy(
                lineHeight = OnboardingType.WelcomeTitle * 1.04f,
                letterSpacing = (-0.01).em,
            ),
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
            text = "Your mobile and Wi-Fi numbers are measured right here, and they never leave.",
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

        val facts = listOf(
            Triple(Icons.Rounded.PersonOff, "No account", "There is nothing to sign up for."),
            Triple(Icons.Rounded.CloudOff, "No cloud", "Nothing is uploaded, ever."),
            Triple(Icons.Rounded.Code, "Open source", "Anyone can read the code."),
        )
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            facts.forEachIndexed { idx, (icon, title, body) ->
                val r = reveal(idx + 2)
                WelcomeFactRow(
                    icon = icon,
                    title = title,
                    body = body,
                    modifier = Modifier.graphicsLayer {
                        alpha = r
                        translationY = (1f - r) * 14.dp.toPx()
                    },
                )
            }
        }
    }
}

@Composable
private fun WelcomeFactRow(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(palette.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = EmberbyteType.heading(OnboardingType.LeadBody),
                color = palette.text,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.Action),
                color = palette.muted,
            )
        }
    }
}
