@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.ui.design.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Action specification for buttons inside an [EmberbyteDialog].
 */
data class DialogAction(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val modifier: Modifier = Modifier,
)

/**
 * Expressive dialog component according to design spec 13.1 and mockup 12.
 *
 * Features:
 * - A mixed-corner `surface2` container.
 * - An optional cookie-shaped icon container with the provided [icon].
 * - Title formatted with [EmberbyteType.heading] and semantic heading role.
 * - Entire dialog marked as an accessible pane (`paneTitle`).
 * - Scrollable content area with `imePadding` preserving visibility of buttons and focused fields.
 * - Pill-shaped buttons: Primary or destructive (coral danger/onDanger with a warning mark), and text dismiss button.
 */
@Composable
fun EmberbyteDialog(
    onDismiss: () -> Unit,
    title: String,
    icon: ImageVector?,
    confirm: DialogAction,
    dismiss: DialogAction?,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val containerShape = mixedCorners(
        large = 32.dp,
        small = 16.dp,
        smallCorner = Corner.BottomEnd,
    )
    val cookieShape = MaterialShapes.Cookie9Sided.toShape()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .semantics { paneTitle = title },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clip(containerShape)
                    .background(palette.surface2, containerShape)
                    .padding(24.dp),
            ) {
                // Header: Cookie icon + Title
                if (icon != null) {
                    ShapedBox(
                        shape = cookieShape,
                        color = if (destructive) palette.danger else palette.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("emberbyte_dialog_header_icon"),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (destructive) palette.onDanger else palette.onPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Title
                Text(
                    text = title,
                    style = EmberbyteType.heading(24.sp),
                    color = if (destructive) palette.danger else palette.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() },
                )

                Spacer(Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                ) {
                    content()
                }

                Spacer(Modifier.height(20.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dismiss != null) {
                        TextButton(
                            onClick = dismiss.onClick,
                            enabled = dismiss.enabled,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = palette.text,
                            ),
                            modifier = Modifier
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .then(dismiss.modifier)
                                .testTag("emberbyte_dialog_dismiss_button"),
                        ) {
                            Text(
                                text = dismiss.label,
                                style = EmberbyteType.heading(15.sp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Button(
                        onClick = confirm.onClick,
                        enabled = confirm.enabled,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (destructive) palette.danger else palette.primary,
                            contentColor = if (destructive) palette.onDanger else palette.onPrimary,
                            disabledContainerColor = (if (destructive) palette.danger else palette.primary).copy(alpha = 0.38f),
                            disabledContentColor = (if (destructive) palette.onDanger else palette.onPrimary).copy(alpha = 0.38f),
                        ),
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .then(confirm.modifier)
                            .testTag("emberbyte_dialog_confirm_button"),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (destructive) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .testTag("emberbyte_dialog_destructive_icon"),
                                )
                            }
                            Text(
                                text = confirm.label,
                                style = EmberbyteType.heading(15.sp),
                            )
                        }
                    }
                }
            }
        }
    }
}
