package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The only corner radii the app uses. 1.0.2 had twelve different values (10 to 36 dp) scattered over 31 call sites;
 * everything now comes from this scale through `MaterialTheme.shapes`.
 *
 * Not to be confused with `AppShape` (`shapes/EmberbyteShapes.kt`), which is the set of identity shapes
 * (cookie, squircle, sunny, burst) used for app icons and tiles.
 */
object EmberbyteShapeRadii {
    val ExtraSmall = 8.dp
    val Small = 12.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
    val LargeIncreased = 28.dp
    val ExtraLargeIncreased = 36.dp
    val ExtraExtraLarge = 48.dp
}

/** Passed to `MaterialExpressiveTheme` so library components and `MaterialTheme.shapes` follow the app's shape language. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val EmberbyteShapeScale: Shapes = Shapes(
    extraSmall = RoundedCornerShape(EmberbyteShapeRadii.ExtraSmall),
    small = RoundedCornerShape(EmberbyteShapeRadii.Small),
    medium = RoundedCornerShape(EmberbyteShapeRadii.Medium),
    large = RoundedCornerShape(EmberbyteShapeRadii.Large),
    largeIncreased = RoundedCornerShape(EmberbyteShapeRadii.LargeIncreased),
    extraLarge = RoundedCornerShape(EmberbyteShapeRadii.ExtraLarge),
    extraLargeIncreased = RoundedCornerShape(EmberbyteShapeRadii.ExtraLargeIncreased),
    extraExtraLarge = RoundedCornerShape(EmberbyteShapeRadii.ExtraExtraLarge),
)

/** Dynamic corner shape for predictive-back transitions. */
fun predictiveBackShape(radius: Dp): Shape = RoundedCornerShape(radius)
