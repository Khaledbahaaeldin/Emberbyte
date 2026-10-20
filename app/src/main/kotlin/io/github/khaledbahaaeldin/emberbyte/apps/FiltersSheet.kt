package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ExpressiveSwitch
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

private val PERIOD_OPTIONS = listOf("Today", "7 days", "30 days", "Cycle")
private val NETWORK_OPTIONS = listOf("All", "Mobile", "Wi-Fi")
private val SORT_OPTIONS = listOf("Usage", "Name")

@Composable
fun FiltersSheetContent(
    range: AppsRange,
    network: AppsNetwork,
    sort: AppSort,
    showSystemApps: Boolean,
    onApply: (range: AppsRange, network: AppsNetwork, sort: AppSort, showSystemApps: Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var currentRange by remember(range) { mutableStateOf(range) }
    var currentNetwork by remember(network) { mutableStateOf(network) }
    var currentSort by remember(sort) { mutableStateOf(sort) }
    var currentShowSystemApps by remember(showSystemApps) { mutableStateOf(showSystemApps) }

    val periodIndex = when (currentRange) {
        AppsRange.TODAY -> 0
        AppsRange.LAST_7_DAYS -> 1
        AppsRange.LAST_30_DAYS -> 2
        AppsRange.THIS_MONTH, AppsRange.CYCLE -> 3
    }

    val networkIndex = when (currentNetwork) {
        AppsNetwork.ALL -> 0
        AppsNetwork.MOBILE -> 1
        AppsNetwork.WIFI -> 2
    }

    val sortIndex = when (currentSort) {
        AppSort.NAME -> 1
        else -> 0
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = palette.text,
        )

        // Period section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Period",
                style = MaterialTheme.typography.labelLarge,
                color = palette.muted,
            )
            ConnectedButtonGroup(
                options = PERIOD_OPTIONS,
                selectedIndex = periodIndex,
                onSelect = { idx ->
                    currentRange = when (idx) {
                        0 -> AppsRange.TODAY
                        1 -> AppsRange.LAST_7_DAYS
                        2 -> AppsRange.LAST_30_DAYS
                        else -> AppsRange.CYCLE
                    }
                },
            )
        }

        // Network section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Network",
                style = MaterialTheme.typography.labelLarge,
                color = palette.muted,
            )
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

        // Sort section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.labelLarge,
                color = palette.muted,
            )
            ConnectedButtonGroup(
                options = SORT_OPTIONS,
                selectedIndex = sortIndex,
                onSelect = { idx ->
                    currentSort = when (idx) {
                        1 -> AppSort.NAME
                        else -> AppSort.BYTES_DESC
                    }
                },
            )
        }

        // System apps toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Show system apps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.text,
                )
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

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    currentRange = AppsRange.TODAY
                    currentNetwork = AppsNetwork.ALL
                    currentSort = AppSort.BYTES_DESC
                    currentShowSystemApps = false
                    onReset()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Reset")
            }
            Button(
                onClick = {
                    onApply(currentRange, currentNetwork, currentSort, currentShowSystemApps)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text("Apply")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(
    initialRange: AppsRange,
    initialNetwork: AppsNetwork,
    initialSort: AppSort,
    initialShowSystemApps: Boolean,
    onApply: (range: AppsRange, network: AppsNetwork, sort: AppSort, showSystemApps: Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = LocalEmberbytePalette.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        modifier = modifier,
    ) {
        FiltersSheetContent(
            range = initialRange,
            network = initialNetwork,
            sort = initialSort,
            showSystemApps = initialShowSystemApps,
            onApply = onApply,
            onReset = onReset,
            onDismiss = onDismiss,
        )
    }
}
