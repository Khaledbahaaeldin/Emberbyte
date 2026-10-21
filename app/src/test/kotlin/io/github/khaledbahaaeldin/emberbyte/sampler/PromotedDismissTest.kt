package io.github.khaledbahaaeldin.emberbyte.sampler

import android.content.Context
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

    // The receiver only hands this function to a background coroutine, so the test calls it directly: the result
    // then never depends on a thread scheduler, which made the receiver version time out on shared CI runners.
    @Test
    fun dismissal_stores_the_given_date() = runBlocking {
        val repository = (context as EmberbyteApplication).graph.settings
        repository.update { it.copy(promotedDismissedOn = null) }
        assertEquals(null, repository.observe().first().promotedDismissedOn)

        recordPromotedDismissal(repository, LocalDate.of(2026, 10, 21))

        assertEquals("2026-10-21", repository.observe().first().promotedDismissedOn)
    }

    @Test
    fun dismissal_replaces_an_older_date() = runBlocking {
        val repository = (context as EmberbyteApplication).graph.settings
        recordPromotedDismissal(repository, LocalDate.of(2026, 10, 20))
        recordPromotedDismissal(repository, LocalDate.of(2026, 10, 21))

        assertEquals("2026-10-21", repository.observe().first().promotedDismissedOn)
    }
}
