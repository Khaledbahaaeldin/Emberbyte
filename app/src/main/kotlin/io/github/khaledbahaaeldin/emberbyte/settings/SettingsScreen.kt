package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.common.ScreenHeader
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark
import java.time.LocalDate

private const val SOURCE_URL = "https://github.com/Khaledbahaaeldin/DataKB"

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

    val pendingState = rememberSaveable(saver = SettingsPendingState.Saver) { SettingsPendingState() }

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
) {
    val palette = LocalEmberbytePalette.current
    val themeName = ThemePresets.firstOrNull { it.id == settings.themeId }?.name
        ?: settings.themeId.replaceFirstChar { it.uppercase() }
    val modeLabel = when (settings.themeMode) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Settings", onBack) }

        item {
            SettingsGroup(
                title = "Notifications",
                iconShape = AppShape.Cookie,
                iconColor = palette.primary,
                onIconColor = palette.onPrimary,
                iconVector = Icons.Rounded.Notifications,
                iconText = null,
            ) {
                SettingsSwitchRow(
                    title = "Live notification",
                    subtitle = "Today's usage in the notification shade",
                    checked = settings.liveNotificationEnabled,
                    iconShape = AppShape.Cookie,
                    iconVector = Icons.Rounded.Notifications,
                    iconText = null,
                    onCheckedChange = { onUpdate { s -> s.copy(liveNotificationEnabled = it) } },
                )
                SettingsSwitchRow(
                    title = "Show speed",
                    subtitle = "Add your current speed to the notification",
                    checked = settings.notificationShowsSpeed,
                    enabled = settings.liveNotificationEnabled,
                    iconShape = AppShape.Squircle,
                    iconVector = Icons.Rounded.Speed,
                    iconText = null,
                    onCheckedChange = { onUpdate { s -> s.copy(notificationShowsSpeed = it) } },
                )
            }
        }

        item {
            SettingsGroup(
                title = "Alerts",
                iconShape = AppShape.Burst,
                iconColor = palette.warn,
                onIconColor = palette.onWarn,
                iconVector = Icons.Rounded.Warning,
                iconText = null,
            ) {
                SettingsSwitchRow(
                    title = "Spike alerts",
                    subtitle = "Tell me when an app suddenly uses a lot of mobile data",
                    checked = settings.spikeAlertsEnabled,
                    iconShape = AppShape.Burst,
                    iconVector = Icons.Rounded.Warning,
                    iconText = null,
                    onCheckedChange = { onUpdate { s -> s.copy(spikeAlertsEnabled = it) } },
                )
            }
        }

        item {
            AppearanceHeroCard(
                themeName = themeName,
                modeLabel = modeLabel,
                settings = settings,
                onUpdate = onUpdate,
                onOpenAppearance = onOpenAppearance,
            )
        }

        item {
            SettingsGroup(
                title = "Units",
                iconShape = AppShape.Sunny,
                iconColor = palette.warn,
                onIconColor = palette.onWarn,
                iconVector = Icons.Rounded.Tune,
                iconText = null,
            ) {
                val unitIndex = if (settings.unitSystem == UnitSystem.DECIMAL) 0 else 1
                ConnectedButtonGroup(
                    options = listOf("Decimal (GB)", "Binary (GiB)"),
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

        item {
            SettingsGroup(
                title = "Data and backup",
                iconShape = AppShape.Burst,
                iconColor = palette.accent,
                onIconColor = palette.onAccent,
                iconVector = Icons.Rounded.History,
                iconText = null,
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
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
        }

        item {
            BackgroundKeepMeasuringCard(
                onOpenBatterySettings = onOpenBatterySettings,
            )
        }

        item {
            SettingsGroup(
                title = "About",
                iconShape = AppShape.Burst,
                iconColor = palette.primary,
                onIconColor = palette.onPrimary,
                iconVector = Icons.Rounded.Info,
                iconText = null,
            ) {
                AboutGroupContent(
                    versionName = versionName,
                    onOpenSource = onOpenSource,
                    onOpenLicenses = onOpenLicenses,
                )
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
    val heroShape = mixedCorners(large = 36.dp, small = 12.dp, smallCorner = Corner.BottomStart)

    Surface(
        shape = heroShape,
        color = palette.surface2,
        contentColor = palette.text,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onOpenAppearance,
                color = palette.surface2,
                contentColor = palette.text,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics(mergeDescendants = true) {},
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ShapedBox(
                                shape = AppShape.Cookie.toShape(),
                                color = palette.primary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clearAndSetSemantics {},
                            ) {
                                Text(
                                    text = "A",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = palette.onPrimary,
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }
                            Text(
                                text = "Appearance",
                                style = EmberbyteType.heading(19.sp),
                                color = palette.text,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = palette.muted,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = themeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                        Text(
                            text = " · $modeLabel · 12 themes",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics {},
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val previewPresets = ThemePresets.take(5)
                        for (preset in previewPresets) {
                            ShapedBox(
                                shape = RoundedCornerShape(10.dp),
                                color = preset.dark.background,
                                modifier = Modifier.size(32.dp),
                            ) {
                                ShapedBox(
                                    shape = AppShape.Cookie.toShape(),
                                    color = preset.dark.primary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .align(Alignment.Center),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "+${ThemePresets.size - previewPresets.size} more",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.primary,
                        )
                    }
                }
            }

            SettingsSwitchRow(
                title = "AMOLED black",
                subtitle = "Pure black backgrounds in dark theme",
                checked = settings.amoledBlack,
                iconShape = AppShape.Squircle,
                iconColor = palette.surface,
                onIconColor = palette.primary,
                iconText = "B",
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
    val shape = mixedCorners(large = 30.dp, small = 10.dp, smallCorner = Corner.BottomEnd)

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
                text = "Keep measuring in the background",
                style = EmberbyteType.heading(15.sp),
                color = palette.onAccent,
            )
            Text(
                text = "Some phones stop background apps. Allow Emberbyte in the battery settings so it can keep measuring.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.onAccent,
            )
            Surface(
                onClick = onOpenBatterySettings,
                shape = RoundedCornerShape(percent = 50),
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
                        text = "Battery settings",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
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
) {
    val palette = LocalEmberbytePalette.current
    val density = LocalDensity.current
    val singleColumn = density.fontScale >= 1.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandMark(size = 48.dp)
            Column {
                Text(
                    text = "Emberbyte $versionName",
                    style = EmberbyteType.heading(20.sp),
                    color = palette.text,
                )
                Text(
                    text = "Open source under the GPL-3.0 or later. Your data never leaves this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }

        if (singleColumn) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AboutPillButton(
                    label = "Source code",
                    shape = RoundedCornerShape(percent = 50),
                    onClick = onOpenSource,
                    modifier = Modifier.fillMaxWidth(),
                )
                AboutPillButton(
                    label = "Open-source licences",
                    shape = RoundedCornerShape(percent = 50),
                    onClick = onOpenLicenses,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AboutPillButton(
                    label = "Source code",
                    shape = RoundedCornerShape(
                        topStart = CornerSize(50),
                        bottomStart = CornerSize(50),
                        topEnd = CornerSize(8.dp),
                        bottomEnd = CornerSize(8.dp),
                    ),
                    onClick = onOpenSource,
                    modifier = Modifier.weight(1f),
                )
                AboutPillButton(
                    label = "Open-source licences",
                    shape = RoundedCornerShape(
                        topStart = CornerSize(8.dp),
                        bottomStart = CornerSize(8.dp),
                        topEnd = CornerSize(50),
                        bottomEnd = CornerSize(50),
                    ),
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
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        onClick = onClick,
        shape = shape,
        color = palette.surface2,
        contentColor = palette.primary,
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.primary,
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
