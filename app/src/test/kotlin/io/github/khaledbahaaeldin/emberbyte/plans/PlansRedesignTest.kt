package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.ArchivedPlansRestoreRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanStatusKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class PlansRedesignTest {
    @get:Rule val rule = createComposeRule()

    private class FakeRestoreRepo(var restored: Boolean = false) : ArchivedPlansRestoreRepository {
        override suspend fun isRestored(): Boolean = restored
        override suspend fun markRestored() {
            restored = true
        }
    }

    private fun sampleCard(
        id: Long = 1L,
        name: String = "Vodafone",
        statusKind: PlanStatusKind = PlanStatusKind.OnTrack,
        pinned: Boolean = true,
        isDormant: Boolean = false,
    ) = PlanCardUi(
        id = id,
        name = name,
        simLabel = "SIM 1",
        fraction = 0.59f,
        ringState = when (statusKind) {
            PlanStatusKind.OverCap -> BudgetStateUi.Exceeded
            PlanStatusKind.RunsOutEarly -> BudgetStateUi.Warning
            else -> BudgetStateUi.Ok
        },
        percentText = "59%",
        usedText = "11.8 GB of 20.0 GB",
        remainingText = "8.2 GB left",
        daysLeftText = "15 days left",
        forecastText = null,
        isEstimated = false,
        chips = listOf("Boost +2 GB"),
        isDormant = isDormant,
        startsAtText = if (isDormant) "Starts 1 Dec" else null,
        pinned = pinned,
        allowancePerDayText = "You can use 560 MB a day",
        timeFraction = 0.5f,
        dayOfCycle = 15,
        cycleLength = 30,
        weekBars = listOf(0.2f, 0.4f, 0.3f, 0.5f, 0.6f, 0.8f, 0.4f),
        statusKind = statusKind,
        runOutDateText = if (statusKind == PlanStatusKind.RunsOutEarly) "30 Nov" else null,
        earlyDaysText = if (statusKind == PlanStatusKind.RunsOutEarly) "4 days early" else null,
        renewsText = "Renews 5 Dec",
        capText = "20.0 GB",
    )

    @Test
    fun restore_runs_once_and_sets_the_flag() = runTest {
        val repo = FakePlanRepository(Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC))
        repo.upsertPlan(Plan(0L, "Old", null, 5_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None, archived = true))
        val restoreRepo = FakeRestoreRepo(restored = false)
        val coordinator = PlansSessionCoordinator()

        val firstCount = runArchivedPlansRestoreOnce(repo, restoreRepo, coordinator)
        assertEquals(1, firstCount)
        assertTrue(restoreRepo.restored)
        assertEquals(1, coordinator.restoredCount.value)

        // Second start does nothing
        val secondCount = runArchivedPlansRestoreOnce(repo, restoreRepo, coordinator)
        assertEquals(0, secondCount)
    }

    @Test
    fun restore_failure_retries_next_start() = runTest {
        val failingRepo = object : io.github.khaledbahaaeldin.emberbyte.data.PlanRepository by FakePlanRepository() {
            override suspend fun restoreArchivedPlans(): Outcome<Int> =
                Outcome.Failure(EmberbyteError.Storage("disk error"))
        }
        val restoreRepo = FakeRestoreRepo(restored = false)
        val coordinator = PlansSessionCoordinator()

        val count = runArchivedPlansRestoreOnce(failingRepo, restoreRepo, coordinator)
        assertEquals(0, count)
        assertFalse(restoreRepo.restored)
    }

    @Test
    fun restored_notice_is_queued_for_the_plans_tab() {
        val events = mutableListOf<PlansEvent>()
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(
                        front = sampleCard(),
                        restoredCount = 2,
                        loaded = true,
                    ),
                    onEvent = { events += it },
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithText("2 archived plans were restored. You can delete the ones you do not need.").assertIsDisplayed()
        rule.onNodeWithContentDescription("Dismiss").performClick()
        assertEquals(listOf<PlansEvent>(PlansEvent.DismissRestoredNotice), events)
    }

    @Test
    fun front_card_shows_ring_number_status_tracks_and_chips() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(
                        front = sampleCard(),
                        loaded = true,
                    ),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithText("Front plan").assertIsDisplayed()
        rule.onNodeWithText("59% used").assertIsDisplayed()
        rule.onNodeWithText("8.2 GB left").assertIsDisplayed()
        rule.onNodeWithText("Will last the cycle").assertIsDisplayed()
        rule.onNodeWithText("Time").assertIsDisplayed()
        rule.onNodeWithText("Data").assertIsDisplayed()
        rule.onNodeWithText("Renews 5 Dec").assertIsDisplayed()
        rule.onNodeWithText("You can use 560 MB a day").assertIsDisplayed()
    }

    @Test
    fun front_card_tints_warn_and_danger() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                FrontPlanCard(
                    model = sampleCard(statusKind = PlanStatusKind.RunsOutEarly),
                    expandedDetails = false,
                    whatIfExtraGb = 0,
                    whatIfSentence = null,
                    onOpenPlan = {},
                    onPinToggle = {},
                    onDelete = {},
                    onToggleDetails = {},
                    onSelectWhatIf = {},
                )
            }
        }
        rule.onNodeWithText("Runs out 30 Nov (4 days early)").assertIsDisplayed()
    }

    @Test
    fun tiles_keep_days_left_and_kebab_never_overlaps_the_ring() {
        val front = sampleCard(id = 1L, name = "Front")
        val tile = sampleCard(id = 2L, name = "Travel", pinned = false)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(front = front, tiles = listOf(tile), loaded = true),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        val kebabBounds = rule.onNodeWithContentDescription("Plan options for Travel").fetchSemanticsNode().boundsInRoot
        val ringPercentBounds = rule.onAllNodes(androidx.compose.ui.test.hasText("59%"), useUnmergedTree = true).fetchSemanticsNodes().last().boundsInRoot
        assertFalse("Kebab must not overlap tile ring", kebabBounds.overlaps(ringPercentBounds))
        rule.onAllNodes(androidx.compose.ui.test.hasText("15 days left")).fetchSemanticsNodes().let {
            assertEquals(2, it.size)
        }
    }

    @Test
    fun tapping_a_tile_pins_it_and_exposes_the_action() {
        val events = mutableListOf<PlansEvent>()
        val tile = sampleCard(id = 2L, name = "Travel", pinned = false)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(front = sampleCard(id = 1L), tiles = listOf(tile), loaded = true),
                    onEvent = { events += it },
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithText("Travel").performClick()
        assertEquals(listOf<PlansEvent>(PlansEvent.SetPinned(2L, true)), events)
    }

    @Test
    fun upcoming_card_is_dashed_with_start_date() {
        val upcoming = sampleCard(id = 3L, name = "Next Month", isDormant = true, statusKind = PlanStatusKind.Upcoming)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(front = sampleCard(), upcoming = listOf(upcoming), loaded = true),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithTag("upcoming_plan_card").assertIsDisplayed()
        rule.onNodeWithText("20.0 GB, Starts 1 Dec").assertIsDisplayed()
    }

    @Test
    fun insight_names_the_worst_plan_and_all_on_track() {
        var openedId: Long? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansInsightCard(
                    insight = PlansInsight(
                        worstPlanId = 9L,
                        worstPlanName = "Work SIM",
                        earlyDays = 7,
                        dailyAllowanceBytes = 51_000_000L,
                        allOnTrack = false,
                    ),
                    onOpenWorstPlan = { openedId = it },
                )
            }
        }
        rule.onNodeWithText("Work SIM is running ahead. At this pace it ends 7 days early.").assertIsDisplayed()
        rule.onNodeWithText("Open the forecast").performClick()
        assertEquals(9L, openedId)
    }

    @Test
    fun insight_says_all_on_track() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansInsightCard(
                    insight = PlansInsight(null, null, 0, null, allOnTrack = true),
                    onOpenWorstPlan = {},
                )
            }
        }
        rule.onNodeWithText("All plans are on track. Nothing runs out before it renews.").assertIsDisplayed()
    }

    @Test
    fun timeline_labels_do_not_overlap() {
        val events = listOf(
            RenewalEvent(null, "Today", LocalDate.of(2026, 11, 15), 0, isToday = true, isUpcoming = false),
            RenewalEvent(1L, "Work", LocalDate.of(2026, 11, 16), 1, isToday = false, isUpcoming = false),
            RenewalEvent(2L, "Home", LocalDate.of(2026, 11, 25), 10, isToday = false, isUpcoming = false),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                RenewalTimelineCard(events = events)
            }
        }

        val todayBounds = rule.onNodeWithText("Today").fetchSemanticsNode().boundsInRoot
        val workBounds = rule.onNodeWithText("Work").fetchSemanticsNode().boundsInRoot
        assertFalse("Close timeline labels must stagger vertically without overlap", todayBounds.overlaps(workBounds))
    }

    @Test
    fun empty_state_has_one_button() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(loaded = true),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }
        rule.onNodeWithText("Nothing to track yet").assertIsDisplayed()
        rule.onNodeWithText("Set up your first plan").assertIsDisplayed()
    }

    @Test
    fun menu_has_pin_edit_and_delete_with_icons_and_opens_below_the_button() {
        assertTrue(PlanMenuDropdownOffset.y >= 4.dp)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(front = sampleCard(name = "Vodafone"), loaded = true),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithContentDescription("Plan options for Vodafone").performClick()
        rule.onNodeWithText("Unpin").assertIsDisplayed()
        assertEquals(2, rule.onAllNodes(androidx.compose.ui.test.hasText("Edit")).fetchSemanticsNodes().size)
        rule.onNodeWithText("Delete").assertIsDisplayed()
        rule.onNodeWithText("Archive").assertDoesNotExist()
    }

    @Test
    fun editor_danger_zone_only_has_delete() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = PlanEditorUiState(isNew = false, planId = 1L, loading = false, form = PlanEditorForm(name = "Main", capText = "10")),
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        rule.onNodeWithText("Delete plan").assertIsDisplayed()
        rule.onNodeWithText("Archive plan").assertDoesNotExist()
        rule.onNodeWithText("Unarchive plan").assertDoesNotExist()
    }

    @Test
    fun reduced_motion_is_static() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    PlansContent(
                        state = PlansUiState(front = sampleCard(), loaded = true),
                        onEvent = {},
                        onOpenPlan = {},
                        onNewPlan = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("8.2 GB left").assertIsDisplayed()
    }
}
