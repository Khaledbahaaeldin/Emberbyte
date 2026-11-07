package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val SnapMotionScheme = object : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = snap()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun connectedShapes(index: Int, count: Int): ToggleButtonShapes = when {
    count <= 1 -> {
        val pill = ButtonGroupDefaults.connectedButtonCheckedShape
        ToggleButtonShapes(shape = pill, pressedShape = pill, checkedShape = pill)
    }
    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
    index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
}

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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonGroup(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current

    val content: @Composable () -> Unit = {
        val motion = MaterialTheme.motionScheme
        Row(
            modifier = modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val colors = ToggleButtonDefaults.colors(
                containerColor = palette.surface2,
                contentColor = palette.muted,
                checkedContainerColor = palette.primary,
                checkedContentColor = palette.onPrimary,
                disabledContainerColor = palette.surface2.copy(alpha = 0.38f),
                disabledContentColor = palette.muted.copy(alpha = 0.38f),
            )

            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val shapes = connectedShapes(index = index, count = options.size)

                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = { onSelect(index) },
                    enabled = enabled,
                    shapes = shapes,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .semantics {
                            role = Role.RadioButton
                            selected = isSelected
                        },
                ) {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(animationSpec = motion.fastEffectsSpec()) +
                            expandHorizontally(animationSpec = motion.fastSpatialSpec()),
                        exit = fadeOut(animationSpec = motion.fastEffectsSpec()) +
                            shrinkHorizontally(animationSpec = motion.fastSpatialSpec()),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("connected_button_group_check_icon"),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
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

    if (reduceMotion) {
        MaterialExpressiveTheme(
            motionScheme = SnapMotionScheme,
            content = content,
        )
    } else {
        content()
    }
}
