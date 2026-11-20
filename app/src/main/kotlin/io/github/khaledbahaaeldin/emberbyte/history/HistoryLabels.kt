package io.github.khaledbahaaeldin.emberbyte.history

import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class HistoryBarLabel(
    val label: String,
    val description: String,
    val title: String? = null,
    val shortLabel: String? = null,
    val isBoundary: Boolean = false,
)

/** Formats a compact date range string, e.g. "24 Sep to 7 Oct", "28 Dec to 4 Jan 2027", or "28 Sep to 4 Oct". */
fun formatDateRange(start: LocalDate, end: LocalDate, locale: Locale): String {
    val startMonth = start.month.getDisplayName(TextStyle.SHORT, locale)
    val endMonth = end.month.getDisplayName(TextStyle.SHORT, locale)
    return when {
        start.month == end.month && start.year == end.year -> {
            "${start.dayOfMonth} to ${end.dayOfMonth} $startMonth"
        }
        start.year == end.year -> {
            "${start.dayOfMonth} $startMonth to ${end.dayOfMonth} $endMonth"
        }
        else -> {
            "${start.dayOfMonth} $startMonth ${start.year} to ${end.dayOfMonth} $endMonth ${end.year}"
        }
    }
}

object HistoryLabels {
    fun format(
        date: LocalDate,
        previousDate: LocalDate?,
        granularity: Granularity,
        totalBytes: Long,
        units: ByteUnits,
        locale: Locale = Locale.getDefault(),
    ): HistoryBarLabel {
        val spoken = spokenBytes(totalBytes, units)
        return when (granularity) {
            Granularity.DAY, Granularity.HOUR -> {
                val isMonthRollover = (previousDate != null && previousDate.month != date.month) ||
                    (previousDate == null && date.dayOfMonth == 1)
                val label = if (isMonthRollover) {
                    "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, locale)}"
                } else {
                    val initial = date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
                    "$initial\n${date.dayOfMonth}"
                }
                val title = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)}, " +
                    "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, locale)}"
                val description = "$title, $spoken"
                HistoryBarLabel(
                    label = label,
                    description = description,
                    title = title,
                    isBoundary = isMonthRollover,
                )
            }
            Granularity.WEEK -> {
                val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = monday.plusDays(6)
                val fullMonth = monday.month.getDisplayName(TextStyle.FULL, locale)
                val label = formatDateRange(monday, sunday, locale)
                val shortLabel = "${monday.dayOfMonth} ${monday.month.getDisplayName(TextStyle.SHORT, locale)}"
                val title = "Week of ${monday.dayOfMonth} $fullMonth"
                val description = "$title, $spoken"
                HistoryBarLabel(label = label, description = description, title = title, shortLabel = shortLabel)
            }
            Granularity.MONTH -> {
                val shortMonth = date.month.getDisplayName(TextStyle.SHORT, locale)
                val fullMonth = date.month.getDisplayName(TextStyle.FULL, locale)
                val isYearBoundary = (previousDate != null && previousDate.year != date.year) ||
                    (previousDate == null && date.monthValue == 1)
                val label = if (isYearBoundary) {
                    val twoDigitYear = String.format(Locale.ROOT, "%02d", date.year % 100)
                    "$shortMonth $twoDigitYear"
                } else {
                    shortMonth
                }
                val title = "$fullMonth ${date.year}"
                val description = "$title, $spoken"
                HistoryBarLabel(label = label, description = description, title = title)
            }
        }
    }
}
