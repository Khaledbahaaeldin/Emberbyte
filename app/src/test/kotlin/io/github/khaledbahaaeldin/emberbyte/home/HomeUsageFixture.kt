package io.github.khaledbahaaeldin.emberbyte.home

import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.AppUsageRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.DayUsage
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.RankedApp
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageCardState
import io.github.khaledbahaaeldin.emberbyte.ui.design.usage.UsageFilters
import java.time.LocalDate

/** A seven-day card state for Home tests: today is Friday 20 November 2026 and four apps are ranked. */
internal fun sampleUsageCard(
    selectedIndex: Int = 6,
    filters: UsageFilters = UsageFilters(),
    rows: List<Pair<String, Long>> = listOf(
        "com.video" to 1_590_000_000L,
        "com.chat" to 900_000_000L,
        "com.maps" to 400_000_000L,
        "com.music" to 100_000_000L,
    ),
): UsageCardState {
    val today = LocalDate.of(2026, 11, 20)
    val totals = listOf(900L, 1_400L, 700L, 2_100L, 1_100L, 1_600L, 2_990L).map { it * 1_000_000L }
    val days = totals.mapIndexed { i, t -> DayUsage(today.minusDays((6 - i).toLong()), t, t / 2, t / 2) }
    val sum = rows.sumOf { it.second }
    return UsageCardState(
        days = days,
        selectedIndex = selectedIndex,
        rows = rows.map { (pkg, amount) ->
            RankedApp(AppUsageRow(pkg, pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }, 0L, amount), amount, (amount * 100 / sum).toInt())
        },
        filters = filters,
        units = ByteUnits.DECIMAL,
    )
}
