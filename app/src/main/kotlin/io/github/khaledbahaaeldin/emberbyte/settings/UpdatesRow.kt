package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape

@Composable
fun UpdatesRow(
    updateSource: UpdateSource,
    modifier: Modifier = Modifier,
    onOpenReleases: (() -> Unit)? = null,
) {
    val subtitle = if (updateSource.isKnownUpdater) {
        stringResource(R.string.settings_updates_managed_by, updateSource.displayName)
    } else {
        stringResource(R.string.settings_updates_github_releases)
    }
    val onClick = if (updateSource.isKnownUpdater) null else onOpenReleases

    SettingsRow(
        title = stringResource(R.string.settings_updates_title),
        subtitle = subtitle,
        iconVector = Icons.Rounded.Refresh,
        iconText = null,
        iconShape = AppShape.Squircle,
        showChevron = onClick != null,
        onClick = onClick,
        modifier = modifier,
    )
}
