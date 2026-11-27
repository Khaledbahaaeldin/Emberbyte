package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText

/** One active (non-default) filter as a removable chip. */
internal data class ActiveFilter(val kind: AppFilterKind, val label: String)

/** The chips for the filters that differ from the defaults, labelled in full ("Network: Mobile", "Sort: Least data"). */
internal fun activeFilters(network: AppsNetwork, sort: AppSort, showSystemApps: Boolean): List<ActiveFilter> = buildList {
    if (network != AppsNetwork.ALL) {
        add(ActiveFilter(AppFilterKind.NETWORK, "Network: ${if (network == AppsNetwork.MOBILE) "Mobile" else "Wi-Fi"}"))
    }
    if (sort != AppSort.BYTES_DESC) add(ActiveFilter(AppFilterKind.SORT, "Sort: ${sortLabel(sort)}"))
    if (showSystemApps) add(ActiveFilter(AppFilterKind.SYSTEM_APPS, "System apps"))
}

/**
 * Removable chips under the period buttons. They wrap instead of clipping; a trailing "Reset" chip appears when two or
 * more filters are active.
 */
@Composable
internal fun AppsFilterChips(
    filters: List<ActiveFilter>,
    onRemove: (AppFilterKind) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    FlowRow(
        modifier = modifier.testTag("apps_filter_chips"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            Row(
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .clip(AppsShapes.Chip)
                    .background(palette.primary.copy(alpha = 0.16f))
                    .clickable(role = Role.Button, onClickLabel = "Remove filter") { onRemove(filter.kind) }
                    .padding(start = 12.dp, end = 8.dp)
                    .semantics { contentDescription = "Remove ${filter.label} filter" }
                    .testTag("apps_chip_${filter.kind.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(filter.label, style = EmberbyteType.grotesk(AppsType.Chip, 600), color = palette.linkText)
                Icon(Icons.Rounded.Close, contentDescription = null, tint = palette.linkText, modifier = Modifier.size(16.dp))
            }
        }
        if (filters.size >= 2) {
            Row(
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .clip(AppsShapes.Chip)
                    .clickable(role = Role.Button, onClick = onReset)
                    .padding(horizontal = 12.dp)
                    .testTag("apps_chip_reset"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Reset", style = EmberbyteType.grotesk(AppsType.Chip, 700), color = palette.linkText)
            }
        }
    }
}
