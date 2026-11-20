package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteShapeRadii
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette

@Composable
fun PlusButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .testTag("plus_button")
            .size(56.dp)
            .pressMorph(
                shape = CircleShape,
                pressedScale = 1f,
                pressedCorner = EmberbyteShapeRadii.Small,
                interactionSource = interactionSource,
            )
            .background(palette.primary)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
                onClickLabel = contentDescription,
            )
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = palette.onPrimary,
            modifier = Modifier.size(28.dp),
        )
    }
}
