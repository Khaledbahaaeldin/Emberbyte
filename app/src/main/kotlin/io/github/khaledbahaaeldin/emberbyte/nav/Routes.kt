package io.github.khaledbahaaeldin.emberbyte.nav

import android.net.Uri

object Routes {
    const val ONBOARDING = "onboarding"
    const val HISTORY = "history"
    /** History with an optional day (0 = oldest of the Home strip, 6 = today) to open on. */
    const val HISTORY_PATTERN = "history?day={day}"
    const val HOME_DAY_RESULT = "home_day"
    const val SETTINGS = "settings"
    const val APPEARANCE = "settings/appearance"
    const val LICENSES = "licenses"
    const val LICENCES = "settings/licences"
    const val APP_DETAIL = "apps/{packageName}"
    const val PLAN_NEW = "plans/new"
    const val PLAN_EDITOR = "plans/{planId}"
    fun appDetail(packageName: String): String = "apps/" + Uri.encode(packageName)
    fun planEditor(planId: Long): String = "plans/$planId"
    fun historyAt(day: Int): String = "history?day=$day"
}
