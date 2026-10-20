package io.github.khaledbahaaeldin.emberbyte.data.plan

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.backup.BackupCodec
import io.github.khaledbahaaeldin.emberbyte.data.backup.DefaultBackupService
import io.github.khaledbahaaeldin.emberbyte.data.backup.PackageResolver
import io.github.khaledbahaaeldin.emberbyte.data.backup.RoomRestoreStore
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.export.StreamingExportService
import io.github.khaledbahaaeldin.emberbyte.data.store.RoomUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.NetworkKind
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.plan.PlanUrgency
import io.github.khaledbahaaeldin.emberbyte.engine.usage.AppMeta
import io.github.khaledbahaaeldin.emberbyte.engine.usage.HourlyUsage
import io.github.khaledbahaaeldin.emberbyte.engine.usage.MinuteTotal
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MidCycleRepositoryTest {
    private val utc = ZoneOffset.UTC
    private val fast = BackupCodec.Params(memoryKiB = 64, iterations = 1, parallelism = 1)
    private lateinit var context: Context
    private lateinit var db: EmberbyteDatabase
    private lateinit var planStore: RoomPlanStore
    private lateinit var usageStore: RoomUsageStore

    private var currentInstant: Instant = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = object : Clock() {
        override fun getZone(): ZoneId = utc
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = currentInstant
    }
    private val ticks = MutableSharedFlow<Instant>(replay = 1)

    private lateinit var repository: RoomPlanRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        planStore = RoomPlanStore(db)
        usageStore = RoomUsageStore(db)
        ticks.tryEmit(currentInstant)
        repository = RoomPlanRepository(planStore, usageStore, clock, ticks)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun addUsage(at: String, bytes: Long, subId: Int = 1) {
        usageStore.addMinute(
            MinuteTotal(
                minuteStart = Instant.parse(at),
                network = NetworkKind.MOBILE,
                subscriptionId = subId,
                rxBytes = bytes,
                txBytes = 0L,
            )
        )
    }

    private suspend fun addHourlyUsage(hourStart: String, bytes: Long, subId: Int = 1, pkg: String = "com.example.app", uid: Int = 10001) {
        usageStore.upsertAppMeta(listOf(AppMeta(uid = uid, packageName = pkg, label = pkg)))
        usageStore.upsertHourly(
            listOf(
                HourlyUsage(
                    hourStart = Instant.parse(hourStart),
                    uid = uid,
                    network = NetworkKind.MOBILE,
                    subscriptionId = subId,
                    rxBytes = bytes,
                    txBytes = 0L,
                )
            )
        )
    }

    private fun setTime(instantStr: String) {
        currentInstant = Instant.parse(instantStr)
        ticks.tryEmit(currentInstant)
    }

    private fun createBackupService(database: EmberbyteDatabase): DefaultBackupService {
        val usage = RoomUsageStore(database)
        val plans = RoomPlanStore(database)
        val budgets = RoomBudgetStore(database)
        val export = StreamingExportService(usage, plans, budgets, clock)
        val restore = RoomRestoreStore(database)
        val resolver = object : PackageResolver {
            override fun resolve(packageName: String): AppMeta =
                AppMeta(uid = 10001, packageName = packageName, label = packageName)
        }
        return DefaultBackupService(export, restore, resolver, fast)
    }

    @Test
    fun plan_created_on_the_15th_with_entered_3gb_shows_3gb_plus_later_measured_bytes() = runBlocking {
        setTime("2026-10-15T12:00:00Z")
        val enteredAt = Instant.parse("2026-10-15T12:00:00Z")
        val cycleStart = Instant.parse("2026-10-01T00:00:00Z")
        val plan = Plan(
            id = 0L,
            name = "Mid-Cycle 3GB",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc),
            rollover = Rollover.None,
            firstCycle = FirstCycle.Entered(
                usedBytes = 3_000_000_000L,
                enteredAt = enteredAt,
                cycleStart = cycleStart,
            ),
        )
        val saveResult = repository.upsertPlan(plan)
        assertTrue(saveResult is Outcome.Success)
        val planId = (saveResult as Outcome.Success).value

        // Usage before enteredAt (should be ignored for this plan)
        addUsage("2026-10-10T10:00:00Z", 2_000_000_000L)

        // Usage after enteredAt (should be added)
        addUsage("2026-10-16T10:00:00Z", 500_000_000L)

        setTime("2026-10-17T12:00:00Z")
        val state = repository.observePlanState(planId).first()
        requireNotNull(state)

        assertEquals(3_000_000_000L + 500_000_000L, state.usedBytes)
        assertEquals(3_000_000_000L, state.enteredBytes)
        assertEquals(10_000_000_000L - 3_500_000_000L, state.remainingBytes)
        assertFalse(state.isDormant)
    }

    @Test
    fun plan_created_on_the_15th_starting_next_cycle_shows_nothing_until_the_1st_then_counts_from_zero() = runBlocking {
        setTime("2026-10-15T12:00:00Z")
        val startsAt = Instant.parse("2026-11-01T00:00:00Z")
        val plan = Plan(
            id = 0L,
            name = "Starts Next Month",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc),
            rollover = Rollover.None,
            firstCycle = FirstCycle.StartsNextCycle(startsAt = startsAt),
        )
        val saveResult = repository.upsertPlan(plan)
        assertTrue(saveResult is Outcome.Success)
        val planId = (saveResult as Outcome.Success).value

        // Measured usage during current cycle (October)
        addUsage("2026-10-20T10:00:00Z", 4_000_000_000L)

        // Still in October: plan is dormant, shows 0 used, cap remaining
        setTime("2026-10-25T12:00:00Z")
        val dormantState = repository.observePlanState(planId).first()
        requireNotNull(dormantState)
        assertTrue(dormantState.isDormant)
        assertEquals(0L, dormantState.usedBytes)
        assertEquals(10_000_000_000L, dormantState.remainingBytes)
        assertEquals(startsAt, dormantState.startsAt)

        // Move to November (after 1st): dormant lifts, counts from zero
        setTime("2026-11-02T12:00:00Z")
        val activeState = repository.observePlanState(planId).first()
        requireNotNull(activeState)
        assertFalse(activeState.isDormant)
        assertEquals(0L, activeState.usedBytes)
        assertEquals(10_000_000_000L, activeState.remainingBytes)

        // New usage in November
        addUsage("2026-11-03T10:00:00Z", 1_200_000_000L)
        val updatedState = repository.observePlanState(planId).first()
        requireNotNull(updatedState)
        assertEquals(1_200_000_000L, updatedState.usedBytes)
        assertEquals(8_800_000_000L, updatedState.remainingBytes)
    }

    @Test
    fun entered_plan_across_the_cycle_boundary_resets_to_phone_record() = runBlocking {
        setTime("2026-10-15T12:00:00Z")
        val cycleStart = Instant.parse("2026-10-01T00:00:00Z")
        val enteredAt = Instant.parse("2026-10-15T12:00:00Z")
        val plan = Plan(
            id = 0L,
            name = "Entered Transition",
            subscriptionId = 1,
            capBytes = 10_000_000_000L,
            cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc),
            rollover = Rollover.None,
            firstCycle = FirstCycle.Entered(
                usedBytes = 4_000_000_000L,
                enteredAt = enteredAt,
                cycleStart = cycleStart,
            ),
        )
        val saveResult = repository.upsertPlan(plan)
        assertTrue(saveResult is Outcome.Success)
        val planId = (saveResult as Outcome.Success).value

        addUsage("2026-10-18T10:00:00Z", 1_000_000_000L)

        // In October: 4GB entered + 1GB measured = 5GB
        setTime("2026-10-25T12:00:00Z")
        val octState = repository.observePlanState(planId).first()
        requireNotNull(octState)
        assertEquals(5_000_000_000L, octState.usedBytes)
        assertEquals(4_000_000_000L, octState.enteredBytes)

        // Move to November across the boundary: resets enteredBytes, only measures November usage
        setTime("2026-11-05T12:00:00Z")
        addUsage("2026-11-02T10:00:00Z", 750_000_000L)

        val novState = repository.observePlanState(planId).first()
        requireNotNull(novState)
        assertFalse(novState.isDormant)
        assertNull(novState.enteredBytes)
        assertEquals(750_000_000L, novState.usedBytes)
        assertEquals(9_250_000_000L, novState.remainingBytes)
    }

    @Test
    fun restore_of_a_backup_with_entered_and_later_plans_gives_identical_states() = runBlocking {
        val dbB = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            setTime("2026-10-15T12:00:00Z")
            val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc)

            // Plan 1: PhoneRecord
            val plan1 = Plan(1L, "Phone Plan", 1, 10_000_000_000L, cycle, Rollover.None, false, FirstCycle.PhoneRecord, pinned = false)
            // Plan 2: Entered mid-cycle
            val plan2 = Plan(
                2L, "Entered Plan", 2, 20_000_000_000L, cycle, Rollover.None, false,
                FirstCycle.Entered(4_000_000_000L, Instant.parse("2026-10-10T12:00:00Z"), Instant.parse("2026-10-01T00:00:00Z")),
                pinned = false,
            )
            // Plan 3: StartsNextCycle
            val plan3 = Plan(
                3L, "Later Plan", 3, 15_000_000_000L, cycle, Rollover.None, false,
                FirstCycle.StartsNextCycle(Instant.parse("2026-11-01T00:00:00Z")),
                pinned = true,
            )

            planStore.savePlanWithChildren(plan1, emptyList(), emptyList(), setOf(80))
            planStore.savePlanWithChildren(plan2, emptyList(), emptyList(), setOf(50, 90))
            planStore.savePlanWithChildren(plan3, emptyList(), emptyList(), emptySet())

            // Hourly usage (which is preserved in backups)
            addHourlyUsage("2026-10-12T10:00:00Z", 1_000_000_000L, subId = 1, pkg = "com.app.one", uid = 10001)
            addHourlyUsage("2026-10-12T11:00:00Z", 2_000_000_000L, subId = 2, pkg = "com.app.two", uid = 10002)

            val statesA = repository.observeActivePlanStates().first().associateBy { it.plan.name }
            assertEquals(3, statesA.size)

            // Backup DB A
            val backupBytes = ByteArrayOutputStream()
            val backupServiceA = createBackupService(db)
            val backupResult = backupServiceA.createBackup(backupBytes, "passphrase123".toCharArray())
            assertTrue(backupResult is Outcome.Success)

            // Restore into DB B
            val backupServiceB = createBackupService(dbB)
            val restoreResult = backupServiceB.restoreBackup(ByteArrayInputStream(backupBytes.toByteArray()), "passphrase123".toCharArray())
            assertTrue("Restore must succeed: $restoreResult", restoreResult is Outcome.Success)

            val planStoreB = RoomPlanStore(dbB)
            val usageStoreB = RoomUsageStore(dbB)
            val ticksB = MutableSharedFlow<Instant>(replay = 1)
            ticksB.tryEmit(currentInstant)
            val repoB = RoomPlanRepository(planStoreB, usageStoreB, clock, ticksB)

            val statesB = repoB.observeActivePlanStates().first().associateBy { it.plan.name }
            assertEquals(statesA.keys, statesB.keys)

            for (name in statesA.keys) {
                val a = statesA[name]!!
                val b = statesB[name]!!
                assertEquals("Cap match for $name", a.plan.capBytes, b.plan.capBytes)
                assertEquals("FirstCycle match for $name", a.plan.firstCycle, b.plan.firstCycle)
                assertEquals("Pinned match for $name", a.plan.pinned, b.plan.pinned)
                assertEquals("UsedBytes match for $name", a.usedBytes, b.usedBytes)
                assertEquals("RemainingBytes match for $name", a.remainingBytes, b.remainingBytes)
                assertEquals("EffectiveCapBytes match for $name", a.effectiveCapBytes, b.effectiveCapBytes)
                assertEquals("IsDormant match for $name", a.isDormant, b.isDormant)
                assertEquals("StartsAt match for $name", a.startsAt, b.startsAt)
                assertEquals("EnteredBytes match for $name", a.enteredBytes, b.enteredBytes)
            }
        } finally {
            dbB.close()
        }
    }

    @Test
    fun pinned_plan_survives_backup_roundtrip_and_leads_the_urgency_order() = runBlocking {
        val dbB = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            setTime("2026-10-15T12:00:00Z")
            val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, utc)

            // Plan 1: High usage (90%), unpinned
            val plan1 = Plan(1L, "Normal Unpinned High Usage", 1, 10_000_000_000L, cycle, Rollover.None, false, FirstCycle.PhoneRecord, pinned = false)
            // Plan 2: Low usage (10%), pinned
            val plan2 = Plan(2L, "Important Pinned Low Usage", 2, 10_000_000_000L, cycle, Rollover.None, false, FirstCycle.PhoneRecord, pinned = true)

            planStore.savePlanWithChildren(plan1, emptyList(), emptyList(), emptySet())
            planStore.savePlanWithChildren(plan2, emptyList(), emptyList(), emptySet())

            // 9 GB on sub 1, 1 GB on sub 2 via hourly usage
            addHourlyUsage("2026-10-10T10:00:00Z", 9_000_000_000L, subId = 1, pkg = "com.app.work", uid = 10001)
            addHourlyUsage("2026-10-10T11:00:00Z", 1_000_000_000L, subId = 2, pkg = "com.app.family", uid = 10002)

            // Backup DB A
            val backupBytes = ByteArrayOutputStream()
            val backupServiceA = createBackupService(db)
            val backupResult = backupServiceA.createBackup(backupBytes, "passphrase123".toCharArray())
            assertTrue(backupResult is Outcome.Success)

            // Restore into DB B
            val backupServiceB = createBackupService(dbB)
            val restoreResult = backupServiceB.restoreBackup(ByteArrayInputStream(backupBytes.toByteArray()), "passphrase123".toCharArray())
            assertTrue(restoreResult is Outcome.Success)

            val planStoreB = RoomPlanStore(dbB)
            val usageStoreB = RoomUsageStore(dbB)
            val ticksB = MutableSharedFlow<Instant>(replay = 1)
            ticksB.tryEmit(currentInstant)
            val repoB = RoomPlanRepository(planStoreB, usageStoreB, clock, ticksB)

            val activeStatesB = repoB.observeActivePlanStates().first()
            assertEquals(2, activeStatesB.size)

            val restoredPlans = planStoreB.observePlans().first().associateBy { it.name }
            assertEquals(false, restoredPlans["Normal Unpinned High Usage"]?.pinned)
            assertEquals(true, restoredPlans["Important Pinned Low Usage"]?.pinned)

            // Check urgency ranking
            val ranked = PlanUrgency.order(activeStatesB, currentInstant)
            val front = PlanUrgency.front(activeStatesB, currentInstant)

            assertEquals("Important Pinned Low Usage", ranked.first().plan.name)
            assertTrue(ranked.first().plan.pinned)
            assertEquals("Important Pinned Low Usage", front?.plan?.name)
        } finally {
            dbB.close()
        }
    }
}
