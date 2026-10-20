package io.github.khaledbahaaeldin.emberbyte

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailContent
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailUiState
import io.github.khaledbahaaeldin.emberbyte.apps.AppsContent
import io.github.khaledbahaaeldin.emberbyte.apps.AppsUiState
import io.github.khaledbahaaeldin.emberbyte.data.PermissionState
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakePermissionRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.history.HistoryScreen
import io.github.khaledbahaaeldin.emberbyte.history.HistoryViewModel
import io.github.khaledbahaaeldin.emberbyte.home.HomeContent
import io.github.khaledbahaaeldin.emberbyte.home.HomeUiState
import io.github.khaledbahaaeldin.emberbyte.onboarding.OnboardingContent
import io.github.khaledbahaaeldin.emberbyte.onboarding.OnboardingUiState
import io.github.khaledbahaaeldin.emberbyte.plans.PlanEditorContent
import io.github.khaledbahaaeldin.emberbyte.plans.PlanEditorUiState
import io.github.khaledbahaaeldin.emberbyte.plans.PlansContent
import io.github.khaledbahaaeldin.emberbyte.plans.PlansUiState
import io.github.khaledbahaaeldin.emberbyte.settings.DataUiState
import io.github.khaledbahaaeldin.emberbyte.settings.SettingsContent
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeSweepTest {
    @get:Rule val rule = createComposeRule()

    private val allPresets = listOf(
        "dusk", "ember", "monochrome", "catppuccin", "gruvbox", "nord",
        "rosepine", "solarized", "dracula", "tokyonight", "everforest", "kanagawa"
    )

    private fun sweep(render: @Composable () -> Unit) {
        var spec by mutableStateOf(EmberbyteThemeSpec.Default)
        rule.setContent {
            EmberbyteTheme(spec) {
                val palette = LocalEmberbytePalette.current
                assertNotEquals("Text colour must differ from background", palette.text, palette.background)
                assertNotEquals("Text colour must differ from surface", palette.text, palette.surface)
                assertNotEquals("Text colour must differ from surface2", palette.text, palette.surface2)
                render()
            }
        }
        for (preset in allPresets) {
            for (dark in listOf(true, false)) {
                spec = EmberbyteThemeSpec(presetId = preset, darkTheme = dark, amoled = false, wallpaperColors = false)
                rule.waitForIdle()
            }
        }
    }

    @Test
    fun home_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            HomeContent(
                state = HomeUiState(),
                onEvent = {},
                onOpenSettings = {},
                onOpenHistory = {},
                onPromptAction = {},
                onOpenApp = {},
                onSetUpPlan = {},
            )
        }
    }

    @Test
    fun apps_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            AppsContent(
                state = AppsUiState(loaded = true),
                onEvent = {},
                onOpenApp = {},
                onPromptAction = {},
            )
        }
    }

    @Test
    fun app_detail_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            AppDetailContent(
                state = AppDetailUiState(
                    packageName = "com.example.app",
                    label = "Example App",
                    loaded = true,
                ),
                onEvent = {},
                onBack = {},
            )
        }
    }

    @Test
    fun history_screen_renders_in_all_twelve_themes_dark_and_light() {
        val clock = Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC)
        val vm = HistoryViewModel(
            usage = FakeUsageRepository(clock = clock, tick = flowOf(Unit)),
            settings = FakeSettingsRepository(),
            permissions = FakePermissionRepository(PermissionState(usageAccess = true, notifications = true, phoneState = true)),
            clock = clock,
            locale = Locale.ENGLISH,
            dates = flowOf(LocalDate.of(2026, 10, 7)),
        )
        sweep {
            HistoryScreen(viewModel = vm, onBack = {})
        }
    }

    @Test
    fun plans_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            PlansContent(
                state = PlansUiState(loaded = true),
                onEvent = {},
                onOpenPlan = {},
                onNewPlan = {},
            )
        }
    }

    @Test
    fun plan_editor_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            PlanEditorContent(
                state = PlanEditorUiState(loading = false),
                onEvent = {},
                onBack = {},
                onPrompt = {},
            )
        }
    }

    @Test
    fun settings_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            SettingsContent(
                settings = Settings(),
                onUpdate = {},
                onBack = {},
                versionName = "1.0.0",
                onOpenSource = {},
                onOpenBatterySettings = {},
                onOpenAppearance = {},
                dataUiState = DataUiState(),
                onExportCsv = {},
                onExportJson = {},
                onCreateBackup = {},
                onRestoreChosen = {},
                onRestoreBackup = {},
                onClearDataMessage = {},
                restoreFileChosen = false,
                onDismissRestorePassphrase = {},
            )
        }
    }

    @Test
    fun onboarding_screen_renders_in_all_twelve_themes_dark_and_light() {
        sweep {
            OnboardingContent(
                state = OnboardingUiState(),
                onOpenUsageAccess = {},
                onRequestNotifications = {},
                onSetUpPlan = {},
                onFinish = {},
            )
        }
    }
}
