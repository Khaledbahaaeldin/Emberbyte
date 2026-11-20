package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/** Horizontal padding inside a segment, on each side. */
private val SegmentHorizontalPadding = 16.dp

/** Extra room so a label never sits exactly on the edge of its segment. */
private val SegmentSlack = 4.dp

/** Space reserved for the check mark (16 dp icon plus 4 dp gap) in every segment, so selection never resizes anything. */
private val CheckSlot = 20.dp

/** Above this system font scale the check mark is dropped to save width; selection is still shown by shape and colour. */
private const val CHECK_ICON_MAX_FONT_SCALE = 1.3f

/** Vertical gap between rows when the group has to wrap. */
private val RowSpacing = 4.dp

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
 * Splits segments with the given [needed] widths (in pixels) into rows that each fit [available] pixels.
 *
 * One row when everything fits (or the width is unbounded). Otherwise the fewest rows, in the original order, whose
 * widest row is as narrow as possible, so a group wraps 2 + 2 instead of 3 + 1. When even one segment per row does
 * not fit, each segment gets its own row. Returns the indices of each row.
 */
internal fun packRows(needed: List<Int>, gap: Int, available: Int): List<List<Int>> {
    val n = needed.size
    if (n == 0) return listOf(emptyList())
    fun width(from: Int, to: Int): Int = (from until to).sumOf { needed[it] } + gap * (to - from - 1)
    if (available == Int.MAX_VALUE || width(0, n) <= available) return listOf((0 until n).toList())
    val inf = Int.MAX_VALUE
    for (rows in 2..n) {
        val best = Array(rows + 1) { IntArray(n + 1) { inf } }
        val cut = Array(rows + 1) { IntArray(n + 1) { -1 } }
        best[0][0] = 0
        for (k in 1..rows) {
            for (i in k..n) {
                for (j in (k - 1) until i) {
                    if (best[k - 1][j] == inf) continue
                    val widest = maxOf(best[k - 1][j], width(j, i))
                    if (widest < best[k][i]) {
                        best[k][i] = widest
                        cut[k][i] = j
                    }
                }
            }
        }
        if (best[rows][n] <= available) {
            val result = ArrayDeque<List<Int>>()
            var end = n
            for (k in rows downTo 1) {
                val start = cut[k][end]
                result.addFirst((start until end).toList())
                end = start
            }
            return result.toList()
        }
    }
    return (0 until n).map { listOf(it) }
}

/**
 * Connected button group control.
 *
 * Provides a row of connected pill segments where:
 * - Outer ends are rounded pills, inner corners are 8 dp.
 * - The selected segment forms a full pill with a check mark icon in [primary]/[onPrimary] (the check is dropped above
 *   a system font scale of 1.3 to save width).
 * - Unselected segments use [surface2]/[muted].
 * - The container uses [selectableGroup], each segment has [Role.RadioButton] and a minimum height of 48 dp.
 * - Segment labels are wrapped in [ForceLtr], stay on one line and are never broken mid-word.
 * - Segments are sized to their labels. When the labels do not fit on one row the group wraps into balanced rows with
 *   connected shapes recalculated per row.
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
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val labelStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        val showCheck = density.fontScale <= CHECK_ICON_MAX_FONT_SCALE
        val colors = ToggleButtonDefaults.colors(
            containerColor = palette.surface2,
            contentColor = palette.muted,
            checkedContainerColor = palette.primary,
            checkedContentColor = palette.onPrimary,
            disabledContainerColor = palette.surface2.copy(alpha = 0.38f),
            disabledContentColor = palette.muted.copy(alpha = 0.38f),
        )

        BoxWithConstraints(modifier = modifier) {
            val bounded = constraints.hasBoundedWidth
            val maxWidthPx = if (bounded) constraints.maxWidth else Int.MAX_VALUE
            val needed = remember(options, labelStyle, density.density, density.fontScale, showCheck) {
                val chrome = with(density) {
                    (SegmentHorizontalPadding * 2 + SegmentSlack + if (showCheck) CheckSlot else 0.dp).roundToPx()
                }
                options.map { label ->
                    textMeasurer.measure(label, labelStyle, softWrap = false, maxLines = 1).size.width + chrome
                }
            }
            val gapPx = with(density) { ButtonGroupDefaults.ConnectedSpaceBetween.roundToPx() }
            val rows = remember(needed, gapPx, maxWidthPx) { packRows(needed, gapPx, maxWidthPx) }

            Column(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(RowSpacing),
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        row.forEachIndexed { position, index ->
                            val isSelected = index == selectedIndex
                            val sizing = if (bounded) Modifier.weight(needed[index].toFloat()) else Modifier
                            ToggleButton(
                                checked = isSelected,
                                onCheckedChange = { onSelect(index) },
                                enabled = enabled,
                                shapes = connectedShapes(index = position, count = row.size),
                                colors = colors,
                                contentPadding = PaddingValues(horizontal = SegmentHorizontalPadding),
                                modifier = sizing
                                    .defaultMinSize(minHeight = 48.dp)
                                    .semantics {
                                        role = Role.RadioButton
                                        selected = isSelected
                                    },
                            ) {
                                if (showCheck) {
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
                                }
                                ForceLtr {
                                    Text(
                                        text = options[index],
                                        style = labelStyle,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
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
