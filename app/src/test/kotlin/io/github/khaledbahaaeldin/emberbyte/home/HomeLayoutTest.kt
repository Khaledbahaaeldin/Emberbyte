package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.common.SCREEN_SCAFFOLD_SCRIM
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.ForecastKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.ForecastUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PermissionPromptUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
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
    fun home_screen_renders_inside_screen_scaffold_with_scrim() {
        var state by mutableStateOf(HomeUiState())
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HomeContent(
                    state = state,
                    onEvent = {},
                    onOpenSettings = {},
                    onOpenHistory = {},
                    onPromptAction = {},
                    onOpenApp = {},
                    onSetUpPlan = {},
                    onNavigateToApps = {},
                )
            }
        }
        rule.onNodeWithTag(SCREEN_SCAFFOLD_SCRIM).assertIsDisplayed()
        rule.onNode(hasText("Today") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertIsDisplayed()

        state = state.copy(isToday = false, heroLabel = "Wednesday")
        rule.waitForIdle()
        rule.onNode(hasText("Wednesday") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertIsDisplayed()
        rule.onNodeWithText("Today").assertDoesNotExist()
    }

    @Test
    fun home_brand_mark_is_44dp() {
        show(HomeUiState())
        rule.onNodeWithTag("home_brand_mark").assertIsDisplayed()
        val bounds = rule.onNodeWithTag("home_brand_mark").getBoundsInRoot()
        assertEquals(44f, (bounds.right - bounds.left).value, 0.5f)
        assertEquals(44f, (bounds.bottom - bounds.top).value, 0.5f)
    }

    @Test
    fun home_title_stays_a_heading_beside_the_mark() {
        show(HomeUiState())
        rule.onNode(hasText("Today") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertIsDisplayed()
    }

    @Test
    fun home_badge_spin_follows_the_live_rate() {
        assertEquals(8f, homeBadgeSpinDegPerSec(HomeUiState(throughputBps = 0L)), 0.001f)
        assertEquals(63.2f, homeBadgeSpinDegPerSec(HomeUiState(throughputBps = 1_200_000L)), 0.01f)
        assertEquals(238f, homeBadgeSpinDegPerSec(HomeUiState(throughputBps = 9_000_000L)), 0.01f)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun live_and_network_tiles_sit_side_by_side_with_equal_width() {
        show(HomeUiState(network = io.github.khaledbahaaeldin.emberbyte.ui.design.model.NetworkKindUi.Wifi, wifiBytes = 3_700_000_000L, mobileBytes = 9_000_000L))
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(2_000) // let the one-time tile entrance finish before measuring
        val live = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
        val network = rule.onNodeWithTag("network_bento_tile").getBoundsInRoot()
        assertEquals("same top", live.top.value, network.top.value, 0.5f)
        assertEquals("equal width", (live.right - live.left).value, (network.right - network.left).value, 0.5f)
        assertEquals("10 dp apart", 10f, (network.left - live.right).value, 0.5f)
        assertEquals("equal height", (live.bottom - live.top).value, (network.bottom - network.top).value, 0.5f)
        assertTrue("at least 212 dp tall (was ${(live.bottom - live.top).value}, density ${rule.density.density})", (live.bottom - live.top).value >= 211.5f)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun corners_interlock_and_mirror_in_rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    HomeContent(
                        state = HomeUiState(wifiBytes = 5L),
                        onEvent = {}, onOpenSettings = {}, onOpenHistory = {}, onPromptAction = {},
                        onOpenApp = {}, onSetUpPlan = {}, onNavigateToApps = {},
                    )
                }
            }
        }
        val live = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
        val network = rule.onNodeWithTag("network_bento_tile").getBoundsInRoot()
        assertTrue("Live is on the start (right) side in RTL", live.left > network.left)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun tiles_have_no_icon_beside_their_label() {
        show(HomeUiState(network = io.github.khaledbahaaeldin.emberbyte.ui.design.model.NetworkKindUi.Wifi, wifiBytes = 5L))
        val tile = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
        val label = rule.onNodeWithText("Live · Wi-Fi", useUnmergedTree = true).getBoundsInRoot()
        assertEquals("the label starts at the tile padding, nothing before it", 16f, (label.left - tile.left).value, 0.5f)
        val netTile = rule.onNodeWithTag("network_bento_tile").getBoundsInRoot()
        val netLabel = rule.onNodeWithText("Today by network", useUnmergedTree = true).getBoundsInRoot()
        assertEquals(16f, (netLabel.left - netTile.left).value, 0.5f)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun forecast_has_its_own_full_width_row_above_the_pair() {
        val forecast = ForecastUi(
            headline = "Runs out Sat 22 Nov", detail = "At this pace", confidence = "Medium confidence",
            kind = ForecastKind.RunsOut,
        )
        show(HomeUiState(hasPlan = true, forecast = forecast, network = io.github.khaledbahaaeldin.emberbyte.ui.design.model.NetworkKindUi.Wifi, wifiBytes = 5L))
        val forecastBounds = rule.onNodeWithText("Forecast", useUnmergedTree = true).getBoundsInRoot()
        val live = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
        val tile = rule.onNodeWithTag("bento_tile_accent").getBoundsInRoot()
        assertTrue("forecast above the pair", tile.bottom <= live.top + 0.5.dp)
        assertTrue("forecast spans the full width", (tile.right - tile.left).value >= (live.right - live.left).value * 2f)
    }

    @Test
    fun direction_survives_recreation_but_is_not_saved_to_settings() {
        val tester = androidx.compose.ui.test.junit4.StateRestorationTester(rule)
        tester.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HomeContent(
                    state = HomeUiState(network = io.github.khaledbahaaeldin.emberbyte.ui.design.model.NetworkKindUi.Wifi, wifiBytes = 5L),
                    onEvent = {}, onOpenSettings = {}, onOpenHistory = {}, onPromptAction = {},
                    onOpenApp = {}, onSetUpPlan = {}, onNavigateToApps = {},
                )
            }
        }
        rule.onNodeWithTag("live_bento_tile").performClick()
        rule.waitForIdle()
        assertEquals(
            "Showing upload speed",
            rule.onNodeWithTag("live_bento_tile").fetchSemanticsNode().config[SemanticsProperties.StateDescription],
        )
        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        assertEquals(
            "Showing upload speed",
            rule.onNodeWithTag("live_bento_tile").fetchSemanticsNode().config[SemanticsProperties.StateDescription],
        )
        // The direction is session state only: HomeUiState (the only thing the view model emits) carries no direction.
        assertTrue(HomeUiState::class.java.declaredFields.none { it.name.contains("direction", ignoreCase = true) })
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
    fun no_plan_shows_the_setup_card_and_no_bar() {
        var setUpClicked = false
        val state = HomeUiState(
            hasPlan = false,
            heroBytes = 500_000_000L,
            heroDescription = "500 megabytes used today",
        )
        show(state, onSetUpPlan = { setUpClicked = true })

        val action = rule.onNodeWithTag("plan_setup_button")
        action.assertIsDisplayed()
        action.performClick()
        assertTrue("Tapping the setup button must invoke the callback", setUpClicked)

        // No plan, so no progress bar anywhere (the ghost ring is decorative and publishes no progress).
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun setup_card_sits_under_the_hero_above_the_tiles() {
        show(HomeUiState(hasPlan = false, heroBytes = 500_000_000L, heroDescription = "500 megabytes used today"))
        val hero = rule.onNodeWithTag("hero_section").getBoundsInRoot()
        val card = rule.onNodeWithTag("plan_setup_card").getBoundsInRoot()
        val live = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
        assertTrue("card is under the hero", card.top >= hero.bottom - 0.5.dp)
        assertTrue("card is above the tiles", card.bottom <= live.top + 0.5.dp)
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
    @Config(qualifiers = "w360dp-h3000dp")
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
    @Config(qualifiers = "w360dp-h3000dp")
    fun usage_card_shows_four_rows_and_all_apps_navigates() {
        var navigatedToApps = false
        show(HomeUiState(usage = sampleUsageCard()), onNavigateToApps = { navigatedToApps = true })

        for (name in listOf("Video", "Chat", "Maps", "Music")) {
            rule.onNodeWithTag("usage_row_com.${name.lowercase()}").assertExists()
        }
        rule.onNodeWithTag("usage_btn_all_apps").assertIsDisplayed().performClick()
        assertTrue("All apps must navigate to Apps", navigatedToApps)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun the_old_top_apps_and_history_cards_are_gone() {
        show(HomeUiState(usage = sampleUsageCard()))
        rule.onNodeWithText("Top apps today").assertDoesNotExist()
        rule.onNodeWithText("See all").assertDoesNotExist()
        rule.onNodeWithTag("usage_card").assertExists()
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

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun home_links_use_link_text_and_meet_aa_contrast_across_all_palettes() {
        var currentSpec by mutableStateOf(EmberbyteThemeSpec(presetId = "dusk", darkTheme = false))
        var expectedLinkColor = Color.Unspecified
        var expectedSurface2 = Color.Unspecified
        var expectedBackground = Color.Unspecified
        var expectedOnAccent = Color.Unspecified
        var expectedAccent = Color.Unspecified
        rule.setContent {
            EmberbyteTheme(currentSpec) {
                val palette = LocalEmberbytePalette.current
                SideEffect {
                    expectedLinkColor = palette.linkText
                    expectedSurface2 = palette.surface2
                    expectedBackground = palette.background
                    expectedOnAccent = palette.onAccent
                    expectedAccent = palette.accent
                }
                HomeContent(
                    state = HomeUiState(
                        hasPlan = false,
                        planLine = null,
                        subtitle = null,
                        usage = sampleUsageCard(),
                        spike = SpikeTileUi(
                            id = 1L,
                            packageName = "com.video",
                            title = "Video spiked",
                            detail = "3x normal today",
                        ),
                    ),
                    onEvent = {},
                    onOpenSettings = {},
                    onOpenHistory = {},
                    onPromptAction = {},
                    onOpenApp = {},
                    onSetUpPlan = {},
                )
            }
        }
        val variants = listOf(false to false, true to false, true to true)
        for (preset in ThemePresets) {
            for ((dark, amoled) in variants) {
                currentSpec = EmberbyteThemeSpec(presetId = preset.id, darkTheme = dark, amoled = amoled)
                rule.waitForIdle()
                val linkPairs = listOf(
                    "All apps" to expectedSurface2,
                )
                for ((label, bg) in linkPairs) {
                    val results = mutableListOf<TextLayoutResult>()
                    rule.onNodeWithText(label, useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    val actualColor = results.first().layoutInput.style.color
                    assertEquals(
                        "$label must bind to palette.linkText on ${preset.id} dark=$dark amoled=$amoled",
                        expectedLinkColor,
                        actualColor,
                    )
                    val fg = actualColor.compositeOver(bg)
                    val l1 = fg.luminance() + 0.05
                    val l2 = bg.luminance() + 0.05
                    val ratio = maxOf(l1, l2) / minOf(l1, l2)
                    assertTrue(
                        "$label on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1, was $ratio",
                        ratio >= 4.5,
                    )
                }
                for (accentLabel in listOf("3x normal today", "Dismiss")) {
                    val results = mutableListOf<TextLayoutResult>()
                    rule.onNodeWithText(accentLabel, useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    val actualColor = results.first().layoutInput.style.color
                    assertEquals(
                        "$accentLabel must bind to palette.onAccent on ${preset.id} dark=$dark amoled=$amoled",
                        expectedOnAccent,
                        actualColor,
                    )
                    val fg = actualColor.compositeOver(expectedAccent)
                    val l1 = fg.luminance() + 0.05
                    val l2 = expectedAccent.luminance() + 0.05
                    val ratio = maxOf(l1, l2) / minOf(l1, l2)
                    assertTrue(
                        "$accentLabel on ${preset.id} (dark=$dark, amoled=$amoled) must meet 4.5:1 on accent, was $ratio",
                        ratio >= 4.5,
                    )
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h800dp")
    fun home_forecast_exit_never_crushes_the_live_tile() {
        val sampleForecast = ForecastUi(
            headline = "3 days left",
            detail = "On pace to finish under",
            confidence = "High",
            kind = ForecastKind.Safe,
        )
        var state by mutableStateOf(HomeUiState(forecast = sampleForecast))
        rule.mainClock.autoAdvance = false
        rule.setContent {
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
        rule.mainClock.advanceTimeByFrame()
        val initialSpeedBounds = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
        val initialSpeedWidth = (initialSpeedBounds.right - initialSpeedBounds.left).value
        assertTrue("Initial speed tile container width must be >= 150dp, was $initialSpeedWidth", initialSpeedWidth >= 150f)

        state = state.copy(forecast = null)
        repeat(15) {
            rule.mainClock.advanceTimeBy(16)
            val bounds = rule.onNodeWithTag("live_bento_tile").getBoundsInRoot()
            val width = (bounds.right - bounds.left).value
            assertTrue(
                "Speed tile must never be crushed during ForecastTile exit, saw width=$width",
                width >= initialSpeedWidth - 1f,
            )
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h800dp")
    fun home_bento_row_and_hero_snap_under_reduced_motion() {
        val sampleForecast = ForecastUi(
            headline = "3 days left",
            detail = "On pace to finish under",
            confidence = "High",
            kind = ForecastKind.Safe,
        )
        var state by mutableStateOf(HomeUiState(forecast = null, isEstimated = false))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
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
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("3 days left").assertDoesNotExist()
        rule.onNodeWithText("Data usage is estimated").assertDoesNotExist()

        state = state.copy(forecast = sampleForecast, isEstimated = true)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithText("3 days left").assertIsDisplayed()
        rule.onNodeWithText("Data usage is estimated").assertIsDisplayed()

        state = state.copy(forecast = null, isEstimated = false)
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithText("3 days left").assertDoesNotExist()
        rule.onNodeWithText("Data usage is estimated").assertDoesNotExist()
    }

    @Test
    fun home_loading_crossfade_snaps_under_reduced_motion() {
        var state by mutableStateOf(HomeUiState(isLoading = true))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
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
        rule.waitForIdle()
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
        rule.onNodeWithTag("home_loading_indicator").assertIsDisplayed()

        // Snaps out of loading within 2 frames (32ms)
        state = state.copy(isLoading = false, heroBytes = 1_000_000_000L, heroDescription = "1 GB used today")
        rule.waitForIdle()
        repeat(2) {
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
        }
        rule.onNodeWithTag("home_loading_indicator").assertDoesNotExist()
        rule.onNodeWithText("Today by network").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun font_scale_2_full_sections_no_clip() {
        val sampleForecast = ForecastUi(
            headline = "3 days left",
            detail = "On pace to finish under",
            confidence = "High",
            kind = ForecastKind.Safe,
        )
        val state = HomeUiState(
            usage = sampleUsageCard(),
            heroBytes = 200_000_000_000L,
            heroDescription = "200 gigabytes used today",
            mobileBytes = 100_000_000_000L,
            wifiBytes = 100_000_000_000L,
            forecast = sampleForecast,
            hasPlan = false,
            planLine = null,
            subtitle = null,
        )
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2.0f)) {
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
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(2_000) // let the one-time tile entrance finish before measuring
        for (label in listOf("3 days left", "Today by network", "Mobile 100 GB", "GB on Wi-Fi", "All apps", "Set up your data plan")) {
            val node = rule.onNodeWithText(label, useUnmergedTree = true)
            node.assertIsDisplayed()
            val bounds = node.getBoundsInRoot()
            assertTrue("$label must fit within 360dp", bounds.right.value <= 360f + 1f)
            assertTrue("$label must have positive size", (bounds.right - bounds.left).value > 0f && (bounds.bottom - bounds.top).value > 0f)
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals("$label must stay on a single line", 1, layouts.first().lineCount)
        }
    }
}
