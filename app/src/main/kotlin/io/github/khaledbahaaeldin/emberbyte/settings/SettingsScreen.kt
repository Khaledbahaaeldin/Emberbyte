@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.common.EmberbyteScreenScaffold
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.nav.NavDiagnostics
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteShapeRadii
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePreset
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark
import java.time.LocalDate

private const val SOURCE_URL = "https://github.com/Khaledbahaaeldin/Emberbyte"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    dataViewModel: DataViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLicenses: () -> Unit = {},
    onOpenAppearance: () -> Unit = {},
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    val dataUiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }

    val installerPackage = remember(context) { InstallSourceReader.getInstallerPackageName(context) }
    val updateSource = remember(installerPackage) { classifyInstaller(installerPackage) }

    val pendingState = rememberSaveable(saver = SettingsPendingState.Saver) { SettingsPendingState() }
    val promotedGate = remember(context) { io.github.khaledbahaaeldin.emberbyte.sampler.PromotedGate(context) }

    val createCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { dataViewModel.exportCsv(pendingState.pendingRange, it) }
    }
    val createJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(dataViewModel::exportJson)
    }
    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) {
            dataViewModel.discardHeldPassphrase()
        } else {
            when (val action = backupPickerResult(uri, dataViewModel.takeHeldPassphrase())) {
                is BackupPickerAction.Create -> dataViewModel.createBackup(action.uri, action.passphrase)
                is BackupPickerAction.DeleteAndRetry -> {
                    runCatching { DocumentsContract.deleteDocument(context.contentResolver, action.uri) }
                    dataViewModel.showMessage("The backup was cancelled because the screen was recreated. Please try again.", isError = true)
                }
                BackupPickerAction.Ignore -> {}
            }
        }
    }
    val openBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingState.pendingRestoreUri = uri
    }

    SettingsContent(
        settings = settings,
        onUpdate = viewModel::update,
        onBack = onBack,
        versionName = versionName,
        onOpenSource = { runCatching { uriHandler.openUri(SOURCE_URL) } },
        onOpenLicenses = onOpenLicenses,
        updateSource = updateSource,
        onOpenUpdater = {
            val launch = installerPackage?.let { pkg ->
                runCatching { context.packageManager.getLaunchIntentForPackage(pkg) }.getOrNull()
            }
            if (launch != null) {
                runCatching { context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            } else {
                runCatching { uriHandler.openUri(RELEASES_URL) }
            }
        },
        onOpenReleases = { runCatching { uriHandler.openUri(RELEASES_URL) } },
        onCopyNavLog = {
            val info = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
            val header = "Emberbyte $versionName (code ${info?.longVersionCode ?: 0}) | Android ${Build.VERSION.RELEASE} " +
                "(API ${Build.VERSION.SDK_INT}) | ${Build.MANUFACTURER} ${Build.MODEL}"
            val count = NavDiagnostics.copyToClipboard(context, header)
            Toast.makeText(context, "Copied navigation log ($count events)", Toast.LENGTH_SHORT).show()
        },
        onOpenBatterySettings = {
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        },
        dataUiState = dataUiState,
        onExportCsv = { range ->
            pendingState.pendingRange = range
            val date = LocalDate.now(dataViewModel.clock)
            createCsv.launch("emberbyte-usage-$date.csv")
        },
        onExportJson = {
            val date = LocalDate.now(dataViewModel.clock)
            createJson.launch("emberbyte-export-$date.json")
        },
        onCreateBackup = { passphrase ->
            dataViewModel.holdPassphrase(passphrase)
            val date = LocalDate.now(dataViewModel.clock)
            createBackup.launch("emberbyte-backup-$date.emberbyte")
        },
        onRestoreChosen = {
            openBackup.launch(arrayOf("application/octet-stream", "*/*"))
        },
        onRestoreBackup = { passphrase ->
            val uri = pendingState.pendingRestoreUri
            if (uri != null) {
                dataViewModel.restoreBackup(uri, passphrase)
                pendingState.pendingRestoreUri = null
            }
        },
        onClearDataMessage = dataViewModel::clearMessage,
        restoreFileChosen = pendingState.pendingRestoreUri != null,
        onDismissRestorePassphrase = {
            pendingState.pendingRestoreUri = null
        },
        onOpenAppearance = onOpenAppearance,
        mayPostLiveUpdates = promotedGate.mayPost(),
        onOpenLiveUpdatesSettings = {
            runCatching {
                context.startActivity(promotedGate.settingsIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
        modifier = modifier,
    )
}

class SettingsPendingState(
    initialRange: ExportRange = ExportRange.LAST_7_DAYS,
    initialRestoreUri: Uri? = null,
) {
    var pendingRange: ExportRange by mutableStateOf(initialRange)
    var pendingRestoreUri: Uri? by mutableStateOf(initialRestoreUri)
    var pendingCreatePassphrase: CharArray? by mutableStateOf(null)

    companion object {
        val Saver: Saver<SettingsPendingState, Map<String, Any?>> = Saver(
            save = { state ->
                mapOf(
                    "range" to state.pendingRange.name,
                    "restoreUri" to state.pendingRestoreUri?.toString(),
                )
            },
            restore = { map ->
                val rangeName = map["range"] as? String
                val range = rangeName?.let { runCatching { ExportRange.valueOf(it) }.getOrNull() } ?: ExportRange.LAST_7_DAYS
                val uriStr = map["restoreUri"] as? String
                val uri = uriStr?.toUri()
                SettingsPendingState(range, uri)
            },
        )
    }
}

internal fun previewPresetsFor(themeId: String, count: Int = 5): List<ThemePreset> {
    val firstN = ThemePresets.take(count)
    val selected = ThemePresets.firstOrNull { it.id == themeId }
    return if (selected == null || firstN.any { it.id == selected.id }) {
        firstN
    } else {
        firstN.dropLast(1) + selected
    }
}

@Composable
internal fun SettingsContent(
    settings: Settings,
    onUpdate: ((Settings) -> Settings) -> Unit,
    onBack: () -> Unit,
    versionName: String,
    onOpenSource: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLicenses: () -> Unit = {},
    dataUiState: DataUiState = DataUiState(),
    onExportCsv: (ExportRange) -> Unit = {},
    onExportJson: () -> Unit = {},
    onCreateBackup: (CharArray) -> Unit = {},
    onRestoreChosen: () -> Unit = {},
    onRestoreBackup: (CharArray) -> Unit = {},
    onClearDataMessage: () -> Unit = {},
    restoreFileChosen: Boolean = false,
    onDismissRestorePassphrase: () -> Unit = {},
    onOpenAppearance: () -> Unit = {},
    mayPostLiveUpdates: Boolean = true,
    onOpenLiveUpdatesSettings: () -> Unit = {},
    onCopyNavLog: () -> Unit = {},
    updateSource: UpdateSource = UpdateSource.Unknown,
    onOpenUpdater: () -> Unit = {},
    onOpenReleases: () -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    val themeName = ThemePresets.firstOrNull { it.id == settings.themeId }?.name
        ?: settings.themeId.replaceFirstChar { it.uppercase() }
    val modeLabel = when (settings.themeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_mode_system)
        ThemeMode.LIGHT -> stringResource(R.string.settings_mode_light)
        ThemeMode.DARK -> stringResource(R.string.settings_mode_dark)
    }

    EmberbyteScreenScaffold(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        modifier = modifier,
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = scaffoldPadding.calculateTopPadding(),
                bottom = scaffoldPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "notifications") {
                SettingsGroup(
                    title = stringResource(R.string.settings_group_notifications),
                    iconShape = AppShape.Cookie,
                    iconColor = palette.primary,
                    onIconColor = palette.onPrimary,
                    iconVector = Icons.Rounded.Notifications,
                ) {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_live_title),
                        subtitle = stringResource(R.string.settings_live_subtitle),
                        checked = settings.liveNotificationEnabled,
                        iconVector = Icons.Rounded.Notifications,
                        onCheckedChange = { onUpdate { s -> s.copy(liveNotificationEnabled = it, promotedDismissedOn = null) } },
                    )
                    LiveUpdatesRow(
                        mayPost = mayPostLiveUpdates,
                        onClick = onOpenLiveUpdatesSettings,
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_show_speed_title),
                        subtitle = stringResource(R.string.settings_show_speed_subtitle),
                        checked = settings.notificationShowsSpeed,
                        enabled = settings.liveNotificationEnabled,
                        iconVector = Icons.Rounded.Speed,
                        onCheckedChange = { onUpdate { s -> s.copy(notificationShowsSpeed = it) } },
                    )
                }
            }

            item(key = "alerts") {
                SettingsGroup(
                    title = stringResource(R.string.settings_group_alerts),
                    iconShape = AppShape.Burst,
                    iconColor = palette.warn,
                    onIconColor = palette.onWarn,
                    iconVector = Icons.Rounded.Warning,
                ) {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_spike_alerts_title),
                        subtitle = stringResource(R.string.settings_spike_alerts_subtitle),
                        checked = settings.spikeAlertsEnabled,
                        iconVector = Icons.Rounded.Warning,
                        onCheckedChange = { onUpdate { s -> s.copy(spikeAlertsEnabled = it) } },
                    )
                }
            }

            item(key = "appearance") {
                AppearanceHeroCard(
                    themeName = themeName,
                    modeLabel = modeLabel,
                    settings = settings,
                    onUpdate = onUpdate,
                    onOpenAppearance = onOpenAppearance,
                )
            }

            item(key = "units") {
                SettingsGroup(
                    title = stringResource(R.string.settings_group_units),
                    iconShape = AppShape.Sunny,
                    iconColor = palette.warn,
                    onIconColor = palette.onWarn,
                    iconVector = Icons.Rounded.Tune,
                ) {
                    val unitIndex = if (settings.unitSystem == UnitSystem.DECIMAL) 0 else 1
                    ConnectedButtonGroup(
                        options = listOf(
                            stringResource(R.string.settings_units_decimal),
                            stringResource(R.string.settings_units_binary),
                        ),
                        selectedIndex = unitIndex,
                        onSelect = { index ->
                            val nextUnit = if (index == 0) UnitSystem.DECIMAL else UnitSystem.BINARY
                            onUpdate { s -> s.copy(unitSystem = nextUnit) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                    )
                }
            }

            item(key = "data") {
                SettingsGroup(
                    title = stringResource(R.string.settings_group_data),
                    iconShape = AppShape.Burst,
                    iconColor = palette.accent,
                    onIconColor = palette.onAccent,
                    iconVector = Icons.Rounded.History,
                ) {
                    DataSection(
                        uiState = dataUiState,
                        onExportCsv = onExportCsv,
                        onExportJson = onExportJson,
                        onCreateBackup = onCreateBackup,
                        onRestoreChosen = onRestoreChosen,
                        onRestoreBackup = onRestoreBackup,
                        onClearMessage = onClearDataMessage,
                        restoreFileChosen = restoreFileChosen,
                        onDismissRestorePassphrase = onDismissRestorePassphrase,
                    )
                }
            }

            item(key = "keep_measuring") {
                BackgroundKeepMeasuringCard(
                    onOpenBatterySettings = onOpenBatterySettings,
                )
            }

            item(key = "about") {
                SettingsGroup(
                    title = stringResource(R.string.settings_group_about),
                    iconShape = AppShape.Burst,
                    iconColor = palette.primary,
                    onIconColor = palette.onPrimary,
                    iconVector = Icons.Rounded.Info,
                ) {
                    AboutGroupContent(
                        versionName = versionName,
                        onOpenSource = onOpenSource,
                        onOpenLicenses = onOpenLicenses,
                        onCopyNavLog = onCopyNavLog,
                        updateSource = updateSource,
                        onOpenUpdater = onOpenUpdater,
                        onOpenReleases = onOpenReleases,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceHeroCard(
    themeName: String,
    modeLabel: String,
    settings: Settings,
    onUpdate: ((Settings) -> Settings) -> Unit,
    onOpenAppearance: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val systemDark = isSystemInDarkTheme()
    val effectiveDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val heroShape = mixedCorners(
        large = EmberbyteShapeRadii.ExtraLargeIncreased,
        small = EmberbyteShapeRadii.Small,
        smallCorner = Corner.BottomStart,
    )
    val previewPresets = remember(settings.themeId) { previewPresetsFor(settings.themeId) }

    Surface(
        shape = heroShape,
        color = palette.surface,
        contentColor = palette.text,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                onClick = onOpenAppearance,
                color = palette.surface,
                contentColor = palette.text,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics(mergeDescendants = true) {},
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .clearAndSetSemantics {},
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            ShapedBox(
                                shape = AppShape.Cookie.toShape(),
                                color = palette.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.Center)
                                        .testTag(SETTINGS_ROW_ICON_TAG),
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_appearance_title),
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = palette.text,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = themeName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.muted,
                                )
                                Text(
                                    text = stringResource(R.string.settings_appearance_summary, modeLabel, ThemePresets.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.muted,
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = palette.muted,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (preset in previewPresets) {
                            val swatchPalette = if (effectiveDark) preset.dark else preset.light
                            ShapedBox(
                                shape = MaterialTheme.shapes.small,
                                color = swatchPalette.surface2,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("appearance_hero_swatch_${preset.id}")
                                    .clearAndSetSemantics {},
                            ) {
                                ShapedBox(
                                    shape = AppShape.Cookie.toShape(),
                                    color = swatchPalette.primary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .align(Alignment.Center),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.settings_appearance_more, ThemePresets.size - previewPresets.size),
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            color = palette.linkText,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }
                }
            }

            SettingsSwitchRow(
                title = stringResource(R.string.settings_amoled_title),
                subtitle = stringResource(R.string.settings_amoled_subtitle),
                checked = settings.amoledBlack,
                iconVector = Icons.Rounded.DarkMode,
                onCheckedChange = { onUpdate { s -> s.copy(amoledBlack = it) } },
            )
        }
    }
}

@Composable
private fun BackgroundKeepMeasuringCard(
    onOpenBatterySettings: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val shape = mixedCorners(
        large = EmberbyteShapeRadii.ExtraLarge,
        small = EmberbyteShapeRadii.ExtraSmall,
        smallCorner = Corner.BottomEnd,
    )

    Surface(
        shape = shape,
        color = palette.accent,
        contentColor = palette.onAccent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_keep_measuring_title),
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = palette.onAccent,
            )
            Text(
                text = stringResource(R.string.settings_keep_measuring_body),
                style = MaterialTheme.typography.bodySmall,
                color = palette.onAccent,
            )
            Surface(
                onClick = onOpenBatterySettings,
                shape = CircleShape,
                color = palette.onAccent,
                contentColor = palette.accent,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics(mergeDescendants = true) {},
            ) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.settings_battery_button),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = palette.accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutGroupContent(
    versionName: String,
    onOpenSource: () -> Unit,
    onOpenLicenses: () -> Unit,
    onCopyNavLog: () -> Unit,
    updateSource: UpdateSource,
    onOpenUpdater: () -> Unit,
    onOpenReleases: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val density = LocalDensity.current
    val singleColumn = density.fontScale >= 1.5f
    val copyLogLabel = stringResource(R.string.settings_copy_nav_log)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .pointerInput(onCopyNavLog) { detectTapGestures(onLongPress = { onCopyNavLog() }) }
                .semantics {
                    onLongClick(label = copyLogLabel) {
                        onCopyNavLog()
                        true
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandMark(size = 48.dp)
            Column {
                Text(
                    text = stringResource(R.string.settings_about_version, versionName),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = palette.text,
                )
                Text(
                    text = stringResource(R.string.settings_about_licence_line),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }

        UpdatesRow(
            updateSource = updateSource,
            versionName = versionName,
            onOpenUpdater = onOpenUpdater,
            onOpenReleases = onOpenReleases,
        )

        if (singleColumn) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AboutPillButton(
                    label = stringResource(R.string.settings_about_source),
                    shape = CircleShape,
                    onClick = onOpenSource,
                    modifier = Modifier.fillMaxWidth(),
                )
                AboutPillButton(
                    label = stringResource(R.string.settings_about_licences),
                    shape = CircleShape,
                    onClick = onOpenLicenses,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AboutPillButton(
                    label = stringResource(R.string.settings_about_source),
                    shape = AppsShapes.PeriodStart,
                    onClick = onOpenSource,
                    modifier = Modifier.weight(1f),
                )
                AboutPillButton(
                    label = stringResource(R.string.settings_about_licences),
                    shape = AppsShapes.PeriodEnd,
                    onClick = onOpenLicenses,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AboutPillButton(
    label: String,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        onClick = onClick,
        shape = shape,
        color = palette.surface2,
        contentColor = palette.linkText,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = palette.linkText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

internal sealed interface BackupPickerAction {
    data object Ignore : BackupPickerAction
    data class Create(val uri: Uri, val passphrase: CharArray) : BackupPickerAction
    data class DeleteAndRetry(val uri: Uri) : BackupPickerAction
}

internal fun backupPickerResult(uri: Uri?, held: CharArray?): BackupPickerAction = when {
    uri == null -> {
        held?.fill('\u0000')
        BackupPickerAction.Ignore
    }
    held == null -> BackupPickerAction.DeleteAndRetry(uri)
    else -> BackupPickerAction.Create(uri, held)
}
