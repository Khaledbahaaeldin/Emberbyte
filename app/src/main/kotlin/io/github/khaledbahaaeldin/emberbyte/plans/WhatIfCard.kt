package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import kotlin.math.roundToInt

@Composable
internal fun WhatIfCard(
    capGb: Int,
    extraGb: Int,
    nowText: String,
    withExtraText: String,
    unitSystem: UnitSystem,
    onChangeExtraGb: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val maxExtra = (capGb.coerceAtLeast(1) * 4).coerceAtLeast(4)
    var sliderGb by remember(extraGb) { mutableIntStateOf(extraGb.coerceIn(0, maxExtra)) }
    val unitSuffix = if (unitSystem == UnitSystem.BINARY) "GiB" else "GB"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = mixedCorners(large = 28.dp, small = 10.dp, smallCorner = Corner.BottomEnd),
        color = palette.primary.copy(alpha = 0.14f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "What if",
                style = EmberbyteType.heading(PlanFlowType.Micro).copy(letterSpacing = 0.08.em),
                color = palette.primary,
            )
            Text(
                text = "+$sliderGb $unitSuffix",
                style = EmberbyteType.heading(PlanFlowType.CardTitle),
                color = palette.text,
            )
            Slider(
                value = sliderGb.toFloat(),
                onValueChange = { sliderGb = it.roundToInt().coerceIn(0, maxExtra) },
                onValueChangeFinished = { onChangeExtraGb(sliderGb) },
                valueRange = 0f..maxExtra.toFloat(),
                steps = (maxExtra - 1).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = nowText,
                    style = EmberbyteType.heading(PlanFlowType.Caption),
                    color = palette.muted,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = withExtraText,
                    style = EmberbyteType.heading(PlanFlowType.Caption),
                    color = palette.text,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
