package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class WhatIfUi(
    val amountText: String,
    val unit: CapUnit,
    val currentText: String,
    val whatIfText: String?,
    val error: String?,
)

private fun daysBetween(a: Instant, b: Instant): Long = abs(a.epochSecond - b.epochSecond) / 86_400

/**
 * Formats a forecast sentence for What-if, always using a full date ("EEE d MMM", e.g. "Sat 22 Nov")
 * rather than a bare weekday so near-term dates are never ambiguous (D11).
 */
fun whatIfSentence(
    forecast: Forecast,
    remainingBytes: Long,
    now: Instant,
    zone: ZoneId,
    locale: Locale,
): String {
    if (forecast.runOutEarliest == null && forecast.runOutExpected == null && forecast.runOutLatest == null) {
        return "Won't run out this cycle"
    }
    val expected = forecast.runOutExpected
    if (expected != null && expected <= now) {
        return if (remainingBytes <= 0L) "Data used up" else "runs out today"
    }
    val formatter = DateTimeFormatter.ofPattern("EEE d MMM", locale)
    if (expected == null && forecast.runOutEarliest != null) {
        val day = formatter.format(forecast.runOutEarliest!!.atZone(zone))
        return "Could run out as early as $day"
    }
    val at = expected ?: forecast.runOutEarliest ?: forecast.runOutLatest!!
    val fullDate = formatter.format(at.atZone(zone))
    val spread = max(
        forecast.runOutEarliest?.let { daysBetween(at, it) } ?: 0L,
        forecast.runOutLatest?.let { daysBetween(at, it) } ?: 0L,
    )
    return when {
        forecast.runOutLatest == null && forecast.runOutEarliest != null -> "runs out $fullDate (could be later)"
        spread == 0L -> "runs out $fullDate"
        spread == 1L -> "runs out $fullDate (±1 day)"
        else -> "runs out $fullDate (±$spread days)"
    }
}
