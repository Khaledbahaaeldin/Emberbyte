package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
@OptIn(ExperimentalCoroutinesApi::class)
class PlansArchiveMenuTest {
    @get:Rule val rule = createComposeRule()

    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleCard(id: Long, name: String, archived: Boolean = false) = PlanCardUi(
        id = id,
        name = name,
        simLabel = "SIM 1",
        fraction = 0.4f,
        ringState = BudgetStateUi.Ok,
        percentText = "40%",
        usedText = "4.0 GB of 10.0 GB",
        remainingText = "6.0 GB left",
        daysLeftText = "14 days left",
        forecastText = if (archived) null else "Won't run out this cycle",
        isEstimated = false,
        chips = emptyList(),
        archived = archived,
    )

    @Test
    fun archived_plan_can_be_unarchived() {
        val events = mutableListOf<PlansEvent>()
        val archivedCard = sampleCard(id = 7L, name = "Old Plan", archived = true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(
                        cards = emptyList(),
                        archivedCards = listOf(archivedCard),
                        showArchived = true,
                        loaded = true,
                    ),
                    onEvent = { events += it },
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        // Archived card has a kebab menu offering Unarchive
        rule.onNodeWithContentDescription("Plan options for Old Plan").assertIsDisplayed().performClick()
        rule.onNodeWithText("Unarchive").assertIsDisplayed().performClick()
        assertEquals(listOf<PlansEvent>(PlansEvent.UnarchivePlan(7L)), events)
    }

    @Test
    fun archive_shows_a_snackbar_with_undo() {
        val events = mutableListOf<PlansEvent>()
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(
                        cards = emptyList(),
                        archivedCards = listOf(sampleCard(id = 1L, name = "Main SIM", archived = true)),
                        loaded = true,
                        undoArchivedPlanId = 1L,
                    ),
                    onEvent = { events += it },
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        rule.onNodeWithText("Plan archived", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Undo").assertIsDisplayed().performClick()
        assertTrue(events.contains(PlansEvent.UndoArchive))
    }

    @Test
    fun undo_restores_the_plan() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val usageStore = InMemoryUsageStore()
        val repo = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val settings = FakeSettingsRepository()
        val sims = FakeSimInfoSource()
        val vm = PlansViewModel(repo, settings, sims, clock, Locale.ENGLISH)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val plan = Plan(0L, "Main SIM", 5, 10_000_000_000L, Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC), Rollover.None)
        repo.upsertPlan(plan)
        advanceUntilIdle()
        val planId = vm.uiState.value.cards.single().id

        vm.onEvent(PlansEvent.ArchivePlan(planId))
        advanceUntilIdle()
        assertEquals(0, vm.uiState.value.cards.size)
        assertEquals(1, vm.uiState.value.archivedCards.size)
        assertEquals(planId, vm.uiState.value.undoArchivedPlanId)

        vm.onEvent(PlansEvent.UndoArchive)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.cards.size)
        assertEquals(0, vm.uiState.value.archivedCards.size)
        assertEquals(null, vm.uiState.value.undoArchivedPlanId)

        job.cancel()
    }

    @Test
    fun menu_does_not_overlap_the_card_text() {
        val front = sampleCard(id = 1L, name = "Front SIM")
        val second = sampleCard(id = 2L, name = "Travel SIM")
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlansContent(
                    state = PlansUiState(
                        cards = listOf(front, second),
                        loaded = true,
                    ),
                    onEvent = {},
                    onOpenPlan = {},
                    onNewPlan = {},
                )
            }
        }

        val frontKebab = rule.onNodeWithContentDescription("Plan options for Front SIM").fetchSemanticsNode().boundsInRoot
        val frontTitle = rule.onNodeWithText("Front SIM", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("Front card kebab must not overlap title text", !frontKebab.overlaps(frontTitle))

        val tileKebab = rule.onNodeWithContentDescription("Plan options for Travel SIM").fetchSemanticsNode().boundsInRoot
        val tileTitle = rule.onNodeWithText("Travel SIM", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val tileRingPercent = rule.onAllNodes(androidx.compose.ui.test.hasText("40%"), useUnmergedTree = true).fetchSemanticsNodes()[1].boundsInRoot
        val tileDaysLeft = rule.onAllNodes(androidx.compose.ui.test.hasText("14 days left"), useUnmergedTree = true).fetchSemanticsNodes().last().boundsInRoot
        assertTrue("Small tile must keep 'days left' visible", tileDaysLeft.width > 0f && tileDaysLeft.height > 0f)
        assertTrue("Small tile kebab must not overlap title", !tileKebab.overlaps(tileTitle))
        assertTrue("Small tile kebab must not overlap ring", !tileKebab.overlaps(tileRingPercent))

        assertTrue("Dropdown offset must push menu below kebab", PlanDropdownOffset.y >= 4.dp)
        rule.onNodeWithContentDescription("Plan options for Front SIM").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Pin").assertIsDisplayed()
        rule.onNodeWithText("Edit").assertIsDisplayed()
        rule.onNodeWithText("Archive").assertIsDisplayed()
    }
}
