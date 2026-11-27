@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ExpressiveSwitch
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

internal val SettingsRowAlphaKey = SemanticsPropertyKey<Float>("SettingsRowAlpha")
internal const val SETTINGS_ROW_ICON_TAG = "settings_row_icon_vector"
private val LeadingSlotWidth = 48.dp
private val LeadingTileSize = 36.dp
private val LeadingGlyphSize = 20.dp
private const val LEADING_TINT_ALPHA = 0.12f
private const val DISABLED_ROW_ALPHA = 0.38f

@Composable
private fun rememberAnimatedRowAlpha(enabled: Boolean): Float {
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else DISABLED_ROW_ALPHA,
        animationSpec = if (reduceMotion) snap() else motion.effects(),
        label = "SettingsRowAlpha",
    )
    return alpha
}

@Composable
private fun SettingsLeadingIconSlot(
    iconVector: ImageVector,
    rowAlpha: Float,
) {
    val palette = LocalEmberbytePalette.current
    Box(
        modifier = Modifier
            .width(LeadingSlotWidth)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.CenterStart,
    ) {
        ShapedBox(
            shape = AppShape.Cookie.toShape(),
            color = palette.primary.copy(alpha = LEADING_TINT_ALPHA * rowAlpha),
            modifier = Modifier.size(LeadingTileSize),
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = palette.primary.copy(alpha = rowAlpha),
                modifier = Modifier
                    .size(LeadingGlyphSize)
                    .align(Alignment.Center)
                    .testTag(SETTINGS_ROW_ICON_TAG),
            )
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    iconVector: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    secondarySubtitle: String? = null,
    value: String? = null,
    showLeadingIcon: Boolean = true,
    showChevron: Boolean = false,
    trailingIcon: ImageVector? = null,
    trailingIconTag: String? = null,
    singleLineSubtitle: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    val rowShape = MaterialTheme.shapes.large
    val rowAlpha = rememberAnimatedRowAlpha(enabled)
    val fontScale = LocalDensity.current.fontScale
    val resolvedTrailing = trailingIcon ?: if (showChevron) Icons.AutoMirrored.Rounded.KeyboardArrowRight else null

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showLeadingIcon) {
                SettingsLeadingIconSlot(iconVector = iconVector, rowAlpha = rowAlpha)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLargeEmphasized,
                    color = palette.text.copy(alpha = rowAlpha),
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted.copy(alpha = rowAlpha),
                        maxLines = if (singleLineSubtitle && fontScale < 1.5f) 1 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (secondarySubtitle != null) {
                    Text(
                        text = secondarySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted.copy(alpha = rowAlpha),
                    )
                }
            }
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = palette.primary.copy(alpha = rowAlpha),
                )
            }
            if (resolvedTrailing != null) {
                val trailingMod = if (trailingIconTag != null) {
                    Modifier.testTag(trailingIconTag)
                } else {
                    Modifier.clearAndSetSemantics {}
                }
                Icon(
                    imageVector = resolvedTrailing,
                    contentDescription = null,
                    tint = palette.muted.copy(alpha = rowAlpha),
                    modifier = trailingMod,
                )
            }
        }
    }

    val rowSemantics = modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .testTag("settings_row_$title")
        .semantics(mergeDescendants = true) {
            set(SettingsRowAlphaKey, rowAlpha)
        }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = rowShape,
            color = Color.Transparent,
            contentColor = palette.text,
            modifier = rowSemantics,
            content = content,
        )
    } else {
        Box(modifier = rowSemantics) {
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
    iconVector: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showLeadingIcon: Boolean = true,
) {
    val palette = LocalEmberbytePalette.current
    val rowAlpha = rememberAnimatedRowAlpha(enabled)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("settings_row_$title")
            .semantics(mergeDescendants = true) {
                set(SettingsRowAlphaKey, rowAlpha)
            }
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showLeadingIcon) {
            SettingsLeadingIconSlot(iconVector = iconVector, rowAlpha = rowAlpha)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLargeEmphasized,
                color = palette.text.copy(alpha = rowAlpha),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted.copy(alpha = rowAlpha),
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.clearAndSetSemantics {}) {
            ExpressiveSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}

@Composable
fun LiveUpdatesRow(
    mayPost: Boolean,
    modifier: Modifier = Modifier,
    isSamsung: Boolean = Build.MANUFACTURER.equals("samsung", ignoreCase = true),
    onClick: () -> Unit,
) {
    SettingsRow(
        title = stringResource(R.string.settings_live_updates_title),
        subtitle = stringResource(
            if (mayPost) R.string.settings_live_updates_allowed else R.string.settings_live_updates_off,
        ),
        secondarySubtitle = if (isSamsung) stringResource(R.string.settings_now_bar_hint) else null,
        iconVector = Icons.Rounded.Notifications,
        showChevron = true,
        onClick = onClick,
        modifier = modifier,
    )
}
