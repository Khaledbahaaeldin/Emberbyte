package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.plan.CycleCalculator
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Translatable versions of [cycleSummaryLine] and [nextResetDescription]. The sentences come from
 * string resources; the dates and weekday names come from the locale. (The "1st, 2nd" suffix of
 * [ordinalDay] is English only for now.)
 */
@Composable
internal fun cycleSummaryText(cycle: Cycle, locale: Locale): String = when (cycle) {
    is Cycle.MonthlyOnDay -> if (cycle.dayOfMonth >= 31) {
        stringResource(R.string.plan_flow_resets_last_day)
    } else {
        stringResource(R.string.plan_flow_resets_monthly, ordinalDay(cycle.dayOfMonth))
    }
    is Cycle.EveryNDays -> if (cycle.days == 7) {
        stringResource(
            R.string.plan_flow_resets_weekly,
            cycle.anchor.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
        )
    } else {
        stringResource(R.string.plan_flow_resets_days, cycle.days)
    }
}

@Composable
internal fun nextResetText(cycle: Cycle, now: Instant, zone: ZoneId, locale: Locale): String {
    val nextDate = CycleCalculator.windowAt(cycle, now).end.atZone(zone).toLocalDate()
    return stringResource(
        R.string.plan_flow_next_reset,
        DateTimeFormatter.ofPattern("EEE d MMMM", locale).format(nextDate),
    )
}
