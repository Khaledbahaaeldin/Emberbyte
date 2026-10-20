package io.github.khaledbahaaeldin.emberbyte.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal object SettingsKeys {
    val LIVE_NOTIFICATION = booleanPreferencesKey("live_notification_enabled")
    val NOTIFICATION_SHOWS_SPEED = booleanPreferencesKey("notification_shows_speed")
    val SPIKE_ALERTS = booleanPreferencesKey("spike_alerts_enabled")
    val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
    val THEME_ID = stringPreferencesKey("theme_id")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val USE_WALLPAPER_COLORS = booleanPreferencesKey("use_wallpaper_colors")
    val THEME_MIGRATED = booleanPreferencesKey("theme_migrated")
    val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    val HAPTICS = booleanPreferencesKey("haptics_enabled")
    val PROMOTED_DISMISSED_ON = stringPreferencesKey("promoted_dismissed_on")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val LEGACY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
}

internal fun Preferences.toSettings(): Settings {
    val d = Settings()
    return Settings(
        liveNotificationEnabled = this[SettingsKeys.LIVE_NOTIFICATION] ?: d.liveNotificationEnabled,
        notificationShowsSpeed = this[SettingsKeys.NOTIFICATION_SHOWS_SPEED] ?: d.notificationShowsSpeed,
        spikeAlertsEnabled = this[SettingsKeys.SPIKE_ALERTS] ?: d.spikeAlertsEnabled,
        amoledBlack = this[SettingsKeys.AMOLED_BLACK] ?: d.amoledBlack,
        themeId = this[SettingsKeys.THEME_ID] ?: d.themeId,
        themeMode = this[SettingsKeys.THEME_MODE]
            ?.let { name -> runCatching { ThemeMode.valueOf(name.uppercase()) }.getOrNull() } ?: d.themeMode,
        useWallpaperColors = this[SettingsKeys.USE_WALLPAPER_COLORS] ?: d.useWallpaperColors,
        unitSystem = this[SettingsKeys.UNIT_SYSTEM]
            ?.let { name -> runCatching { UnitSystem.valueOf(name) }.getOrNull() } ?: d.unitSystem,
        hapticsEnabled = this[SettingsKeys.HAPTICS] ?: d.hapticsEnabled,
        promotedDismissedOn = this[SettingsKeys.PROMOTED_DISMISSED_ON],
    )
}

internal fun MutablePreferences.write(settings: Settings) {
    this[SettingsKeys.LIVE_NOTIFICATION] = settings.liveNotificationEnabled
    this[SettingsKeys.NOTIFICATION_SHOWS_SPEED] = settings.notificationShowsSpeed
    this[SettingsKeys.SPIKE_ALERTS] = settings.spikeAlertsEnabled
    this[SettingsKeys.AMOLED_BLACK] = settings.amoledBlack
    this[SettingsKeys.THEME_ID] = settings.themeId
    this[SettingsKeys.THEME_MODE] = settings.themeMode.name.lowercase()
    this[SettingsKeys.USE_WALLPAPER_COLORS] = settings.useWallpaperColors
    this[SettingsKeys.UNIT_SYSTEM] = settings.unitSystem.name
    this[SettingsKeys.HAPTICS] = settings.hapticsEnabled
    if (settings.promotedDismissedOn != null) {
        this[SettingsKeys.PROMOTED_DISMISSED_ON] = settings.promotedDismissedOn
    } else {
        this.remove(SettingsKeys.PROMOTED_DISMISSED_ON)
    }
}

private fun Flow<Preferences>.safe(): Flow<Preferences> = catch { error ->
    if (error is IOException) emit(emptyPreferences()) else throw error
}

class DataStoreSettingsRepository(private val store: DataStore<Preferences>) : SettingsRepository {
    private var migrated = false

    override fun observe(): Flow<Settings> = flow {
        migrateIfNeeded()
        emitAll(store.data.safe().map { it.toSettings() })
    }

    override suspend fun update(transform: (Settings) -> Settings): Outcome<Unit> = try {
        store.edit { prefs ->
            if (prefs[SettingsKeys.THEME_MIGRATED] == null) {
                migratePreferences(prefs)
            }
            prefs.write(transform(prefs.toSettings()))
        }
        migrated = true
        Outcome.Success(Unit)
    } catch (error: IOException) {
        Outcome.Failure(EmberbyteError.Storage(error.message ?: "I/O error"))
    }

    private suspend fun migrateIfNeeded() {
        if (migrated) return
        try {
            store.edit { p ->
                if (p[SettingsKeys.THEME_MIGRATED] == null) {
                    migratePreferences(p)
                }
            }
            migrated = true
        } catch (_: IOException) {
        }
    }

    private fun migratePreferences(p: MutablePreferences) {
        val legacyDynamic = p[SettingsKeys.LEGACY_USE_DYNAMIC_COLOR]
        if (legacyDynamic == true) {
            p[SettingsKeys.USE_WALLPAPER_COLORS] = true
        } else {
            p[SettingsKeys.THEME_ID] = "dusk"
            p[SettingsKeys.USE_WALLPAPER_COLORS] = false
        }
        if (p[SettingsKeys.THEME_ID] == null) {
            p[SettingsKeys.THEME_ID] = "dusk"
        }
        if (p[SettingsKeys.THEME_MODE] == null) {
            p[SettingsKeys.THEME_MODE] = ThemeMode.SYSTEM.name.lowercase()
        }
        if (p[SettingsKeys.USE_WALLPAPER_COLORS] == null) {
            p[SettingsKeys.USE_WALLPAPER_COLORS] = false
        }
        p[SettingsKeys.THEME_MIGRATED] = true
    }
}

interface OnboardingRepository {
    /** `null` while the stored value is still loading. */
    fun observeCompleted(): Flow<Boolean?>
    suspend fun complete()
}

class DataStoreOnboardingRepository(private val store: DataStore<Preferences>) : OnboardingRepository {
    override fun observeCompleted(): Flow<Boolean?> = store.data.safe()
        .map<Preferences, Boolean?> { it[SettingsKeys.ONBOARDING_COMPLETED] ?: false }
        .onStart { emit(null) }

    override suspend fun complete() {
        store.edit { it[SettingsKeys.ONBOARDING_COMPLETED] = true }
    }
}
