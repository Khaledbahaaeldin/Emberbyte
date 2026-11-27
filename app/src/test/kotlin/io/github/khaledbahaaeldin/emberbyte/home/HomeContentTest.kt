package io.github.khaledbahaaeldin.emberbyte.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.GapUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PermissionPromptUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HomeContentTest {
    @get:Rule val rule = createComposeRule()

    private fun show(
        state: HomeUiState,
        onEvent: (HomeEvent) -> Unit = {},
        onSettings: () -> Unit = {},
        onHistory: (Int) -> Unit = {},
        onPrompt: (String) -> Unit = {},
        onOpenApp: (String) -> Unit = {},
        onSetUpPlan: () -> Unit = {},
    ) = rule.setContent {
        EmberbyteTheme(EmberbyteThemeSpec.Default) {
            HomeContent(
                state = state,
                onEvent = onEvent,
                onOpenSettings = onSettings,
                onOpenHistory = onHistory,
                onPromptAction = onPrompt,
                onOpenApp = onOpenApp,
                onSetUpPlan = onSetUpPlan,
            )
        }
    }

    @Test fun settings_icon_opens_settings() {
        var opened = false
        show(HomeUiState(), onSettings = { opened = true })
        rule.onNodeWithContentDescription("Settings").performClick()
        assertEquals(true, opened)
    }

    @Test fun prompts_and_gap_banner_are_shown_and_report_their_action() {
        var picked: String? = null
        show(
            HomeUiState(
                prompts = listOf(PermissionPromptUi("usage_access", "Allow usage access", "Body", "Open settings")),
                gap = GapUi("Not measured 02:10–03:40 because the app was stopped"),
            ),
            onPrompt = { picked = it },
        )
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Open settings"))
        rule.onNodeWithText("Not measured 02:10–03:40 because the app was stopped").assertExists()
        rule.onNodeWithText("Open settings").performClick()
        assertEquals("usage_access", picked)
    }

    @Test fun no_plan_shows_the_setup_card_and_neither_the_old_link_nor_the_old_card() {
        show(HomeUiState(hasPlan = false))
        rule.onNodeWithText("Set up your data plan", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("See what is left and when it runs out.", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Set up plan", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Set up a plan").assertDoesNotExist()
        rule.onNodeWithText("Data plan").assertDoesNotExist()
        rule.onNodeWithText("Set up your data plan to see what is left and when it runs out.").assertDoesNotExist()
    }

    @Test fun with_a_plan_the_setup_card_is_not_shown() {
        show(HomeUiState(hasPlan = true))
        rule.onNodeWithTag("plan_setup_card").assertDoesNotExist()
        rule.onNodeWithText("Set up plan").assertDoesNotExist()
    }

    @Test fun setup_card_button_opens_the_new_plan_flow() {
        var opened = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HomeContent(
                    state = HomeUiState(hasPlan = false),
                    onEvent = {}, onOpenSettings = {}, onOpenHistory = {}, onPromptAction = {}, onOpenApp = {},
                    onSetUpPlan = { opened++ },
                )
            }
        }
        rule.onNodeWithTag("plan_setup_button").performClick()
        assertEquals(1, opened)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun the_history_button_opens_history_on_the_selected_day() {
        var openedDay = -1
        show(HomeUiState(usage = sampleUsageCard(selectedIndex = 3)), onHistory = { openedDay = it })
        rule.onNodeWithTag("usage_btn_history").performClick()
        assertEquals(3, openedDay)
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun expanding_a_top_app_and_tapping_app_details_opens_it() {
        var opened: String? = null
        show(HomeUiState(usage = sampleUsageCard()), onOpenApp = { opened = it })
        rule.onNodeWithTag("usage_row_com.video").performClick()
        rule.onNodeWithTag("usage_row_details", useUnmergedTree = true).performClick()
        assertEquals("com.video", opened)
    }

    @Test fun locked_top_apps_explain_why() {
        show(HomeUiState(topAppsLocked = true))
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Allow usage access to see which apps use your data."))
        rule.onNodeWithText("Allow usage access to see which apps use your data.").assertIsDisplayed()
    }

    @Test fun rtl_layout_direction_isolates_home_subtitle_with_force_ltr() {
        val subtitle1 = "3.80 GB left · won't run out this cycle"
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    HomeContent(
                        state = HomeUiState(subtitle = subtitle1),
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
        val node1 = rule.onNodeWithText(subtitle1)
        node1.assertIsDisplayed()
        assertEquals(LayoutDirection.Ltr, node1.fetchSemanticsNode().layoutInfo.layoutDirection)
    }

    @Test fun rtl_layout_direction_isolates_no_data_left_with_force_ltr() {
        val subtitle2 = "No data left"
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    HomeContent(
                        state = HomeUiState(subtitle = subtitle2),
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
        val node2 = rule.onNodeWithText(subtitle2)
        node2.assertIsDisplayed()
        assertEquals(LayoutDirection.Ltr, node2.fetchSemanticsNode().layoutInfo.layoutDirection)
    }

    @Test fun loading_shows_loading_state() {
        show(HomeUiState(isLoading = true))
        rule.onNodeWithContentDescription("Loading today's usage").assertIsDisplayed()
    }

    @Test
    fun removing_a_prompt_keeps_the_others_and_does_not_crash() {
        val p1 = PermissionPromptUi("p1", "Prompt 1", "Detail 1", "Fix")
        val p2 = PermissionPromptUi("p2", "Prompt 2", "Detail 2", "Fix")
        var promptList by mutableStateOf(listOf(p1, p2))

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HomeContent(
                    state = HomeUiState(prompts = promptList),
                    onEvent = {},
                    onOpenSettings = {},
                    onOpenHistory = {},
                    onPromptAction = {},
                    onOpenApp = {},
                    onSetUpPlan = {},
                )
            }
        }

        rule.onNodeWithText("Prompt 1").assertIsDisplayed()
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Prompt 2"))
        rule.onNodeWithText("Prompt 2").assertIsDisplayed()

        // Remove p1
        promptList = listOf(p2)
        rule.waitForIdle()

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Prompt 2"))
        rule.onNodeWithText("Prompt 2").assertIsDisplayed()
        rule.onNodeWithText("Prompt 1").assertDoesNotExist()
    }

    @Test
    fun home_hero_labeled_today_with_system_traffic_footnote() {
        show(HomeUiState(heroBytes = 500_000_000L, isToday = true))
        rule.onNodeWithText("Today").assertIsDisplayed()
        rule.onNodeWithText("Includes system traffic").assertIsDisplayed()
    }

    @Test
    fun home_hero_shows_selected_day_label_when_not_today() {
        show(HomeUiState(heroBytes = 500_000_000L, isToday = false, heroLabel = "Wednesday"))
        rule.onNodeWithText("Wednesday").assertIsDisplayed()
        rule.onNodeWithText("Includes system traffic").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h3000dp")
    fun home_day_buttons_select_the_day() {
        val events = mutableListOf<HomeEvent>()
        show(state = HomeUiState(usage = sampleUsageCard()), onEvent = { events += it })
        rule.onNodeWithTag("usage_day_2").performClick()
        assertEquals(listOf<HomeEvent>(HomeEvent.SelectDay(2)), events)
    }
}
