package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.khaledbahaaeldin.emberbyte.common.assertNoTouchTargetSmallerThan48dp
import io.github.khaledbahaaeldin.emberbyte.data.Settings
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class SettingsAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    @Before fun setup() { RevealGate.enabled = false }
    @After fun teardown() { RevealGate.enabled = true }

    private fun show(settings: Settings = Settings()) {
        val repo = FakeSettingsRepository(settings)
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
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = {})
            }
        }
    }

    @Test
    fun switches_in_settings_have_role_switch() {
        show(Settings(liveNotificationEnabled = true, amoledBlack = false))
        val liveNode = rule.onNodeWithTag("hub_switch_live")
        liveNode.assertIsDisplayed()
        liveNode.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))

        val amoledNode = rule.onNodeWithTag("hub_switch_amoled")
        amoledNode.assertIsDisplayed()
        amoledNode.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
    }

    @Test
    @Config(qualifiers = "w411dp-h3000dp")
    fun settings_touch_targets_are_at_least_48dp() {
        show()
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun data_dialogs_touch_targets_are_at_least_48dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CreateBackupPassphraseDialog(onDismiss = {}, onCreate = {})
            }
        }
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun restore_dialogs_touch_targets_are_at_least_48dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                RestoreWarningDialog(onDismiss = {}, onConfirm = {})
            }
        }
        assertNoTouchTargetSmallerThan48dp(rule)
    }

    @Test
    fun csv_range_dialog_touch_targets_are_at_least_48dp() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CsvRangeDialog(onDismiss = {}, onSelectRange = {})
            }
        }
        assertNoTouchTargetSmallerThan48dp(rule)
    }
}
