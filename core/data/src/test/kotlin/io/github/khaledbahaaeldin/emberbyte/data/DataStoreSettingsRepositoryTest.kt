package io.github.khaledbahaaeldin.emberbyte.data

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.data.backup.BackupCodec
import io.github.khaledbahaaeldin.emberbyte.data.backup.DefaultBackupService
import io.github.khaledbahaaeldin.emberbyte.data.backup.PackageResolver
import io.github.khaledbahaaeldin.emberbyte.data.backup.RoomRestoreStore
import io.github.khaledbahaaeldin.emberbyte.data.budget.RoomBudgetStore
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.export.StreamingExportService
import io.github.khaledbahaaeldin.emberbyte.data.plan.RoomPlanStore
import io.github.khaledbahaaeldin.emberbyte.data.store.RoomUsageStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataStoreSettingsRepositoryTest {
    private val app: Application get() = ApplicationProvider.getApplicationContext()

    @Test
    fun last_backup_at_defaults_to_null_and_round_trips() = runBlocking {
        val file = File(app.filesDir, "test_settings_${System.nanoTime()}.preferences_pb")
        val store = PreferenceDataStoreFactory.create(produceFile = { file })
        val repo = DataStoreSettingsRepository(store)

        assertNull(repo.observe().first().lastBackupAt)

        val timestamp = Instant.parse("2026-11-20T12:34:56Z")
        repo.update { it.copy(lastBackupAt = timestamp) }
        assertEquals(timestamp, repo.observe().first().lastBackupAt)

        repo.update { it.copy(lastBackupAt = null) }
        assertNull(repo.observe().first().lastBackupAt)
    }

    @Test
    fun last_backup_is_not_exported() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(app, EmberbyteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val file = File(app.filesDir, "test_export_${System.nanoTime()}.preferences_pb")
            val store = PreferenceDataStoreFactory.create(produceFile = { file })
            val repo = DataStoreSettingsRepository(store)
            val timestamp = Instant.parse("2026-11-20T12:34:56Z")
            repo.update { it.copy(lastBackupAt = timestamp) }

            val clock = Clock.fixed(timestamp, ZoneOffset.UTC)
            val exportService = StreamingExportService(
                RoomUsageStore(db),
                RoomPlanStore(db),
                RoomBudgetStore(db),
                clock,
            )
            val backupService = DefaultBackupService(
                export = exportService,
                restore = RoomRestoreStore(db),
                resolver = PackageResolver { null },
                params = BackupCodec.Params(memoryKiB = 64, iterations = 1, parallelism = 1),
            )
            val out = ByteArrayOutputStream()
            backupService.createBackup(out, "passphrase1".toCharArray())

            val freshFile = File(app.filesDir, "test_fresh_${System.nanoTime()}.preferences_pb")
            val freshStore = PreferenceDataStoreFactory.create(produceFile = { freshFile })
            val freshRepo = DataStoreSettingsRepository(freshStore)
            backupService.restoreBackup(ByteArrayInputStream(out.toByteArray()), "passphrase1".toCharArray())
            assertNull(freshRepo.observe().first().lastBackupAt)
        } finally {
            db.close()
        }
    }
}
