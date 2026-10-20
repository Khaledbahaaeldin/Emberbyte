package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    defaultShape: Shape = RoundedCornerShape(28.dp),
): Shape = if (corner != null) {
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = LocalEmberbytePalette.current
    val (background, foreground) = tileColors(container, palette, scheme)
    val tileShape = bentoTileShape(corner, MaterialTheme.shapes.extraLarge)
    val tileModifier = modifier.testTag("bento_tile_${container.name.lowercase()}")
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
                    )
                    trailing()
                }
            } else {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = foreground.copy(alpha = TILE_TITLE_ALPHA),
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
