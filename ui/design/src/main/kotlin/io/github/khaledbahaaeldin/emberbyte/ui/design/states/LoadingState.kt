package io.github.khaledbahaaeldin.emberbyte.ui.design.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

/**
 * Shared loading state component across screens (Contract 13.1, Plan B6).
 *
 * Shows:
 * - A progress indicator described by [label] for accessibility
 * - A static indicator icon when reduced motion is on ([LocalReduceMotion]).
 */
@Composable
fun LoadingState(
    label: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current

    Box(
        modifier = modifier.semantics {
            contentDescription = label
        },
        contentAlignment = Alignment.Center,
    ) {
        if (reduceMotion) {
            Icon(
                imageVector = Icons.Rounded.HourglassEmpty,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(36.dp),
            )
        } else {
            CircularProgressIndicator(
                color = palette.primary,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
