@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette

enum class NavAccent { Primary, Secondary, Tertiary, Error }

data class NavBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accent: NavAccent,
)

private const val MAX_NAV_FONT_SCALE = 1.15f

@Composable
fun CompactNavBar(
    items: List<NavBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
    hapticsEnabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    val reduceMotion = LocalReduceMotion.current
    val motion = MaterialTheme.motionScheme
    val density = LocalDensity.current
    val cappedDensity = remember(density) { Density(density.density, minOf(density.fontScale, MAX_NAV_FONT_SCALE)) }

    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        val content: @Composable () -> Unit = {
            NavBarRow(
                items = items,
                selectedId = selectedId,
                onSelect = onSelect,
                hapticsEnabled = hapticsEnabled,
                trailing = trailing,
            )
        }

        if (reduceMotion) {
            if (visible) {
                Box(modifier) { content() }
            }
        } else {
            AnimatedVisibility(
                visible = visible,
                modifier = modifier,
                enter = slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { it } +
                    fadeIn(motion.defaultEffectsSpec()),
                exit = slideOutVertically(motion.defaultSpatialSpec<IntOffset>()) { it } +
                    fadeOut(motion.defaultEffectsSpec()),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun NavBarRow(
    items: List<NavBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    hapticsEnabled: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    val palette = LocalEmberbytePalette.current
    val totalBarWidth = (68 * items.size + 20).dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier
                .testTag("compact_nav_bar")
                .width(totalBarWidth)
                .semantics { contentDescription = "Navigation" }
                .selectableGroup(),
            shape = CircleShape,
            color = palette.surface2,
        ) {
            NavBarSlots(
                items = items,
                selectedId = selectedId,
                onSelect = onSelect,
                hapticsEnabled = hapticsEnabled,
            )
        }

        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
private fun NavBarSlots(
    items: List<NavBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    hapticsEnabled: Boolean,
) {
    val palette = LocalEmberbytePalette.current
    val motion = MaterialTheme.motionScheme
    val reduceMotion = LocalReduceMotion.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current

    val selectedIndex = items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    var pressedIndex by remember { mutableStateOf<Int?>(null) }
    var previewIndex by remember { mutableStateOf<Int?>(null) }
    var dragX by remember { mutableFloatStateOf(0f) }

    val slotWidthPx = with(density) { 68.dp.toPx() }
    val marginPx = with(density) { 7.dp.toPx() }
    val minPillWidthPx = with(density) { 54.dp.roundToPx() }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val starts = remember(items.size, isRtl) {
        FloatArray(items.size) { i ->
            val slotIndex = if (isRtl) (items.size - 1 - i) else i
            slotIndex * slotWidthPx
        }
    }
    val ends = remember(items.size, isRtl) {
        FloatArray(items.size) { i ->
            val slotIndex = if (isRtl) (items.size - 1 - i) else i
            (slotIndex + 1) * slotWidthPx
        }
    }

    val shownIndex = previewIndex ?: selectedIndex

    fun slotLeft(idx: Int): Float {
        val s = if (idx in starts.indices) minOf(starts[idx], ends[idx]) else idx * slotWidthPx
        return s + marginPx
    }

    fun slotRight(idx: Int): Float {
        val e = if (idx in ends.indices) maxOf(starts[idx], ends[idx]) else (idx + 1) * slotWidthPx
        return e - marginPx
    }

    val targetLeft: Float
    val targetRight: Float

    if (reduceMotion) {
        targetLeft = slotLeft(shownIndex)
        targetRight = slotRight(shownIndex)
    } else {
        if (previewIndex != null) {
            val p = pressedIndex ?: selectedIndex
            val n = previewIndex ?: selectedIndex
            targetLeft = minOf(slotLeft(p), slotLeft(n))
            targetRight = maxOf(slotRight(p), slotRight(n))
        } else {
            targetLeft = slotLeft(selectedIndex)
            targetRight = slotRight(selectedIndex)
        }
    }

    val followSpec = CompactNavMotion.pillFollowSpec<Float>(previewIndex, reduceMotion, motion)
    val animLeft by animateFloatAsState(targetValue = targetLeft, animationSpec = followSpec, label = "pillLeft")
    val animRight by animateFloatAsState(targetValue = targetRight, animationSpec = followSpec, label = "pillRight")

    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .width((68 * items.size).dp)
            .height(52.dp)
            .draggable(
                state = rememberDraggableState { delta ->
                    dragX += delta
                    val nearest = itemIndexAt(dragX, starts.asList(), ends.asList())
                    if (nearest != previewIndex && hapticsEnabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }
                    previewIndex = nearest
                },
                orientation = Orientation.Horizontal,
                onDragStarted = { offset ->
                    dragX = offset.x
                    val idx = itemIndexAt(dragX, starts.asList(), ends.asList())
                    pressedIndex = idx
                    previewIndex = idx
                },
                onDragStopped = {
                    val finalIdx = previewIndex ?: selectedIndex
                    if (finalIdx != selectedIndex) {
                        onSelect(items[finalIdx].id)
                    }
                    pressedIndex = null
                    previewIndex = null
                },
            ),
    ) {
        // Elastic / jumping primary pill behind the selected icon
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val pillWidth = (animRight - animLeft).toInt().coerceAtLeast(minPillWidthPx)
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = pillWidth,
                            maxWidth = pillWidth,
                            minHeight = 28.dp.roundToPx(),
                            maxHeight = 28.dp.roundToPx(),
                        ),
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(animLeft.toInt(), (constraints.maxHeight - placeable.height) / 2)
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .testTag("compact_nav_pill")
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(palette.primary),
            )
        }

        // Equal slots Row
        Row(
            modifier = Modifier.width((68 * items.size).dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = (index == shownIndex)
                Column(
                    modifier = Modifier
                        .testTag("nav_${item.id}")
                        .onGloballyPositioned { coords ->
                            val x = coords.positionInParent().x
                            starts[index] = x
                            ends[index] = x + coords.size.width
                        }
                        .width(68.dp)
                        .height(52.dp)
                        .defaultMinSize(minWidth = 68.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .selectable(
                            selected = (index == selectedIndex),
                            onClick = {
                                if (item.id != selectedId) {
                                    if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onSelect(item.id)
                                }
                            },
                            role = Role.Tab,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .height(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) palette.onPrimary else palette.muted,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = if (selected) palette.text else palette.muted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
