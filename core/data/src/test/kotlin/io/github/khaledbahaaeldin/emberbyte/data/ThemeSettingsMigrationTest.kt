package io.github.khaledbahaaeldin.emberbyte.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ThemeSettingsMigrationTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun TestScope.store() =
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "prefs.preferences_pb") }

    private val legacyDynamicColorKey = booleanPreferencesKey("use_dynamic_color")
    private val themeIdKey = stringPreferencesKey("theme_id")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val useWallpaperColorsKey = booleanPreferencesKey("use_wallpaper_colors")
    private val themeMigratedKey = booleanPreferencesKey("theme_migrated")

    @Test
    fun fresh_install_gets_dusk_and_no_wallpaper_colors() = runTest {
        val dataStore = store()
        val repo = DataStoreSettingsRepository(dataStore)

        val settings = repo.observe().first()
        assertEquals("dusk", settings.themeId)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertFalse(settings.useWallpaperColors)

        val stored = dataStore.data.first()
        assertEquals(true, stored[themeMigratedKey])
        assertEquals("dusk", stored[themeIdKey])
        assertEquals("system", stored[themeModeKey])
        assertEquals(false, stored[useWallpaperColorsKey])
    }

    @Test
    fun legacy_true_keeps_wallpaper_colors_on() = runTest {
        val dataStore = store()
        dataStore.edit { it[legacyDynamicColorKey] = true }

        val repo = DataStoreSettingsRepository(dataStore)
        val settings = repo.observe().first()

        assertEquals("dusk", settings.themeId)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.useWallpaperColors)

        val stored = dataStore.data.first()
        assertEquals(true, stored[themeMigratedKey])
        assertEquals(true, stored[useWallpaperColorsKey])
    }

    @Test
    fun legacy_false_gets_dusk() = runTest {
        val dataStore = store()
        dataStore.edit { it[legacyDynamicColorKey] = false }

        val repo = DataStoreSettingsRepository(dataStore)
        val settings = repo.observe().first()

        assertEquals("dusk", settings.themeId)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertFalse(settings.useWallpaperColors)

        val stored = dataStore.data.first()
        assertEquals(true, stored[themeMigratedKey])
        assertEquals("dusk", stored[themeIdKey])
        assertEquals(false, stored[useWallpaperColorsKey])
    }

    @Test
    fun migration_runs_once_even_if_the_legacy_key_changes_later() = runTest {
        val dataStore = store()
        dataStore.edit { it[legacyDynamicColorKey] = false }

        val repo = DataStoreSettingsRepository(dataStore)
        val firstRead = repo.observe().first()
        assertFalse(firstRead.useWallpaperColors)

        val dataStore2 = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmp.root, "prefs2.preferences_pb")
        }
        dataStore2.edit {
            it[themeMigratedKey] = true
            it[useWallpaperColorsKey] = false
            it[legacyDynamicColorKey] = true
        }
        val repo2 = DataStoreSettingsRepository(dataStore2)
        val secondRead = repo2.observe().first()
        assertFalse(secondRead.useWallpaperColors)
    }

    @Test
    fun a_migrated_user_keeps_their_chosen_theme() = runTest {
        val dataStore = store()
        dataStore.edit {
            it[themeMigratedKey] = true
            it[themeIdKey] = "kanagawa"
            it[themeModeKey] = "light"
            it[useWallpaperColorsKey] = true
            it[legacyDynamicColorKey] = false
        }

        val repo = DataStoreSettingsRepository(dataStore)
        val settings = repo.observe().first()

        assertEquals("kanagawa", settings.themeId)
        assertEquals(ThemeMode.LIGHT, settings.themeMode)
        assertTrue(settings.useWallpaperColors)
    }
}
