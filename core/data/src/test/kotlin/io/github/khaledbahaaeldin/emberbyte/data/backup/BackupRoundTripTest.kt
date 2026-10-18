package io.github.khaledbahaaeldin.emberbyte.data.backup

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.export.StreamingExportService
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.store.RoomUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import io.github.khaledbahaaeldin.emberbyte.engine.usage.AppMeta
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
class BackupRoundTripTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC)
    private val fast = BackupCodec.Params(memoryKiB = 64, iterations = 1, parallelism = 1)
    private lateinit var dbA: EmberbyteDatabase
    private lateinit var dbB: EmberbyteDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        dbA = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
        dbB = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
    }

    @After fun tearDown() {
        dbA.close()
        dbB.close()
    }

    private fun service(db: EmberbyteDatabase): DefaultBackupService {
        val usage = RoomUsageStore(db)
        val plans = RoomPlanStore(db)
        val budgets = RoomBudgetStore(db)
        val export = StreamingExportService(usage, plans, budgets, clock)
        val restore = RoomRestoreStore(db)
        val resolver = object : PackageResolver {
            override fun resolve(packageName: String): AppMeta =
                AppMeta(uid = 10001, packageName = packageName, label = packageName)
        }
        return DefaultBackupService(export, restore, resolver, fast)
    }

    @Test
    fun v2_roundtrips_every_first_cycle_mode_and_pinned() = runBlocking {
        val planStoreA = RoomPlanStore(dbA)
        val cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        val plan1 = Plan(1, "Phone", null, 10_000_000_000L, cycle, Rollover.None, false, FirstCycle.PhoneRecord, pinned = false)
        val plan2 = Plan(2, "PhonePinned", 1, 5_000_000_000L, cycle, Rollover.None, false, FirstCycle.PhoneRecord, pinned = true)
        val plan3 = Plan(3, "Entered", 2, 20_000_000_000L, cycle, Rollover.None, false,
            FirstCycle.Entered(500_000_000L, Instant.parse("2026-10-15T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z")),
            pinned = false)
        val plan4 = Plan(4, "Later", 3, 15_000_000_000L, cycle, Rollover.None, false,
            FirstCycle.StartsNextCycle(Instant.parse("2026-11-01T00:00:00Z")),
            pinned = true)

        planStoreA.savePlanWithChildren(plan1, emptyList(), emptyList(), emptySet())
        planStoreA.savePlanWithChildren(plan2, emptyList(), emptyList(), setOf(80))
        planStoreA.savePlanWithChildren(plan3, emptyList(), emptyList(), setOf(50, 90))
        planStoreA.savePlanWithChildren(plan4, emptyList(), emptyList(), emptySet())

        val backupBytes = ByteArrayOutputStream()
        val backupResult = service(dbA).createBackup(backupBytes, "passphrase123".toCharArray())
        assertTrue("Backup creation must succeed", backupResult is Outcome.Success)

        val restoreResult = service(dbB).restoreBackup(ByteArrayInputStream(backupBytes.toByteArray()), "passphrase123".toCharArray())
        assertTrue("Restore must succeed: $restoreResult", restoreResult is Outcome.Success)

        val planStoreB = RoomPlanStore(dbB)
        val restoredPlans = planStoreB.observePlans().first().associateBy { it.id }

        assertEquals(4, restoredPlans.size)
        assertEquals(plan1, restoredPlans[1])
        assertEquals(plan2, restoredPlans[2])
        assertEquals(plan3, restoredPlans[3])
        assertEquals(plan4, restoredPlans[4])
    }
}
