package io.github.khaledbahaaeldin.emberbyte.sampler

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri

class PromotedGate(
    private val context: Context,
    private val sdkInt: () -> Int = { Build.VERSION.SDK_INT },
    private val sdkIntFull: () -> Int = { if (Build.VERSION.SDK_INT >= 36) Build.VERSION.SDK_INT_FULL else 0 },
    private val canPostProvider: (() -> Boolean)? = null,
) {
    companion object {
        const val ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS = "android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS"
    }

    fun supportsPromotion(): Boolean = sdkInt() >= 36

    fun usesExtraForPromotion(): Boolean {
        if (sdkInt() < 36) return false
        if (sdkInt() > 36) return false
        return sdkIntFull() < 3600001
    }

    fun mayPost(): Boolean {
        if (!supportsPromotion()) return false
        if (canPostProvider != null) return canPostProvider.invoke()
        val currentSdk = sdkInt()
        return if (currentSdk >= 36) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (nm != null && Build.VERSION.SDK_INT >= 36) {
                runCatching {
                    val method = NotificationManager::class.java.getMethod("canPostPromotedNotifications")
                    method.invoke(nm) as? Boolean ?: true
                }.getOrDefault(true)
            } else {
                true
            }
        } else {
            false
        }
    }

    fun settingsIntent(): Intent {
        return if (supportsPromotion()) {
            Intent(ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS)
                .setData("package:${context.packageName}".toUri())
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }
}
