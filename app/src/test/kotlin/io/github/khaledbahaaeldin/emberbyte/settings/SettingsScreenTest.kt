package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h6000dp")
class SettingsScreenTest {
    @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Before fun setup() { RevealGate.enabled = false }
    @After fun teardown() { RevealGate.enabled = true }

    private fun show(
        onBack: () -> Unit = {},
        settings: Settings = Settings(),
        fontScale: Float = 1f,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) {
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
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density, fontScale = fontScale),
                LocalLayoutDirection provides layoutDirection,
            ) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    SettingsScreen(viewModel = svm, dataViewModel = dvm, onBack = onBack)
                }
            }
        }
    }

    @Test
    fun settings_hub_renders_at_default_scale() {
        show()
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_data").assertIsDisplayed()
    }

    @Test
    fun settings_hub_renders_at_font_scale_2_and_narrow_width() {
        show(fontScale = 2f)
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithTag("hub_switch_live").assertIsDisplayed()
        rule.onNodeWithTag("hub_card_about").assertIsDisplayed()
    }

    @Test
    fun settings_hub_renders_under_rtl() {
        show(layoutDirection = LayoutDirection.Rtl)
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
    }

    @Test
    fun system_back_inside_an_area_returns_to_the_hub_not_to_the_previous_screen() {
        var leftSettings = false
        show(onBack = { leftSettings = true })
        rule.onNodeWithTag("hub_card_about").performScrollTo().performClick()
        rule.onNodeWithTag("about_version_card").assertIsDisplayed()

        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()

        rule.onNodeWithTag("pulse_card").assertIsDisplayed()
        assertEquals("system back closed the area only", false, leftSettings)
    }
}
