package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailContent
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailEvent
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailScreen
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailViewModel
import io.github.khaledbahaaeldin.emberbyte.apps.BudgetEditorDialog
import io.github.khaledbahaaeldin.emberbyte.apps.BudgetEditorUi
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemorySpikeStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.spike.RoomSpikeRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppBudget
import io.github.khaledbahaaeldin.emberbyte.engine.model.BudgetPeriod
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class DialogFamilyTest {
    @get:Rule val rule = createComposeRule()

    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-10-05T12:00:00Z")
    private val date = LocalDate.of(2026, 10, 5)
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        RevealGate.enabled = false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun every_dialog_has_an_icon() {
        var which by mutableIntStateOf(0)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                when (which) {
                    0 -> CsvRangeDialog(onDismiss = {}, onSelectRange = {})
                    1 -> CreateBackupPassphraseDialog(onDismiss = {}, onCreate = {})
                    2 -> RestoreWarningDialog(onDismiss = {}, onConfirm = {})
                    3 -> RestorePassphraseDialog(onDismiss = {}, onRestore = {})
                    4 -> BudgetEditorDialog(
                        editor = BudgetEditorUi(
                            limitText = "500",
                            unit = CapUnit.MB,
                            period = BudgetPeriod.DAILY,
                            planId = null,
                            warnAtPercent = 80,
                        ),
                        planOptions = emptyList(),
                        onEvent = {},
                    )
                }
            }
        }

        for (index in 0..4) {
            which = index
            rule.waitForIdle()
            rule.onNodeWithTag("emberbyte_dialog_header_icon", useUnmergedTree = true).assertIsDisplayed()
            val dismissBounds = rule.onNodeWithTag("emberbyte_dialog_dismiss_button", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            val confirmBounds = rule.onNodeWithTag("emberbyte_dialog_confirm_button", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertTrue("Dialog $index must place dismiss before confirm", dismissBounds.left < confirmBounds.left)
        }
    }

    @Test
    fun export_dialog_has_explicit_export_and_text_cancel() {
        var exported: ExportRange? = null
        var dismissed = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CsvRangeDialog(
                    onDismiss = { dismissed = true },
                    onSelectRange = { exported = it },
                )
            }
        }

        rule.onNodeWithText("Last 30 days").performClick()
        rule.onNodeWithText("Export").assertIsDisplayed().performClick()
        assertEquals(ExportRange.LAST_30_DAYS, exported)

        rule.onNodeWithText("Cancel").assertIsDisplayed().performClick()
        assertTrue(dismissed)
    }

    @Test
    fun budget_limit_rejects_minus_and_spaces() {
        val seenLimits = mutableListOf<String>()
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                BudgetEditorDialog(
                    editor = BudgetEditorUi(
                        limitText = "10",
                        unit = CapUnit.MB,
                        period = BudgetPeriod.DAILY,
                        planId = null,
                        warnAtPercent = 80,
                    ),
                    planOptions = emptyList(),
                    onEvent = { ev ->
                        if (ev is AppDetailEvent.SetBudgetLimit) seenLimits += ev.text
                    },
                )
            }
        }

        // Unit group has an explicit "Unit" label
        rule.onNodeWithText("Unit").assertIsDisplayed()

        // Minus and spaces are rejected by the numeric filter
        rule.onNode(hasSetTextAction()).performTextInput("-5")
        rule.onNode(hasSetTextAction()).performTextInput(" 20 ")
        assertTrue("Minus and spaces must not be forwarded as valid budget limits: $seenLimits", seenLimits.none { '-' in it || ' ' in it })
    }

    @Test
    fun remove_budget_can_be_undone() = runTest(dispatcher) {
        val planStore = InMemoryPlanStore()
        val budgetStore = InMemoryBudgetStore()
        val spikeStore = InMemorySpikeStore()
        val usageStore = InMemoryUsageStore()
        val plans = RoomPlanRepository(planStore, usageStore, clock, ticks = flowOf(now))
        val budgets = RoomBudgetRepository(budgetStore, planStore, usageStore, clock, ticks = flowOf(now))
        val spikes = RoomSpikeRepository(spikeStore)
        val settings = FakeSettingsRepository()
        val usageRepo = FakeUsageRepository(clock, MutableSharedFlow())

        budgets.upsert(
            AppBudget(
                id = 0L,
                packageName = "com.video",
                limitBytes = 500_000_000L,
                period = BudgetPeriod.DAILY,
                planId = null,
                warnAtPercent = 80,
                enabled = true,
            ),
        )

        val vm = AppDetailViewModel(
            packageName = "com.video",
            usage = usageRepo,
            settings = settings,
            budgets = budgets,
            spikes = spikes,
            plans = plans,
            is24Hour = { true },
            clock = clock,
            locale = Locale.ENGLISH,
            dates = flowOf(date),
        )

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppDetailScreen(viewModel = vm, onBack = {})
            }
        }
        advanceUntilIdle()
        rule.waitForIdle()

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Remove"))
        rule.onNodeWithText("Remove").performClick()
        advanceUntilIdle()
        rule.waitForIdle()

        // Undo action is offered and restores the deleted budget
        rule.onNodeWithText("Undo").assertIsDisplayed().performClick()
        advanceUntilIdle()
        rule.waitForIdle()

        val restored = budgets.observeStatuses().first().firstOrNull { it.budget.packageName == "com.video" }
        assertNotNull("Expected budget to be restored after Undo", restored)
        assertEquals(500_000_000L, restored!!.budget.limitBytes)
    }
}
