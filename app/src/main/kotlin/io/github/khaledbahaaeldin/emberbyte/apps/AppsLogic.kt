package io.github.khaledbahaaeldin.emberbyte.apps

import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi

/** Number of filters that differ from the defaults (network, sort, system apps). The period is not a filter. */
internal fun activeFilterCount(network: AppsNetwork, sort: AppSort, showSystemApps: Boolean): Int =
    (if (network != AppsNetwork.ALL) 1 else 0) +
        (if (sort != AppSort.BYTES_DESC) 1 else 0) +
        (if (showSystemApps) 1 else 0)

/** The period as it reads in the summary label ("Apps used today", "... in 7 days"). */
internal fun periodLabel(range: AppsRange): String = when (range) {
    AppsRange.TODAY -> "today"
    AppsRange.LAST_7_DAYS -> "in 7 days"
    AppsRange.LAST_30_DAYS -> "in 30 days"
    AppsRange.THIS_MONTH, AppsRange.CYCLE -> "this cycle"
}

/** Share of the split bar kept for the smaller side so it stays visible (about 14 dp of a 300 dp bar). */
private const val MIN_SPLIT_SHARE = 0.047f

/** Weights of the Wi-Fi and mobile segments, summing to 1; a non-zero side never gets less than [MIN_SPLIT_SHARE]. */
internal fun splitWeights(wifi: Long, mobile: Long): Pair<Float, Float> {
    val w = wifi.coerceAtLeast(0L)
    val m = mobile.coerceAtLeast(0L)
    if (w == 0L && m == 0L) return 0.5f to 0.5f
    if (m == 0L) return 1f to 0f
    if (w == 0L) return 0f to 1f
    val mobileShare = (m.toFloat() / (w + m).toFloat()).coerceIn(MIN_SPLIT_SHARE, 1f - MIN_SPLIT_SHARE)
    return (1f - mobileShare) to mobileShare
}

/** An app's amount for the selected network. */
internal fun networkAmount(app: AppRowUi, network: AppsNetwork): Long = when (network) {
    AppsNetwork.ALL -> app.mobileBytes + app.wifiBytes
    AppsNetwork.MOBILE -> app.mobileBytes
    AppsNetwork.WIFI -> app.wifiBytes
}

/** The top row by the selected network's amount. */
internal fun mostUsed(rows: List<AppRowUi>, network: AppsNetwork): AppRowUi? =
    rows.maxByOrNull { networkAmount(it, network) }

/** The sort as shown on the list header and in the filter chip. */
internal fun sortLabel(sort: AppSort): String = when (sort) {
    AppSort.NAME -> "Name"
    AppSort.BYTES_ASC -> "Least data"
    AppSort.BYTES_DESC, AppSort.SCREEN_TIME_DESC -> "Most data"
}
