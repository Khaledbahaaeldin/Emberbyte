package io.github.khaledbahaaeldin.emberbyte.settings

import android.net.Uri

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText

@Composable
fun DataSection(
    uiState: DataUiState,
    onExportCsv: (ExportRange) -> Unit,
    onExportJson: () -> Unit,
    onCreateBackup: (CharArray) -> Unit,
    onRestoreChosen: () -> Unit,
    onRestoreBackup: (CharArray) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
    restoreFileChosen: Boolean = false,
    onDismissRestorePassphrase: () -> Unit = {},
) {
    var showRangeDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateBackupDialog by rememberSaveable { mutableStateOf(false) }
    var showRestoreWarningDialog by rememberSaveable { mutableStateOf(false) }
    val createBackupRequester = remember { FocusRequester() }
    val restoreBackupRequester = remember { FocusRequester() }
    val palette = LocalEmberbytePalette.current

    if (showRangeDialog) {
        CsvRangeDialog(
            onDismiss = { showRangeDialog = false },
            onSelectRange = { range ->
                showRangeDialog = false
                onExportCsv(range)
            },
        )
    }

    if (showCreateBackupDialog) {
        CreateBackupPassphraseDialog(
            onDismiss = {
                showCreateBackupDialog = false
                runCatching { createBackupRequester.requestFocus() }
            },
            onCreate = { passphrase ->
                showCreateBackupDialog = false
                onCreateBackup(passphrase)
                runCatching { createBackupRequester.requestFocus() }
            },
        )
    }

    if (showRestoreWarningDialog) {
        RestoreWarningDialog(
            onDismiss = {
                showRestoreWarningDialog = false
                runCatching { restoreBackupRequester.requestFocus() }
            },
            onConfirm = {
                showRestoreWarningDialog = false
                onRestoreChosen()
            },
        )
    }

    if (restoreFileChosen) {
        RestorePassphraseDialog(
            onDismiss = {
                onDismissRestorePassphrase()
                runCatching { restoreBackupRequester.requestFocus() }
            },
            onRestore = { passphrase ->
                onRestoreBackup(passphrase)
                runCatching { restoreBackupRequester.requestFocus() }
            },
        )
    }

    val rowsEnabled = !uiState.busy

    Column(modifier = modifier.fillMaxWidth()) {
        SettingsRow(
            title = stringResource(R.string.settings_export_csv_title),
            subtitle = stringResource(R.string.settings_export_csv_subtitle),
            enabled = rowsEnabled,
            iconVector = Icons.Rounded.FileDownload,
            onClick = { showRangeDialog = true },
        )
        SettingsRow(
            title = stringResource(R.string.settings_export_json_title),
            subtitle = stringResource(R.string.settings_export_json_subtitle),
            enabled = rowsEnabled,
            iconVector = Icons.Rounded.DataObject,
            onClick = onExportJson,
        )
        SettingsRow(
            title = stringResource(R.string.settings_backup_create_title),
            subtitle = stringResource(R.string.settings_backup_create_subtitle),
            enabled = rowsEnabled,
            iconVector = Icons.Rounded.Backup,
            onClick = { showCreateBackupDialog = true },
            modifier = Modifier.focusRequester(createBackupRequester),
        )
        SettingsRow(
            title = stringResource(R.string.settings_backup_restore_title),
            subtitle = stringResource(R.string.settings_backup_restore_subtitle),
            enabled = rowsEnabled,
            iconVector = Icons.Rounded.Restore,
            onClick = { showRestoreWarningDialog = true },
            modifier = Modifier.focusRequester(restoreBackupRequester),
        )

        EmberbyteAnimatedVisibility(visible = uiState.busy) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                LoadingState(label = stringResource(R.string.settings_working))
            }
        }

        EmberbyteAnimatedVisibility(visible = uiState.message != null) {
            val msg = uiState.message.orEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.isError) palette.danger else palette.text,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                TextButton(
                    onClick = onClearMessage,
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.linkText),
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Text(stringResource(R.string.settings_dismiss))
                }
            }
        }
    }
}

internal sealed interface BackupPickerAction {
    data class Create(val uri: Uri, val passphrase: CharArray) : BackupPickerAction
    data class DeleteAndRetry(val uri: Uri) : BackupPickerAction
    data object Ignore : BackupPickerAction
}

internal fun backupPickerResult(uri: Uri?, heldPassphrase: CharArray?): BackupPickerAction = when {
    uri == null -> BackupPickerAction.Ignore
    heldPassphrase == null -> BackupPickerAction.DeleteAndRetry(uri)
    else -> BackupPickerAction.Create(uri, heldPassphrase)
}
