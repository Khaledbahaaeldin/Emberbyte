package io.github.khaledbahaaeldin.emberbyte.data

import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageStatsTest {
    @Test
    fun storage_snapshot_falls_back_to_the_file_total_on_error() = runBlocking {
        val tempFile = File.createTempFile("emberbyte_db", ".db")
        try {
            tempFile.writeBytes(ByteArray(4096))
            val stats = StorageStats(
                dbFileProvider = { tempFile },
                countsProvider = { error("DB unavailable") },
            )
            val result = stats.snapshot()
            assertTrue(result is Outcome.Success<*>)
            val snap = (result as Outcome.Success<StorageSnapshot>).value
            assertEquals(4096L, snap.fileBytes)
            assertEquals(0L, snap.hourlyRows)
            assertEquals(0L, snap.dailyRows)
            assertEquals(0L, snap.planRows)
            assertEquals(0L, snap.otherRows)
        } finally {
            tempFile.delete()
        }
    }
}
