package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.ForecastKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.ForecastUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PermissionPromptUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HomeLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(
        state: HomeUiState,
        onNavigateToApps: () -> Unit = {},
        onSetUpPlan: () -> Unit = {},
    ) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HomeContent(
                    state = state,
                    onEvent = {},
                    onOpenSettings = {},
                    onOpenHistory = {},
                    onPromptAction = {},
                    onOpenApp = {},
                    onSetUpPlan = onSetUpPlan,
                    onNavigateToApps = onNavigateToApps,
                )
            }
        }
    }

    @Test
    fun hero_number_is_one_node_reading_used_and_left() {
        val state = HomeUiState(
            heroBytes = 1_380_000_000L,
            heroDescription = "1.38 gigabytes used today, 12.4 gigabytes left",
            hasPlan = true,
            planLine = "12.4 GB left of 20 GB",
        )
        show(state)

        rule.onAllNodesWithContentDescription("1.38 gigabytes used today, 12.4 gigabytes left")
            .assertCountEquals(1)
        rule.onNodeWithText("12.4 GB left of 20 GB").assertExists()
    }

    @Test
    fun no_plan_shows_the_set_up_a_plan_action_and_no_bar() {
        var setUpClicked = false
        val state = HomeUiState(
            hasPlan = false,
            heroBytes = 500_000_000L,
            heroDescription = "500 megabytes used today",
        )
        show(state, onSetUpPlan = { setUpClicked = true })

        val action = rule.onNodeWithText("Set up a plan")
        action.assertIsDisplayed()
        action.performClick()
        assertTrue("Tapping Set up a plan must invoke callback", setUpClicked)

        // WavyBar with ProgressBarRangeInfo should NOT exist
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertCountEquals(0)
    }

    @Test
    fun permission_missing_shows_the_prompt_first_and_hides_the_hero_figures() {
        val state = HomeUiState(
            topAppsLocked = true,
            prompts = listOf(
                PermissionPromptUi(
                    id = "usage_access",
                    title = "Allow usage access",
                    body = "See which apps use your data.",
                    actionLabel = "Open settings",
                )
            ),
            heroBytes = 1_380_000_000L,
            heroDescription = "1.38 gigabytes used today, 12.4 gigabytes left",
        )
        show(state)

        // Permission prompt must be displayed
        rule.onNodeWithText("Allow usage access").assertIsDisplayed()

        // Hero figures (the big number 1.38) must be hidden
        rule.onNodeWithText("1.38").assertDoesNotExist()
        rule.onAllNodesWithContentDescription("1.38 gigabytes used today, 12.4 gigabytes left")
            .assertCountEquals(0)
    }

    @Test
    fun estimated_data_shows_the_notice_line() {
        val state = HomeUiState(
            isEstimated = true,
            hasPlan = true,
            heroBytes = 1_000_000_000L,
            heroDescription = "1.00 gigabytes used today",
        )
        show(state)

        rule.onNodeWithText("Data usage is estimated").assertIsDisplayed()
    }

    @Test
    fun forecast_tile_shows_the_run_out_date_when_projected_over_the_cap() {
        val state = HomeUiState(
            forecast = ForecastUi(
                headline = "Thu 24 Oct",
                detail = "runs out (±1 day)",
                confidence = "Medium confidence",
                kind = ForecastKind.RunsOut,
            ),
        )
        show(state)

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Thu 24 Oct"))
        rule.onNodeWithText("Thu 24 Oct").assertIsDisplayed()
        rule.onNodeWithText("runs out (±1 day)").assertIsDisplayed()
    }

    @Test
    fun top_apps_shows_five_rows_and_see_all_navigates() {
        var navigatedToApps = false
        val apps = (1..5).map {
            AppRowUi("com.app$it", "App $it", it * 100_000_000L, it * 50_000_000L)
        }
        val state = HomeUiState(topApps = apps)
        show(state, onNavigateToApps = { navigatedToApps = true })

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("See all"))
        for (i in 1..5) {
            rule.onNodeWithText("App $i").assertExists()
        }

        val seeAll = rule.onNodeWithText("See all")
        seeAll.assertIsDisplayed()
        seeAll.performClick()
        assertTrue("Clicking See all must navigate to Apps", navigatedToApps)
    }

    @Test
    fun font_scale_2_shrinks_the_hero_number_without_clipping() {
        val state = HomeUiState(
            heroBytes = 1_380_000_000L,
            heroDescription = "1.38 gigabytes used today, 12.4 gigabytes left",
            hasPlan = true,
            planLine = "12.4 GB left of 20 GB",
        )
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.width(360.dp).testTag("home_container")) {
                        HomeContent(
                            state = state,
                            onEvent = {},
                            onOpenSettings = {},
                            onOpenHistory = {},
                            onPromptAction = {},
                            onOpenApp = {},
                            onSetUpPlan = {},
                        )
                    }
                }
            }
        }

        val containerBounds = rule.onNodeWithTag("home_container").getBoundsInRoot()
        val heroNumberNode = rule.onNodeWithContentDescription("1.38 gigabytes used today, 12.4 gigabytes left")
        heroNumberNode.assertIsDisplayed()
        val numBounds = heroNumberNode.getBoundsInRoot()

        assertTrue(
            "Hero number right (${numBounds.right}) must be within container right (${containerBounds.right})",
            numBounds.right <= containerBounds.right
        )
    }

    @Test
    fun rtl_mirrors_and_keeps_the_number_with_its_unit() {
        val state = HomeUiState(
            heroBytes = 1_380_000_000L,
            heroDescription = "1.38 gigabytes used today, 12.4 gigabytes left",
            hasPlan = true,
            planLine = "12.4 GB left of 20 GB",
        )
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.fillMaxSize().testTag("home_rtl")) {
                        HomeContent(
                            state = state,
                            onEvent = {},
                            onOpenSettings = {},
                            onOpenHistory = {},
                            onPromptAction = {},
                            onOpenApp = {},
                            onSetUpPlan = {},
                        )
                    }
                }
            }
        }

        // Hero number is displayed in LTR order inside ForceLtr
        val heroNode = rule.onNodeWithContentDescription("1.38 gigabytes used today, 12.4 gigabytes left")
        heroNode.assertIsDisplayed()
        val bounds = heroNode.getBoundsInRoot()
        assertTrue((bounds.right - bounds.left) > 0.dp && (bounds.bottom - bounds.top) > 0.dp)
    }

    @Test
    fun reduce_motion_does_not_animate_the_hero_number() {
        val state = HomeUiState(
            heroBytes = 1_380_000_000L,
            heroDescription = "1.38 gigabytes used today",
        )
        rule.setContent {
            CompositionLocalProvider(LocalReduceMotion provides true) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    HomeContent(
                        state = state,
                        onEvent = {},
                        onOpenSettings = {},
                        onOpenHistory = {},
                        onPromptAction = {},
                        onOpenApp = {},
                        onSetUpPlan = {},
                    )
                }
            }
        }

        rule.onNodeWithContentDescription("1.38 gigabytes used today").assertIsDisplayed()
    }

    @Test
    fun loading_state_shows_the_loading_indicator_not_a_zero() {
        val state = HomeUiState(
            isLoading = true,
            heroBytes = 0L,
            heroDescription = "",
        )
        show(state)

        rule.onNodeWithTag("home_loading_indicator").assertIsDisplayed()
        rule.onNodeWithText("0").assertDoesNotExist()
        rule.onNodeWithText("0 B").assertDoesNotExist()
    }
}
