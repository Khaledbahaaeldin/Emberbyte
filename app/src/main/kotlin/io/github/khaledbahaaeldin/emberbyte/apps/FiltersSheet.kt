package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ExpressiveSwitch
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

private val NETWORK_OPTIONS = listOf("All", "Mobile", "Wi-Fi")
private val SORT_OPTIONS = listOf("Most", "Least", "Name")

/**
 * The Apps filters: Network, Sort by and Show system apps. The period lives on the page as buttons. The sheet edits a
 * draft: Reset clears the draft only, Apply commits it and closes the sheet.
 */
@Composable
fun FiltersSheetContent(
    network: AppsNetwork,
    sort: AppSort,
    showSystemApps: Boolean,
    onApply: (network: AppsNetwork, sort: AppSort, showSystemApps: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var currentNetwork by remember(network) { mutableStateOf(network) }
    var currentSort by remember(sort) { mutableStateOf(sort) }
    var currentShowSystemApps by remember(showSystemApps) { mutableStateOf(showSystemApps) }

    val networkIndex = when (currentNetwork) {
        AppsNetwork.ALL -> 0
        AppsNetwork.MOBILE -> 1
        AppsNetwork.WIFI -> 2
    }
    val sortIndex = when (currentSort) {
        AppSort.BYTES_ASC -> 1
        AppSort.NAME -> 2
        else -> 0
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag("apps_filters_sheet"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Filters",
            style = EmberbyteType.grotesk(AppsType.SheetTitle, 700),
            color = palette.text,
            modifier = Modifier.semantics { heading() },
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Network", style = EmberbyteType.grotesk(AppsType.SheetLabel, 600), color = palette.muted)
            ConnectedButtonGroup(
                options = NETWORK_OPTIONS,
                selectedIndex = networkIndex,
                onSelect = { idx ->
                    currentNetwork = when (idx) {
                        0 -> AppsNetwork.ALL
                        1 -> AppsNetwork.MOBILE
                        else -> AppsNetwork.WIFI
                    }
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sort by", style = EmberbyteType.grotesk(AppsType.SheetLabel, 600), color = palette.muted)
            ConnectedButtonGroup(
                options = SORT_OPTIONS,
                selectedIndex = sortIndex,
                onSelect = { idx ->
                    currentSort = when (idx) {
                        1 -> AppSort.BYTES_ASC
                        2 -> AppSort.NAME
                        else -> AppSort.BYTES_DESC
                    }
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Show system apps", style = EmberbyteType.grotesk(AppsType.SheetLabel, 600), color = palette.text)
                Text(
                    text = "Include OS and pre-installed background processes",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
            ExpressiveSwitch(
                checked = currentShowSystemApps,
                onCheckedChange = { currentShowSystemApps = it },
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = {
                    // Only the draft: nothing changes on the page until Apply.
                    currentNetwork = AppsNetwork.ALL
                    currentSort = AppSort.BYTES_DESC
                    currentShowSystemApps = false
                },
                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("apps_filters_reset"),
            ) {
                Text("Reset", style = EmberbyteType.grotesk(AppsType.SheetButton, 700))
            }
            Button(
                onClick = {
                    onApply(currentNetwork, currentSort, currentShowSystemApps)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
                modifier = Modifier.weight(1.6f).heightIn(min = 52.dp).testTag("apps_filters_apply"),
            ) {
                Text("Apply", style = EmberbyteType.grotesk(AppsType.SheetButton, 700))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(
    initialNetwork: AppsNetwork,
    initialSort: AppSort,
    initialShowSystemApps: Boolean,
    onApply: (network: AppsNetwork, sort: AppSort, showSystemApps: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = LocalEmberbytePalette.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp), bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)),
        modifier = modifier,
    ) {
        FiltersSheetContent(
            network = initialNetwork,
            sort = initialSort,
            showSystemApps = initialShowSystemApps,
            onApply = onApply,
            onDismiss = onDismiss,
        )
    }
}
