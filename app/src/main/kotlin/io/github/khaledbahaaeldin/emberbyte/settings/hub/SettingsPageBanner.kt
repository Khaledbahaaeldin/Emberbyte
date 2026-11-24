@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.rememberAmbientSeconds
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.SettingsType
import kotlin.math.PI
import kotlin.math.sin

@Composable
internal fun SettingsPageBanner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    amoled: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val seconds = rememberAmbientSeconds(!reduceMotion)

    val baseSurface = if (amoled) palette.surface else palette.surface2
    val topColor = lerp(baseSurface, tint, if (amoled) 0.18f else 0.30f)
    val bottomColor = lerp(baseSurface, tint, if (amoled) 0.08f else 0.14f)
    val shape = mixedCorners(large = 38.dp, small = 12.dp, smallCorner = Corner.BottomStart)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(178.dp),
        shape = shape,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // Concentric ripple rings behind shaped icon
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(116.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val c = tint.copy(alpha = 0.22f)
                    drawCircle(color = c, radius = size.minDimension * 0.48f, style = Stroke(2.dp.toPx()))
                    drawCircle(color = c, radius = size.minDimension * 0.36f, style = Stroke(2.dp.toPx()))
                }
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            val bobDp = if (reduceMotion) 0f else sin((seconds.value / 6.0) * 2.0 * PI).toFloat() * 5f
                            translationY = bobDp.dp.toPx()
                        }
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(tint.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.text,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(0.72f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.surface.copy(alpha = 0.55f))
                        .testTag("banner_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.text,
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = title,
                    style = EmberbyteType.title(SettingsType.BannerTitle),
                    color = palette.text,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.PulseBody),
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
internal fun SettingsUniformGroup(
    amoled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (amoled) palette.surface else palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            content = content,
        )
    }
}

@Composable
internal fun SettingsUniformRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val palette = LocalEmberbytePalette.current
    val bg = if (highlighted) palette.primary.copy(alpha = 0.14f) else Color.Transparent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 62.dp)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(palette.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = EmberbyteType.heading(SettingsType.RowTitle),
                color = palette.text,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = SettingsType.RowSubtitle),
                    color = palette.muted,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}
