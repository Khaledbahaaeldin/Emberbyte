package io.github.khaledbahaaeldin.emberbyte.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.export.StreamingExportService
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.store.RoomUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.usage.AppMeta
import java.io.File
import java.time.Clock
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
class SeededBackupFixtureTest {
    private lateinit var context: Context
    private lateinit var db: EmberbyteDatabase

    private val fakePackages = mapOf(
        "com.fake.browser" to AppMeta(1001, "com.fake.browser", "Browser"),
        "com.fake.video" to AppMeta(1002, "com.fake.video", "Video"),
        "com.fake.social" to AppMeta(1003, "com.fake.social", "Social"),
        "com.fake.music" to AppMeta(1004, "com.fake.music", "Music"),
        "com.fake.chat" to AppMeta(1005, "com.fake.chat", "Chat"),
        "com.fake.cloud" to AppMeta(1006, "com.fake.cloud", "Cloud"),
    )

    private class FakePackageResolver(private val packages: Map<String, AppMeta>) : PackageResolver {
        override fun resolve(packageName: String): AppMeta? = packages[packageName]
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun restores_seeded_backup_fixture_with_expected_counts() = runBlocking {
        val planStore = RoomPlanStore(db)
        val budgetStore = RoomBudgetStore(db)
        val usageStore = RoomUsageStore(db)
        val exportService = StreamingExportService(usageStore, planStore, budgetStore, Clock.systemUTC())
        val restoreStore = RoomRestoreStore(db)
        val service = DefaultBackupService(
            export = exportService,
            restore = restoreStore,
            resolver = FakePackageResolver(fakePackages),
        )

        val candidate1 = File("tools/seed/fixtures/small.emberbyte")
        val candidate2 = File("../../tools/seed/fixtures/small.emberbyte")
        val candidate3 = File("../tools/seed/fixtures/small.emberbyte")
        val fixture = listOf(candidate1, candidate2, candidate3).firstOrNull { it.exists() }
            ?: error("tools/seed/fixtures/small.emberbyte not found")

        val result = fixture.inputStream().use { input ->
            service.restoreBackup(input, "test-passphrase".toCharArray())
        }

        assertTrue("Restore failed: $result", result is Outcome.Success)
        val summary = (result as Outcome.Success<RestoreSummary>).value

        assertEquals(1, summary.plans)
        assertEquals(1, summary.budgets)
        assertEquals(1008, summary.usageRows)
        assertEquals(0, summary.skippedUsageRows)

        val plans = planStore.observePlans().first()
        assertEquals(1, plans.size)
        assertEquals(21474836480L, plans.first().capBytes)

        val budgets = budgetStore.budgets()
        assertEquals(1, budgets.size)
        assertEquals("com.fake.video", budgets.first().packageName)
    }
}
