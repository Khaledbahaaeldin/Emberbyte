package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape

internal class UpdatesRowState(
    val subtitle: String,
    val onClick: (() -> Unit)?,
) {
    companion object {
        fun from(source: UpdateSource, onOpenReleases: (() -> Unit)?): UpdatesRowState {
            return if (source.isKnownUpdater) {
                UpdatesRowState(
                    subtitle = "Managed by ${source.displayName}",
                    onClick = null,
                )
            } else {
                UpdatesRowState(
                    subtitle = "GitHub Releases",
                    onClick = onOpenReleases,
                )
            }
        }
    }
}

@Composable
fun UpdatesRow(
    updateSource: UpdateSource,
    modifier: Modifier = Modifier,
    onOpenReleases: (() -> Unit)? = null,
) {
    val state = UpdatesRowState.from(updateSource, onOpenReleases)

    SettingsRow(
        title = "Updates",
        subtitle = state.subtitle,
        iconVector = Icons.Rounded.Refresh,
        iconText = null,
        iconShape = AppShape.Squircle,
        showChevron = state.onClick != null,
        onClick = state.onClick,
        modifier = modifier,
    )
}
