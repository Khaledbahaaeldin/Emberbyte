package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.UsageType

/** The Home usage filters as a bottom sheet: edit a draft, then Apply or Reset. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageFiltersSheet(
    filters: UsageFilters,
    onApply: (UsageFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        UsageFiltersContent(filters = filters, onApply = onApply, onReset = onReset)
    }
}

/** The sheet's content, separate so it can be tested without a window: a draft that Apply commits and Reset clears. */
@Composable
fun UsageFiltersContent(
    filters: UsageFilters,
    onApply: (UsageFilters) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var draft by remember(filters) { mutableStateOf(filters) }
    val networks = listOf("All", "Mobile", "Wi-Fi")

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).testTag("usage_filters_sheet"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Filters",
            style = EmberbyteType.grotesk(UsageType.Title, 700),
            color = palette.text,
            modifier = Modifier.semantics { heading() },
        )
        Text("Network", style = EmberbyteType.grotesk(UsageType.Summary, 600), color = palette.text)
        ConnectedButtonGroup(
            options = networks,
            selectedIndex = draft.network.ordinal,
            onSelect = { draft = draft.copy(network = NetworkFilter.entries[it]) },
            modifier = Modifier.fillMaxWidth().testTag("usage_filters_network"),
        )
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Show system apps", style = EmberbyteType.grotesk(UsageType.Summary, 600), color = palette.text)
            Switch(
                checked = draft.showSystemApps,
                onCheckedChange = { draft = draft.copy(showSystemApps = it) },
                thumbContent = if (draft.showSystemApps) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.padding(2.dp)) }
                } else null,
                modifier = Modifier.testTag("usage_filters_system"),
            )
        }
        Text(
            "Period, sort and more are in Apps",
            style = EmberbyteType.grotesk(UsageType.RowSub, 500),
            color = palette.muted,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    draft = UsageFilters()
                    onReset()
                },
                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("usage_filters_reset"),
            ) { Text("Reset", style = EmberbyteType.grotesk(UsageType.Button, 700)) }
            Button(
                onClick = { onApply(draft) },
                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("usage_filters_apply"),
            ) { Text("Apply", style = EmberbyteType.grotesk(UsageType.Button, 700)) }
        }
    }
}
