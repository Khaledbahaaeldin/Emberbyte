package io.github.khaledbahaaeldin.emberbyte.settings

import android.net.Uri
import io.github.khaledbahaaeldin.emberbyte.data.backup.BackupService
import io.github.khaledbahaaeldin.emberbyte.data.backup.RestoreSummary
import io.github.khaledbahaaeldin.emberbyte.data.export.ExportService
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPendingStateTest {
    private fun makeDataVm(): DataViewModel = DataViewModel(
        export = object : ExportService {
            override suspend fun exportCsv(range: DateRange, out: OutputStream) = Outcome.Success(Unit)
            override suspend fun exportJson(out: OutputStream) = Outcome.Success(Unit)
        },
        backup = object : BackupService {
            override suspend fun createBackup(out: OutputStream, passphrase: CharArray) = Outcome.Success(Unit)
            override suspend fun restoreBackup(input: InputStream, passphrase: CharArray) =
                Outcome.Success(RestoreSummary(0, 0, 0, 0))
        },
        opener = object : StreamOpener {
            override fun output(uri: Uri) = ByteArrayOutputStream()
            override fun input(uri: Uri) = ByteArrayInputStream(ByteArray(0))
        },
    )

    @Test
    fun held_passphrase_is_cleared_after_take_or_discard() {
        val vm = makeDataVm()
        val secret = "secretPassphrase".toCharArray()
        vm.holdPassphrase(secret)

        val taken = vm.takeHeldPassphrase()
        assertArrayEquals("secretPassphrase".toCharArray(), taken)
        assertNull(vm.takeHeldPassphrase())
    }

    @Test
    fun discarding_held_passphrase_zeroes_and_clears_it() {
        val vm = makeDataVm()
        val secret = "superSecretPassword123".toCharArray()
        vm.holdPassphrase(secret)
        vm.discardHeldPassphrase()

        assertTrue(secret.all { it == '\u0000' })
        assertNull(vm.takeHeldPassphrase())
    }
}
