package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import java.math.BigDecimal
import java.text.DecimalFormatSymbols

@Composable
internal fun DataSection(
    capText: String,
    capUnit: CapUnit,
    unitSystem: UnitSystem,
    capError: String?,
    onSetCap: (String) -> Unit,
    onSetCapUnit: (CapUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val decimalSeparator = DecimalFormatSymbols.getInstance(locale).decimalSeparator

    EditorSectionCard(title = "Data", modifier = modifier) {
        OutlinedTextField(
            value = capText,
            onValueChange = onSetCap,
            label = { Text("Cap") },
            singleLine = true,
            isError = capError != null,
            supportingText = capError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .testTag("editor_cap_field"),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(-1, 1).forEach { delta ->
                val label = if (delta > 0) "+$delta" else "$delta"
                FilterChip(
                    selected = false,
                    onClick = {
                        val current = capText.trim().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val next = (current + BigDecimal.valueOf(delta.toLong())).max(BigDecimal.ONE)
                        val formatted = next.stripTrailingZeros().toPlainString().replace('.', decimalSeparator)
                        onSetCap(formatted)
                    },
                    label = { Text(label) },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                )
            }
        }

        val unitLabels = if (unitSystem == UnitSystem.BINARY) {
            listOf("MiB", "GiB", "TiB")
        } else {
            listOf("MB", "GB", "TB")
        }
        ConnectedButtonGroup(
            options = unitLabels,
            selectedIndex = capUnit.ordinal,
            onSelect = { idx -> onSetCapUnit(CapUnit.entries[idx]) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
