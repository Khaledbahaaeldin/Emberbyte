package io.github.khaledbahaaeldin.emberbyte.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
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
internal fun SeeYourDataStep(
    reveal: (Int) -> Float,
    layerHiddenByShared: Boolean,
    onAnchorRect: (LayerRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TodayExampleCard(
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
                .alpha(if (layerHiddenByShared) 0f else 1f)
                .testTag("see_data_example_card"),
        )

        val r0 = reveal(0)
        Text(
            text = "See your data",
            style = EmberbyteType.title(OnboardingType.StepTitle).copy(letterSpacing = (-0.01).em),
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
            text = "Big, clear numbers for today's mobile and Wi-Fi traffic, live speed, and every app that uses your connection.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = OnboardingType.LeadBody),
            color = palette.muted,
            modifier = Modifier.graphicsLayer {
                alpha = r1
                translationY = (1f - r1) * 14.dp.toPx()
            },
        )

        val r2 = reveal(2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = r2
                    translationY = (1f - r2) * 14.dp.toPx()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricTile(
                label = "Live speed",
                value = "2.4 MB/s",
                smallCorner = Corner.BottomEnd,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                label = "Apps today",
                value = "23",
                smallCorner = Corner.BottomStart,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun TodayExampleCard(modifier: Modifier = Modifier) {
    val palette = LocalEmberbytePalette.current
    val wifiColor = lerp(palette.surface, palette.primary, 0.38f)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Example: 1.4 gigabytes today" },
        shape = mixedCorners(large = 36.dp, small = 12.dp, smallCorner = Corner.BottomEnd),
        color = palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Today",
                    style = EmberbyteType.heading(OnboardingType.Action),
                    color = palette.muted,
                )
                Surface(
                    shape = CircleShape,
                    color = palette.surface,
                ) {
                    Text(
                        text = "Example",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = OnboardingType.Tag),
                        color = palette.muted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "1.4",
                    style = EmberbyteType.hero(OnboardingType.TodayFigure).copy(
                        letterSpacing = (-0.055).em,
                        fontFeatureSettings = "tnum",
                    ),
                    color = palette.text,
                )
                Text(
                    text = "GB",
                    style = EmberbyteType.heading(OnboardingType.TodayUnit),
                    color = palette.muted,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(920f)
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(palette.primary),
                )
                Box(
                    modifier = Modifier
                        .weight(480f)
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(wifiColor),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LegendItem("Mobile 920 MB", palette.primary)
                LegendItem("Wi-Fi 480 MB", wifiColor)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: androidx.compose.ui.graphics.Color) {
    val palette = LocalEmberbytePalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.Caption),
            color = palette.muted,
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    smallCorner: Corner,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier.height(92.dp),
        shape = mixedCorners(large = 28.dp, small = 10.dp, smallCorner = smallCorner),
        color = palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = OnboardingType.Caption),
                color = palette.muted,
            )
            Text(
                text = value,
                style = EmberbyteType.heading(OnboardingType.CardTitle),
                color = palette.text,
            )
        }
    }
}
