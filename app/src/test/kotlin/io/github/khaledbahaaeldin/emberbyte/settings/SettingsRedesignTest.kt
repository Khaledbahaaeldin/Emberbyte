package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.StorageSnapshot
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
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class SettingsRedesignTest {
    @get:Rule val rule = createComposeRule()

    private val now = Instant.parse("2026-11-20T10:00:00Z")
    private val fixedClock = Clock.fixed(now, ZoneOffset.UTC)

    @Before
    fun setup() {
        RevealGate.enabled = false
    }

    @After
    fun teardown() {
        RevealGate.enabled = true
    }

    private fun makeViewModels(
        settings: Settings = Settings(),
        batteryOk: Boolean = true,
        usageOk: Boolean = true,
    ): Pair<SettingsViewModel, DataViewModel> {
        val settingsRepo = FakeSettingsRepository(settings)
        val usageRepo = FakeUsageRepository(clock = fixedClock)
        val svm = SettingsViewModel(
            settings = settingsRepo,
            usage = usageRepo,
            batteryUnrestricted = { batteryOk },
            usageAccess = { usageOk },
            clock = fixedClock,
        )
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
            clock = fixedClock,
            settings = settingsRepo,
        )
        return svm to dvm
    }

    private fun setContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            EmberbyteTheme(spec = EmberbyteThemeSpec(darkTheme = true)) {
                content()
            }
        }
    }

    @Test
    fun hub_shows_pulse_switches_mode_bar_and_six_cards() {
        val (svm, dvm) = makeViewModels()
        setContent {
            SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = {}, clock = fixedClock)
        }
        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
        rule.onNodeWithTag("hub_switch_live").assertIsDisplayed()
        rule.onNodeWithTag("hub_switch_speed").assertIsDisplayed()
        rule.onNodeWithTag("hub_switch_spikes").assertIsDisplayed()
        rule.onNodeWithTag("hub_switch_amoled").assertIsDisplayed()
        rule.onNodeWithTag("mode_bar_auto").assertIsDisplayed()
        rule.onNodeWithTag("mode_bar_light").assertIsDisplayed()
        rule.onNodeWithTag("mode_bar_dark").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_look_and_feel").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_notifications").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_alerts").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_units").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_data").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_about").assertIsDisplayed()
    }

    @Test
    fun pulse_card_is_warn_when_a_check_fails() {
        val (svm, dvm) = makeViewModels(batteryOk = false)
        setContent {
            SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = {}, clock = fixedClock)
        }
        rule.onNodeWithText("3/4").assertIsDisplayed()
        rule.onNodeWithTag("pulse_action_button").assertIsDisplayed()
    }

    @Test
    fun the_action_button_and_the_card_tap_are_separate() {
        val (svm, dvm) = makeViewModels(settings = Settings(liveNotificationEnabled = false))
        var openedKeepMeasuring = false
        setContent {
            SettingsScreen(
                viewModel = svm,
                dataViewModel = dvm,
                onBack = {},
                onOpenArea = { area, _ -> if (area == SettingsArea.KeepMeasuring) openedKeepMeasuring = true },
                clock = fixedClock,
            )
        }
        rule.onNodeWithTag("pulse_action_button").performClick()
        assertEquals(false, openedKeepMeasuring)
    }

    @Test
    fun data_card_shows_no_backup_yet_until_a_backup_exists() {
        val (svm1, dvm1) = makeViewModels(settings = Settings(lastBackupAt = null))
        setContent {
            SettingsScreen(viewModel = svm1, dataViewModel = dvm1, onBack = {}, clock = fixedClock)
        }
        rule.onNodeWithText("No backup yet").assertIsDisplayed()
    }

    @Test
    fun notifications_preview_follows_the_switches() {
        val (svm, dvm) = makeViewModels(settings = Settings(liveNotificationEnabled = true, notificationShowsSpeed = true))
        setContent {
            NotificationsScreen(viewModel = svm, isDark = true, onBack = {})
        }
        rule.onNodeWithText("1.4 MB/s").assertIsDisplayed()
        rule.onNodeWithTag("notif_speed_switch").performClick()
        rule.waitForIdle()
        assertTrue(rule.onAllNodesWithText("1.4 MB/s").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun alerts_chart_and_switch() {
        val (svm, dvm) = makeViewModels()
        setContent {
            AlertsScreen(viewModel = svm, isDark = true, onBack = {})
        }
        rule.onNodeWithTag("alerts_example_chart").assertIsDisplayed()
        rule.onNodeWithTag("alerts_spikes_switch").assertIsDisplayed()
    }

    @Test
    fun units_example_shows_both_units() {
        val (svm, dvm) = makeViewModels(settings = Settings(unitSystem = UnitSystem.DECIMAL))
        setContent {
            UnitsScreen(viewModel = svm, isDark = true, onBack = {})
        }
        rule.onNodeWithText("1.00 GB").assertIsDisplayed()
        rule.onNodeWithText("0.93 GiB").assertIsDisplayed()
        rule.onNodeWithText("Decimal matches what your carrier bills. Binary matches what Android storage shows.").assertIsDisplayed()
    }

    @Test
    fun data_screen_has_storage_card_export_and_backup() {
        val snap = StorageSnapshot(11_800_000L, 1000L, 300L, 15L, 85L, now.minusSeconds(86_400L * 30))
        setContent {
            DataBackupContent(
                settings = Settings(),
                storage = snap,
                dataState = DataUiState(),
                isDark = true,
                clock = fixedClock,
                selectedRange = ExportRange.LAST_30_DAYS,
                onBack = {},
                onSelectRange = {},
                onExportCsv = {},
                onExportJson = {},
                onOpenBackupSheet = {},
                onPickRestoreFile = {},
            )
        }
        rule.onNodeWithTag("data_storage_card").assertIsDisplayed()
        rule.onNodeWithTag("export_csv_row").assertIsDisplayed()
        rule.onNodeWithTag("export_json_row").assertIsDisplayed()
        rule.onNodeWithTag("encrypted_backup_row").assertIsDisplayed()
        rule.onNodeWithTag("restore_backup_row").assertIsDisplayed()
    }

    @Test
    fun backup_sheet_blocks_short_passphrases_and_shows_strength() {
        setContent {
            BackupPassphraseSheetBody(
                mode = BackupSheetMode.Backup,
                fileName = null,
                fileSizeBytes = null,
                unitSystem = UnitSystem.DECIMAL,
                onConfirm = {},
            )
        }
        rule.onNodeWithTag("backup_passphrase_field").performTextInput("short")
        rule.onNodeWithTag("backup_sheet_confirm").assertIsNotEnabled()
        rule.onNodeWithText("Too short").assertIsDisplayed()

        rule.onNodeWithTag("backup_passphrase_field").performTextClearance()
        rule.onNodeWithTag("backup_passphrase_field").performTextInput("LongerPass1!")
        rule.waitForIdle()
        rule.onNodeWithTag("backup_sheet_confirm").assertIsEnabled()
        rule.onNodeWithTag("passphrase_strength_label").assertIsDisplayed()
        rule.onNodeWithText("Strong").assertIsDisplayed()
    }

    @Test
    fun restore_sheet_shows_the_file() {
        setContent {
            BackupPassphraseSheetBody(
                mode = BackupSheetMode.Restore,
                fileName = "my-backup.ebk",
                fileSizeBytes = 2048L,
                unitSystem = UnitSystem.DECIMAL,
                onConfirm = {},
            )
        }
        rule.onNodeWithTag("restore_file_info").assertIsDisplayed()
    }

    @Test
    fun keep_measuring_lists_four_checks_with_fix() {
        val (svm, dvm) = makeViewModels(batteryOk = false)
        setContent {
            KeepMeasuringScreen(viewModel = svm, isDark = true, onAction = {}, onBack = {})
        }
        rule.onNodeWithText("Battery unrestricted").assertIsDisplayed()
        rule.onNodeWithText("Usage access").assertIsDisplayed()
        rule.onNodeWithText("Live notification").assertIsDisplayed()
        rule.onNodeWithText("Sampling recently").assertIsDisplayed()
        rule.onNodeWithTag("fix_check_battery").assertIsDisplayed()
    }

    @Test
    fun about_shows_version_and_update_state() {
        setContent {
            AboutScreen(
                versionName = "1.0.3",
                updateSource = UpdateSource.Obtainium,
                isDark = true,
                amoled = false,
                hasWhatsNew = true,
                onOpenWhatsNew = {},
                onOpenLicences = {},
                onBack = {},
            )
        }
        rule.onNodeWithText("Emberbyte 1.0.3").assertIsDisplayed()
        rule.onNodeWithText("Obtainium").assertIsDisplayed()
    }

    @Test
    fun typing_filters_results_and_empty_result_text() {
        val (svm, dvm) = makeViewModels()
        setContent {
            SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = {}, clock = fixedClock)
        }
        rule.onNodeWithTag("settings_search_button").performClick()
        rule.onNodeWithTag("settings_search_field").performTextInput("battery")
        rule.onNodeWithTag("search_result_battery").assertIsDisplayed()

        rule.onNodeWithTag("settings_search_field").performTextInput("zzzzzz")
        rule.onNodeWithText("Nothing matches \"batteryzzzzzz\". Try battery, backup or theme.").assertIsDisplayed()
    }

    @Test
    fun reduced_motion_is_static() {
        val (svm, dvm) = makeViewModels()
        rule.setContent {
            EmberbyteTheme(spec = EmberbyteThemeSpec(darkTheme = true)) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = {}, clock = fixedClock)
                }
            }
        }
        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
    }
}
