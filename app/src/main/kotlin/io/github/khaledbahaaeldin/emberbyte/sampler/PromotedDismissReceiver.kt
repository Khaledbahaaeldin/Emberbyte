package io.github.khaledbahaaeldin.emberbyte.sampler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.khaledbahaaeldin.emberbyte.EmberbyteApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class PromotedDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as? EmberbyteApplication
        val repository = app?.graph?.settings
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val today = LocalDate.now().toString()
                repository?.update { it.copy(promotedDismissedOn = today) }
            } finally {
                pending?.finish()
            }
        }
    }
}
