package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Expressive switch conforming to Material 3 Expressive guidelines.
 *
 * Displays a check icon in the thumb when checked and ensures a minimum touch target of 48 dp.
 */
@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = LocalEmberbytePalette.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        enabled = enabled,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(SwitchDefaults.IconSize)
                        .testTag("switch_check_icon"),
                )
            }
        } else null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = palette.onPrimary,
            checkedTrackColor = palette.primary,
            checkedIconColor = palette.primary,
            uncheckedThumbColor = palette.muted,
            uncheckedTrackColor = palette.surface2,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedThumbColor = palette.onPrimary.copy(alpha = 0.38f),
            disabledCheckedTrackColor = palette.primary.copy(alpha = 0.12f),
            disabledUncheckedThumbColor = palette.muted.copy(alpha = 0.38f),
            disabledUncheckedTrackColor = palette.surface2.copy(alpha = 0.12f),
            disabledUncheckedBorderColor = Color.Transparent,
        ),
    )
}
