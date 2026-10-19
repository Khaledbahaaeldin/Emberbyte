@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark

internal const val ONBOARDING_PAGE_COUNT = 4

@Composable
private fun adaptiveTitleSize(baseSp: Float): TextUnit {
    val fontScale = LocalDensity.current.fontScale
    val scaled = if (fontScale > 1.3f) baseSp * (1.35f / fontScale) else baseSp
    return scaled.sp
}

/**
 * Page 1: Welcome page with [BrandMark] and the privacy promise.
 */
@Composable
internal fun WelcomeOnboardingPage(
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val headSize = adaptiveTitleSize(40f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            ShapedBox(
                shape = AppShape.Cookie.toShape(),
                color = palette.surface2,
                modifier = Modifier.size(168.dp),
            )
            BrandMark(
                size = 96.dp,
            )
            ShapedBox(
                shape = AppShape.Burst.toShape(),
                color = palette.accent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-24).dp, y = 8.dp)
                    .size(48.dp),
            )
            ShapedBox(
                shape = AppShape.Sunny.toShape(),
                color = palette.warn,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 24.dp, y = (-8).dp)
                    .size(38.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrandMark(size = 28.dp)
            Text(
                text = "Emberbyte",
                style = EmberbyteType.heading(16.sp),
                color = palette.primary,
            )
        }

        Text(
            text = "Your data stays on your phone.",
            style = EmberbyteType.title(headSize),
            lineHeight = headSize * 1.05f,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "Nothing ever leaves your phone: no account, no cloud. Just your mobile and Wi-Fi numbers measured right here.",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
        )
    }
}

/**
 * Page 2: "See your data" with a sample [HeroNumber].
 */
@Composable
internal fun SeeYourDataOnboardingPage(
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val headSize = adaptiveTitleSize(38f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "STEP 1 OF 3",
            style = EmberbyteType.badge(12.sp),
            color = palette.muted,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = mixedCorners(large = 32.dp, small = 12.dp, smallCorner = Corner.BottomEnd),
            color = palette.surface,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Today",
                    style = EmberbyteType.heading(14.sp),
                    color = palette.muted,
                )
                HeroNumber(
                    value = "1.4",
                    unit = "GB",
                    contentDescription = "Sample usage 1.4 gigabytes today",
                    size = 76.sp,
                )
                Text(
                    text = "Mobile 920 MB · Wi-Fi 480 MB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.text,
                )
            }
        }

        Text(
            text = "See your data",
            style = EmberbyteType.title(headSize),
            lineHeight = headSize * 1.05f,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "Big, clear numbers for today's mobile and Wi-Fi traffic, live speed, and every app that uses your connection.",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
        )
    }
}

/**
 * Page 3: "Stay under your cap" with a sample [WavyRing].
 */
@Composable
internal fun StayUnderYourCapOnboardingPage(
    hasPlan: Boolean,
    onSetUpPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val headSize = adaptiveTitleSize(38f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "STEP 2 OF 3",
            style = EmberbyteType.badge(12.sp),
            color = palette.muted,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = mixedCorners(large = 32.dp, small = 12.dp, smallCorner = Corner.TopEnd),
            color = palette.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WavyRing(
                    fraction = 0.62f,
                    state = BudgetStateUi.Ok,
                    size = 88.dp,
                    label = "62%",
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Your data plan",
                        style = EmberbyteType.heading(16.sp),
                        color = palette.text,
                    )
                    Text(
                        text = "12.4 GB left · 18 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                    Spacer(Modifier.height(4.dp))
                    if (hasPlan) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = palette.primary,
                            )
                            Text(
                                text = "Plan added",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.text,
                            )
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onSetUpPlan,
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = palette.primary,
                                contentColor = palette.onPrimary,
                            ),
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Set up a plan")
                        }
                    }
                }
            }
        }

        Text(
            text = "Stay under your cap",
            style = EmberbyteType.title(headSize),
            lineHeight = headSize * 1.05f,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "Add your cap and renewal day to see what is left and when it runs out.",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
        )
    }
}

/**
 * Page 4: "Allow usage access" with the existing permission action and an explanation.
 */
@Composable
internal fun AllowUsageAccessOnboardingPage(
    state: OnboardingUiState,
    onOpenUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val headSize = adaptiveTitleSize(38f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "STEP 3 OF 3",
            style = EmberbyteType.badge(12.sp),
            color = palette.muted,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            ShapedBox(
                shape = AppShape.Sunny.toShape(),
                color = palette.surface2,
                modifier = Modifier.size(104.dp),
            )
            ShapedBox(
                shape = AppShape.Cookie.toShape(),
                color = palette.primary,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = palette.onPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp),
                )
            }
        }

        Text(
            text = "Allow usage access",
            style = EmberbyteType.title(headSize),
            lineHeight = headSize * 1.05f,
            color = palette.primary,
            modifier = Modifier.semantics { heading() },
        )

        Text(
            text = "Allow usage access to get per-app numbers. Skip it and you still see your totals.",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
        )

        PermissionRowCard(
            shape = AppShape.Cookie,
            icon = Icons.Rounded.Visibility,
            title = "Usage access",
            body = "Shows which apps use data.",
            granted = state.usageAccess,
            grantedText = "Usage access allowed",
            actionLabel = "Open settings",
            onAction = onOpenUsageAccess,
        )

        PermissionRowCard(
            shape = AppShape.Sunny,
            icon = Icons.Rounded.Notifications,
            title = "Notifications",
            body = "Today's use and speed in the shade.",
            granted = state.notifications,
            grantedText = "Notifications allowed",
            actionLabel = "Allow notifications",
            onAction = onRequestNotifications,
        )
    }
}

@Composable
private fun PermissionRowCard(
    shape: AppShape,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    granted: Boolean,
    grantedText: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = mixedCorners(large = 26.dp, small = 10.dp, smallCorner = Corner.BottomStart),
        color = palette.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShapedBox(
                    shape = shape.toShape(),
                    color = palette.primary,
                    modifier = Modifier
                        .size(44.dp)
                        .clearAndSetSemantics { },
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.onPrimary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = EmberbyteType.heading(15.sp),
                        color = palette.text,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                    )
                }
            }
            if (granted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = palette.primary,
                    )
                    Text(
                        text = grantedText,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.text,
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = onAction,
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = palette.primary,
                        contentColor = palette.onPrimary,
                    ),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
