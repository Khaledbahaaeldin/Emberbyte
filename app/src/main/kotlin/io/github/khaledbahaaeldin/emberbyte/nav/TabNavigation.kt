package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.navigation.NavController

/**
 * The only way the app switches to a navbar tab (navbar tap, deep link, widget, Home's "see all").
 *
 * Sub-screens above the topmost tab are popped first. Otherwise `saveState = true` would save them together with
 * the tab, and the next tap on that tab would restore the sub-screen (for example a half-filled new plan) instead of
 * the tab itself. A plain `navigate` to a tab would instead leave a duplicate tab entry that later tab taps pop and
 * restore wrongly (found by TabNavigationExhaustiveTest).
 */
internal fun NavController.navigateToTab(destination: Destination) {
    val topTab = currentBackStack.value.lastOrNull { isTabRoute(it.destination.route) }?.destination?.route
    if (topTab != null && currentBackStackEntry?.destination?.route != topTab) {
        popBackStack(topTab, inclusive = false)
    }
    navigate(destination.route) {
        popUpTo(Destination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
