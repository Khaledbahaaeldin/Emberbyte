package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

@Composable
fun DormantBadge(
    text: String = "Dormant",
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Box(
        modifier = modifier
            .background(palette.surface, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = text
            },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = palette.muted,
        )
    }
}
