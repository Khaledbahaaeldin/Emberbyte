package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteShapeScale
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette

enum class TileContainer {
    Default,
    Primary,
    Tertiary,
    Standard,
    Subtle,
    Accent,
    Warn,
    Danger,
    Raised,
}

internal const val TILE_TITLE_ALPHA = 0.87f

internal fun bentoTileShape(
    corner: Corner?,
    defaultShape: CornerBasedShape = EmberbyteShapeScale.largeIncreased as CornerBasedShape,
): CornerBasedShape = if (corner != null) {
    mixedCorners(large = 24.dp, small = 8.dp, smallCorner = corner)
} else {
    defaultShape
}

internal fun tileColors(
    container: TileContainer,
    palette: EmberbytePalette,
    scheme: ColorScheme,
): Pair<Color, Color> = when (container) {
    TileContainer.Default -> scheme.surfaceContainerHigh to scheme.onSurface
    TileContainer.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
    TileContainer.Tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    TileContainer.Standard -> palette.surface2 to palette.text
    TileContainer.Subtle -> palette.surface to palette.text
    TileContainer.Accent -> palette.accent to palette.onAccent
    TileContainer.Warn -> palette.warn to palette.onWarn
    TileContainer.Danger -> palette.danger to palette.onDanger
    TileContainer.Raised -> palette.surface2 to palette.text
}

internal fun tileColors(container: TileContainer, scheme: ColorScheme): Pair<Color, Color> =
    when (container) {
        TileContainer.Default -> scheme.surfaceContainerHigh to scheme.onSurface
        TileContainer.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
        TileContainer.Tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        TileContainer.Standard, TileContainer.Raised -> scheme.surfaceContainerHigh to scheme.onSurface
        TileContainer.Subtle -> scheme.surface to scheme.onSurface
        TileContainer.Accent -> scheme.tertiary to scheme.onTertiary
        TileContainer.Warn, TileContainer.Danger -> scheme.error to scheme.onError
    }

@Composable
fun BentoTile(
    title: String,
    modifier: Modifier = Modifier,
    container: TileContainer = TileContainer.Default,
    corner: Corner? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    headingTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = LocalEmberbytePalette.current
    val (background, foreground) = tileColors(container, palette, scheme)
    val tileShape = bentoTileShape(corner, MaterialTheme.shapes.extraLarge)
    val interactionSource = remember { MutableInteractionSource() }
    val tileModifier = if (onClick != null) {
        modifier
            .testTag("bento_tile_${container.name.lowercase()}")
            .pressMorph(tileShape, interactionSource = interactionSource)
    } else {
        modifier.testTag("bento_tile_${container.name.lowercase()}")
    }
    val titleModifier = if (headingTitle) Modifier.semantics { heading() } else Modifier
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(16.dp)) {
            if (trailing != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        color = foreground.copy(alpha = TILE_TITLE_ALPHA),
                        modifier = titleModifier,
                    )
                    trailing()
                }
            } else if (title.isNotEmpty()) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = foreground.copy(alpha = TILE_TITLE_ALPHA),
                    modifier = titleModifier,
                )
            }
            content()
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = tileModifier,
            shape = tileShape,
            color = background,
            contentColor = foreground,
            interactionSource = interactionSource,
            content = body,
        )
    } else {
        Surface(
            modifier = tileModifier,
            shape = tileShape,
            color = background,
            contentColor = foreground,
            content = body,
        )
    }
}
