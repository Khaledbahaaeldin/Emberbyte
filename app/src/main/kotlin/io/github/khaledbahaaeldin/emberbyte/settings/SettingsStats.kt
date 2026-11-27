package io.github.khaledbahaaeldin.emberbyte.settings

import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class HealthCheckKind {
    BatteryUnrestricted,
    UsageAccess,
    LiveNotification,
    SamplingRecently,
}

enum class HealthAction {
    FixBattery,
    TurnOnLive,
    AllowUsageAccess,
}

data class HealthCheck(
    val kind: HealthCheckKind,
    val passed: Boolean,
)

data class MeasuringHealth(
    val score: Int,
    val checks: List<HealthCheck>,
    val primary: HealthAction?,
    val pausedWithGap: Boolean = false,
)

data class SampleHour(
    val hourStart: Instant,
    val height: Float,
    val isGap: Boolean,
)

enum class SettingsArea {
    LookAndFeel,
    Notifications,
    Alerts,
    Units,
    DataAndBackup,
    KeepMeasuring,
    About,
}

data class SettingEntry(
    val id: String,
    val name: String,
    val area: SettingsArea,
    val areaTitle: String,
    val highlightKey: String = id,
)

enum class HubSwitchId {
    Live,
    Speed,
    Spikes,
    Amoled,
}

data class SwitchUi(
    val id: HubSwitchId,
    val label: String,
    val checked: Boolean,
    val enabled: Boolean,
    val caption: String,
)

data class StorageRowCounts(
    val hourly: Long,
    val daily: Long,
    val plans: Long,
    val other: Long,
)

data class StorageBreakdown(
    val totalBytes: Long,
    val hourlyBytes: Long,
    val dailyBytes: Long,
    val plansBytes: Long,
    val otherBytes: Long,
)

private const val RECENT_SAMPLE_WINDOW_SECONDS = 45L * 60L
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun measuringHealth(
    batteryUnrestricted: Boolean,
    usageAccess: Boolean,
    liveOn: Boolean,
    lastSampleAt: Instant?,
    gapsLast24h: List<io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageGap>,
    now: Instant,
): MeasuringHealth {
    val samplingRecently = lastSampleAt != null && !lastSampleAt.isBefore(now.minusSeconds(RECENT_SAMPLE_WINDOW_SECONDS))
    val checks = listOf(
        HealthCheck(HealthCheckKind.BatteryUnrestricted, batteryUnrestricted),
        HealthCheck(HealthCheckKind.UsageAccess, usageAccess),
        HealthCheck(HealthCheckKind.LiveNotification, liveOn),
        HealthCheck(HealthCheckKind.SamplingRecently, samplingRecently),
    )
    val score = checks.count { it.passed }
    val pausedWithGap = !batteryUnrestricted && gapsLast24h.isNotEmpty()
    val primary = when {
        pausedWithGap -> HealthAction.FixBattery
        !liveOn -> HealthAction.TurnOnLive
        !usageAccess -> HealthAction.AllowUsageAccess
        !batteryUnrestricted -> HealthAction.FixBattery
        else -> null
    }
    return MeasuringHealth(
        score = score,
        checks = checks,
        primary = primary,
        pausedWithGap = pausedWithGap,
    )
}

internal fun sampleStrip(
    coverage: CoverageStatus,
    now: Instant,
    zone: ZoneId,
): List<SampleHour> {
    if (coverage.lastSampleAt == null && coverage.gaps.isEmpty()) return emptyList()
    val currentHour = now.truncatedTo(ChronoUnit.HOURS)
    return (23 downTo 0).map { offset ->
        val hourStart = currentHour.minus(offset.toLong(), ChronoUnit.HOURS)
        val hourEnd = hourStart.plus(1L, ChronoUnit.HOURS)
        val isGap = coverage.gaps.any { gap ->
            gap.from.isBefore(hourEnd) && gap.to.isAfter(hourStart)
        }
        val height = if (isGap) {
            0.22f
        } else {
            val phase = ((24 - offset) * 37) % 10
            0.45f + (phase / 18f)
        }
        SampleHour(hourStart = hourStart, height = height.coerceIn(0.2f, 1f), isGap = isGap)
    }
}

internal fun missedHours(strip: List<SampleHour>): Int = strip.count { it.isGap }

internal fun missedRange(strip: List<SampleHour>, zone: ZoneId): String? {
    val first = strip.firstOrNull { it.isGap } ?: return null
    val last = strip.lastOrNull { it.isGap } ?: return null
    val startText = TIME_FORMAT.format(first.hourStart.atZone(zone))
    val endText = TIME_FORMAT.format(last.hourStart.plus(1L, ChronoUnit.HOURS).atZone(zone))
    return "$startText to $endText"
}

