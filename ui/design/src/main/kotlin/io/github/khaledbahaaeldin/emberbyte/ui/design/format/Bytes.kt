package io.github.khaledbahaaeldin.emberbyte.ui.design.format

import java.math.BigDecimal
import java.math.RoundingMode

enum class ByteUnits(val base: Long, val labels: List<String>) {
    DECIMAL(1000L, listOf("B", "KB", "MB", "GB", "TB")),
    BINARY(1024L, listOf("B", "KiB", "MiB", "GiB", "TiB")),
}

data class FormattedBytes(val value: String, val unit: String)

internal fun scaleBytes(bytes: Long, units: ByteUnits): Pair<Double, Int> {
    val safe = bytes.coerceAtLeast(0L)
    var scaled = safe.toDouble()
    var index = 0
    while (index < units.labels.lastIndex && scaled >= units.base) {
        scaled /= units.base
        index++
    }
    return scaled to index
}

fun formatBytes(bytes: Long, units: ByteUnits): FormattedBytes {
    val (scaledInitial, initialIndex) = scaleBytes(bytes, units)
    var scaled = scaledInitial
    var index = initialIndex
    if (index == 0) return FormattedBytes(bytes.coerceAtLeast(0L).toString(), units.labels[0])

    var rounded = roundForDisplay(scaled)
    if (index < units.labels.lastIndex && rounded >= BigDecimal(units.base)) {
        scaled /= units.base
        index++
        rounded = roundForDisplay(scaled)
    }
    return FormattedBytes(rounded.toPlainString(), units.labels[index])
}

/** Formats bar chart values: one decimal below 10 units, none otherwise. e.g. "1.2 GB", "12 GB". */
fun formatBarValue(bytes: Long, units: ByteUnits): String {
    val (scaledInitial, initialIndex) = scaleBytes(bytes, units)
    var scaled = scaledInitial
    var index = initialIndex
    if (index == 0) return "${bytes.coerceAtLeast(0L)} ${units.labels[0]}"
    var decimals = if (scaled < 10.0) 1 else 0
    var rounded = BigDecimal.valueOf(scaled).setScale(decimals, RoundingMode.HALF_UP)
    if (decimals == 1 && rounded.toDouble() >= 10.0) {
        decimals = 0
        rounded = BigDecimal.valueOf(scaled).setScale(0, RoundingMode.HALF_UP)
    }
    if (index < units.labels.lastIndex && rounded >= BigDecimal(units.base)) {
        scaled /= units.base
        index++
        decimals = if (scaled < 10.0) 1 else 0
        rounded = BigDecimal.valueOf(scaled).setScale(decimals, RoundingMode.HALF_UP)
    }
    return "${rounded.toPlainString()} ${units.labels[index]}"
}

private fun decimalsFor(value: Double): Int = when {
    value >= 100 -> 0
    value >= 10 -> 1
    else -> 2
}

private fun roundForDisplay(value: Double): BigDecimal {
    var decimals = decimalsFor(value)
    var rounded = BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP)
    // Rounding can cross a threshold (99.96 -> 100.0, 9.996 -> 10.00): re-pick the decimals.
    val recheck = decimalsFor(rounded.toDouble())
    if (recheck < decimals) {
        decimals = recheck
        rounded = BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP)
    }
    return rounded
}

fun spokenUnit(unit: String): String = when (unit) {
    "B" -> "bytes"
    "KB" -> "kilobytes"
    "MB" -> "megabytes"
    "GB" -> "gigabytes"
    "TB" -> "terabytes"
    "KiB" -> "kibibytes"
    "MiB" -> "mebibytes"
    "GiB" -> "gibibytes"
    "TiB" -> "tebibytes"
    else -> unit
}
