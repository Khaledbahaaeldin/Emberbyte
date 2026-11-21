package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.home.SpikeTileUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText

/**
 * The newest undismissed data spike, as a quiet banner above the Apps list. Opens the app's detail page; Dismiss hides
 * it. It lives here, not on Home, so the first screen carries less.
 */
@Composable
internal fun SpikeBanner(
    spike: SpikeTileUi,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(palette.warn.copy(alpha = 0.14f))
            .clickable(onClickLabel = "Open app", role = Role.Button, onClick = onOpen)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
            .testTag("apps_spike_banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
            Text("Spike detected", style = MaterialTheme.typography.labelMedium, color = palette.muted)
            Text(spike.title, style = MaterialTheme.typography.titleSmall, color = palette.text)
            ForceLtr {
                Text(spike.detail, style = MaterialTheme.typography.bodySmall, color = palette.muted)
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
            Text("Dismiss", color = palette.linkText)
        }
    }
}
