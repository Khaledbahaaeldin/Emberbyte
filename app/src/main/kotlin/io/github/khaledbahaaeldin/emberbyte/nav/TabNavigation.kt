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
private const val MAX_SUB_SCREENS = 8

internal fun NavController.navigateToTab(destination: Destination) {
    // Pop every sub-screen above the topmost tab (the guard keeps a malformed stack from looping).
    var pops = 0
    while (!isTabRoute(currentBackStackEntry?.destination?.route) && pops < MAX_SUB_SCREENS && popBackStack()) {
        pops++
    }
    navigate(destination.route) {
        popUpTo(Destination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
