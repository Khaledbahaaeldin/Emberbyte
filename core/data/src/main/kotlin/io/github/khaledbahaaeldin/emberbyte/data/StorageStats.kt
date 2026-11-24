package io.github.khaledbahaaeldin.emberbyte.data

import android.content.Context
import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import java.io.File
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StorageCounts(
    val hourlyRows: Long,
    val dailyRows: Long,
    val planRows: Long,
    val otherRows: Long,
    val oldestAt: Instant? = null,
)

data class StorageSnapshot(
    val fileBytes: Long,
    val hourlyRows: Long,
    val dailyRows: Long,
    val planRows: Long,
    val otherRows: Long,
    val oldestAt: Instant? = null,
)

class StorageStats(
    private val dbFileProvider: () -> File,
    private val countsProvider: suspend () -> StorageCounts,
) {
    suspend fun snapshot(): Outcome<StorageSnapshot> = withContext(Dispatchers.IO) {
        val fileBytes = runCatching {
            val f = dbFileProvider()
            val wal = File(f.path + "-wal")
            val shm = File(f.path + "-shm")
            (if (f.exists()) f.length() else 0L) +
                (if (wal.exists()) wal.length() else 0L) +
                (if (shm.exists()) shm.length() else 0L)
        }.getOrDefault(0L)

        val counts = try {
            countsProvider()
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            return@withContext Outcome.Success(
                StorageSnapshot(
                    fileBytes = fileBytes,
                    hourlyRows = 0L,
                    dailyRows = 0L,
                    planRows = 0L,
                    otherRows = 0L,
                    oldestAt = null,
                ),
            )
        }

        Outcome.Success(
            StorageSnapshot(
                fileBytes = fileBytes,
                hourlyRows = counts.hourlyRows,
                dailyRows = counts.dailyRows,
                planRows = counts.planRows,
                otherRows = counts.otherRows,
                oldestAt = counts.oldestAt,
            ),
        )
    }

    companion object {
        fun create(context: Context, db: EmberbyteDatabase): StorageStats = StorageStats(
            dbFileProvider = { context.applicationContext.getDatabasePath("emberbyte.db") },
            countsProvider = {
                val sqlite = db.openHelper.readableDatabase
                fun countTable(table: String): Long =
                    sqlite.query("SELECT COUNT(*) FROM $table").use { c ->
                        if (c.moveToFirst()) c.getLong(0) else 0L
                    }
                val oldestMillis = sqlite.query("SELECT MIN(hourStartMillis) FROM usage_hourly").use { c ->
                    if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else null
                }
                val hourly = countTable("usage_hourly")
                val daily = countTable("total_minute")
                val plans = countTable("plans") + countTable("add_ons") + countTable("free_rules") + countTable("alert_rules")
                val other = countTable("app_meta") + countTable("app_budgets") + countTable("coverage_gaps") + countTable("spike_events")
                StorageCounts(
                    hourlyRows = hourly,
                    dailyRows = daily,
                    planRows = plans,
                    otherRows = other,
                    oldestAt = oldestMillis?.let(Instant::ofEpochMilli),
                )
            },
        )
    }
}
