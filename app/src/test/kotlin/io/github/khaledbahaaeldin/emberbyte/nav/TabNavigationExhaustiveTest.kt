package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Exhaustive search over every short sequence of navbar taps, deep links, sub-screen opens and back presses:
 * a request for a tab must always land on that tab. Found two real bugs (task T2):
 *  - a deep link or widget tab request while a sub-screen is open saved the sub-screen into the tab's state, so the
 *    next tap on that tab restored the sub-screen instead of the tab;
 *  - Home's "see all" opened Apps with a plain navigate, and the following Home tap landed on Apps.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TabNavigationExhaustiveTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var nav: NavHostController
    private val epoch = mutableIntStateOf(0)

    private fun route(): String? {
        var r: String? = null
        rule.runOnUiThread { r = nav.currentBackStackEntry?.destination?.route }
        return r
    }

    private fun reset() {
        rule.runOnUiThread { epoch.intValue = epoch.intValue + 1 }
        rule.waitForIdle()
    }

    private val actions = listOf("navHome", "navApps", "navPlans", "deepHome", "deepApps", "deepPlans", "openDetail", "openNew", "back")

    /** Null when the action is not possible in the current state (as in production); else the tab it requested, or "". */
    private fun apply(a: String): String? {
        val cur = route()
        val isTab = isTabRoute(cur)
        return when (a) {
            "navHome", "navApps", "navPlans" -> if (!isTab) null else { val t = a.removePrefix("nav").lowercase(); rule.runOnUiThread { nav.navigateToTab(Destination.fromId(t)) }; t }
            "deepHome", "deepApps", "deepPlans" -> { val t = a.removePrefix("deep").lowercase(); rule.runOnUiThread { nav.navigateToTab(Destination.fromId(t)) }; t }
            "openDetail" -> if (cur != "apps" && cur != "home") null else { rule.runOnUiThread { nav.navigate("apps/com.x") }; "" }
            "openNew" -> if (cur != "plans") null else { rule.runOnUiThread { nav.navigate("plans/new") }; "" }
            "back" -> if (cur == "home") null else { rule.runOnUiThread { nav.popBackStack() }; "" }
            else -> null
        }
    }

    @Test fun every_short_sequence_lands_on_the_requested_tab() {
        rule.setContent {
            key(epoch.intValue) {
                nav = rememberNavController()
                NavHost(nav, "home") {
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
        }
        rule.waitForIdle()
        val failures = linkedSetOf<String>()
        fun dfs(prefix: List<String>, depth: Int) {
            if (depth == 0) return
            for (a in actions) {
                reset()
                val seq = prefix + a
                var ok = true
                var failed = false
                for ((i, s) in seq.withIndex()) {
                    val want = apply(s)
                    if (want == null) { ok = false; break }
                    rule.waitForIdle()
                    if (want.isNotEmpty() && route() != want) { if (i == seq.lastIndex) failed = true; ok = false; break }
                }
                if (failed) { failures += seq.joinToString(" > ") + " => landed on ${route()}"; continue }
                if (ok) dfs(seq, depth - 1)
            }
        }
        dfs(emptyList(), 3)
        assertTrue("Tab requests that did not land on the tab:\n${failures.take(10).joinToString("\n")}", failures.isEmpty())
    }

    @Test fun tabs_are_only_opened_through_navigateToTab() {
        val app = File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/EmberbyteApp.kt").readText()
        val plain = Regex("""navController\.navigate\(Destination\.[A-Za-z]+\.route\s*\)(?!\s*\{)""")
        assertTrue("EmberbyteApp must open tabs with navigateToTab, not a plain navigate", !plain.containsMatchIn(app))
        val deepLinks = File("src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/nav/DeepLinkNavigation.kt").readText()
        assertTrue("Deep links must open tabs with navigateToTab", deepLinks.contains("navigateToTab"))
    }
}
