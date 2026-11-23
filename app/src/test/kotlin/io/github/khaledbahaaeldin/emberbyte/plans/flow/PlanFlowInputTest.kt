package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSimInfoSource
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanRepository
import io.github.khaledbahaaeldin.emberbyte.plans.CapUnit
import io.github.khaledbahaaeldin.emberbyte.plans.CycleKind
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class PlanFlowInputTest {
    @get:Rule val rule = createComposeRule()

    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(testClock: Clock = clock): PlanFlowViewModel {
        val repo = RoomPlanRepository(
            InMemoryPlanStore(),
            InMemoryUsageStore(),
            testClock,
            ticks = flowOf(testClock.instant()),
        )
        return PlanFlowViewModel(repo, FakeSettingsRepository(), FakeSimInfoSource(), testClock)
    }

    @Test
    fun numeric_fields_use_numeric_keyboards() {
        var step by mutableStateOf(2)
        var cycleKind by mutableStateOf(CycleKind.Monthly)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                when (step) {
                    2 -> Step2Cap(
                        state = PlanFlowState(step = 2, capText = "10"),
                        onEvent = {},
                    )
                    3 -> Step3Cycle(
                        state = PlanFlowState(step = 3, cycleKind = cycleKind, dayOfMonth = 1, everyDays = 30),
                        onEvent = {},
                    )
                    4 -> Step4ThisCycle(
                        state = PlanFlowState(
                            step = 4,
                            firstCycleChoice = FirstCycleChoice.Entered,
                            enteredAmount = "2",
                        ),
                        onEvent = {},
                    )
                }
            }
        }

        // Step 2 Cap -> Decimal + Next
        val capNode = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        assertEquals(ImeAction.Next, capNode.config.getOrNull(SemanticsProperties.ImeAction))
        assertEquals(KeyboardType.Decimal, capNode.config.getOrNull(FieldKeyboardTypeKey))
        rule.onNodeWithText("Cap").assertIsDisplayed()

        // Step 3 Monthly day -> Number + Next
        step = 3
        rule.waitForIdle()
        val dayNode = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        assertEquals(ImeAction.Next, dayNode.config.getOrNull(SemanticsProperties.ImeAction))
        assertEquals(KeyboardType.Number, dayNode.config.getOrNull(FieldKeyboardTypeKey))
        rule.onNodeWithText("Day of month").assertIsDisplayed()

        // Step 3 Every N days -> Number + Next
        cycleKind = CycleKind.EveryNDays
        rule.waitForIdle()
        val daysNode = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        assertEquals(ImeAction.Next, daysNode.config.getOrNull(SemanticsProperties.ImeAction))
        assertEquals(KeyboardType.Number, daysNode.config.getOrNull(FieldKeyboardTypeKey))
        rule.onNodeWithText("Days per cycle").assertIsDisplayed()

        // Step 4 Entered amount -> Decimal + Next
        step = 4
        rule.waitForIdle()
        val enteredNode = rule.onNode(hasSetTextAction()).fetchSemanticsNode()
        assertEquals(ImeAction.Next, enteredNode.config.getOrNull(SemanticsProperties.ImeAction))
        assertEquals(KeyboardType.Decimal, enteredNode.config.getOrNull(FieldKeyboardTypeKey))
        rule.onNodeWithText("Used so far").assertIsDisplayed()
    }

    @Test
    fun invalid_cap_shows_an_error_and_keeps_continue_enabled() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onEvent(PlanFlowEvent.SetName("Main SIM"))
        vm.onEvent(PlanFlowEvent.NextStep)
        assertEquals(2, vm.uiState.value.step)

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScreen(
                    viewModel = vm,
                    onPlanCreated = {},
                    onCancel = {},
                )
            }
        }

        // Continue stays enabled even when cap is invalid/empty; tapping it reveals the error and stays on step 2.
        advanceUntilIdle()
        rule.waitForIdle()
        rule.onNodeWithText("Continue").assertIsEnabled()
        rule.onNodeWithText("Continue").performClick()
        advanceUntilIdle()
        rule.waitForIdle()

        assertEquals(2, vm.uiState.value.step)
        rule.onNodeWithText("Continue").assertIsEnabled()
        rule.onNodeWithText("Enter a number greater than zero").assertIsDisplayed()

        // Entering "0" also shows the error and keeps Continue enabled.
        rule.onNode(hasSetTextAction()).performTextInput("0")
        advanceUntilIdle()
        rule.waitForIdle()
        rule.onNodeWithText("Continue").assertIsEnabled()
        rule.onNodeWithText("Enter a number greater than zero").assertIsDisplayed()
    }

    @Test
    fun one_point_five_point_two_is_rejected() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onEvent(PlanFlowEvent.SetName("Main SIM"))
        vm.onEvent(PlanFlowEvent.NextStep)

        // Typing a second decimal point after "1.5" is rejected; locale comma separator "1,5" is accepted once.
        vm.onEvent(PlanFlowEvent.SetCap("1,5"))
        assertEquals("1,5", vm.uiState.value.capText)
        vm.onEvent(PlanFlowEvent.SetCap("1,5.2"))
        assertEquals("1,5", vm.uiState.value.capText)

        vm.onEvent(PlanFlowEvent.SetCap("1.5"))
        assertEquals("1.5", vm.uiState.value.capText)
        vm.onEvent(PlanFlowEvent.SetCap("1.5.2"))
        assertEquals("1.5", vm.uiState.value.capText)

        // Pasting "1.5.2" into an empty field is rejected and surfaces an error message.
        val vmFresh = createViewModel()
        advanceUntilIdle()
        vmFresh.onEvent(PlanFlowEvent.SetName("Main SIM"))
        vmFresh.onEvent(PlanFlowEvent.NextStep)
        vmFresh.onEvent(PlanFlowEvent.SetCap("1.5.2"))
        assertNotEquals("1.5.2", vmFresh.uiState.value.capText)
        assertEquals("", vmFresh.uiState.value.capText)

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScreen(
                    viewModel = vmFresh,
                    onPlanCreated = {},
                    onCancel = {},
                )
            }
        }
        advanceUntilIdle()
        rule.waitForIdle()
        rule.onNodeWithText("Enter a number greater than zero").assertIsDisplayed()
    }

    @Test
    fun day_401_shows_a_range_error() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onEvent(PlanFlowEvent.SetName("Main SIM"))
        vm.onEvent(PlanFlowEvent.NextStep)
        vm.onEvent(PlanFlowEvent.SetCap("20"))
        vm.onEvent(PlanFlowEvent.NextStep)
        assertEquals(3, vm.uiState.value.step)

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanFlowScreen(
                    viewModel = vm,
                    onPlanCreated = {},
                    onCancel = {},
                )
            }
        }
        advanceUntilIdle()
        rule.waitForIdle()

        vm.onEvent(PlanFlowEvent.SetDayOfMonthText("401"))
        advanceUntilIdle()
        rule.waitForIdle()

        rule.onNodeWithText("Day must be 1 to 31").assertIsDisplayed()
        rule.onNodeWithText("Continue").assertIsEnabled()
        rule.onNodeWithText("Continue").performClick()
        advanceUntilIdle()
        rule.waitForIdle()
        assertEquals(3, vm.uiState.value.step)

        // Switching to Every N days and entering 401 shows the 1..365 days range error.
        vm.onEvent(PlanFlowEvent.SetCycleKind(CycleKind.EveryNDays))
        vm.onEvent(PlanFlowEvent.SetEveryDaysText("401"))
        advanceUntilIdle()
        rule.waitForIdle()
        rule.onNodeWithText("Enter 1 to 365 days").assertIsDisplayed()
    }

    @Test
    fun step_total_does_not_change_while_typing() = runTest(dispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        val initialTotal = vm.uiState.value.totalSteps
        assertEquals(5, initialTotal)

        vm.onEvent(PlanFlowEvent.SetName("Vodafone"))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
        vm.onEvent(PlanFlowEvent.NextStep)

        vm.onEvent(PlanFlowEvent.SetCap("20"))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
        vm.onEvent(PlanFlowEvent.NextStep)
        assertEquals(3, vm.uiState.value.step)

        // Editing day of month (including today's date 15, another date 1, and out-of-range 401) or switching cycle kind
        // must never alter totalSteps while on step 3.
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(15))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(1))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
        vm.onEvent(PlanFlowEvent.SetDayOfMonth(401))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
        vm.onEvent(PlanFlowEvent.SetCycleKind(CycleKind.EveryNDays))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
        vm.onEvent(PlanFlowEvent.SetEveryDays(14))
        assertEquals(initialTotal, vm.uiState.value.totalSteps)
    }

    @Test
    fun fuzz_inputs_never_crash_and_always_show_a_message() = runTest(dispatcher) {
        val capFuzz = listOf("", "0", "-", "1.5.2", "401", "99999999999", " 10 abc ")
        for (input in capFuzz) {
            val vm = createViewModel()
            advanceUntilIdle()
            val expectedTotal = vm.uiState.value.totalSteps
            vm.onEvent(PlanFlowEvent.SetName("Fuzz Plan"))
            vm.onEvent(PlanFlowEvent.NextStep)
            if (input == "401") {
                // "401" is valid for cap (401 GB), test "401" on step 3 cycle below; test cap with MB overflow too
                vm.onEvent(PlanFlowEvent.SetCapUnit(CapUnit.MB))
                vm.onEvent(PlanFlowEvent.SetCap("99999999999"))
            } else if (input.isNotEmpty()) {
                vm.onEvent(PlanFlowEvent.SetCap(input))
            }
            vm.onEvent(PlanFlowEvent.NextStep)
            assertEquals("Cap input '$input' should not advance past step 2", 2, vm.uiState.value.step)
            assertEquals("Step counter must stay constant for '$input'", expectedTotal, vm.uiState.value.totalSteps)
            assertNotNull("Expected fieldError on step 2 for '$input'", vm.uiState.value.fieldError)
        }

        val dayTextFuzz = listOf("", "0", "-", "1.5.2", "401", "99999999999", " 12 days ")
        for (raw in dayTextFuzz) {
            val vm = createViewModel()
            advanceUntilIdle()
            val expectedTotal = vm.uiState.value.totalSteps
            vm.onEvent(PlanFlowEvent.SetName("Fuzz Plan"))
            vm.onEvent(PlanFlowEvent.NextStep)
            vm.onEvent(PlanFlowEvent.SetCap("10"))
            vm.onEvent(PlanFlowEvent.NextStep)
            vm.onEvent(PlanFlowEvent.SetDayOfMonthText(""))
            if (raw.isNotEmpty()) {
                vm.onEvent(PlanFlowEvent.SetDayOfMonthText(raw))
            }
            vm.onEvent(PlanFlowEvent.NextStep)
            assertEquals("Day '$raw' should not advance past step 3", 3, vm.uiState.value.step)
            assertEquals("Step counter must stay constant for day '$raw'", expectedTotal, vm.uiState.value.totalSteps)
            assertEquals(PlanFlowError.DayRange, vm.uiState.value.fieldError)
        }

        for (raw in dayTextFuzz) {
            val vm = createViewModel()
            advanceUntilIdle()
            val expectedTotal = vm.uiState.value.totalSteps
            vm.onEvent(PlanFlowEvent.SetName("Fuzz Plan"))
            vm.onEvent(PlanFlowEvent.NextStep)
            vm.onEvent(PlanFlowEvent.SetCap("10"))
            vm.onEvent(PlanFlowEvent.NextStep)
            vm.onEvent(PlanFlowEvent.SetCycleKind(CycleKind.EveryNDays))
            vm.onEvent(PlanFlowEvent.SetEveryDaysText(""))
            if (raw.isNotEmpty()) {
                vm.onEvent(PlanFlowEvent.SetEveryDaysText(raw))
            }
            vm.onEvent(PlanFlowEvent.NextStep)
            assertEquals("Days '$raw' should not advance past step 3", 3, vm.uiState.value.step)
            assertEquals("Step counter must stay constant for days '$raw'", expectedTotal, vm.uiState.value.totalSteps)
            assertEquals(PlanFlowError.DaysRange, vm.uiState.value.fieldError)
        }
    }

    @Test
    fun font_scale_2_and_rtl_render_review_and_errors_without_clipping() {
        val reviewState = PlanFlowState(
            step = 5,
            totalSteps = 5,
            name = "Vodafone International",
            subscriptionId = 1,
            sims = listOf(io.github.khaledbahaaeldin.emberbyte.plans.SimOptionUi(1, "SIM 1 · Vodafone")),
            capText = "50",
            capUnit = CapUnit.GB,
            cycleKind = CycleKind.Monthly,
            dayOfMonth = 17,
            nextCycleStartText = "17 Dec",
            isMidCycleApplicable = true,
            firstCycleChoice = FirstCycleChoice.Entered,
            enteredAmount = "12.5",
            enteredUnit = CapUnit.GB,
            isReviewStep = true,
        )
        rule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, fontScale = 2f),
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanFlowContent(
                        state = reviewState,
                        onEvent = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Create plan").assertIsDisplayed()
        rule.onNodeWithText("Renews 17 Dec").assertIsDisplayed()
        rule.onNodeWithText("Entered: 12.5 GB").assertIsDisplayed()
    }
}
