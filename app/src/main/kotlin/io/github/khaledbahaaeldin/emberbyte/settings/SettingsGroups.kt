package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ExpressiveSwitch
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    iconShape: AppShape = AppShape.Cookie,
    iconColor: Color = LocalEmberbytePalette.current.primary,
    onIconColor: Color = LocalEmberbytePalette.current.onPrimary,
    iconText: String? = title.firstOrNull()?.uppercase(),
    iconVector: ImageVector? = null,
    containerColor: Color = LocalEmberbytePalette.current.surface,
    contentColor: Color = LocalEmberbytePalette.current.text,
    shape: Shape = RoundedCornerShape(30.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics(mergeDescendants = true) { heading() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                ShapedBox(
                    shape = iconShape.toShape(),
                    color = iconColor,
                    modifier = Modifier
                        .size(22.dp)
                        .clearAndSetSemantics {},
                ) {
                    if (iconVector != null) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = onIconColor,
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.Center),
                        )
                    } else if (iconText != null) {
                        Text(
                            text = iconText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                            ),
                            color = onIconColor,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                    ),
                    color = palette.muted,
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    iconShape: AppShape = AppShape.Cookie,
    iconColor: Color = LocalEmberbytePalette.current.surface2,
    onIconColor: Color = LocalEmberbytePalette.current.primary,
    iconText: String? = title.firstOrNull()?.uppercase(),
    iconVector: ImageVector? = null,
    showLeadingIcon: Boolean = true,
    showChevron: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    val rowShape = RoundedCornerShape(22.dp)
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showLeadingIcon) {
                ShapedBox(
                    shape = iconShape.toShape(),
                    color = if (enabled) iconColor else iconColor.copy(alpha = 0.38f),
                    modifier = Modifier
                        .size(32.dp)
                        .clearAndSetSemantics {},
                ) {
                    if (iconVector != null) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = if (enabled) onIconColor else onIconColor.copy(alpha = 0.38f),
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.Center),
                        )
                    } else if (iconText != null) {
                        Text(
                            text = iconText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (enabled) onIconColor else onIconColor.copy(alpha = 0.38f),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = EmberbyteType.heading(15.sp),
                    color = if (enabled) palette.text else palette.text.copy(alpha = 0.38f),
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) palette.muted else palette.muted.copy(alpha = 0.38f),
                    )
                }
            }
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (enabled) palette.primary else palette.primary.copy(alpha = 0.38f),
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (enabled) palette.muted else palette.muted.copy(alpha = 0.38f),
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = rowShape,
            color = Color.Transparent,
            contentColor = palette.text,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .semantics(mergeDescendants = true) {},
            content = content,
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .semantics(mergeDescendants = true) {},
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconShape: AppShape = AppShape.Cookie,
    iconColor: Color = LocalEmberbytePalette.current.surface2,
    onIconColor: Color = LocalEmberbytePalette.current.primary,
    iconText: String? = title.firstOrNull()?.uppercase(),
    iconVector: ImageVector? = null,
    showLeadingIcon: Boolean = true,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) {}
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showLeadingIcon) {
            ShapedBox(
                shape = iconShape.toShape(),
                color = if (enabled) iconColor else iconColor.copy(alpha = 0.38f),
                modifier = Modifier
                    .size(32.dp)
                    .clearAndSetSemantics {},
            ) {
                if (iconVector != null) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = if (enabled) onIconColor else onIconColor.copy(alpha = 0.38f),
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center),
                    )
                } else if (iconText != null) {
                    Text(
                        text = iconText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (enabled) onIconColor else onIconColor.copy(alpha = 0.38f),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = EmberbyteType.heading(15.sp),
                color = if (enabled) palette.text else palette.text.copy(alpha = 0.38f),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) palette.muted else palette.muted.copy(alpha = 0.38f),
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.clearAndSetSemantics {}) {
            ExpressiveSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}
