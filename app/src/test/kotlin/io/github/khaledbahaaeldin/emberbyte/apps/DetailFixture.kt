package io.github.khaledbahaaeldin.emberbyte.apps

import io.github.khaledbahaaeldin.emberbyte.apps.detail.BudgetProjection
import io.github.khaledbahaaeldin.emberbyte.apps.detail.DetailRange
import io.github.khaledbahaaeldin.emberbyte.apps.detail.PlanImpact
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import java.time.LocalDate
import java.time.ZoneId

/** Seven days ending Friday 20 November 2026 (Saturday 14 is the busiest), in the system zone. */
internal fun sampleSeries(zone: ZoneId = ZoneId.systemDefault()): List<UsagePoint> {
    val end = LocalDate.of(2026, 11, 20)
    val wifi = listOf(426L, 180L, 200L, 210L, 90L, 140L, 150L)
    return wifi.mapIndexed { i, w ->
        UsagePoint(end.minusDays((6 - i).toLong()).atStartOfDay(zone).toInstant(), 30_000_000L, w * 1_000_000L)
    }
}

internal fun sampleDetail(): AppDetailUiState {
    val hours = LongArray(24).also { it[19] = 330_000_000L; it[20] = 150_000_000L; it[21] = 100_000_000L; it[8] = 80_000_000L }
    return AppDetailUiState(
        packageName = "com.video",
        label = "Video",
        totalBytes = 1_606_000_000L,
        mobileBytes = 210_000_000L,
        wifiBytes = 1_396_000_000L,
        dailySeries = sampleSeries(),
        range = DetailRange.Week,
        previousTotalBytes = 1_434_000_000L,
        rank = 2,
        appCount = 47,
        shareOfAll = 0.34f,
        hourTotals = hours,
        planMobileImpact = PlanImpact(210_000_000L, 20_000_000_000L, 0.011f, "in a week"),
        units = ByteUnits.DECIMAL,
        loaded = true,
    )
}
