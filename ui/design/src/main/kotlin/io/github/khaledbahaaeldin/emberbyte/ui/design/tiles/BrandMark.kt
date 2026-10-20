@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Brand flame path from the expressive brand design.
 */
internal const val BRAND_FLAME_PATH =
    "M13 2c.6 3-1.2 4.4-2.6 6.2C9 10 8 11.6 8 13.6A4 4 0 0 0 12 18a4 4 0 0 0 4-4.2c0-1.3-.5-2.3-1.2-3.3.1 1 0 1.8-.7 2.4.2-3-1-6.3-1.1-10.9zM7 14a5 5 0 0 0 10 0c0 3.6-2.2 6-5 6s-5-2.4-5-6z"

internal val BrandFlameVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "BrandFlame",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(BRAND_FLAME_PATH).toNodes(),
        fill = SolidColor(Color.White),
    ).build()
}

/**
 * Expressive brand mark: burst shape in primary with the flame in onPrimary (Contract 13.1).
 * Decorative, completely hidden from the semantics tree.
 */
@Composable
fun BrandMark(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { },
        shape = AppShape.Burst.toShape(),
        color = palette.primary,
        contentColor = palette.onPrimary,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BrandFlameVector,
                contentDescription = null,
                modifier = Modifier.size(size * 0.48f),
                tint = palette.onPrimary,
            )
        }
    }
}
