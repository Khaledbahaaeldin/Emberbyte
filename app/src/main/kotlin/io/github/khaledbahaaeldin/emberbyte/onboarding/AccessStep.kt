package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType

internal enum class RowState { Allowed, Next, Waiting }

@Composable
internal fun AccessStep(
    usageAccess: Boolean,
    notifications: Boolean,
    phoneState: Boolean,
    batteryUnrestricted: Boolean,
    reveal: (Int) -> Float,
    onOpenUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestPhone: () -> Unit,
    onRequestBattery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val next = nextPermissionAction(usageAccess, notifications, phoneState, batteryUnrestricted)
    fun stateOf(allowed: Boolean, mine: PermissionAction) = when {
        allowed -> RowState.Allowed
        next == mine -> RowState.Next
        else -> RowState.Waiting
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        val r0 = reveal(0)
        Text(
            text = stringResource(R.string.onb_access_title),
            style = onboardingTitleStyle(OnboardingType.StepTitle),
            color = palette.text,
            modifier = Modifier
                .semantics { heading() }
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                },
        )
        Spacer(Modifier.height(14.dp))
        val r1 = reveal(1)
        Text(
            text = stringResource(R.string.onb_access_lead),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = OnboardingType.LeadBody),
            color = palette.muted,
            modifier = Modifier.graphicsLayer {
                alpha = r1
                translationY = (1f - r1) * 14.dp.toPx()
            },
        )
        Spacer(Modifier.height(28.dp))

        val r2 = reveal(2)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.graphicsLayer {
                alpha = r2
                translationY = (1f - r2) * 14.dp.toPx()
            },
        ) {
            PermissionRow(
                icon = Icons.Rounded.Visibility,
                title = stringResource(R.string.onb_usage_title),
                subtitle = stringResource(R.string.onb_usage_sub),
                tag = stringResource(R.string.onb_tag_needed),
                required = true,
                state = stateOf(usageAccess, PermissionAction.Usage),
                smallCorner = Corner.BottomStart,
                onClick = onOpenUsageAccess,
                testTag = "permission_row_usage",
            )
            PermissionRow(
                icon = Icons.Rounded.NotificationsActive,
                title = stringResource(R.string.onb_notif_title),
                subtitle = stringResource(R.string.onb_notif_sub),
                tag = stringResource(R.string.onb_tag_optional),
                required = false,
                state = stateOf(notifications, PermissionAction.Notifications),
                smallCorner = Corner.BottomStart,
                onClick = onRequestNotifications,
                testTag = "permission_row_notifications",
            )
            PermissionRow(
                icon = Icons.Rounded.PhoneAndroid,
                title = stringResource(R.string.onb_phone_title),
                subtitle = stringResource(R.string.onb_phone_sub),
                tag = stringResource(R.string.onb_tag_optional),
                required = false,
                state = stateOf(phoneState, PermissionAction.Phone),
                smallCorner = Corner.BottomStart,
                onClick = onRequestPhone,
                testTag = "permission_row_phone",
            )
            PermissionRow(
                icon = Icons.Rounded.BatteryChargingFull,
                title = stringResource(R.string.onb_battery_title),
                subtitle = stringResource(R.string.onb_battery_sub),
                tag = stringResource(R.string.onb_tag_optional),
                required = false,
                state = stateOf(batteryUnrestricted, PermissionAction.Battery),
                smallCorner = Corner.BottomStart,
                onClick = onRequestBattery,
                testTag = "permission_row_battery",
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    required: Boolean,
    state: RowState,
    smallCorner: Corner,
    onClick: () -> Unit,
    testTag: String,
) {
    val palette = LocalEmberbytePalette.current
    val allowed = state == RowState.Allowed
    val description = if (allowed) {
        stringResource(R.string.onb_row_desc_allowed, title)
    } else {
        stringResource(R.string.onb_row_desc_open, title, subtitle)
    }
    val shape = mixedCorners(large = 26.dp, small = 10.dp, smallCorner = smallCorner)
    Surface(
        shape = shape,
        color = when (state) {
            RowState.Next -> palette.surface2
            RowState.Waiting -> palette.surface
            RowState.Allowed -> palette.background
        },
        border = when (state) {
            RowState.Next -> BorderStroke(2.dp, palette.primary)
            RowState.Allowed -> BorderStroke(1.5.dp, palette.surface2)
            RowState.Waiting -> null
        },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp)
            .clip(shape)
            .then(
                if (allowed) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = description
                        role = Role.Image
                    }
                } else {
                    Modifier
                        .clickable(role = Role.Button, onClick = onClick)
                        .semantics { contentDescription = description }
                },
            )
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (allowed) palette.accent else palette.surface2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (allowed) Icons.Rounded.Check else icon,
                    contentDescription = null,
                    tint = if (allowed) palette.onAccent else palette.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = EmberbyteType.heading(OnboardingType.RowTitle),
                    color = palette.text,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.RowBody),
                    color = palette.muted,
                )
            }
            Tag(
                text = if (allowed) stringResource(R.string.onb_tag_allowed) else tag,
                filled = required && !allowed,
                ok = allowed,
            )
        }
    }
}

@Composable
private fun Tag(text: String, filled: Boolean, ok: Boolean) {
    val palette = LocalEmberbytePalette.current
    Text(
        text = text,
        style = EmberbyteType.heading(OnboardingType.Tag),
        color = when {
            ok -> palette.accent
            filled -> palette.background
            else -> palette.muted
        },
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .clip(CircleShape)
            .background(
                when {
                    ok -> palette.background
                    filled -> palette.primary
                    else -> palette.surface2
                },
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
