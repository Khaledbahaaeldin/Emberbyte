@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import io.github.khaledbahaaeldin.emberbyte.settings.HubSwitchId
import io.github.khaledbahaaeldin.emberbyte.settings.SwitchUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.MorphShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType

@Composable
internal fun ShapeSwitchesRow(
    switches: List<SwitchUi>,
    onToggle: (HubSwitchId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        switches.forEach { item ->
            ShapeSwitchTile(
                item = item,
                onClick = { onToggle(item.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ShapeSwitchTile(
    item: SwitchUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val target = if (item.checked && item.enabled) 1f else 0f
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = if (reduceMotion) snap() else motion.spatial(),
        label = "shape_switch_${item.id.name}",
    )

    val polygon = when (item.id) {
        HubSwitchId.Live -> MaterialShapes.Cookie9Sided
        HubSwitchId.Speed -> MaterialShapes.SoftBurst
        HubSwitchId.Spikes -> MaterialShapes.Cookie6Sided
        HubSwitchId.Amoled -> MaterialShapes.Pentagon
    }
    val morph = remember(polygon) { Morph(MaterialShapes.Circle, polygon) }
    val shape: Shape = if (progress <= 0.01f) CircleShape else MorphShape(morph, progress)
    val rotationDeg = if (reduceMotion) 0f else 24f * progress

    val containerColor = if (item.checked && item.enabled) palette.primary else palette.surface2
    val iconTint = if (item.checked && item.enabled) palette.background else palette.text
    val icon: ImageVector = when (item.id) {
        HubSwitchId.Live -> Icons.Rounded.NotificationsActive
        HubSwitchId.Speed -> Icons.Rounded.Speed
        HubSwitchId.Spikes -> Icons.Rounded.TrendingUp
        HubSwitchId.Amoled -> Icons.Rounded.Contrast
    }

    Column(
        modifier = modifier
            .testTag("hub_switch_${item.id.name.lowercase()}")
            .semantics(mergeDescendants = true) {
                stateDescription = item.caption
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onClick = onClick,
            )
            .alpha(if (item.enabled) 1f else 0.56f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .graphicsLayer { rotationZ = rotationDeg }
                .clip(shape)
                .background(containerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = -rotationDeg },
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.label,
            style = EmberbyteType.heading(SettingsType.PulseBody),
            color = palette.text,
            textAlign = TextAlign.Center,
        )
        Text(
            text = item.caption,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro),
            color = if (item.enabled) palette.muted else palette.warn,
            textAlign = TextAlign.Center,
        )
    }
}
