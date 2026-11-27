package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.data.backup.BackupService
import io.github.khaledbahaaeldin.emberbyte.data.backup.RestoreSummary
import io.github.khaledbahaaeldin.emberbyte.data.export.ExportService
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class SettingsContentTest {
    @get:Rule val rule = createComposeRule()

    @Before fun setup() { RevealGate.enabled = false }
    @After fun teardown() { RevealGate.enabled = true }

    private fun show(initial: Settings = Settings()): FakeSettingsRepository {
        val repo = FakeSettingsRepository(initial)
        val svm = SettingsViewModel(repo, usage = FakeUsageRepository())
        val dvm = DataViewModel(
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
                override fun output(uri: android.net.Uri) = ByteArrayOutputStream()
                override fun input(uri: android.net.Uri) = ByteArrayInputStream(ByteArray(0))
            },
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec(darkTheme = true)) {
                SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = {})
            }
        }
        return repo
    }

    @Test
    fun toggling_spike_alerts_updates_spike_alerts_enabled() = runBlocking {
        val repo = show(Settings(spikeAlertsEnabled = true))
        rule.onNodeWithTag("hub_switch_spikes").performClick()
        rule.waitForIdle()
        val updated = withTimeout(2_000) { repo.observe().first { !it.spikeAlertsEnabled } }
        assertFalse(updated.spikeAlertsEnabled)
    }

    @Test
    fun toggling_the_live_notification_updates_only_that_field() = runBlocking {
        val repo = show(Settings(liveNotificationEnabled = true))
        rule.onNodeWithTag("hub_switch_live").performClick()
        rule.waitForIdle()
        val updated = withTimeout(2_000) { repo.observe().first { !it.liveNotificationEnabled } }
        assertFalse(updated.liveNotificationEnabled)
    }

    @Test
    fun mode_bar_switches_theme_mode() = runBlocking {
        val repo = show(Settings(themeMode = ThemeMode.SYSTEM))
        rule.onNodeWithTag("mode_bar_dark").performClick()
        rule.waitForIdle()
        val updated = withTimeout(2_000) { repo.observe().first { it.themeMode == ThemeMode.DARK } }
        assertEquals(ThemeMode.DARK, updated.themeMode)
    }

    @Test
    fun units_screen_switches_unit_system() = runBlocking {
        val repo = show(Settings(unitSystem = UnitSystem.DECIMAL))
        rule.onNodeWithTag("hub_card_units").performClick()
        rule.onNodeWithTag("unit_binary_button").performClick()
        rule.waitForIdle()
        val updated = withTimeout(2_000) { repo.observe().first { it.unitSystem == UnitSystem.BINARY } }
        assertEquals(UnitSystem.BINARY, updated.unitSystem)
        assertTrue(updated.unitSystem == UnitSystem.BINARY)
    }

    @Test
    fun about_card_opens_about_screen_and_licences_row_is_visible() {
        show()
        rule.onNodeWithTag("hub_card_about").performClick()
        rule.onNodeWithTag("about_licences_row").assertIsDisplayed()
    }
}
