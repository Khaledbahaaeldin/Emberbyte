package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.OnboardingType
import kotlin.math.roundToInt

@Composable
internal fun SharedOnboardingLayer(
    position: Float,
    rects: List<LayerRect>,
    usageAccess: Boolean,
    modifier: Modifier = Modifier,
) {
    if (rects.size != 4) return
    val state = layerState(position, rects)
    if (!state.visible) return

    val palette = LocalEmberbytePalette.current
    val density = LocalDensity.current
    val fillColor: Color = lerp(palette.surface2, palette.primary, state.primaryMix)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearAndSetSemantics {}
            .testTag("onboarding_shared_layer"),
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(state.rect.x.roundToInt(), state.rect.y.roundToInt()) }
                .size(
                    width = with(density) { state.rect.w.toDp() },
                    height = with(density) { state.rect.h.toDp() },
                )
                .drawBehind {
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height,
                                topLeftCornerRadius = CornerRadius(state.radiiX[0], state.radiiY[0]),
                                topRightCornerRadius = CornerRadius(state.radiiX[1], state.radiiY[1]),
                                bottomRightCornerRadius = CornerRadius(state.radiiX[2], state.radiiY[2]),
                                bottomLeftCornerRadius = CornerRadius(state.radiiX[3], state.radiiY[3]),
                            ),
                        )
                    }
                    drawPath(path, color = fillColor)
                },
        ) {
            if (state.alphaMark > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = state.alphaMark },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "E",
                        style = EmberbyteType.title(OnboardingType.MarkGlyph),
                        color = palette.background,
                    )
                }
            }
            if (state.alphaCard > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = state.alphaCard },
                ) {
                    TodayExampleCard()
                }
            }
            if (state.alphaDisc > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = state.alphaDisc },
                    contentAlignment = Alignment.Center,
                ) {
                    PlanCapDisc()
                }
            }
            if (state.alphaPermission > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = state.alphaPermission },
                ) {
                    PermissionCard(
                        icon = Icons.Rounded.Visibility,
                        title = "Usage access",
                        subtitle = "Shows which apps use data.",
                        allowed = usageAccess,
                        allowedLabel = "Usage access allowed",
                        buttonLabel = "Open settings",
                        smallCorner = Corner.BottomEnd,
                        onAction = {},
                        buttonTag = "shared_usage_preview",
                    )
                }
            }
        }
    }
}
