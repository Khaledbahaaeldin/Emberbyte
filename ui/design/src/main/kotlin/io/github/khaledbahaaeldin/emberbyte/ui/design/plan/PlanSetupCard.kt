package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.GhostRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.BentoType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.liveTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.entrance

/** The card appears this long after the screen's first composition, before the bento tiles. */
private const val SETUP_ENTRANCE_START_MS = 80

/**
 * The one call to action Home shows while there is no plan: a decorative ghost ring, a title, one line of body and a
 * single button. The card itself is not clickable; the button is the only action.
 */
@Composable
fun PlanSetupCard(
    onSetUp: () -> Unit,
    modifier: Modifier = Modifier,
    playEntrance: Boolean = true,
) {
    val palette = LocalEmberbytePalette.current
    val colours = palette.liveTile()
    val shape = remember { mixedCorners(large = 28.dp, small = 8.dp, smallCorner = Corner.BottomEnd) }
    val buttonShape = CircleShape
    val buttonInteraction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .entrance(startMs = SETUP_ENTRANCE_START_MS, play = playEntrance, fromScale = 0.97f, fromDy = 14.dp)
            .pressMorph(shape, pressedScale = 0.97f, pressedCorner = 18.dp)
            .clip(shape)
            .background(colours.container)
            .padding(16.dp)
            .testTag("plan_setup_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Decorative: no progress, no description.
            WavyRing(
                fraction = 0f,
                state = BudgetStateUi.Ok,
                size = 76.dp,
                label = "",
                modifier = Modifier.testTag("plan_setup_ring"),
                ghost = GhostRing(),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Set up your data plan",
                    style = EmberbyteType.grotesk(BentoType.SetupTitle, 700).copy(
                        letterSpacing = (-0.01).em,
                        lineHeight = 1.1.em,
                        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
                    ),
                    color = colours.onContainer,
                    modifier = Modifier
                        .testTag("plan_setup_title")
                        .semantics { heading() },
                )
                Text(
                    text = "See what is left and when it runs out.",
                    style = EmberbyteType.grotesk(BentoType.SetupBody, 500).copy(lineHeight = 1.35.em),
                    color = colours.onContainerSecondary,
                )
            }
        }

        Button(
            onClick = onSetUp,
            interactionSource = buttonInteraction,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .pressMorph(buttonShape, pressedScale = 0.96f, pressedCorner = 18.dp, interactionSource = buttonInteraction)
                .semantics { contentDescription = "Set up your data plan" }
                .testTag("plan_setup_button"),
        ) {
            Text(
                text = "Set up plan",
                style = EmberbyteType.grotesk(BentoType.SetupButton, 700),
                maxLines = 1,
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
