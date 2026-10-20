package io.github.khaledbahaaeldin.emberbyte.sampler

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.common.remainingText
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.nav.DeepLinks
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.roundToInt

import android.content.res.Configuration
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.engine.plan.percentUsed
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.toArgb

data class LiveProgress(val fractionUsed: Float, val chip: String)

data class LiveNotificationText(
    val title: String,
    val text: String?,
    val contentDescription: String = "",
    val progress: LiveProgress? = null,
    val state: BudgetStateUi = BudgetStateUi.Ok,
)

fun liveNotificationStateOf(planState: PlanState?): BudgetStateUi {
    if (planState == null) return BudgetStateUi.Ok
    val percent = planState.percentUsed()
    return when {
        percent >= 100 || planState.remainingBytes <= 0L -> BudgetStateUi.Exceeded
        percent >= 80 -> BudgetStateUi.Warning
        else -> BudgetStateUi.Ok
    }
}

fun formatProgressChip(bytes: Long, units: ByteUnits): String {
    val safe = bytes.coerceAtLeast(0L)
    if (safe == 0L) return "0 B"
    var scaled = safe.toDouble()
    var index = 0
    while (index < units.labels.lastIndex && scaled >= units.base) {
        scaled /= units.base
        index++
    }
    if (index == 0) return "$safe ${units.labels[0]}"

    val (valueStr, finalUnit) = if (scaled < 10.0) {
        val r = BigDecimal.valueOf(scaled).setScale(1, RoundingMode.HALF_UP)
        if (r >= BigDecimal(10)) {
            r.setScale(0, RoundingMode.HALF_UP).toPlainString() to units.labels[index]
        } else {
            r.stripTrailingZeros().toPlainString() to units.labels[index]
        }
    } else {
        var r = BigDecimal.valueOf(scaled).setScale(0, RoundingMode.HALF_UP)
        if (index < units.labels.lastIndex && (r >= BigDecimal.valueOf(units.base) || (units == ByteUnits.BINARY && r >= BigDecimal(1000)))) {
            scaled /= units.base
            index++
            val nextUnit = units.labels[index]
            val nextR = BigDecimal.valueOf(scaled).setScale(1, RoundingMode.HALF_UP)
            val formatted = if (nextR >= BigDecimal(10)) {
                nextR.setScale(0, RoundingMode.HALF_UP)
            } else {
                nextR.stripTrailingZeros()
            }
            formatted.toPlainString() to nextUnit
        } else {
            if (index == units.labels.lastIndex && r >= BigDecimal(1000)) {
                r = BigDecimal(999)
            }
            r.toPlainString() to units.labels[index]
        }
    }
    return "$valueStr $finalUnit"
}

fun liveProgressOf(
    planState: PlanState?,
    units: ByteUnits,
    todayBytes: Long,
): LiveProgress? {
    if (planState == null) return null
    val isExhausted = planState.remainingBytes <= 0L
    val fraction = if (isExhausted) {
        1f
    } else {
        val clamped = planState.fractionUsed.coerceIn(0f, 1f)
        (kotlin.math.round(clamped * 1000f) / 1000f)
    }
    val state = liveNotificationStateOf(planState)
    val usedToday = formatProgressChip(todayBytes, units)
    val remaining = if (isExhausted) "0 B" else formatProgressChip(planState.remainingBytes, units)
    val chip = ChipText.forState(state, usedToday, remaining)
    return LiveProgress(fractionUsed = fraction, chip = chip)
}

fun liveNotificationText(
    todayBytes: Long,
    rxBps: Long,
    units: ByteUnits,
    showSpeed: Boolean,
    offline: Boolean = false,
    remainingBytes: Long? = null,
    progress: LiveProgress? = null,
    state: BudgetStateUi = BudgetStateUi.Ok,
): LiveNotificationText {
    val today = formatBytes(todayBytes, units)
    val spokenToday = spokenBytes(todayBytes, units)
    val parts = buildList {
        if (remainingBytes != null) {
            add(remainingText(remainingBytes, units))
        }
        if (showSpeed) {
            val speed = formatBytes(rxBps, units)
            add(if (offline) "No connection" else "↓ ${speed.value} ${speed.unit}/s")
        }
    }
    val spokenParts = buildList {
        add("$spokenToday used today")
        if (remainingBytes != null) {
            add("${spokenBytes(remainingBytes, units)} left")
        }
        if (showSpeed) {
            if (offline) {
                add("No connection")
            } else {
                val speed = formatBytes(rxBps, units)
                val unitSpoken = when (speed.unit) {
                    "GB", "GiB" -> "gigabytes"
                    "MB", "MiB" -> "megabytes"
                    "KB", "KiB" -> "kilobytes"
                    else -> "bytes"
                }
                add("${speed.value} $unitSpoken per second download")
            }
        }
    }
    return LiveNotificationText(
        title = "${today.value} ${today.unit} today",
        text = parts.joinToString(" · ").ifEmpty { null },
        contentDescription = spokenParts.joinToString(". ") + ".",
        progress = progress,
        state = state,
    )
}

enum class LiveNotificationStyle {
    COMPAT,
    PROGRESS_AND_CHIP,
    PROMOTED,
}

fun resolveLiveNotificationStyle(
    sdkInt: Int,
    sdkIntFull: Int,
    hasProgress: Boolean,
): LiveNotificationStyle {
    if (!hasProgress || sdkInt < 36) return LiveNotificationStyle.COMPAT
    return if (sdkInt > 36 || sdkIntFull >= Build.VERSION_CODES_FULL.BAKLAVA_1) {
        LiveNotificationStyle.PROMOTED
    } else {
        LiveNotificationStyle.PROGRESS_AND_CHIP
    }
}

