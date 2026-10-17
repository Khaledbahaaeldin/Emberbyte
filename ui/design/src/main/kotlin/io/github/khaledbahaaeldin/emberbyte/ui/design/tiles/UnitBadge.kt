@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Compact cookie-shaped badge displaying a unit of data measurement (Contract 13.1).
 *
 * Rendered with [MaterialShapes.Cookie9Sided], with primary badge color and onPrimary text.
 * Hidden from the accessibility semantics tree as the parent hero number or row provides
 * the full accessible plain-language description.
 */
@Composable
fun UnitBadge(
    unit: String,
    size: Dp = 62.dp,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val badgeShape = MaterialShapes.Cookie9Sided.toShape()
    val textSize = (size.value * 0.28f).sp

    ForceLtr {
        Box(
            modifier = modifier
                .size(size)
                .clearAndSetSemantics { }
                .background(
                    color = palette.primary,
                    shape = badgeShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = unit,
                modifier = Modifier.clearAndSetSemantics { },
                color = palette.onPrimary,
                style = EmberbyteType.badge(textSize),
                maxLines = 1,
            )
        }
    }
}
