package io.github.khaledbahaaeldin.emberbyte.sampler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Remembers that the user dismissed the promoted notification today; kept apart from the receiver so it can be tested without a thread hop. */
internal suspend fun recordPromotedDismissal(repository: SettingsRepository, today: LocalDate = LocalDate.now()) {
    repository.update { it.copy(promotedDismissedOn = today.toString()) }
}

class PromotedDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as? EmberbyteApplication
        val repository = app?.graph?.settings
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (repository != null) recordPromotedDismissal(repository)
            } finally {
                pending?.finish()
            }
        }
    }
}
