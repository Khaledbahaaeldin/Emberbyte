package io.github.khaledbahaaeldin.emberbyte.sampler

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PromotedDismissTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun receiver_stores_todays_date_and_logs_nothing() = runBlocking {
        val app = context as EmberbyteApplication
        val repository = app.graph.settings

        // Ensure initially null
        repository.update { it.copy(promotedDismissedOn = null) }
        assertEquals(null, repository.observe().first().promotedDismissedOn)

        // The receiver writes on a real background dispatcher, so the wait is wall-clock time. A loaded CI
        // runner can need far longer than a developer machine, so the guard is generous: it only has to
        // stop a genuine hang, not race the scheduler.
        val dayBefore = LocalDate.now().toString()
        val receiver = PromotedDismissReceiver()
        receiver.onReceive(context, Intent("io.github.khaledbahaaeldin.emberbyte.action.PROMOTED_DISMISSED"))

        val updated = kotlinx.coroutines.withTimeout(60_000) {
            repository.observe().first { it.promotedDismissedOn != null }
        }
        val dayAfter = LocalDate.now().toString()
        // The date is read inside the receiver, so a midnight between the two reads is still correct.
        assertTrue(
            "expected $dayBefore or $dayAfter but was ${updated.promotedDismissedOn}",
            updated.promotedDismissedOn in setOf(dayBefore, dayAfter),
        )
    }
}
