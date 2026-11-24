package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.annotation.StringRes
import io.github.khaledbahaaeldin.emberbyte.R
import kotlin.math.abs

internal const val ONBOARDING_PAGE_COUNT = 3

internal enum class PermissionAction {
    Usage,
    Notifications,
    Phone,
    Battery,
    Done,
}

/** What the big button of a page does. The label comes from [ctaLabelRes]; the screen maps it to an action. */
internal enum class OnboardingCta {
    Start,
    Usage,
    Notifications,
    Phone,
    Battery,
    Continue,
    SetUpPlan,
    Done,
}

internal fun nextPermissionAction(
    usageAccess: Boolean,
    notifications: Boolean,
    phoneState: Boolean = true,
    batteryUnrestricted: Boolean = true,
): PermissionAction = when {
    !usageAccess -> PermissionAction.Usage
    !notifications -> PermissionAction.Notifications
    !phoneState -> PermissionAction.Phone
    !batteryUnrestricted -> PermissionAction.Battery
    else -> PermissionAction.Done
}

internal fun ctaFor(
    page: Int,
    usageAccess: Boolean,
    notifications: Boolean,
    phoneState: Boolean = true,
    batteryUnrestricted: Boolean = true,
    hasPlan: Boolean = false,
): OnboardingCta = when (page) {
    0 -> OnboardingCta.Start
    1 -> when (nextPermissionAction(usageAccess, notifications, phoneState, batteryUnrestricted)) {
        PermissionAction.Usage -> OnboardingCta.Usage
        PermissionAction.Notifications -> OnboardingCta.Notifications
        PermissionAction.Phone -> OnboardingCta.Phone
        PermissionAction.Battery -> OnboardingCta.Battery
        PermissionAction.Done -> OnboardingCta.Continue
    }
    else -> if (hasPlan) OnboardingCta.Done else OnboardingCta.SetUpPlan
}

@StringRes
internal fun ctaLabelRes(cta: OnboardingCta): Int = when (cta) {
    OnboardingCta.Start -> R.string.onb_cta_start
    OnboardingCta.Usage -> R.string.onb_cta_usage
    OnboardingCta.Notifications -> R.string.onb_cta_notifications
    OnboardingCta.Phone -> R.string.onb_cta_phone
    OnboardingCta.Battery -> R.string.onb_cta_battery
    OnboardingCta.Continue -> R.string.onb_cta_continue
    OnboardingCta.SetUpPlan -> R.string.onb_cta_set_up
    OnboardingCta.Done -> R.string.onb_cta_done
}

/** The quiet text button under the big one, or null when the page has none. */
internal enum class OnboardingQuiet { NotNow, NotNowHome }

internal fun quietFor(page: Int, allPermissionsDone: Boolean, hasPlan: Boolean): OnboardingQuiet? = when (page) {
    1 -> if (allPermissionsDone) null else OnboardingQuiet.NotNow
    2 -> if (hasPlan) null else OnboardingQuiet.NotNowHome
    else -> null
}

@StringRes
internal fun quietLabelRes(quiet: OnboardingQuiet): Int = when (quiet) {
    OnboardingQuiet.NotNow -> R.string.onb_not_now
    OnboardingQuiet.NotNowHome -> R.string.onb_not_now_home
}

/** The top-right Skip leaves the whole intro, so it only shows on the first two pages. */
internal fun showsSkip(page: Int): Boolean = page < ONBOARDING_PAGE_COUNT - 1

internal fun pillWidthDp(index: Int, position: Float): Float {
    val dist = abs(position - index).coerceIn(0f, 1f)
    return 8f + (28f - 8f) * (1f - dist)
}

internal fun parallaxOffset(d: Float, widthPx: Float, isOutgoing: Boolean): Float =
    if (isOutgoing) d * widthPx * 0.3f else d * widthPx

internal fun pageAlpha(d: Float): Float =
    (1f - 1.15f * abs(d)).coerceIn(0f, 1f)

internal fun revealFraction(q: Float, index: Int): Float =
    ((q - 0.22f - 0.07f * index) / 0.45f).coerceIn(0f, 1f)
