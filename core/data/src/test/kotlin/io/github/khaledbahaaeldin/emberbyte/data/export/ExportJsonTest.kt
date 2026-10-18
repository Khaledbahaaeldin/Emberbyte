package io.github.khaledbahaaeldin.emberbyte.data.export

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.store.RoomUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExportJsonTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC)
    private lateinit var context: Context
    private lateinit var db: EmberbyteDatabase
    private lateinit var usage: RoomUsageStore
    private lateinit var plans: RoomPlanStore
    private lateinit var budgets: RoomBudgetStore
    private lateinit var export: StreamingExportService

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
        usage = RoomUsageStore(db)
        plans = RoomPlanStore(db)
        budgets = RoomBudgetStore(db)
        export = StreamingExportService(usage, plans, budgets, clock)
    }

    @After fun tearDown() {
        db.close()
    }

    @Test
    fun json_export_carries_the_new_plan_fields() = runBlocking {
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan1 = Plan(1, "Phone", null, 10_000_000_000L, cycle, Rollover.None, false, FirstCycle.PhoneRecord, pinned = true)
        val plan2 = Plan(2, "Entered", 1, 20_000_000_000L, cycle, Rollover.None, false,
            FirstCycle.Entered(500_000_000L, Instant.parse("2026-10-15T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z")),
            pinned = false)
        val plan3 = Plan(3, "Later", 2, 15_000_000_000L, cycle, Rollover.None, false,
            FirstCycle.StartsNextCycle(Instant.parse("2026-11-01T00:00:00Z")),
            pinned = true)

        plans.savePlanWithChildren(plan1, emptyList(), emptyList(), emptySet())
        plans.savePlanWithChildren(plan2, emptyList(), emptyList(), emptySet())
        plans.savePlanWithChildren(plan3, emptyList(), emptyList(), emptySet())

        val out = ByteArrayOutputStream()
        val result = export.exportJson(out)
        assertTrue(result is Outcome.Success)

        val json = JSONObject(out.toString(Charsets.UTF_8.name()))
        assertEquals(2, json.getInt("contractVersion"))

        val plansArray = json.getJSONArray("plans")
        assertEquals(3, plansArray.length())

        val p1 = plansArray.getJSONObject(0)
        assertEquals(true, p1.getBoolean("pinned"))
        assertEquals("phone", p1.getJSONObject("firstCycle").getString("mode"))

        val p2 = plansArray.getJSONObject(1)
        assertEquals(false, p2.getBoolean("pinned"))
        val fc2 = p2.getJSONObject("firstCycle")
        assertEquals("entered", fc2.getString("mode"))
        assertEquals(500_000_000L, fc2.getLong("usedBytes"))
        assertEquals(Instant.parse("2026-10-15T00:00:00Z").toEpochMilli(), fc2.getLong("enteredAt"))
        assertEquals(Instant.parse("2026-10-01T00:00:00Z").toEpochMilli(), fc2.getLong("cycleStart"))

        val p3 = plansArray.getJSONObject(2)
        assertEquals(true, p3.getBoolean("pinned"))
        val fc3 = p3.getJSONObject("firstCycle")
        assertEquals("later", fc3.getString("mode"))
        assertEquals(Instant.parse("2026-11-01T00:00:00Z").toEpochMilli(), fc3.getLong("startsAt"))
    }

    @Test
    fun csv_export_is_byte_identical_to_before() = runBlocking {
        val out = ByteArrayOutputStream()
        val range = DateRange(Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-10-18T00:00:00Z"))
        val result = export.exportCsv(range, out)
        assertTrue(result is Outcome.Success)
        val expectedHeader = "hourStart,package,network,subscriptionId,rxBytes,txBytes\n"
        assertEquals(expectedHeader, out.toString(Charsets.UTF_8.name()))
    }
}
