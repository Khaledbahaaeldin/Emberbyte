package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape

@Composable
fun UpdatesRow(
    updateSource: UpdateSource,
    modifier: Modifier = Modifier,
    onOpenReleases: (() -> Unit)? = null,
    onOpenUpdater: (() -> Unit)? = null,
) {
    val onClick = if (updateSource.isKnownUpdater) onOpenUpdater else onOpenReleases
    val secondarySubtitle = if (!updateSource.isKnownUpdater) {
        "github.com/Khaledbahaaeldin/Emberbyte/releases"
    } else {
        null
    }

    SettingsRow(
        title = "Updates",
        subtitle = updateSource.subtitle,
        secondarySubtitle = secondarySubtitle,
        iconVector = Icons.Rounded.Refresh,
        iconText = null,
        iconShape = AppShape.Squircle,
        showChevron = onClick != null,
        onClick = onClick,
        modifier = modifier,
    )
}
