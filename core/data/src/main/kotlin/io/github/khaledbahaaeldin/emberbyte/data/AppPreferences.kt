package io.github.khaledbahaaeldin.emberbyte.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

/** Settings + onboarding + What's new state backed by ONE DataStore file; keeps DataStore types out of `:app`. */
class AppPreferences(
    val settings: SettingsRepository,
    val onboarding: OnboardingRepository,
    val whatsNew: WhatsNewPrefsRepository,
) {
    companion object {
        fun create(context: Context): AppPreferences {
            val store = PreferenceDataStoreFactory.create(
                produceFile = { context.applicationContext.preferencesDataStoreFile("emberbyte_prefs") },
            )
            return AppPreferences(
                DataStoreSettingsRepository(store),
                DataStoreOnboardingRepository(store),
                DataStoreWhatsNewPrefsRepository(store),
            )
        }
    }
}
