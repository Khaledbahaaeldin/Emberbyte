@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.StorageSnapshot
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsPageBanner
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformGroup
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsUniformRow
import io.github.khaledbahaaeldin.emberbyte.settings.hub.StorageMiniBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.staggerEntrance
import java.time.Clock
import java.time.format.DateTimeFormatter

internal enum class BackupSheetMode { Backup, Restore }

@Composable
fun DataBackupScreen(
    settingsViewModel: SettingsViewModel,
    dataViewModel: DataViewModel,
    isDark: Boolean,
    clock: Clock,
    highlightKey: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hubState by settingsViewModel.hubState.collectAsStateWithLifecycle()
    val dataState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedRange by rememberSaveable { mutableStateOf(ExportRange.LAST_30_DAYS) }
    var sheetMode by remember { mutableStateOf<BackupSheetMode?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var pendingRestoreMeta by remember { mutableStateOf<Pair<String, Long?>?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) dataViewModel.exportCsv(selectedRange, uri)
    }
    val jsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) dataViewModel.exportJson(uri)
    }
    val backupCreateLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val held = dataViewModel.takeHeldPassphrase()
        if (uri != null && held != null) {
            dataViewModel.createBackup(uri, held)
        } else {
            held?.fill('\u0000')
        }
    }
    val restoreOpenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            var displayName = "backup.ebk"
            var sizeBytes: Long? = null
            runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0 && !c.isNull(nameIdx)) displayName = c.getString(nameIdx)
                        if (sizeIdx >= 0 && !c.isNull(sizeIdx)) sizeBytes = c.getLong(sizeIdx)
                    }
                }
            }
            pendingRestoreUri = uri
            pendingRestoreMeta = displayName to sizeBytes
            sheetMode = BackupSheetMode.Restore
        }
    }

    DataBackupContent(
        settings = hubState.settings,
        storage = hubState.storage,
        dataState = dataState,
        isDark = isDark,
        clock = clock,
        selectedRange = selectedRange,
        highlightKey = highlightKey,
        onBack = onBack,
        onSelectRange = { selectedRange = it },
        onExportCsv = { csvLauncher.launch("emberbyte-usage.csv") },
        onExportJson = { jsonLauncher.launch("emberbyte-export.json") },
        onOpenBackupSheet = { sheetMode = BackupSheetMode.Backup },
        onPickRestoreFile = { restoreOpenLauncher.launch(arrayOf("*/*")) },
        modifier = modifier,
    )

    val mode = sheetMode
    if (mode != null) {
        BackupPassphraseSheet(
            mode = mode,
            fileName = pendingRestoreMeta?.first,
            fileSizeBytes = pendingRestoreMeta?.second,
            unitSystem = hubState.settings.unitSystem,
            onDismiss = {
                sheetMode = null
                pendingRestoreUri = null
                pendingRestoreMeta = null
            },
            onConfirm = { passphrase ->
                if (mode == BackupSheetMode.Backup) {
                    dataViewModel.holdPassphrase(passphrase)
                    sheetMode = null
                    backupCreateLauncher.launch("emberbyte-backup.ebk")
                } else {
                    val uri = pendingRestoreUri
                    sheetMode = null
                    pendingRestoreUri = null
                    pendingRestoreMeta = null
                    if (uri != null) dataViewModel.restoreBackup(uri, passphrase)
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DataBackupContent(
    settings: Settings,
    storage: StorageSnapshot?,
    dataState: DataUiState,
    isDark: Boolean,
    clock: Clock,
    selectedRange: ExportRange,
    highlightKey: String? = null,
    onBack: () -> Unit,
    onSelectRange: (ExportRange) -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onOpenBackupSheet: () -> Unit,
    onPickRestoreFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale = LocalConfiguration.current.locales[0]
    val byteUnits = if (settings.unitSystem == UnitSystem.BINARY) ByteUnits.BINARY else ByteUnits.DECIMAL
    val amoledDark = settings.amoledBlack && isDark
    val totalBytes = storage?.fileBytes ?: 0L
    val breakdown = storageBreakdown(
        fileBytes = totalBytes,
        rowCounts = StorageRowCounts(
            hourly = storage?.hourlyRows ?: 0L,
            daily = storage?.dailyRows ?: 0L,
            plans = storage?.planRows ?: 0L,
            other = storage?.otherRows ?: 0L,
        ),
    )
    val totalLabel = if (totalBytes > 0L) "about ${plainBytes(totalBytes, byteUnits)}" else "0 B"
    val sinceLabel = storage?.oldestAt?.let {
        "Recording since ${DateTimeFormatter.ofPattern("d MMM yyyy", locale).format(it.atZone(clock.zone))}"
    } ?: "Stored locally on this phone"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "banner") {
            SettingsPageBanner(
                title = "Data and backup",
                subtitle = totalLabel,
                icon = Icons.Rounded.Save,
                tint = palette.accent,
                amoled = amoledDark,
                onBack = onBack,
            )
        }

        item(key = "storage_card") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(0, play = true)
                    .testTag("data_storage_card"),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (amoledDark) palette.surface else palette.surface2,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = totalLabel,
                        style = EmberbyteType.title(SettingsType.PulseHeadline),
                        color = palette.text,
                    )
                    Text(
                        text = sinceLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.PulseBody),
                        color = palette.muted,
                    )
                    StorageMiniBar(breakdown = breakdown)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LegendDot("Hourly", plainBytes(breakdown.hourlyBytes, byteUnits), palette.primary)
                        LegendDot("Daily", plainBytes(breakdown.dailyBytes, byteUnits), palette.accent)
                        LegendDot("Plans", plainBytes(breakdown.plansBytes, byteUnits), palette.warn)
                        LegendDot("Other", plainBytes(breakdown.otherBytes, byteUnits), palette.muted)
                    }
                }
            }
        }

        item(key = "export_group") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(1, play = true),
            ) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val ranges = listOf(
                        ExportRange.LAST_7_DAYS to "7 days",
                        ExportRange.LAST_30_DAYS to "30 days",
                        ExportRange.LAST_90_DAYS to "90 days",
                        ExportRange.ALL to "All time",
                    )
                    ranges.forEach { (range, label) ->
                        FilterChip(
                            selected = selectedRange == range,
                            onClick = { onSelectRange(range) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("csv_range_${range.name.lowercase()}"),
                        )
                    }
                }
                SettingsUniformRow(
                    icon = Icons.Rounded.TableChart,
                    title = "Export CSV",
                    subtitle = "Daily and per-app usage table",
                    highlighted = highlightKey == "export_csv",
                    onClick = onExportCsv,
                    modifier = Modifier.testTag("export_csv_row"),
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.Description,
                    title = "Export JSON",
                    subtitle = "Full history and plans",
                    highlighted = highlightKey == "export_json",
                    onClick = onExportJson,
                    modifier = Modifier.testTag("export_json_row"),
                )
            }
        }

        item(key = "backup_group") {
            SettingsUniformGroup(
                amoled = amoledDark,
                modifier = Modifier.staggerEntrance(2, play = true),
            ) {
                SettingsUniformRow(
                    icon = Icons.Rounded.Backup,
                    title = "Encrypted backup",
                    subtitle = settings.lastBackupAt?.let {
                        "Last backed up ${DateTimeFormatter.ofPattern("d MMM", locale).format(it.atZone(clock.zone))}"
                    } ?: "Protect with a passphrase",
                    highlighted = highlightKey == "encrypted_backup",
                    onClick = onOpenBackupSheet,
                    modifier = Modifier.testTag("encrypted_backup_row"),
                )
                SettingsUniformRow(
                    icon = Icons.Rounded.Restore,
                    title = "Restore backup",
                    subtitle = "Import an encrypted Emberbyte backup file",
                    highlighted = highlightKey == "restore_backup",
                    onClick = onPickRestoreFile,
                    modifier = Modifier.testTag("restore_backup_row"),
                )
            }
        }

        val msg = dataState.message
        if (msg != null) {
            item(key = "status") {
                Text(
                    text = msg,
                    style = EmberbyteType.heading(SettingsType.PulseBody),
                    color = if (dataState.isError) palette.danger else palette.primary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, sizeText: String, color: Color) {
    val palette = LocalEmberbytePalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            text = "$label · $sizeText",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.Micro),
            color = palette.muted,
        )
    }
}

