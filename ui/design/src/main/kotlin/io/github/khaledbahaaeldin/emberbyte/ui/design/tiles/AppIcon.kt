package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Renders an app icon clipped to the given [shape], or fallback avatar letter if [icon] is null (Contract 13.6).
 *
 * Hidden from the merged semantics tree so screen readers do not stutter on decorative app icons.
 */
@Composable
fun AppIcon(
    label: String,
    icon: ImageBitmap?,
    shape: AppShape,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val composeShape = shape.toShape()

    Box(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { }
            .clip(composeShape)
            .background(palette.primary),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.testTag("AppIcon:${shape.name}"))
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val letter = label.firstOrNull()?.uppercase() ?: ""
            Text(
                text = letter,
                style = MaterialTheme.typography.titleMedium,
                color = palette.onPrimary,
            )
        }
    }
}
