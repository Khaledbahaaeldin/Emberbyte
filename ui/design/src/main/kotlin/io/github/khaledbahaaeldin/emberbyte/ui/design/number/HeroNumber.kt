package io.github.khaledbahaaeldin.emberbyte.ui.design.number

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UnitBadge

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em

internal fun formatHeroValue(value: String): AnnotatedString {
    return buildAnnotatedString {
        append(value)
        addStyle(SpanStyle(letterSpacing = 0.04.em), 0, value.length)
        val dotIndex = value.indexOf('.')
        if (dotIndex != -1 && dotIndex < value.lastIndex) {
            addStyle(SpanStyle(letterSpacing = 0.08.em), dotIndex, dotIndex + 1)
        }
    }
}

internal val HERO_NUMBER_MIN_SIZE: TextUnit = 36.sp
internal val HERO_NUMBER_BADGE_SIZE: Dp = 62.dp
internal val HERO_NUMBER_SPACING: Dp = 8.dp

internal fun calculateHeroNumberSize(
    value: String,
    targetSize: TextUnit,
    availableWidthPx: Float,
    density: Density,
    textMeasurer: TextMeasurer,
    badgeWidthPx: Float,
    spacingPx: Float,
    minSize: TextUnit = HERO_NUMBER_MIN_SIZE,
): TextUnit {
    val minSp = minSize.value
    val maxSp = targetSize.value
    if (availableWidthPx.isInfinite() || maxSp <= minSp) {
        return maxOf(maxSp, minSp).sp
    }

    val availableTextWidthPx = (availableWidthPx - badgeWidthPx - spacingPx).coerceAtLeast(0f)
    val annotatedValue = formatHeroValue(value)

    // Check if targetSize fits
    val maxLayout = textMeasurer.measure(
        text = annotatedValue,
        style = EmberbyteType.hero(maxSp.sp),
        maxLines = 1,
        density = density,
    )
    if (maxLayout.size.width <= availableTextWidthPx) {
        return maxSp.sp
    }

    // Check if minSize fits: if even minSize doesn't fit, clamp to minSize ("never below 36sp")
    val minLayout = textMeasurer.measure(
        text = annotatedValue,
        style = EmberbyteType.hero(minSp.sp),
        maxLines = 1,
        density = density,
    )
    if (minLayout.size.width >= availableTextWidthPx) {
        return minSp.sp
    }

    // Binary search over size in [minSp, maxSp]
    var low = minSp
    var high = maxSp
    var best = minSp
    for (i in 0 until 14) {
        val mid = (low + high) / 2f
        val layout = textMeasurer.measure(
            text = annotatedValue,
            style = EmberbyteType.hero(mid.sp),
            maxLines = 1,
            density = density,
        )
        if (layout.size.width <= availableTextWidthPx) {
            best = mid
            low = mid + 0.25f
        } else {
            high = mid - 0.25f
        }
    }
    return best.sp
}

/**
 * Large expressive hero number displaying a data value and its cookie unit badge (Contract 13.1).
 *
 * Exposes a single accessibility semantics node with [contentDescription]. Auto-shrinks
 * the value text via binary search down to a minimum of 36.sp so that it never clips even
 * under high font scaling (e.g. font scale 2.0).
 */
@Composable
fun HeroNumber(
    value: String,
    unit: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 96.sp,
    color: Color = LocalEmberbytePalette.current.primary,
    onSizeResolved: ((TextUnit) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    ForceLtr {
        BoxWithConstraints(
            modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        ) {
            val availableWidthPx = if (constraints.hasBoundedWidth) {
                constraints.maxWidth.toFloat()
            } else {
                Float.POSITIVE_INFINITY
            }

            val badgeWidthPx = with(density) { HERO_NUMBER_BADGE_SIZE.toPx() }
            val spacingPx = with(density) { HERO_NUMBER_SPACING.toPx() }

            val chosenSize = calculateHeroNumberSize(
                value = value,
                targetSize = size,
                availableWidthPx = availableWidthPx,
                density = density,
                textMeasurer = textMeasurer,
                badgeWidthPx = badgeWidthPx,
                spacingPx = spacingPx,
            )

            onSizeResolved?.invoke(chosenSize)

            val annotatedValue = formatHeroValue(value)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HERO_NUMBER_SPACING),
            ) {
                Text(
                    text = annotatedValue,
                    modifier = Modifier.clearAndSetSemantics { },
                    color = color,
                    style = EmberbyteType.hero(chosenSize),
                    maxLines = 1,
                    softWrap = false,
                )
                UnitBadge(
                    unit = unit,
                    size = HERO_NUMBER_BADGE_SIZE,
                )
            }
        }
    }
}
