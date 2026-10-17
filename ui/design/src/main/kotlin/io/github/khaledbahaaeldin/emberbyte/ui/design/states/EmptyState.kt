package io.github.khaledbahaaeldin.emberbyte.ui.design.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Shared empty state component across screens (Home, Apps, History, Plans, App detail).
 *
 * Uses:
 * - A cookie-shaped icon container ([MaterialShapes.Cookie9Sided])
 * - Title in [EmberbyteType.heading]
 * - Body in palette `muted`
 * - One merged semantic node for title and body, plus the optional interactive [action].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    val cookieShape = MaterialShapes.Cookie9Sided.toShape()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cookie-shaped icon container
        ShapedBox(
            shape = cookieShape,
            color = palette.surface2,
            modifier = Modifier.size(72.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        // Title and body merged into one semantic node
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = EmberbyteType.heading(20.sp),
                color = palette.text,
            )
            Text(
                text = body,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        // Optional action button
        if (action != null) {
            action()
        }
    }
}
