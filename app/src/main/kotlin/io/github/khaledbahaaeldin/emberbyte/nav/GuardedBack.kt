package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Wraps form screens with a predictive back guard.
 *
 * When [dirty] is false:
 * The handler is disabled so the system back invocation and normal pop transition execute cleanly.
 *
 * When [dirty] is true:
 * Intercepts the back gesture with [PredictiveBackHandler], previews the screen shrinking as the
 * user drags, and upon gesture commit displays the "Discard changes?" confirmation dialog.
 * If cancelled, smoothly springs back to rest via [BackTransitions.cancelSpring].
 * If the user confirms in the dialog, invokes [onConfirmLeave].
 */
@Composable
fun GuardedBack(
    dirty: Boolean,
    onConfirmLeave: () -> Unit,
    onDismiss: () -> Unit = {},
    content: @Composable (shrink: Float) -> Unit,
) {
    val progress = remember { Animatable(0f) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val resetToRest: () -> Unit = {
        coroutineScope.launch {
            progress.animateTo(0f, BackTransitions.cancelSpring)
        }
    }

    val dismissDialog: () -> Unit = {
        showDiscardDialog = false
        resetToRest()
        onDismiss()
    }

    PredictiveBackHandler(enabled = dirty) { progressFlow ->
        NavDiagnostics.record("BACK_START", "guarded")
        try {
            progressFlow.collect { backEvent ->
                val p = backEvent.progress.coerceIn(0f, 1f)
                progress.snapTo(p)
            }
            NavDiagnostics.record("BACK_COMMIT", "guarded")
            showDiscardDialog = true
        } catch (_: CancellationException) {
            NavDiagnostics.record("BACK_CANCEL", "guarded")
            resetToRest()
        }
    }

    val palette = LocalEmberbytePalette.current
    val layoutDirection = LocalLayoutDirection.current
    val origin = remember(layoutDirection) { BackTransitions.transformOriginFor(layoutDirection) }
    val guardedShape = remember { GuardedBackShape { progress.value } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val currentProgress = progress.value
                val shrink = BackTransitions.shrinkFor(currentProgress)
                val cornerRadius = BackTransitions.cornerRadiusFor(currentProgress)
                val shadowElevation = BackTransitions.shadowDpFor(currentProgress)

                scaleX = shrink
                scaleY = shrink
                transformOrigin = origin
                this.shadowElevation = shadowElevation.toPx()
                this.shape = guardedShape
                this.clip = cornerRadius > 0.dp
            }
            .background(palette.background),
    ) {
        content(BackTransitions.shrinkFor(progress.value))
    }

    if (showDiscardDialog) {
        val discardRequester = remember { FocusRequester() }
        EmberbyteDialog(
            onDismiss = dismissDialog,
            title = "Discard changes?",
            icon = Icons.Rounded.Delete,
            confirm = DialogAction(
                label = "Discard",
                onClick = {
                    showDiscardDialog = false
                    onConfirmLeave()
                },
                modifier = Modifier.focusRequester(discardRequester),
            ),
            dismiss = DialogAction(
                label = "Keep editing",
                onClick = dismissDialog,
            ),
            destructive = true,
        ) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.yield()
                discardRequester.requestFocus()
            }
            Text(
                text = "You edited this plan. If you leave now, those changes are lost.",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalEmberbytePalette.current.muted,
            )
        }
    }
}

private class GuardedBackShape(private val progressProvider: () -> Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radiusPx = with(density) { BackTransitions.cornerRadiusFor(progressProvider()).toPx() }
        if (radiusPx <= 0f) {
            return Outline.Rectangle(Rect(Offset.Zero, size))
        }
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
            ),
        )
    }

    override fun equals(other: Any?): Boolean = other is GuardedBackShape
    override fun hashCode(): Int = GuardedBackShape::class.hashCode()
}
