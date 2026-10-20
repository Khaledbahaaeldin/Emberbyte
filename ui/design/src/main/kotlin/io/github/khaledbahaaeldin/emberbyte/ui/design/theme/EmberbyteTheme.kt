@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.resolvePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.toColorScheme

/** True when the user turned system animations off. Springs become snaps and fades. */
val LocalReduceMotion: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

val LocalEmberbytePalette: ProvidableCompositionLocal<EmberbytePalette> =
    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette

internal fun isReduceMotion(animatorDurationScale: Float): Boolean = animatorDurationScale == 0f

internal fun readReduceMotion(context: Context): Boolean = isReduceMotion(
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f),
)

/** True while the system animator scale is 0; updates live when the user changes the setting. */
@Composable
internal fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    var reduce by remember(context) { mutableStateOf(readReduceMotion(context)) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduce = readReduceMotion(context)
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        reduce = readReduceMotion(context)
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    return reduce
}

// The dynamic lambdas are only invoked when resolvePalette sees sdkInt >= 31; lint cannot see through it.
@SuppressLint("NewApi")
@Composable
fun EmberbyteTheme(
    spec: EmberbyteThemeSpec = EmberbyteThemeSpec.Default,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val palette = resolvePalette(
        spec = spec,
        sdkInt = Build.VERSION.SDK_INT,
        dynamic = {
            if (spec.darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        },
    )
    val scheme = palette.toColorScheme(spec.darkTheme)
    val reduceMotion = rememberReduceMotion()
    CompositionLocalProvider(
        LocalEmberbytePalette provides palette,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
