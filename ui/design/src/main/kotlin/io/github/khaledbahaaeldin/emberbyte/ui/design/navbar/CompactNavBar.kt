@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.navbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class NavAccent { Primary, Secondary, Tertiary, Error }

data class NavBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accent: NavAccent,
)

internal object CompactNavBarRecompositionTracker {
    var count: Int = 0
}

@Stable
private class ItemCellSpring(initialX: Float, initialWidth: Float) {
    val xAnim = Animatable(initialX, visibilityThreshold = 0.1f)
    val widthAnim = Animatable(initialWidth, visibilityThreshold = 0.1f)
    var targetX = initialX
    var targetWidth = initialWidth
}

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

@Composable
private fun NavBarRow(
    items: List<NavBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    hapticsEnabled: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    val palette = LocalEmberbytePalette.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
    )

    val collapsedCellDp = 56.dp
    val paddingDp = 6.dp
    val barHeightDp = 64.dp
    val iconSizeDp = 24.dp
    val minExpandedCellDp = 92.dp

    val widestLabelPx = remember(items, density.density, density.fontScale, labelStyle) {
        val measurerPx = items.maxOfOrNull { item ->
            textMeasurer.measure(
                text = item.label,
                style = labelStyle,
                maxLines = 1,
                softWrap = false,
                density = density,
            ).size.width.toFloat()
        } ?: 0f
        // Fallback character-width floor (7.2sp per glyph) when Robolectric returns 0px for custom fonts
        val charFloorPx = with(density) {
            items.maxOfOrNull { item ->
                item.label.length * (7.2f.sp.toPx())
            } ?: 0f
        }
        maxOf(measurerPx, charFloorPx)
    }

    val minExpandedPx = with(density) { minExpandedCellDp.toPx() }
    val iconAndGapPx = with(density) { (iconSizeDp + 12.dp).toPx() }
    val expandedCellPx = maxOf(minExpandedPx, widestLabelPx + iconAndGapPx)

    val collapsedCount = (items.size - 1).coerceAtLeast(0)
    val collapsedCellPx = with(density) { collapsedCellDp.toPx() }
    val paddingPx = with(density) { paddingDp.toPx() }
    val totalBarWidthPx = collapsedCount * collapsedCellPx + expandedCellPx + 2f * paddingPx
    val totalBarWidthDp = with(density) { totalBarWidthPx.toDp() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier
                .testTag("compact_nav_bar")
                .width(totalBarWidthDp)
                .height(barHeightDp)
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
                totalBarWidthDp = totalBarWidthDp,
                totalBarWidthPx = totalBarWidthPx,
                collapsedCellPx = collapsedCellPx,
                expandedCellPx = expandedCellPx,
                paddingPx = paddingPx,
                labelStyle = labelStyle,
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
    totalBarWidthDp: androidx.compose.ui.unit.Dp,
    totalBarWidthPx: Float,
    collapsedCellPx: Float,
    expandedCellPx: Float,
    paddingPx: Float,
    labelStyle: androidx.compose.ui.text.TextStyle,
) {
    CompactNavBarRecompositionTracker.count++

    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val scope = rememberCoroutineScope()

    val selectedIndex = items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentHapticsEnabled by rememberUpdatedState(hapticsEnabled)
    val currentReduceMotion by rememberUpdatedState(reduceMotion)
    val currentIsRtl by rememberUpdatedState(isRtl)

    val pillHeightPx = with(density) { 52.dp.toPx() }
    val pillTopPx = with(density) { 6.dp.toPx() }
    val wallMarginPx = with(density) { 2.dp.toPx() }
    val minPillWidthPx = with(density) { 40.dp.toPx() }
    val iconSizePx = with(density) { 24.dp.toPx() }
    val iconLabelGapPx = with(density) { 6.dp.toPx() }

    fun computeCells(selIdx: Int, rtl: Boolean): Pair<FloatArray, FloatArray> {
        val xs = FloatArray(items.size)
        val ws = FloatArray(items.size)
        var x = paddingPx
        for (v in items.indices) {
            val logicalIdx = if (rtl) (items.size - 1 - v) else v
            val w = if (logicalIdx == selIdx) expandedCellPx else collapsedCellPx
            xs[logicalIdx] = x
            ws[logicalIdx] = w
            x += w
        }
        return xs to ws
    }

    val initialCells = remember(items.size, isRtl, collapsedCellPx, expandedCellPx, paddingPx) {
        computeCells(selectedIndex, isRtl)
    }

    val pillSpring = remember(items.size) {
        PillSpring(
            initialLeft = initialCells.first[selectedIndex],
            initialRight = initialCells.first[selectedIndex] + initialCells.second[selectedIndex],
            barInnerLeft = wallMarginPx,
            barInnerRight = totalBarWidthPx - wallMarginPx,
            minPillWidth = minPillWidthPx,
            reduceMotion = reduceMotion,
        )
    }
    pillSpring.barInnerLeft = wallMarginPx
    pillSpring.barInnerRight = totalBarWidthPx - wallMarginPx
    pillSpring.reduceMotion = reduceMotion

    val cellSprings = remember(items.size) {
        Array(items.size) { idx ->
            ItemCellSpring(
                initialX = initialCells.first[idx],
                initialWidth = initialCells.second[idx],
            )
        }
    }

    val unitScale = remember {
        object : androidx.compose.ui.MotionDurationScale {
            override val scaleFactor: Float get() = 1f
        }
    }

    fun launchTargetTransition(targetIdx: Int, velocityPxPerSec: Float, snap: Boolean) {
        val (targetXs, targetWs) = computeCells(targetIdx, isRtl)
        val pLeft = targetXs[targetIdx]
        val pRight = pLeft + targetWs[targetIdx]

        scope.launch(context = unitScale, start = CoroutineStart.UNDISPATCHED) {
            if (snap || reduceMotion) {
                pillSpring.reduceMotion = true
                pillSpring.retarget(pLeft, pRight, 0f)
                pillSpring.reduceMotion = reduceMotion
            } else {
                pillSpring.retarget(pLeft, pRight, velocityPxPerSec)
            }
        }
        for (i in items.indices) {
            val tx = targetXs[i]
            val tw = targetWs[i]
            val cs = cellSprings[i]
            cs.targetX = tx
            cs.targetWidth = tw
            scope.launch(context = unitScale, start = CoroutineStart.UNDISPATCHED) {
                if (snap || reduceMotion) {
                    cs.xAnim.snapTo(tx)
                } else {
                    cs.xAnim.animateTo(
                        targetValue = tx,
                        animationSpec = spring(
                            dampingRatio = NavBarSpring.DAMPING_RATIO,
                            stiffness = NavBarSpring.cellStiffness,
                            visibilityThreshold = 0.5f,
                        ),
                    )
                }
            }
            scope.launch(context = unitScale, start = CoroutineStart.UNDISPATCHED) {
                if (snap || reduceMotion) {
                    cs.widthAnim.snapTo(tw)
                } else {
                    cs.widthAnim.animateTo(
                        targetValue = tw,
                        animationSpec = spring(
                            dampingRatio = NavBarSpring.DAMPING_RATIO,
                            stiffness = NavBarSpring.cellStiffness,
                            visibilityThreshold = 0.5f,
                        ),
                    )
                }
            }
        }
    }

    val lastLayoutKey = remember { mutableListOf<Any>(isRtl, collapsedCellPx, expandedCellPx, totalBarWidthPx) }
    val lastSelectedIndex = remember { intArrayOf(selectedIndex) }

    val currentKey = listOf<Any>(isRtl, collapsedCellPx, expandedCellPx, totalBarWidthPx)
    val layoutChanged = currentKey != lastLayoutKey
    val selectionChanged = selectedIndex != lastSelectedIndex[0]
    if (layoutChanged) {
        lastLayoutKey.clear()
        lastLayoutKey.addAll(currentKey)
    }
    lastSelectedIndex[0] = selectedIndex

    val (targetXs, targetWs) = computeCells(selectedIndex, isRtl)
    val expectedL = targetXs[selectedIndex]
    val expectedR = expectedL + targetWs[selectedIndex]
    val alreadyTargeting = abs(pillSpring.targetLeft - expectedL) < 0.5f &&
        abs(pillSpring.targetRight - expectedR) < 0.5f

    if (layoutChanged || reduceMotion || (selectionChanged && !alreadyTargeting)) {
        launchTargetTransition(
            targetIdx = selectedIndex,
            velocityPxPerSec = 0f,
            snap = layoutChanged || reduceMotion,
        )
    }

    val pillColor = palette.primary
    val innerWidthPx = (totalBarWidthPx - 2f * paddingPx).coerceAtLeast(1f)

    Layout(
        modifier = Modifier
            .width(totalBarWidthDp)
            .height(64.dp)
            .drawBehind {
                val l = pillSpring.left
                val r = pillSpring.right
                val w = (r - l).coerceAtLeast(minPillWidthPx)
                val radius = pillHeightPx / 2f
                drawRoundRect(
                    color = pillColor,
                    topLeft = Offset(l, pillTopPx),
                    size = Size(w, pillHeightPx),
                    cornerRadius = CornerRadius(radius, radius),
                )
            }
            .pointerInput(items.size, totalBarWidthPx, collapsedCellPx, expandedCellPx, paddingPx) {
                val touchSlop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val velocityTracker = VelocityTracker()
                    velocityTracker.addPosition(down.uptimeMillis, down.position)
                    val startX = down.position.x
                    val startPillLeft = pillSpring.left
                    val startPillWidth = (pillSpring.right - pillSpring.left).coerceAtLeast(expandedCellPx)
                    var dragging = false
                    var lastX = startX
                    var lastUptime = down.uptimeMillis
                    var lastVelPxPerSec = 0f
                    var endX = startX

                    var pointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.firstOrNull()
                        if (change == null || !change.pressed) {
                            break
                        }
                        pointerId = change.id
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        val dtMs = (change.uptimeMillis - lastUptime).coerceAtLeast(1L)
                        lastVelPxPerSec = (change.position.x - lastX) / dtMs * 1000f
                        lastX = change.position.x
                        endX = change.position.x
                        lastUptime = change.uptimeMillis

                        val dxTotal = change.position.x - startX
                        if (!dragging && abs(dxTotal) > touchSlop) {
                            dragging = true
                        }
                        if (dragging) {
                            change.consume()
                            val newLeft = (startPillLeft + dxTotal).coerceIn(
                                paddingPx,
                                (totalBarWidthPx - paddingPx - startPillWidth).coerceAtLeast(paddingPx),
                            )
                            val newRight = newLeft + startPillWidth
                            scope.launch(context = unitScale, start = CoroutineStart.UNDISPATCHED) {
                                pillSpring.dragTo(newLeft, newRight)
                            }
                        }
                    }

                    if (dragging) {
                        val trackerVel = velocityTracker.calculateVelocity().x
                        val rawVel = if (abs(trackerVel) > abs(lastVelPxPerSec)) trackerVel else lastVelPxPerSec
                        val flickThresholdPxPerSec = 600.dp.toPx()
                        val momentumVel = if (abs(rawVel) >= flickThresholdPxPerSec) rawVel else 0f
                        val (curXs, curWs) = computeCells(currentSelectedIndex, currentIsRtl)
                        val selLeft = curXs[currentSelectedIndex]
                        val selRight = selLeft + curWs[currentSelectedIndex]
                        val stayedInsideSelected = startX in selLeft..selRight && endX in selLeft..selRight && momentumVel == 0f

                        val landingIdx = if (stayedInsideSelected) {
                            currentSelectedIndex
                        } else {
                            NavBarSpring.landingIndex(
                                pillLeft = pillSpring.left,
                                pillRight = pillSpring.right,
                                velocityPxPerSec = momentumVel,
                                paddingPx = paddingPx,
                                innerWidthPx = innerWidthPx,
                                itemCount = items.size,
                                isRtl = currentIsRtl,
                            )
                        }
                        launchTargetTransition(
                            targetIdx = landingIdx,
                            velocityPxPerSec = rawVel,
                            snap = currentReduceMotion,
                        )
                        if (landingIdx != currentSelectedIndex) {
                            if (currentHapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            }
                            currentOnSelect(items[landingIdx].id)
                        }
                    } else {
                        val (curXs, curWs) = computeCells(currentSelectedIndex, currentIsRtl)
                        val starts = curXs.toList()
                        val ends = List(items.size) { i -> curXs[i] + curWs[i] }
                        val tappedIdx = itemIndexAt(endX, starts, ends)
                        if (tappedIdx != currentSelectedIndex) {
                            launchTargetTransition(
                                targetIdx = tappedIdx,
                                velocityPxPerSec = 0f,
                                snap = currentReduceMotion,
                            )
                        }
                    }
                }
            },
        content = {
            // 0: Pill semantics/test node placed from pillSpring in layout/draw without recomposition
            Box(
                modifier = Modifier
                    .testTag("compact_nav_pill")
                    .layout { measurable, _ ->
                        val w = (pillSpring.right - pillSpring.left).roundToInt().coerceAtLeast(minPillWidthPx.roundToInt())
                        val h = pillHeightPx.roundToInt()
                        val placeable = measurable.measure(Constraints.fixed(w, h))
                        layout(w, h) {
                            placeable.place(0, 0)
                        }
                    },
            )

            // 1..items.size: Item cells
            items.forEachIndexed { index, item ->
                val isSelected = (index == selectedIndex)
                val cellSpring = cellSprings[index]
                val tintColor = if (isSelected) palette.onPrimary else palette.muted

                Layout(
                    modifier = Modifier
                        .testTag("nav_${item.id}")
                        .clip(CircleShape)
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                if (item.id != selectedId) {
                                    if (hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    }
                                    onSelect(item.id)
                                }
                            },
                            role = Role.Tab,
                        )
                        .semantics {
                            contentDescription = item.label
                        },
                    content = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = tintColor,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = item.label,
                            color = tintColor,
                            style = labelStyle,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .testTag("nav_label_${item.id}")
                                .graphicsLayer {
                                    alpha = NavBarSpring.labelOpacity(
                                        cellWidthPx = cellSpring.widthAnim.value,
                                        collapsedWidthPx = collapsedCellPx,
                                        expandedWidthPx = expandedCellPx,
                                    )
                                    clip = true
                                },
                        )
                    },
                ) { measurables, constraints ->
                    val cellW = constraints.maxWidth
                    val cellH = constraints.maxHeight
                    val iconPlaceable = measurables[0].measure(
                        Constraints.fixed(iconSizePx.roundToInt(), iconSizePx.roundToInt()),
                    )
                    val f = if (expandedCellPx > collapsedCellPx) {
                        ((cellW.toFloat() - collapsedCellPx) / (expandedCellPx - collapsedCellPx)).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                    val maxLabelWidth = ((expandedCellPx - iconSizePx - iconLabelGapPx) * f).roundToInt().coerceAtLeast(0)
                    val labelPlaceable = measurables[1].measure(
                        Constraints(
                            minWidth = 0,
                            maxWidth = Constraints.Infinity,
                            minHeight = 0,
                            maxHeight = cellH,
                        ),
                    )
                    layout(cellW, cellH) {
                        val visibleLabelW = minOf(labelPlaceable.width, maxLabelWidth)
                        val gap = if (f > 0.02f && visibleLabelW > 0) {
                            (iconLabelGapPx * f).roundToInt()
                        } else {
                            0
                        }
                        val totalContentW = iconPlaceable.width + gap + visibleLabelW
                        val startX = ((cellW - totalContentW) / 2f).roundToInt()
                        val iconY = ((cellH - iconPlaceable.height) / 2f).roundToInt()
                        if (isRtl) {
                            val iconX = cellW - startX - iconPlaceable.width
                            iconPlaceable.place(iconX, iconY)
                            val labelX = iconX - gap - visibleLabelW
                            val labelY = ((cellH - labelPlaceable.height) / 2f).roundToInt()
                            labelPlaceable.place(labelX, labelY)
                        } else {
                            iconPlaceable.place(startX, iconY)
                            val labelX = startX + iconPlaceable.width + gap
                            val labelY = ((cellH - labelPlaceable.height) / 2f).roundToInt()
                            labelPlaceable.place(labelX, labelY)
                        }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val barW = constraints.maxWidth
        val barH = constraints.maxHeight
        val cellH = pillHeightPx.roundToInt()
        val cellY = pillTopPx.roundToInt()

        val pillPlaceable = measurables[0].measure(Constraints(0, barW, 0, barH))
        val itemPlaceables = Array(items.size) { i ->
            val cw = cellSprings[i].widthAnim.value.roundToInt().coerceAtLeast(minPillWidthPx.roundToInt())
            measurables[i + 1].measure(Constraints.fixed(cw, cellH))
        }

        layout(barW, barH) {
            pillPlaceable.place(pillSpring.left.roundToInt(), cellY)
            for (i in items.indices) {
                val cx = cellSprings[i].xAnim.value.roundToInt()
                itemPlaceables[i].place(cx, cellY)
            }
        }
    }
}
