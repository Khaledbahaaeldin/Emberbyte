package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.nav.requestIgnoreBatteryOptimizations
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsHub
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsSearchOverlay
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import java.time.Clock
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    dataViewModel: DataViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLicenses: () -> Unit = {},
    onOpenAppearance: () -> Unit = {},
    onOpenWhatsNew: (() -> Unit)? = null,
    hasWhatsNew: Boolean = true,
    onOpenArea: ((SettingsArea, String?) -> Unit)? = null,
    clock: Clock = SystemZoneClock(),
) {
    val hubState by viewModel.hubState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (hubState.settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    LaunchedEffect(isDark) {
        viewModel.setDarkTheme(isDark)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.recheckHealth()
        viewModel.refreshStorage()
    }

    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }
    val installerPackage = remember(context) { InstallSourceReader.getInstallerPackageName(context) }
    val updateSource = remember(installerPackage) { classifyInstaller(installerPackage) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var activeAreaName by rememberSaveable { mutableStateOf<String?>(null) }
    var highlightKey by rememberSaveable { mutableStateOf<String?>(null) }

    val handleHealthAction: (HealthAction) -> Unit = { action ->
        when (action) {
            HealthAction.FixBattery -> context.requestIgnoreBatteryOptimizations()
            HealthAction.TurnOnLive -> {
                viewModel.update { it.copy(liveNotificationEnabled = true) }
            }
            HealthAction.AllowUsageAccess -> {
                val intent = Intent(AndroidSettings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
            }
        }
    }

    // System back inside an area or the search closes that first; without this it left Settings for Home.
    BackHandler(enabled = searchOpen || activeAreaName != null) {
        if (searchOpen) searchOpen = false else activeAreaName = null
    }

    val openArea: (SettingsArea, String?) -> Unit = { area, highlight ->
        if (onOpenArea != null) {
            onOpenArea(area, highlight)
        } else if (area == SettingsArea.LookAndFeel) {
            onOpenAppearance()
        } else {
            activeAreaName = area.name
            highlightKey = highlight
        }
    }

    val palette = LocalEmberbytePalette.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = palette.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val activeArea = activeAreaName?.let { name ->
                SettingsArea.entries.firstOrNull { it.name == name }
            }
            when (activeArea) {
                null -> {
                    SettingsHub(
                        state = hubState,
                        listState = listState,
                        clock = clock,
                        versionName = versionName.ifBlank { "1.0.3" },
                        updateStatus = updateSource.displayName,
                        onBack = onBack,
                        onOpenSearch = { searchOpen = true },
                        onOpenArea = { area -> openArea(area, null) },
                        onHealthAction = handleHealthAction,
                        onToggleSwitch = { id ->
                            val msg = viewModel.toggleSwitch(id, isDark)
                            if (msg != null) {
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        onSelectThemeMode = { mode ->
                            viewModel.update { it.copy(themeMode = mode) }
                        },
                    )
                }
                SettingsArea.Notifications -> NotificationsScreen(
                    viewModel = viewModel,
                    isDark = isDark,
                    highlightKey = highlightKey,
                    onBack = { activeAreaName = null },
                )
                SettingsArea.Alerts -> AlertsScreen(
                    viewModel = viewModel,
                    isDark = isDark,
                    highlightKey = highlightKey,
                    onBack = { activeAreaName = null },
                )
                SettingsArea.Units -> UnitsScreen(
                    viewModel = viewModel,
                    isDark = isDark,
                    highlightKey = highlightKey,
                    onBack = { activeAreaName = null },
                )
                SettingsArea.DataAndBackup -> DataBackupScreen(
                    settingsViewModel = viewModel,
                    dataViewModel = dataViewModel,
                    isDark = isDark,
                    clock = clock,
                    highlightKey = highlightKey,
                    onBack = { activeAreaName = null },
                )
                SettingsArea.KeepMeasuring -> KeepMeasuringScreen(
                    viewModel = viewModel,
                    isDark = isDark,
                    highlightKey = highlightKey,
                    onAction = handleHealthAction,
                    onBack = { activeAreaName = null },
                )
                SettingsArea.About -> AboutScreen(
                    versionName = versionName.ifBlank { "1.0.3" },
                    updateSource = updateSource,
                    isDark = isDark,
                    amoled = hubState.settings.amoledBlack,
                    hasWhatsNew = hasWhatsNew,
                    highlightKey = highlightKey,
                    onOpenWhatsNew = { onOpenWhatsNew?.invoke() },
                    onOpenLicences = onOpenLicenses,
                    onBack = { activeAreaName = null },
                )
                SettingsArea.LookAndFeel -> onOpenAppearance()
            }

            if (searchOpen) {
                SettingsSearchOverlay(
                    onClose = { searchOpen = false },
                    onSelect = { entry ->
                        searchOpen = false
                        openArea(entry.area, entry.highlightKey)
                    },
                )
            }
        }
    }
}
