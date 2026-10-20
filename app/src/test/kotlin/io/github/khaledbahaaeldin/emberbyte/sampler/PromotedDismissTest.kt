package io.github.khaledbahaaeldin.emberbyte.sampler

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

        val receiver = PromotedDismissReceiver()
        receiver.onReceive(context, Intent("io.github.khaledbahaaeldin.emberbyte.action.PROMOTED_DISMISSED"))

        val today = LocalDate.now().toString()
        // Wait / assert stored
        val updated = kotlinx.coroutines.withTimeout(3000) {
            repository.observe().first { it.promotedDismissedOn != null }
        }
        assertEquals(today, updated.promotedDismissedOn)
    }
}
