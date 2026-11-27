package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette

/**
 * Shows [content] with the Expressive spatial spring (height) and the effects spec (fade).
 * Under reduced motion it appears and disappears without any transition.
 *
 * Use it instead of a bare `if (condition) { ... }` wherever visible UI is inserted or removed, so
 * the content below moves with a spring instead of jumping.
 */
@Composable
fun EmberbyteAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (reduceMotion) {
            EnterTransition.None
        } else {
            expandVertically(animationSpec = motion.spatial<IntSize>()) + fadeIn(animationSpec = motion.effects<Float>())
        },
        exit = if (reduceMotion) {
            ExitTransition.None
        } else {
            shrinkVertically(animationSpec = motion.spatial<IntSize>()) + fadeOut(animationSpec = motion.effects<Float>())
        },
        content = content,
    )
}

/**
 * Cross-fades between states with the effects spec. Under reduced motion the fade is skipped.
 */
@Composable
fun <T> EmberbyteCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = if (reduceMotion) snap() else motion.effects<Float>(),
        label = "EmberbyteCrossfade",
        content = content,
    )
}

/**
 * Animates every colour of [target] with the effects spec so a theme or dark-mode change cross-fades
 * instead of flipping in one frame. Under reduced motion it snaps. The first composition returns
 * [target] unchanged.
 */
@Composable
fun animatePaletteAsState(target: EmberbytePalette): EmberbytePalette {
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    val spec: FiniteAnimationSpec<Color> = if (reduceMotion) snap() else motion.effects<Color>()
    return animatePalette(target, spec)
}

/** Same as [animatePaletteAsState] with an explicit spec; used by the theme before the locals exist. */
@Composable
internal fun animatePalette(target: EmberbytePalette, spec: FiniteAnimationSpec<Color>): EmberbytePalette {
    val background by animateColorAsState(target.background, spec, label = "background")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val surface2 by animateColorAsState(target.surface2, spec, label = "surface2")
    val text by animateColorAsState(target.text, spec, label = "text")
    val muted by animateColorAsState(target.muted, spec, label = "muted")
    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "onPrimary")
    val accent by animateColorAsState(target.accent, spec, label = "accent")
    val onAccent by animateColorAsState(target.onAccent, spec, label = "onAccent")
    val warn by animateColorAsState(target.warn, spec, label = "warn")
    val onWarn by animateColorAsState(target.onWarn, spec, label = "onWarn")
    val danger by animateColorAsState(target.danger, spec, label = "danger")
    val onDanger by animateColorAsState(target.onDanger, spec, label = "onDanger")
    val track by animateColorAsState(target.track, spec, label = "track")
    return EmberbytePalette(
        background = background,
        surface = surface,
        surface2 = surface2,
        text = text,
        muted = muted,
        primary = primary,
        onPrimary = onPrimary,
        accent = accent,
        onAccent = onAccent,
        warn = warn,
        onWarn = onWarn,
        danger = danger,
        onDanger = onDanger,
        track = track,
    )
}
