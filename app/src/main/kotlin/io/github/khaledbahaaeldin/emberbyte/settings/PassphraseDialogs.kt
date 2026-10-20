package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog

@Composable
fun CsvRangeDialog(
    onDismiss: () -> Unit,
    onSelectRange: (ExportRange) -> Unit,
) {
    EmberbyteDialog(
        onDismiss = onDismiss,
        title = "Export usage",
        icon = null,
        confirm = DialogAction("Cancel", onDismiss),
        dismiss = null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RangeOptionRow("Last 7 days") { onSelectRange(ExportRange.LAST_7_DAYS) }
            RangeOptionRow("Last 30 days") { onSelectRange(ExportRange.LAST_30_DAYS) }
            RangeOptionRow("All") { onSelectRange(ExportRange.ALL) }
        }
    }
}

@Composable
private fun RangeOptionRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
    )
}

@Composable
fun CreateBackupPassphraseDialog(
    onDismiss: () -> Unit,
    onCreate: (CharArray) -> Unit,
) {
    var passphrase by remember { mutableStateOf("") }
    var repeatPassphrase by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.yield()
        focusRequester.requestFocus()
    }

    val tooShort = passphrase.isNotEmpty() && passphrase.length < 8
    val mismatch = repeatPassphrase.isNotEmpty() && passphrase != repeatPassphrase
    val canCreate = passphrase.length >= 8 && passphrase == repeatPassphrase

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = "Create backup",
        icon = Icons.Rounded.Lock,
        confirm = DialogAction(
            label = "Create",
            onClick = {
                val chars = passphrase.toCharArray()
                onCreate(chars)
            },
            enabled = canCreate,
        ),
        dismiss = DialogAction(
            label = "Cancel",
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
                label = { Text("Passphrase") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            if (tooShort) {
                Text(
                    text = "Use at least 8 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            OutlinedTextField(
                value = repeatPassphrase,
                onValueChange = { repeatPassphrase = it },
                label = { Text("Repeat passphrase") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            if (mismatch) {
                Text(
                    text = "The passphrases do not match",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun RestoreWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = "Restore backup",
        icon = Icons.Rounded.Warning,
        destructive = true,
        confirm = DialogAction(
            label = "Continue",
            onClick = onConfirm,
            modifier = Modifier.focusRequester(focusRequester),
        ),
        dismiss = DialogAction(
            label = "Cancel",
            onClick = onDismiss,
        ),
    ) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.yield()
            focusRequester.requestFocus()
        }
        Text("Restore replaces your plans, budgets and history on this device. Continue?")
    }
}

@Composable
fun RestorePassphraseDialog(
    onDismiss: () -> Unit,
    onRestore: (CharArray) -> Unit,
) {
    var passphrase by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.yield()
        focusRequester.requestFocus()
    }

    EmberbyteDialog(
        onDismiss = onDismiss,
        title = "Restore backup",
        icon = Icons.Rounded.Lock,
        confirm = DialogAction(
            label = "Restore",
            onClick = {
                val chars = passphrase.toCharArray()
                onRestore(chars)
            },
            enabled = passphrase.isNotEmpty(),
        ),
        dismiss = DialogAction(
            label = "Cancel",
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
                label = { Text("Passphrase") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }
    }
}

