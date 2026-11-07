@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

/**
 * Replays the app's tab navigation with the exact NavOptions of the navbar and the production transition lambdas.
 * Proves the finding behind task T2: Navigation reports Apps -> Home as a pop, and the app must still use the
 * tab transition for it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TabNavigationProbeTest {
    @get:Rule val rule = createComposeRule()

    private data class Decision(val lambda: String, val kind: String, val from: String?, val to: String?)

    private val decisions = mutableListOf<Decision>()
    private lateinit var nav: NavHostController

    private fun setUp() {
        val t = AppTransitions(
            motion = MotionScheme.expressive(),
            reduceMotion = false,
            layoutDirection = LayoutDirection.Ltr,
            onTransition = { lambda, kind, from, to -> decisions += Decision(lambda, kind, from, to) },
        )
        rule.setContent {
            nav = rememberNavController()
            NavHost(
                navController = nav,
                startDestination = "home",
                enterTransition = t.enter,
                exitTransition = t.exit,
                popEnterTransition = t.popEnter,
                popExitTransition = t.popExit,
            ) {
                composable("home") { Text("home") }
                composable("apps") { Text("apps") }
                composable("plans") { Text("plans") }
                composable("plans/new") { Text("new") }
                composable(
                    "apps/{packageName}",
                    arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
                ) { Text("detail") }
            }
        }
        rule.waitForIdle()
    }

    private fun tab(route: String) = rule.runOnUiThread { nav.navigateToTab(Destination.fromId(route)) }

    private fun route(): String? {
        var r: String? = null
        rule.runOnUiThread { r = nav.currentBackStackEntry?.destination?.route }
        return r
    }

    @Test fun navigation_reports_apps_to_home_as_a_pop() {
        setUp()
        tab("apps"); rule.waitForIdle()
        decisions.clear()
        tab("home"); rule.waitForIdle()
        assertEquals("home", route())
        assertTrue(
            "Apps -> Home is expected to reach the pop lambdas (the root cause of the back animation): $decisions",
            decisions.any { it.lambda == "popEnter" || it.lambda == "popExit" },
        )
    }

    @Test fun every_tab_pair_uses_the_tab_transition_and_lands_on_the_tapped_tab() {
        setUp()
        val tabs = listOf("home", "apps", "plans")
        for (from in tabs) for (to in tabs) {
            if (from == to) continue
            tab(from); rule.waitForIdle()
            decisions.clear()
            tab(to); rule.waitForIdle()
            assertEquals("$from -> $to must land on $to", to, route())
            assertTrue("$from -> $to produced no transition decision", decisions.isNotEmpty())
            assertTrue(
                "$from -> $to used a non-tab transition: $decisions",
                decisions.all { it.kind == "tab" },
            )
        }
    }

    @Test fun random_tab_sequences_always_land_on_the_tapped_tab() {
        setUp()
        val tabs = listOf("home", "apps", "plans")
        val failures = mutableListOf<String>()
        for (seed in 1..6) {
            val rnd = Random(seed)
            val steps = mutableListOf<String>()
            rule.mainClock.autoAdvance = false
            repeat(80) {
                val last: String? = when (rnd.nextInt(10)) {
                    in 0..5 -> { val t = tabs[rnd.nextInt(3)]; steps += "tab:$t"; tab(t); t }
                    6 -> { val t = tabs[rnd.nextInt(3)]; steps += "deepTab:$t"; tab(t); t }
                    7 -> {
                        steps += "plansNewThenBack"
                        rule.runOnUiThread { if (nav.currentBackStackEntry?.destination?.route == "plans") nav.navigate("plans/new") }
                        rule.mainClock.advanceTimeBy(rnd.nextLong(0, 400))
                        rule.runOnUiThread { if (nav.currentBackStackEntry?.destination?.route == "plans/new") nav.popBackStack() }
                        null
                    }
                    8 -> {
                        steps += "detailThenBack"
                        rule.runOnUiThread { if (nav.currentBackStackEntry?.destination?.route == "apps") nav.navigate("apps/com.x") }
                        rule.mainClock.advanceTimeBy(rnd.nextLong(0, 400))
                        rule.runOnUiThread { if (nav.currentBackStackEntry?.destination?.route == "apps/{packageName}") nav.popBackStack() }
                        null
                    }
                    else -> { steps += "wait"; null }
                }
                rule.mainClock.advanceTimeBy(rnd.nextLong(0, 450))
                if (last != null) {
                    val got = route()
                    if (got != last) failures += "seed=$seed after ${steps.joinToString(" > ")} wanted=$last got=$got"
                }
            }
            rule.mainClock.autoAdvance = true
            rule.waitForIdle()
        }
        assertTrue("Tab taps that did not land on the tapped tab:\n${failures.take(5).joinToString("\n")}", failures.isEmpty())
    }
}
