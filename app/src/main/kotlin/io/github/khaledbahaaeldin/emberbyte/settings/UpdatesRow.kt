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
) {
    val subtitle = when (updateSource) {
        UpdateSource.Obtainium -> "Managed by Obtainium"
        UpdateSource.OrionStore -> "Managed by Orion Store"
        UpdateSource.FDroid -> "Managed by F-Droid"
        UpdateSource.Other, UpdateSource.Unknown -> "GitHub Releases"
    }

    val onClick = if (updateSource.isKnownUpdater) null else onOpenReleases

    SettingsRow(
        title = "Updates",
        subtitle = subtitle,
        iconVector = Icons.Rounded.Refresh,
        iconText = null,
        iconShape = AppShape.Squircle,
        showChevron = onClick != null,
        onClick = onClick,
        modifier = modifier,
    )
}
