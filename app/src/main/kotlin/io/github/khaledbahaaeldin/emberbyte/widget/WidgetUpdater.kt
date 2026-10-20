package io.github.khaledbahaaeldin.emberbyte.widget

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    @VisibleForTesting
    var minIntervalMs: Long = 5_000L

    @VisibleForTesting
    var lastUpdateMs: Long = 0L

    suspend fun request(
        context: Context,
        force: Boolean = false,
        clock: () -> Long = { System.currentTimeMillis() }
    ): Boolean {
        val now = clock()
        if (!force && now - lastUpdateMs < minIntervalMs) {
            return false
        }
        lastUpdateMs = now
        updateAll(context)
        return true
    }

    suspend fun updateAll(context: Context) {
        TodayWidget().updateAll(context)
        PlanWidget().updateAll(context)
        HeroWidget().updateAll(context)
        BentoWidget().updateAll(context)
        DashboardWidget().updateAll(context)
    }
}
