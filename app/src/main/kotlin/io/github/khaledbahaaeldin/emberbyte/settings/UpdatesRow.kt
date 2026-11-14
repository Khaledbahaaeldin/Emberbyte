package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape

/**
 * Points the user to whoever updates the app. It cannot say whether a newer version exists, because the app has no
 * network permission by design.
 *
 * - A known updater (Obtainium, Orion Store, F-Droid) is named, and tapping the row opens it ([onOpenUpdater]).
 * - Any other installer, or none, shows the installed version and opens the GitHub releases page ([onOpenReleases]).
 */
@Composable
fun UpdatesRow(
    updateSource: UpdateSource,
    versionName: String,
    modifier: Modifier = Modifier,
    onOpenUpdater: () -> Unit = {},
    onOpenReleases: () -> Unit = {},
) {
    val subtitle = if (updateSource.isKnownUpdater) {
        stringResource(R.string.settings_updates_managed_by, updateSource.displayName)
    } else {
        stringResource(R.string.settings_updates_check_github, versionName)
    }
    SettingsRow(
        title = stringResource(R.string.settings_updates_title),
        subtitle = subtitle,
        iconVector = Icons.Rounded.SystemUpdate,
        iconText = null,
        iconShape = AppShape.Squircle,
        showChevron = true,
        onClick = if (updateSource.isKnownUpdater) onOpenUpdater else onOpenReleases,
        modifier = modifier,
    )
}
