package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R

internal const val SETTINGS_ROW_EXTERNAL_LINK_TAG = "settings_row_external_link"

/**
 * Points the user to whoever updates the app. It cannot say whether a newer version exists, because the app has no
 * network permission by design.
 *
 * - A known updater (Obtainium, Orion Store, F-Droid) is named, and tapping the row opens it ([onOpenUpdater]).
 * - Any other installer, or none, shows one line with an external-link icon and opens GitHub releases ([onOpenReleases]).
 */
@Composable
fun UpdatesRow(
    updateSource: UpdateSource,
    versionName: String,
    modifier: Modifier = Modifier,
    onOpenUpdater: () -> Unit = {},
    onOpenReleases: () -> Unit = {},
    onOpenWhatsNew: (() -> Unit)? = null,
) {
    val isKnown = updateSource.isKnownUpdater
    val subtitle = if (isKnown) {
        stringResource(R.string.settings_updates_managed_by, updateSource.displayName)
    } else {
        stringResource(R.string.settings_updates_check_github, versionName)
    }
    Column(modifier = modifier) {
        SettingsRow(
            title = stringResource(R.string.settings_updates_title),
            subtitle = subtitle,
            iconVector = Icons.Rounded.SystemUpdate,
            showChevron = isKnown,
            trailingIcon = if (isKnown) null else Icons.AutoMirrored.Rounded.OpenInNew,
            trailingIconTag = if (isKnown) null else SETTINGS_ROW_EXTERNAL_LINK_TAG,
            singleLineSubtitle = true,
            onClick = if (isKnown) onOpenUpdater else onOpenReleases,
        )
        if (onOpenWhatsNew != null) {
            TextButton(
                onClick = onOpenWhatsNew,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .padding(start = 54.dp)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(stringResource(R.string.whatsnew_updates_link))
            }
        }
    }
}
