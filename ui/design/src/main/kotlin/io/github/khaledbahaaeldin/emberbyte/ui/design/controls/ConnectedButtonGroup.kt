package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Connected button group control.
 *
 * Provides a row of connected pill segments where:
 * - Outer ends are rounded pills, inner corners are 8 dp.
 * - The selected segment forms a full pill with a check mark icon in [primary]/[onPrimary].
 * - Unselected segments use [surface2]/[muted].
 * - The container uses [selectableGroup], each segment has [Role.RadioButton] and a minimum height of 48 dp.
 * - Segment labels are wrapped in [ForceLtr].
 */
@Composable
fun ConnectedButtonGroup(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = LocalEmberbytePalette.current

    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val shape: Shape = when {
                isSelected || options.size <= 1 -> RoundedCornerShape(percent = 50)
                index == 0 -> RoundedCornerShape(
                    topStart = CornerSize(50),
                    bottomStart = CornerSize(50),
                    topEnd = CornerSize(8.dp),
                    bottomEnd = CornerSize(8.dp),
                )
                index == options.lastIndex -> RoundedCornerShape(
                    topStart = CornerSize(8.dp),
                    bottomStart = CornerSize(8.dp),
                    topEnd = CornerSize(50),
                    bottomEnd = CornerSize(50),
                )
                else -> RoundedCornerShape(8.dp)
            }

            val containerColor = if (isSelected) palette.primary else palette.surface2
            val contentColor = if (isSelected) palette.onPrimary else palette.muted

            val effectiveContainerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.38f)
            val effectiveContentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)

            Surface(
                shape = shape,
                color = effectiveContainerColor,
                contentColor = effectiveContentColor,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) },
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .defaultMinSize(minHeight = 48.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("connected_button_group_check_icon"),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    ForceLtr {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
