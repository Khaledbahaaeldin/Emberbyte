package io.github.khaledbahaaeldin.emberbyte.data.backup

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.export.StreamingExportService
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.store.RoomUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.usage.AppMeta
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.zip.GZIPOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BackupCompatibilityTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC)
    private val fast = BackupCodec.Params(memoryKiB = 64, iterations = 1, parallelism = 1)
    private lateinit var db: EmberbyteDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java).allowMainThreadQueries().build()
    }

    @After fun tearDown() {
        db.close()
    }

    private fun service(): DefaultBackupService {
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

    private fun gzip(data: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { it.write(data) }
        return out.toByteArray()
    }

    @Test
    fun v1_fixture_restores_all_plans_as_phone_and_unpinned() = runBlocking {
        val stream: InputStream = javaClass.classLoader?.getResourceAsStream("backup_v1_with_plans.emberbyte")
            ?: File("core/data/src/test/resources/backup_v1_with_plans.emberbyte").inputStream()
        val result = service().restoreBackup(stream, "test-passphrase".toCharArray())
        assertTrue("Restore of v1 fixture must succeed: $result", result is Outcome.Success)

        val restoredPlans = RoomPlanStore(db).observePlans().first()
        assertTrue("Restored plans must not be empty", restoredPlans.isNotEmpty())
        for (plan in restoredPlans) {
            assertEquals("V1 plan must have PhoneRecord", FirstCycle.PhoneRecord, plan.firstCycle)
            assertFalse("V1 plan must be unpinned", plan.pinned)
        }
    }

    @Test
    fun version_3_is_rejected_with_invalid_file_and_an_update_message() = runBlocking {
        val payload = """
        {
          "contractVersion": 3,
          "exportedAt": "2026-10-18T12:00:00Z",
          "plans": [],
          "appBudgets": [],
          "usageHourly": []
        }
        """.trimIndent()

        val sealed = BackupCodec.seal(gzip(payload.toByteArray()), "passphrase123".toCharArray(), fast)
        val result = service().restoreBackup(ByteArrayInputStream(sealed), "passphrase123".toCharArray())

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error as EmberbyteError.Invalid
        assertEquals("file", error.field)
        assertTrue("Reason must ask for update: ${error.reason}", error.reason.lowercase().contains("update"))
    }

    @Test
    fun writer_writes_contract_version_2() = runBlocking {
        val out = ByteArrayOutputStream()
        val result = service().createBackup(out, "passphrase123".toCharArray())
        assertTrue(result is Outcome.Success)

        val unsealed = BackupCodec.open(out.toByteArray(), "passphrase123".toCharArray())
        val gzipStream = java.util.zip.GZIPInputStream(ByteArrayInputStream(unsealed))
        val json = JSONObject(gzipStream.bufferedReader().readText())
        assertEquals(2, json.getInt("contractVersion"))
    }

    @Test
    fun corrupt_first_cycle_object_does_not_crash_and_is_reported_invalid() = runBlocking {
        fun testCorruptPayload(planFirstCycleJson: String) {
            val payload = """
            {
              "contractVersion": 2,
              "exportedAt": "2026-10-18T12:00:00Z",
              "plans": [
                {
                  "id": 1,
                  "name": "Corrupt",
                  "subscriptionId": null,
                  "capBytes": 1000,
                  "cycle": {
                    "type": "monthlyOnDay",
                    "dayOfMonth": 1,
                    "time": "00:00",
                    "zone": "UTC"
                  },
                  "rollover": { "type": "none" },
                  "archived": false,
                  "firstCycle": $planFirstCycleJson,
                  "pinned": false
                }
              ],
              "appBudgets": [],
              "usageHourly": []
            }
            """.trimIndent()

            val sealed = BackupCodec.seal(gzip(payload.toByteArray()), "passphrase123".toCharArray(), fast)
            val result = runBlocking { service().restoreBackup(ByteArrayInputStream(sealed), "passphrase123".toCharArray()) }
            assertTrue("Expected failure for firstCycle=$planFirstCycleJson, got $result", result is Outcome.Failure)
            val error = (result as Outcome.Failure).error as EmberbyteError.Invalid
            assertEquals("file", error.field)
        }

        // 1. missing mode
        testCorruptPayload("""{}""")
        // 2. negative usedBytes
        testCorruptPayload("""{"mode": "entered", "usedBytes": -100, "enteredAt": 1000, "cycleStart": 500}""")
        // 3. later without startsAt
        testCorruptPayload("""{"mode": "later"}""")
    }
}
