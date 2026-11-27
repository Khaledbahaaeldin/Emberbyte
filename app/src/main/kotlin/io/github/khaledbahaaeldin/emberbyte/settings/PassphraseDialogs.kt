package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import kotlinx.coroutines.yield

@Composable
fun CsvRangeDialog(
    onDismiss: () -> Unit,
    onSelectRange: (ExportRange) -> Unit,
) {
    var selectedRange by rememberSaveable { mutableStateOf(ExportRange.LAST_7_DAYS) }

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.dialog_export_title),
        icon = Icons.Rounded.FileDownload,
        confirm = DialogAction(
            label = stringResource(R.string.dialog_export_confirm),
            onClick = { onSelectRange(selectedRange) },
        ),
        dismiss = DialogAction(
            label = stringResource(R.string.dialog_cancel),
            onClick = onDismiss,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RangeOptionRow(
                text = stringResource(R.string.dialog_export_last_7),
                selected = selectedRange == ExportRange.LAST_7_DAYS,
                onClick = { selectedRange = ExportRange.LAST_7_DAYS },
            )
            RangeOptionRow(
                text = stringResource(R.string.dialog_export_last_30),
                selected = selectedRange == ExportRange.LAST_30_DAYS,
                onClick = { selectedRange = ExportRange.LAST_30_DAYS },
            )
            RangeOptionRow(
                text = stringResource(R.string.dialog_export_all),
                selected = selectedRange == ExportRange.ALL,
                onClick = { selectedRange = ExportRange.ALL },
            )
        }
    }
}

@Composable
private fun RangeOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {}
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.text,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateBackupPassphraseDialog(
    onDismiss: () -> Unit,
    onCreate: (CharArray) -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    var passphrase by rememberSaveable { mutableStateOf("") }
    var repeatPassphrase by rememberSaveable { mutableStateOf("") }
    var showPassphrase by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        yield()
        runCatching { focusRequester.requestFocus() }
    }

    val tooShort = passphrase.isNotEmpty() && passphrase.length < 8
    val mismatch = repeatPassphrase.isNotEmpty() && passphrase != repeatPassphrase
    val canCreate = passphrase.length >= 8 && passphrase == repeatPassphrase

    val shortMsg = stringResource(R.string.dialog_passphrase_short)
    val mismatchMsg = stringResource(R.string.dialog_passphrase_mismatch)
    val hintMsg = stringResource(R.string.dialog_passphrase_hint)

    val firstError: String? = when {
        tooShort -> shortMsg
        mismatch -> mismatchMsg
        else -> null
    }
    val secondError: String? = if (mismatch) mismatchMsg else null

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.dialog_backup_create_title),
        icon = Icons.Rounded.Lock,
        confirm = DialogAction(
            label = stringResource(R.string.dialog_backup_create_confirm),
            onClick = {
                val chars = passphrase.toCharArray()
                onCreate(chars)
            },
            enabled = canCreate,
        ),
        dismiss = DialogAction(
            label = stringResource(R.string.dialog_cancel),
            onClick = onDismiss,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.dialog_passphrase_warning),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )

            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(stringResource(R.string.dialog_passphrase_label)) },
                singleLine = true,
                visualTransformation = if (showPassphrase) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (showPassphrase) KeyboardType.Text else KeyboardType.Password,
                ),
                trailingIcon = {
                    PassphraseVisibilityToggle(
                        shown = showPassphrase,
                        onToggle = { showPassphrase = !showPassphrase },
                    )
                },
                isError = firstError != null,
                supportingText = if (tooShort) {
                    { Text(shortMsg, color = palette.danger) }
                } else {
                    null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .semantics {
                        if (firstError != null) error(firstError)
                    },
            )

            OutlinedTextField(
                value = repeatPassphrase,
                onValueChange = { repeatPassphrase = it },
                label = { Text(stringResource(R.string.dialog_passphrase_repeat_label)) },
                singleLine = true,
                visualTransformation = if (showPassphrase) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (showPassphrase) KeyboardType.Text else KeyboardType.Password,
                ),
                isError = secondError != null,
                supportingText = when {
                    mismatch -> ({ Text(mismatchMsg, color = palette.danger) })
                    !canCreate && !tooShort -> ({ Text(hintMsg, color = palette.muted) })
                    else -> null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (secondError != null) error(secondError)
                    },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PassphraseVisibilityToggle(
    shown: Boolean,
    onToggle: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val desc = stringResource(if (shown) R.string.dialog_passphrase_hide else R.string.dialog_passphrase_show)
    IconButton(
        onClick = onToggle,
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.iconButtonColors(contentColor = palette.muted),
        modifier = Modifier.size(48.dp),
    ) {
        Icon(
            imageVector = if (shown) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
            contentDescription = desc,
        )
    }
}

@Composable
fun RestoreWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val focusRequester = remember { FocusRequester() }

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.dialog_restore_title),
        icon = Icons.Rounded.Warning,
        destructive = true,
        confirm = DialogAction(
            label = stringResource(R.string.dialog_restore_continue),
            onClick = onConfirm,
            modifier = Modifier.focusRequester(focusRequester),
        ),
        dismiss = DialogAction(
            label = stringResource(R.string.dialog_cancel),
            onClick = onDismiss,
        ),
    ) {
        LaunchedEffect(Unit) {
            yield()
            runCatching { focusRequester.requestFocus() }
        }
        Text(
            text = stringResource(R.string.dialog_restore_warning_body),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.text,
        )
    }
}

@Composable
fun RestorePassphraseDialog(
    onDismiss: () -> Unit,
    onRestore: (CharArray) -> Unit,
) {
    var passphrase by rememberSaveable { mutableStateOf("") }
    var showPassphrase by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        yield()
        runCatching { focusRequester.requestFocus() }
    }

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.dialog_restore_title),
        icon = Icons.Rounded.Lock,
        confirm = DialogAction(
            label = stringResource(R.string.dialog_restore_confirm),
            onClick = {
                val chars = passphrase.toCharArray()
                onRestore(chars)
            },
            enabled = passphrase.isNotEmpty(),
        ),
        dismiss = DialogAction(
            label = stringResource(R.string.dialog_cancel),
            onClick = onDismiss,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(stringResource(R.string.dialog_passphrase_label)) },
                singleLine = true,
                visualTransformation = if (showPassphrase) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (showPassphrase) KeyboardType.Text else KeyboardType.Password,
                ),
                trailingIcon = {
                    PassphraseVisibilityToggle(
                        shown = showPassphrase,
                        onToggle = { showPassphrase = !showPassphrase },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }
    }
}
