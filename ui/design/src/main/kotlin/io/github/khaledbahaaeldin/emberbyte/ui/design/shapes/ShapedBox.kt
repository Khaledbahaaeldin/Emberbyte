package io.github.khaledbahaaeldin.emberbyte.ui.design.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * A shaped container box filled with [color] and clipped to [shape] (Contract 13.1, 13.6).
 */
@Composable
fun ShapedBox(
    shape: Shape,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = color, shape = shape),
        content = content,
    )
}
