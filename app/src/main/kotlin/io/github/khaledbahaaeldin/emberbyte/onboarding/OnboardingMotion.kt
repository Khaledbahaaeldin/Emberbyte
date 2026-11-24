package io.github.khaledbahaaeldin.emberbyte.onboarding

import kotlin.math.abs
import kotlin.math.floor

internal enum class PermissionAction {
    Usage,
    Notifications,
    Done,
}

internal data class LayerRect(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
)

internal data class LayerState(
    val rect: LayerRect,
    val radiiX: List<Float>,
    val radiiY: List<Float>,
    val primaryMix: Float,
    val alphaMark: Float,
    val alphaCard: Float,
    val alphaDisc: Float,
    val alphaPermission: Float,
    val visible: Boolean,
)

internal fun nextPermissionAction(usageAccess: Boolean, notifications: Boolean): PermissionAction = when {
    !usageAccess -> PermissionAction.Usage
    !notifications -> PermissionAction.Notifications
    else -> PermissionAction.Done
}

internal fun ctaLabel(page: Int, usageAccess: Boolean, notifications: Boolean): String = when (page) {
    0 -> "Get started"
    1, 2 -> "Continue"
    else -> when (nextPermissionAction(usageAccess, notifications)) {
        PermissionAction.Usage -> "Allow usage access"
        PermissionAction.Notifications -> "Allow notifications"
        PermissionAction.Done -> "Done"
    }
}

internal fun skipLabel(position: Float): String =
    if (position >= 2.5f) "Skip for now" else "Skip intro"

internal fun backAlpha(position: Float): Float =
    (2f * position).coerceIn(0f, 1f)

internal fun pillWidthDp(index: Int, position: Float): Float {
    val dist = abs(position - index).coerceIn(0f, 1f)
    return 8f + (28f - 8f) * (1f - dist)
}

internal fun parallaxOffset(d: Float, widthPx: Float, isOutgoing: Boolean): Float =
    if (isOutgoing) d * widthPx * 0.3f else d * widthPx

internal fun pageAlpha(d: Float): Float =
    (1f - 1.15f * abs(d)).coerceIn(0f, 1f)

internal fun revealFraction(q: Float, index: Int): Float =
    ((q - 0.22f - 0.07f * index) / 0.45f).coerceIn(0f, 1f)

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun fadeWindow(p: Float, inStart: Float, inEnd: Float, outStart: Float, outEnd: Float): Float = when {
    p < inStart -> 0f
    p < inEnd -> ((p - inStart) / (inEnd - inStart)).coerceIn(0f, 1f)
    p <= outStart -> 1f
    p < outEnd -> (1f - (p - outStart) / (outEnd - outStart)).coerceIn(0f, 1f)
    else -> 0f
}

internal fun layerState(position: Float, rects: List<LayerRect>): LayerState {
    require(rects.size == 4) { "Expected 4 layer rects, got ${rects.size}" }
    val p = position.coerceIn(0f, 3f)
    val seg = floor(p).toInt().coerceIn(0, 2)
    val t = (p - seg).coerceIn(0f, 1f)

    val fromRect = rects[seg]
    val toRect = rects[seg + 1]
    val rect = LayerRect(
        x = lerp(fromRect.x, toRect.x, t),
        y = lerp(fromRect.y, toRect.y, t),
        w = lerp(fromRect.w, toRect.w, t),
        h = lerp(fromRect.h, toRect.h, t),
    )

    // Radii in px/percent-like units per step:
    // Step 0 (mark 72dp cookie): 46%, 54%, 50%, 50% X and 54%, 46%, 54%, 46% Y
    // Step 1 (Today card): 36, 36, 12, 36
    // Step 2 (Plan disc 216dp): 108, 108, 108, 108
    // Step 3 (Permission card): 32, 32, 12, 32
    fun radiiFor(step: Int): Pair<List<Float>, List<Float>> = when (step) {
        0 -> {
            val r = rects[0]
            listOf(r.w * 0.46f, r.w * 0.54f, r.w * 0.50f, r.w * 0.50f) to
                listOf(r.h * 0.54f, r.h * 0.46f, r.h * 0.54f, r.h * 0.46f)
        }
        1 -> listOf(36f, 36f, 12f, 36f).let { it to it }
        2 -> listOf(108f, 108f, 108f, 108f).let { it to it }
        else -> listOf(32f, 32f, 12f, 32f).let { it to it }
    }

    val (rx0, ry0) = radiiFor(seg)
    val (rx1, ry1) = radiiFor(seg + 1)
    val radiiX = List(4) { i -> lerp(rx0[i], rx1[i], t) }
    val radiiY = List(4) { i -> lerp(ry0[i], ry1[i], t) }

    val primaryMix = (1f - p).coerceIn(0f, 1f)
    val alphaMark = if (p >= 0.31f) 0f else (1f - p / 0.31f).coerceIn(0f, 1f)
    val alphaCard = fadeWindow(p, 0.5f, 0.85f, 1.0f, 1.31f)
    val alphaDisc = fadeWindow(p, 1.55f, 1.9f, 2.0f, 2.31f)
    val alphaPermission = ((p - 2.55f) / (2.9f - 2.55f)).coerceIn(0f, 1f)

    return LayerState(
        rect = rect,
        radiiX = radiiX,
        radiiY = radiiY,
        primaryMix = primaryMix,
        alphaMark = alphaMark,
        alphaCard = alphaCard,
        alphaDisc = alphaDisc,
        alphaPermission = alphaPermission,
        visible = p < 2.999f,
    )
}