@Composable
internal fun BackupPassphraseSheet(
    mode: BackupSheetMode,
    fileName: String?,
    fileSizeBytes: Long?,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onConfirm: (CharArray) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetShape = mixedCorners(large = 34.dp, small = 34.dp, smallCorner = Corner.TopStart)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = palette.surface,
    ) {
        BackupPassphraseSheetBody(
            mode = mode,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            unitSystem = unitSystem,
            onConfirm = onConfirm,
        )
    }
}

@Composable
internal fun BackupPassphraseSheetBody(
    mode: BackupSheetMode,
    fileName: String?,
    fileSizeBytes: Long?,
    unitSystem: UnitSystem,
    onConfirm: (CharArray) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var passphrase by remember { mutableStateOf("") }
    val chars = passphrase.toCharArray()
    val strength = passphraseStrength(chars)
    val allowed = if (mode == BackupSheetMode.Backup) backupAllowed(chars) else passphrase.isNotEmpty()
    val byteUnits = if (unitSystem == UnitSystem.BINARY) ByteUnits.BINARY else ByteUnits.DECIMAL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = if (mode == BackupSheetMode.Backup) "Encrypted backup" else "Restore backup",
            style = EmberbyteType.title(SettingsType.PulseHeadline),
            color = palette.text,
        )
        if (mode == BackupSheetMode.Restore && fileName != null) {
            val sizePart = fileSizeBytes?.let { " · ${plainBytes(it, byteUnits)}" }.orEmpty()
            Text(
                text = "$fileName$sizePart",
                style = EmberbyteType.heading(SettingsType.PulseBody),
                color = palette.primary,
                modifier = Modifier.testTag("restore_file_info"),
            )
        }
        OutlinedTextField(
            value = passphrase,
            onValueChange = { passphrase = it },
            label = { Text("Passphrase") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_passphrase_field"),
        )
        if (mode == BackupSheetMode.Backup) {
            val strengthLabel = when (strength) {
                0 -> "Too short"
                1 -> "Okay"
                2, 3 -> "Good"
                else -> "Strong"
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    repeat(4) { idx ->
                        val active = idx < strength
                        val segColor = when {
                            !active -> palette.surface2
                            strength <= 1 -> palette.warn
                            else -> palette.primary
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(segColor),
                        )
                    }
                }
                Text(
                    text = strengthLabel,
                    style = EmberbyteType.heading(SettingsType.Caption),
                    color = if (strength == 0) palette.warn else palette.muted,
                    modifier = Modifier.testTag("passphrase_strength_label"),
                )
            }
        }
        Button(
            onClick = { onConfirm(passphrase.toCharArray()) },
            enabled = allowed,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = palette.background,
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("backup_sheet_confirm"),
        ) {
            Text(
                text = if (mode == BackupSheetMode.Backup) "Save backup" else "Restore",
                style = EmberbyteType.heading(SettingsType.RowTitle),
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}
