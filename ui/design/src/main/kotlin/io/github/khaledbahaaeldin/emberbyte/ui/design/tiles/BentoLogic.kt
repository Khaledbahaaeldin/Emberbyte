package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

/** The network shown big on the "Today by network" tile. */
internal data class BigNetwork(val bytes: Long, val isWifi: Boolean)

/** The larger of the two networks; a tie (including 0 and 0) picks Wi-Fi. */
internal fun bigNetwork(mobile: Long, wifi: Long): BigNetwork =
    if (mobile > wifi) BigNetwork(mobile, isWifi = false) else BigNetwork(wifi, isWifi = true)

/** The mobile segment is never thinner than this when mobile data was used, so a tiny share stays visible. */
internal const val MIN_MOBILE_SEGMENT_DP = 14f

/**
 * Width in dp of the mobile segment of the split bar: proportional, at least [MIN_MOBILE_SEGMENT_DP] when mobile bytes
 * are above zero (9 MB of 3.7 GB is under 1 percent), zero when there is none, and never wider than the bar.
 */
internal fun mobileSegmentWidth(mobile: Long, wifi: Long, barWidthDp: Float): Float {
    if (mobile <= 0L) return 0f
    val total = mobile + maxOf(wifi, 0L)
    val proportional = barWidthDp * (mobile.toFloat() / total.toFloat())
    return proportional.coerceAtLeast(MIN_MOBILE_SEGMENT_DP).coerceAtMost(barWidthDp)
}

/** Vertical scale of the trace: the observed maximum, but never below [floorBps], so an idle line stays flat and low. */
internal fun traceScale(series: List<Long>, floorBps: Long): Long = maxOf(series.maxOrNull() ?: 0L, floorBps)

/** Appends [sample] and keeps only the newest [capacity] values, oldest first. */
fun appendSample(history: List<Long>, sample: Long, capacity: Int = 60): List<Long> {
    val next = history + sample
    return if (next.size > capacity) next.subList(next.size - capacity, next.size).toList() else next
}

/** Ease-out cubic count-up: 1 - (1 - p)^3 of [target], clamped so it never exceeds it. */
internal fun countUpValue(progress: Float, target: Double): Double {
    val p = progress.coerceIn(0f, 1f).toDouble()
    val eased = 1.0 - (1.0 - p) * (1.0 - p) * (1.0 - p)
    return (target * eased).coerceIn(minOf(0.0, target), maxOf(0.0, target))
}
