@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType

@Composable
internal fun AllowAccessStep(
    usageAccess: Boolean,
    notifications: Boolean,
    reveal: (Int) -> Float,
    firstCardHiddenByShared: Boolean,
    onAnchorRect: (LayerRect) -> Unit,
    onOpenUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    phoneState: Boolean = true,
    batteryUnrestricted: Boolean = true,
    onRequestPhone: () -> Unit = {},
    onRequestBattery: () -> Unit = {},
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val r0 = reveal(0)
        Text(
            text = "Allow access",
            style = EmberbyteType.title(OnboardingType.StepTitle).copy(letterSpacing = (-0.035).em),
            color = palette.text,
            modifier = Modifier
                .semantics { heading() }
                .graphicsLayer {
                    alpha = r0
                    translationY = (1f - r0) * 14.dp.toPx()
                },
        )

        val r1 = reveal(1)
        Text(
            text = "Four quick things keep the numbers complete. Skip any and you still see your totals.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = OnboardingType.LeadBody),
            color = palette.muted,
            modifier = Modifier.graphicsLayer {
                alpha = r1
                translationY = (1f - r1) * 14.dp.toPx()
            },
        )

        PermissionCard(
            icon = Icons.Rounded.Visibility,
            title = "Usage access",
            subtitle = "Shows which apps use data.",
            allowed = usageAccess,
            allowedLabel = "Usage access allowed",
            buttonLabel = "Open settings",
            smallCorner = Corner.BottomEnd,
            onAction = onOpenUsageAccess,
            buttonTag = "usage_card_button",
            modifier = Modifier
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    onAnchorRect(
                        LayerRect(
                            x = pos.x,
                            y = pos.y,
                            w = coords.size.width.toFloat(),
                            h = coords.size.height.toFloat(),
                        ),
                    )
                }
                .alpha(if (firstCardHiddenByShared) 0f else 1f)
                .testTag("usage_permission_card"),
        )

        val r2 = reveal(2)
        PermissionCard(
            icon = Icons.Rounded.NotificationsActive,
            title = "Notifications",
            subtitle = "Today's use and speed in the shade.",
            allowed = notifications,
            allowedLabel = "Notifications allowed",
            buttonLabel = "Allow notifications",
            smallCorner = Corner.TopStart,
            onAction = onRequestNotifications,
            buttonTag = "notifications_card_button",
            modifier = Modifier
                .graphicsLayer {
                    alpha = r2
                    translationY = (1f - r2) * 14.dp.toPx()
                }
                .testTag("notifications_permission_card"),
        )

        val r3 = reveal(3)
        PermissionCard(
            icon = Icons.Rounded.PhoneAndroid,
            title = "Phone",
            subtitle = "Tells your SIMs apart. Calls and numbers are never read.",
            allowed = phoneState,
            allowedLabel = "Phone access allowed",
            buttonLabel = "Allow phone access",
            smallCorner = Corner.BottomEnd,
            onAction = onRequestPhone,
            buttonTag = "phone_card_button",
            modifier = Modifier
                .graphicsLayer {
                    alpha = r3
                    translationY = (1f - r3) * 14.dp.toPx()
                }
                .testTag("phone_permission_card"),
        )

        val r4 = reveal(4)
        PermissionCard(
            icon = Icons.Rounded.BatteryChargingFull,
            title = "Battery",
            subtitle = "Do not let the system stop measuring in the background.",
            allowed = batteryUnrestricted,
            allowedLabel = "Battery limits removed",
            buttonLabel = "Stop battery limits",
            smallCorner = Corner.TopStart,
            onAction = onRequestBattery,
            buttonTag = "battery_card_button",
            modifier = Modifier
                .graphicsLayer {
                    alpha = r4
                    translationY = (1f - r4) * 14.dp.toPx()
                }
                .testTag("battery_permission_card"),
        )
    }
}

@Composable
internal fun PermissionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    allowed: Boolean,
    allowedLabel: String,
    buttonLabel: String,
    smallCorner: Corner,
    onAction: () -> Unit,
    buttonTag: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val desc = if (allowed) "$title, allowed" else "$title, not allowed, $buttonLabel"
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = desc },
        shape = mixedCorners(large = 32.dp, small = 12.dp, smallCorner = smallCorner),
        color = palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(palette.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = EmberbyteType.heading(OnboardingType.CardTitle),
                        color = palette.text,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.CardBody),
                        color = palette.muted,
                    )
                }
            }

            if (allowed) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(palette.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = palette.background,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = allowedLabel,
                        style = EmberbyteType.heading(OnboardingType.Action),
                        color = palette.text,
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = onAction,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = palette.surface,
                        contentColor = palette.text,
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag(buttonTag),
                ) {
                    Text(
                        text = buttonLabel,
                        style = EmberbyteType.heading(OnboardingType.Action),
                    )
                }
            }
        }
    }
}
