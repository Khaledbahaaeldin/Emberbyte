package io.github.khaledbahaaeldin.emberbyte.sampler

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.Confidence
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.CycleWindow
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Forecast
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.PlanState
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.widget.WidgetData
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanNotificationDormantTest {
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private fun createPlan(
        id: Long,
        name: String,
        firstCycle: FirstCycle = FirstCycle.PhoneRecord,
        pinned: Boolean = false,
        capBytes: Long = 10_000_000_000L,
    ) = Plan(
        id = id,
        name = name,
        subscriptionId = 1,
        capBytes = capBytes,
        cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
        rollover = Rollover.None,
        firstCycle = firstCycle,
        pinned = pinned,
    )

    private fun createState(
        plan: Plan,
        usedBytes: Long,
        isDormant: Boolean = false,
        startsAt: Instant? = null,
    ) = PlanState(
        plan = plan,
        window = CycleWindow(Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-11-01T00:00:00Z")),
        effectiveCapBytes = plan.capBytes,
        usedBytes = usedBytes,
        remainingBytes = (plan.capBytes - usedBytes).coerceAtLeast(0L),
        rolledOverBytes = 0L,
        addOnBytes = 0L,
        freeBytes = 0L,
        daysLeft = 16,
        fractionUsed = usedBytes.toFloat() / plan.capBytes,
        isApproximate = false,
        isDormant = isDormant,
        startsAt = startsAt,
    )

    @Test
    fun notification_uses_the_front_plan_not_the_first() {
        val plan1 = createPlan(1, "Low Urgency", capBytes = 10_000_000_000L)
        val state1 = createState(plan1, usedBytes = 1_000_000_000L) // 10%

        val plan2 = createPlan(2, "High Urgency Overcap", capBytes = 10_000_000_000L)
        val state2 = createState(plan2, usedBytes = 11_000_000_000L) // overcap

        val front = io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanUrgency.front(listOf(state1, state2), now)
        assertEquals(2L, front?.plan?.id)
    }

    @Test
    fun notification_ignores_a_dormant_front_candidate() {
        val nextMonth = Instant.parse("2026-11-01T00:00:00Z")
        val dormantPinned = createPlan(1, "Dormant Pinned", firstCycle = FirstCycle.StartsNextCycle(nextMonth), pinned = true)
        val stateDormant = createState(dormantPinned, usedBytes = 0L, isDormant = true, startsAt = nextMonth)

        val active = createPlan(2, "Active Plan", capBytes = 10_000_000_000L)
        val stateActive = createState(active, usedBytes = 4_000_000_000L)

        val front = io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanUrgency.front(listOf(stateDormant, stateActive), now)
        assertEquals(2L, front?.plan?.id)
    }

    @Test
    fun widget_data_load_uses_front_plan_and_reports_starts_at_text_when_dormant() = runBlocking {
        val nextMonth = Instant.parse("2026-11-01T00:00:00Z")
        val dormantPlan = createPlan(1, "Dormant Plan", firstCycle = FirstCycle.StartsNextCycle(nextMonth))
        val stateDormant = createState(dormantPlan, usedBytes = 0L, isDormant = true, startsAt = nextMonth)

        val baseRepo = FakePlanRepository(clock)
        val planRepo = object : io.github.khaledbahaaeldin.emberbyte.data.PlanRepository by baseRepo {
            override fun observeActivePlanStates() = flowOf(listOf(stateDormant))
        }
        val usageRepo = FakeUsageRepository()
        val settingsRepo = FakeSettingsRepository()

        val snapshot = WidgetData.load(usageRepo, planRepo, settingsRepo, clock)
        assertEquals(stateDormant, snapshot.plan)

        val heroModel = io.github.khaledbahaaeldin.emberbyte.widget.heroWidgetModel(snapshot)
        // A widget shows "Starts 1 Nov" text from startsAt instead of remaining bytes
        assertTrue(heroModel.caption.startsWith("Starts 1 Nov") || heroModel.caption.contains("Starts"))
        assertFalse(heroModel.caption.contains("left of"))
    }
}
