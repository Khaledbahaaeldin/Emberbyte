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
    val shortLabel: String? = null,
)

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
                val description = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)}, " +
                    "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, locale)}, $spoken"
                HistoryBarLabel(label = label, description = description)
            }
            Granularity.WEEK -> {
                val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = monday.plusDays(6)
                val fullMonth = monday.month.getDisplayName(TextStyle.FULL, locale)
                val label = if (monday.month == sunday.month) {
                    val shortMonth = sunday.month.getDisplayName(TextStyle.SHORT, locale)
                    "${monday.dayOfMonth} to ${sunday.dayOfMonth} $shortMonth"
                } else {
                    val startMonth = monday.month.getDisplayName(TextStyle.SHORT, locale)
                    val endMonth = sunday.month.getDisplayName(TextStyle.SHORT, locale)
                    "${monday.dayOfMonth} $startMonth to ${sunday.dayOfMonth} $endMonth"
                }
                val shortLabel = "${monday.dayOfMonth} ${monday.month.getDisplayName(TextStyle.SHORT, locale)}"
                val description = "Week of ${monday.dayOfMonth} $fullMonth, $spoken"
                HistoryBarLabel(label = label, description = description, shortLabel = shortLabel)
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
                val description = "$fullMonth ${date.year}, $spoken"
                HistoryBarLabel(label = label, description = description)
            }
        }
    }
}
