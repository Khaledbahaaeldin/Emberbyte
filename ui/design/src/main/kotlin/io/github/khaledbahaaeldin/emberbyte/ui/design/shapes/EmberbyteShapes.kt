@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.shapes

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Expressive shape system for app tiles and identity (Contract 13.1, 13.6).
 *
 * Provides four distinct shapes hashed deterministically by package name:
 * - [Cookie]: 9-sided rounded cookie ([MaterialShapes.Cookie9Sided])
 * - [Squircle]: 32% rounded corner squircle ([RoundedCornerShape])
 * - [Sunny]: Rounded sunburst polygon ([MaterialShapes.Sunny])
 * - [Burst]: Soft burst star polygon ([MaterialShapes.SoftBurst])
 */
enum class AppShape {
    Cookie,
    Squircle,
    Sunny,
    Burst,
}

/**
 * Resolves an [AppShape] to a Compose [Shape].
 */
@Composable
fun AppShape.toShape(): Shape = when (this) {
    AppShape.Cookie -> MaterialShapes.Cookie9Sided.toShape()
    AppShape.Squircle -> RoundedCornerShape(32)
    AppShape.Sunny -> MaterialShapes.Sunny.toShape()
    AppShape.Burst -> MaterialShapes.SoftBurst.toShape()
}

/**
 * Corner enumeration for asymmetric corner styling.
 */
enum class Corner {
    TopStart,
    TopEnd,
    BottomEnd,
    BottomStart,
}

/**
 * Creates a [CornerBasedShape] with three corners at [large] radius and one [smallCorner] at [small] radius.
 *
 * Uses Compose's [RoundedCornerShape] with start/end corner definitions so that corners
 * automatically flip in Right-to-Left (RTL) layouts.
 */
fun mixedCorners(
    large: Dp,
    small: Dp,
    smallCorner: Corner,
): CornerBasedShape = when (smallCorner) {
    Corner.TopStart -> RoundedCornerShape(
        topStart = small,
        topEnd = large,
        bottomEnd = large,
        bottomStart = large,
    )
    Corner.TopEnd -> RoundedCornerShape(
        topStart = large,
        topEnd = small,
        bottomEnd = large,
        bottomStart = large,
    )
    Corner.BottomEnd -> RoundedCornerShape(
        topStart = large,
        topEnd = large,
        bottomEnd = small,
        bottomStart = large,
    )
    Corner.BottomStart -> RoundedCornerShape(
        topStart = large,
        topEnd = large,
        bottomEnd = large,
        bottomStart = small,
    )
}

/**
 * Deterministically maps a [packageName] to one of the four [AppShape] variants
 * using a stable positive hash.
 */
fun shapeFor(packageName: String): AppShape {
    val idx = (packageName.hashCode() and 0x7fffffff) % 4
    return AppShape.entries[idx]
}

/** Shapes of the Home usage card (strip, rows and the three connected buttons). Tokens, so the card has no raw radii. */
object UsageShapes {
    val StripButton: CornerBasedShape = RoundedCornerShape(16.dp)
    val StripBar: CornerBasedShape = RoundedCornerShape(13.dp)
    val Row: CornerBasedShape = RoundedCornerShape(22.dp)
    val FeaturedRow: CornerBasedShape = RoundedCornerShape(26.dp)
    val SegmentStart: CornerBasedShape = RoundedCornerShape(topStart = 27.dp, bottomStart = 27.dp, topEnd = 9.dp, bottomEnd = 9.dp)
    val SegmentMiddle: CornerBasedShape = RoundedCornerShape(9.dp)
    val SegmentEnd: CornerBasedShape = RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp, topEnd = 27.dp, bottomEnd = 27.dp)
}

/** Shapes of the Apps page: the connected search and Filters pair, the period buttons and the rows. */
object AppsShapes {
    val SearchField: CornerBasedShape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp)
    val SearchFieldFocused: CornerBasedShape = RoundedCornerShape(28.dp)
    val FiltersButton: CornerBasedShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 28.dp, bottomEnd = 28.dp)
    val PeriodStart: CornerBasedShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 9.dp, bottomEnd = 9.dp)
    val PeriodMiddle: CornerBasedShape = RoundedCornerShape(9.dp)
    val PeriodEnd: CornerBasedShape = RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp, topEnd = 24.dp, bottomEnd = 24.dp)
    val Row: CornerBasedShape = RoundedCornerShape(22.dp)
    val Chip: CornerBasedShape = RoundedCornerShape(50)
}
