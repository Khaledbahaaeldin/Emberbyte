package io.github.khaledbahaaeldin.emberbyte.nav

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.LocalEmberbytePalette
import kotlinx.coroutines.CancellationException

/**
 * Wraps form screens with a predictive back guard.
 *
 * When [dirty] is false:
 * The handler is disabled so the system back invocation and normal pop transition execute cleanly.
 *
 * When [dirty] is true:
 * Intercepts the back gesture with [PredictiveBackHandler], previews the screen shrinking as the
 * user drags, and upon gesture commit displays the "Discard changes?" confirmation dialog.
 * If cancelled, resets shrink to 1.0. If the user confirms in the dialog, invokes [onConfirmLeave].
 */
@Composable
fun GuardedBack(
    dirty: Boolean,
    onConfirmLeave: () -> Unit,
    onDismiss: () -> Unit = {},
    content: @Composable (shrink: Float) -> Unit,
) {
    var shrink by remember { mutableFloatStateOf(1f) }
    var gestureProgress by remember { mutableFloatStateOf(0f) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    PredictiveBackHandler(enabled = dirty) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                val p = backEvent.progress.coerceIn(0f, 1f)
                gestureProgress = p
                shrink = 1.0f - (1.0f - BackTransitions.SHRINK) * p
            }
            showDiscardDialog = true
        } catch (_: CancellationException) {
            shrink = 1f
            gestureProgress = 0f
        }
    }

    val cornerRadius = BackTransitions.cornerRadiusFor(gestureProgress)
    val shadowElevation = (8f * (gestureProgress * 4f).coerceAtMost(1f)).dp
    val palette = LocalEmberbytePalette.current
    val layoutDirection = LocalLayoutDirection.current
    val origin = BackTransitions.transformOriginFor(layoutDirection)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = shrink
                scaleY = shrink
                transformOrigin = origin
                this.shadowElevation = shadowElevation.toPx()
                this.shape = RoundedCornerShape(cornerRadius)
                this.clip = cornerRadius > 0.dp
            }
            .background(palette.background),
    ) {
        content(shrink)
    }

    if (showDiscardDialog) {
        val discardRequester = remember { FocusRequester() }
        EmberbyteDialog(
            onDismiss = {
                showDiscardDialog = false
                shrink = 1f
                gestureProgress = 0f
                onDismiss()
            },
            title = "Discard changes?",
            icon = Icons.Rounded.Delete,
            confirm = DialogAction(
                label = "Discard",
                onClick = {
                    showDiscardDialog = false
                    shrink = 1f
                    gestureProgress = 0f
                    onConfirmLeave()
                },
                modifier = Modifier.focusRequester(discardRequester),
            ),
            dismiss = DialogAction(
                label = "Keep editing",
                onClick = {
                    showDiscardDialog = false
                    shrink = 1f
                    gestureProgress = 0f
                    onDismiss()
                },
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
