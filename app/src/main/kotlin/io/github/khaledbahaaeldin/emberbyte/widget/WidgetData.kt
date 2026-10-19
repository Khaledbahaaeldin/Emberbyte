package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import io.github.khaledbahaaeldin.emberbyte.AppGraph
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.engine.model.*
import io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanUrgency
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Shared data model consumed by the Glance widget family (Contract 13.7, Plan Task E2).
 */
data class WidgetData(
    val todayBytes: Long,
    val plan: PlanState?,
    val topApps: List<AppUsage>,
    val mobileBytes: Long,
    val wifiBytes: Long,
    val dailyBytes: List<Long>,
    val permissionGranted: Boolean,
    val estimated: Boolean,
    val units: UnitSystem = UnitSystem.DECIMAL,
    val now: Instant = Instant.now(),
    val zone: ZoneId = ZoneId.systemDefault(),
    val locale: Locale = Locale.getDefault(),
    val dormantText: String? = null,
) {
    companion object {
        suspend fun load(
            usage: UsageRepository, plans: PlanRepository, settings: SettingsRepository, clock: Clock,
            locale: Locale = Locale.getDefault(), speedTimeoutMillis: Long = 500L,
        ): WidgetSnapshot = WidgetDataLegacy.load(usage, plans, settings, clock, locale, speedTimeoutMillis)
    }
}

/**
 * Loads [WidgetData] using [graph] at instant [now].
 *
 * Front plan is resolved by [PlanUrgency.front].
 * Dormant plans produce the text "Starts <date>" and no figure.
 */
suspend fun loadWidgetData(
    graph: AppGraph,
    now: Instant,
    permissionOverride: Boolean? = null,
    speedTimeoutMillis: Long = 500L,
): WidgetData {
    val zone = graph.clock.zone
    val perms = graph.permissions.observe().first()
    val permissionGranted = permissionOverride ?: perms.usageAccess

    if (!permissionGranted) {
        return WidgetData(
            todayBytes = 0L,
            plan = null,
            topApps = emptyList(),
            mobileBytes = 0L,
            wifiBytes = 0L,
            dailyBytes = emptyList(),
            permissionGranted = false,
            estimated = false,
            now = now,
            zone = zone
        )
    }

    val todayMobile = graph.usage.observeToday(UsageFilter(network = NetworkKind.MOBILE)).firstOrNull()?.totalBytes ?: 0L
    val todayWifi = graph.usage.observeToday(UsageFilter(network = NetworkKind.WIFI)).firstOrNull()?.totalBytes ?: 0L
    val todayAll = graph.usage.observeToday().firstOrNull()?.totalBytes ?: (todayMobile + todayWifi)

    val activeStates = graph.plans.observeActivePlanStates().firstOrNull() ?: emptyList()
    val frontPlan = PlanUrgency.front(activeStates, now)

    val todayDate = now.atZone(zone).toLocalDate()
    val todayStart = todayDate.atStartOfDay(zone).toInstant()
    val todayEnd = todayDate.plusDays(1).atStartOfDay(zone).toInstant()

    val topApps = graph.usage.observeApps(DateRange(todayStart, todayEnd))
        .firstOrNull()
        ?.sortedByDescending { it.totalBytes }
        ?.take(4) ?: emptyList()

    val weekStart = todayDate.minusDays(6).atStartOfDay(zone).toInstant()
    val dailyPoints = graph.usage.observeSeries(DateRange(weekStart, todayEnd), Granularity.DAY).firstOrNull() ?: emptyList()
    val dailyBytes: List<Long> = dailyPoints.map { it.totalBytes }

    val estimated = frontPlan?.isApproximate ?: false
    val dormantText = if (frontPlan == null) resolveDormantText(graph, now) else null
    val settings = graph.settings.observe().firstOrNull()
    val units = settings?.unitSystem ?: UnitSystem.DECIMAL

    return WidgetData(
        todayBytes = todayAll,
        plan = frontPlan,
        topApps = topApps,
        mobileBytes = todayMobile,
        wifiBytes = todayWifi,
        dailyBytes = dailyBytes,
        permissionGranted = true,
        estimated = estimated,
        units = units,
        now = now,
        zone = zone,
        locale = Locale.getDefault(),
        dormantText = dormantText
    )
}

/**
 * Returns formatted "Starts <date>" text when all plans are dormant, or null.
 */
suspend fun resolveDormantText(graph: AppGraph, now: Instant): String? {
    val activeStates = graph.plans.observeActivePlanStates().firstOrNull() ?: emptyList()
    val dormantOnly = activeStates.filter { it.isDormant }
    if (dormantOnly.isEmpty()) return null

    val earliest = dormantOnly.mapNotNull { it.startsAt }.minOrNull() ?: return "Starts next cycle"
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    return "Starts ${formatter.format(earliest.atZone(graph.clock.zone))}"
}

// -------------------------------------------------------------------------
// Legacy / compatibility snapshot and loader for existing callers
// -------------------------------------------------------------------------

data class WidgetSnapshot(
    val today: DayUsage,
    val plan: PlanState?,
    val forecast: Forecast?,
    val speed: LiveSpeed?,
    val topApps: List<AppUsage>,
    val units: UnitSystem,
    val now: java.time.Instant,
    val zone: ZoneId,
    val locale: Locale,
)

object WidgetDataLegacy {
    suspend fun load(
        usage: UsageRepository, plans: PlanRepository, settings: SettingsRepository, clock: Clock,
        locale: Locale = Locale.getDefault(), speedTimeoutMillis: Long = 500L,
    ): WidgetSnapshot {
        val zone = clock.zone
        val today = usage.observeToday().first()
        val allPlans = plans.observeActivePlanStates().first()
        val plan = PlanUrgency.front(allPlans, clock.instant())
            ?: allPlans.firstOrNull()
        val forecast = plan?.let { plans.observeForecast(it.plan.id).firstOrNull() }
        val speed = withTimeoutOrNull(speedTimeoutMillis) { usage.observeLiveSpeed().firstOrNull() }
        val start = today.date.atStartOfDay(zone).toInstant()
        val end = today.date.plusDays(1).atStartOfDay(zone).toInstant()
        val apps = usage.observeApps(DateRange(start, end)).first().sortedByDescending { it.totalBytes }.take(2)
        return WidgetSnapshot(today, plan, forecast, speed, apps, settings.observe().first().unitSystem, clock.instant(), zone, locale)
    }
}