internal fun defaultSettingsSearchIndex(): List<SettingEntry> = listOf(
    SettingEntry("live_notification", "Live notification", SettingsArea.Notifications, "Notifications"),
    SettingEntry("show_speed", "Show speed", SettingsArea.Notifications, "Notifications"),
    SettingEntry("live_updates", "Live updates", SettingsArea.Notifications, "Notifications"),
    SettingEntry("spike_alerts", "Spike alerts", SettingsArea.Alerts, "Alerts"),
    SettingEntry("theme", "Theme and colours", SettingsArea.LookAndFeel, "Look and feel"),
    SettingEntry("theme_mode", "Dark, light or system", SettingsArea.LookAndFeel, "Look and feel"),
    SettingEntry("amoled", "AMOLED black", SettingsArea.LookAndFeel, "Look and feel"),
    SettingEntry("units", "Units", SettingsArea.Units, "Units"),
    SettingEntry("export_csv", "Export CSV", SettingsArea.DataAndBackup, "Data and backup"),
    SettingEntry("export_json", "Export JSON", SettingsArea.DataAndBackup, "Data and backup"),
    SettingEntry("encrypted_backup", "Encrypted backup", SettingsArea.DataAndBackup, "Data and backup"),
    SettingEntry("restore_backup", "Restore backup", SettingsArea.DataAndBackup, "Data and backup"),
    SettingEntry("battery", "Battery unrestricted", SettingsArea.KeepMeasuring, "Keep measuring"),
    SettingEntry("usage_access", "Usage access", SettingsArea.KeepMeasuring, "Keep measuring"),
    SettingEntry("check_updates", "Check for updates", SettingsArea.About, "About"),
    SettingEntry("licences", "Licences", SettingsArea.About, "About"),
    SettingEntry("source_code", "Source code", SettingsArea.About, "About"),
    SettingEntry("copy_debug_log", "Copy debug log", SettingsArea.About, "About"),
)

internal fun searchSettings(index: List<SettingEntry>, query: String): List<SettingEntry> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return index
    return index.filter { entry ->
        entry.name.lowercase().contains(q) || entry.areaTitle.lowercase().contains(q)
    }
}

internal fun switchState(settings: Settings, isDark: Boolean): List<SwitchUi> = listOf(
    SwitchUi(
        id = HubSwitchId.Live,
        label = "Live",
        checked = settings.liveNotificationEnabled,
        enabled = true,
        caption = if (settings.liveNotificationEnabled) "On" else "Off",
    ),
    SwitchUi(
        id = HubSwitchId.Speed,
        label = "Speed",
        checked = settings.notificationShowsSpeed,
        enabled = settings.liveNotificationEnabled,
        caption = when {
            !settings.liveNotificationEnabled -> "Needs live"
            settings.notificationShowsSpeed -> "On"
            else -> "Off"
        },
    ),
    SwitchUi(
        id = HubSwitchId.Spikes,
        label = "Spikes",
        checked = settings.spikeAlertsEnabled,
        enabled = true,
        caption = if (settings.spikeAlertsEnabled) "On" else "Off",
    ),
    SwitchUi(
        id = HubSwitchId.Amoled,
        label = "AMOLED",
        checked = settings.amoledBlack,
        enabled = isDark,
        caption = when {
            !isDark -> "Dark only"
            settings.amoledBlack -> "On"
            else -> "Off"
        },
    ),
)

internal fun passphraseStrength(passphrase: CharArray): Int {
    if (passphrase.size < 8) return 0
    var score = 1
    if (passphrase.size >= 12) score++
    val hasUpper = passphrase.any { it.isUpperCase() }
    val hasLower = passphrase.any { it.isLowerCase() }
    if (hasUpper && hasLower) score++
    val hasDigit = passphrase.any { it.isDigit() }
    val hasSymbol = passphrase.any { !it.isLetterOrDigit() }
    if (hasDigit && hasSymbol) score++
    return score.coerceIn(0, 4)
}

internal fun backupAllowed(passphrase: CharArray): Boolean = passphrase.size >= 8

internal fun storageBreakdown(fileBytes: Long, rowCounts: StorageRowCounts): StorageBreakdown {
    val safeTotal = fileBytes.coerceAtLeast(0L)
    val h = rowCounts.hourly.coerceAtLeast(0L)
    val d = rowCounts.daily.coerceAtLeast(0L)
    val p = rowCounts.plans.coerceAtLeast(0L)
    val o = rowCounts.other.coerceAtLeast(0L)
    val sum = h + d + p + o
    if (safeTotal == 0L || sum == 0L) {
        return StorageBreakdown(safeTotal, 0L, 0L, 0L, safeTotal)
    }
    val hourlyBytes = (safeTotal * h) / sum
    val dailyBytes = (safeTotal * d) / sum
    val plansBytes = (safeTotal * p) / sum
    val otherBytes = (safeTotal - hourlyBytes - dailyBytes - plansBytes).coerceAtLeast(0L)
    return StorageBreakdown(
        totalBytes = safeTotal,
        hourlyBytes = hourlyBytes,
        dailyBytes = dailyBytes,
        plansBytes = plansBytes,
        otherBytes = otherBytes,
    )
}
