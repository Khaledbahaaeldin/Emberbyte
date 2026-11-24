package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos

private const val FRAME_INTERVAL_NANOS = 33_333_333L

@Composable
internal fun rememberSettingsAmbientSeconds(enabled: Boolean): State<Float> {
    val seconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        var startNanos = -1L
        var lastEmitNanos = -1L
        while (true) {
            withFrameNanos { frameNanos ->
                if (startNanos < 0L) startNanos = frameNanos
                if (lastEmitNanos < 0L || frameNanos - lastEmitNanos >= FRAME_INTERVAL_NANOS) {
                    lastEmitNanos = frameNanos
                    seconds.floatValue = (frameNanos - startNanos) / 1_000_000_000f
                }
            }
        }
    }
    return seconds
}