interface PlatformNotificationCaller {
    fun setProgressStyle(builder: Notification.Builder, fractionUsed: Float): Notification.Builder =
        setProgressStyle(builder, fractionUsed, 0)
    fun setProgressStyle(builder: Notification.Builder, fractionUsed: Float, color: Int): Notification.Builder
    fun setShortCriticalText(builder: Notification.Builder, chip: String): Notification.Builder
    fun setRequestPromotedOngoing(builder: Notification.Builder, request: Boolean): Notification.Builder
}

internal object DefaultPlatformNotificationCaller : PlatformNotificationCaller {
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    override fun setProgressStyle(builder: Notification.Builder, fractionUsed: Float, color: Int): Notification.Builder {
        val progressUnits = (fractionUsed * 1000f).roundToInt()
        val segment = Notification.ProgressStyle.Segment(1000).setColor(color)
        val style = Notification.ProgressStyle()
            .setProgress(progressUnits)
            .addProgressSegment(segment)
        return builder.setStyle(style)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    override fun setShortCriticalText(builder: Notification.Builder, chip: String): Notification.Builder {
        return builder.setShortCriticalText(chip)
    }

    // Lint NewApi: "Call requires API level 36.1 (current min is 36): android.app.Notification.Builder#setRequestPromotedOngoing"
    // setRequestPromotedOngoing was finalized in Android 16 QPR2 (API 36.1); @RequiresApi only accepts integer API levels.
    @SuppressLint("NewApi")
    override fun setRequestPromotedOngoing(builder: Notification.Builder, request: Boolean): Notification.Builder {
        return builder.setRequestPromotedOngoing(request)
    }
}

open class LiveNotificationBuilder(
    private val context: Context,
    private val sdkInt: Int = Build.VERSION.SDK_INT,
    private val sdkIntFull: Int = if (Build.VERSION.SDK_INT >= 36) Build.VERSION.SDK_INT_FULL else 0,
    private val platformCaller: PlatformNotificationCaller = DefaultPlatformNotificationCaller,
    private val settingsSupplier: () -> Settings = { Settings() },
    private val isNight: () -> Boolean = {
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    },
    private val promotedGate: PromotedGate = PromotedGate(context, { sdkInt }, { sdkIntFull }),
    private val promotedDismissedOnSupplier: () -> String? = { settingsSupplier().promotedDismissedOn },
    private val todaySupplier: () -> String = { LocalDate.now().toString() },
) {
    private fun resolveActivePalette(): EmberbytePalette {
        val settings = settingsSupplier()
        val dark = when (settings.themeMode) {
            ThemeMode.SYSTEM -> isNight()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        val preset = ThemePresets.firstOrNull { it.id == settings.themeId }
            ?: ThemePresets.first()
        return if (dark) preset.dark else preset.light
    }

    private fun resolveStateColorArgb(palette: EmberbytePalette, state: BudgetStateUi): Int {
        val color = when (state) {
            BudgetStateUi.Ok -> palette.primary
            BudgetStateUi.Warning -> palette.warn
            BudgetStateUi.Exceeded -> palette.danger
        }
        return color.toArgb()
    }

    open fun build(text: LiveNotificationText): Notification {
        val style = resolveLiveNotificationStyle(sdkInt, sdkIntFull, text.progress != null)
        val palette = resolveActivePalette()
        val stateColorArgb = resolveStateColorArgb(palette, text.state)
        if (style == LiveNotificationStyle.COMPAT || text.progress == null) {
            return buildStandard(text, stateColorArgb)
        }
        return buildLiveUpdateNotification(text, text.progress, style, stateColorArgb)
    }

    fun buildStandard(text: LiveNotificationText): Notification {
        val palette = resolveActivePalette()
        val stateColorArgb = resolveStateColorArgb(palette, text.state)
        return buildStandard(text, stateColorArgb)
    }

    fun buildStandard(text: LiveNotificationText, colorArgb: Int): Notification {
        val open = PendingIntent.getActivity(
            context,
            0,
            DeepLinks.intent(context, DeepLinks.HOME),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val deleteIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, PromotedDismissReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_stat_ember)
            .setColor(colorArgb)
            .setContentTitle(text.title)
            .setContentText(text.text)
            .setTicker(text.contentDescription)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(open)
            .setDeleteIntent(deleteIntent)
            .build()
    }

    private fun buildLiveUpdateNotification(
        text: LiveNotificationText,
        progress: LiveProgress,
        style: LiveNotificationStyle,
        colorArgb: Int,
    ): Notification {
        val open = PendingIntent.getActivity(
            context,
            0,
            DeepLinks.intent(context, DeepLinks.HOME),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val deleteIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, PromotedDismissReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = Notification.Builder(context, NotificationChannels.LIVE)
            .setSmallIcon(R.drawable.ic_stat_ember)
            .setColor(colorArgb)
            .setContentTitle(text.title)
            .setContentText(text.text)
            .setTicker(text.contentDescription)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(open)
            .setDeleteIntent(deleteIntent)

        if (Build.VERSION.SDK_INT >= 31) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        platformCaller.setProgressStyle(builder, progress.fractionUsed, colorArgb)
        platformCaller.setShortCriticalText(builder, progress.chip)

        val settings = settingsSupplier()
        val isDismissedToday = promotedDismissedOnSupplier() == todaySupplier()
        val shouldPromote = settings.liveNotificationEnabled &&
            promotedGate.mayPost() &&
            !isDismissedToday

        if (shouldPromote) {
            if (promotedGate.usesExtraForPromotion()) {
                val extras = android.os.Bundle()
                extras.putBoolean("android.requestPromotedOngoing", true)
                builder.addExtras(extras)
            } else {
                platformCaller.setRequestPromotedOngoing(builder, true)
            }
        }
        return builder.build()
    }
}
