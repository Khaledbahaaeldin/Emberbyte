package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.EstimatedBadge

@Composable
internal fun EditorPreviewCard(
    preview: PlanCardUi,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .testTag("editor_preview_card"),
        shape = mixedCorners(large = 30.dp, small = 12.dp, smallCorner = Corner.BottomStart),
        color = palette.surface2,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WavyRing(
                fraction = preview.fraction,
                state = preview.ringState,
                size = 120.dp,
                label = preview.percentText,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (preview.isEstimated) {
                    EstimatedBadge()
                }
                Text(
                    text = preview.usedText,
                    style = EmberbyteType.heading(PlanFlowType.CardTitle),
                    color = palette.text,
                )
                Text(
                    text = preview.startsAtText ?: preview.daysLeftText,
                    style = EmberbyteType.heading(PlanFlowType.Caption),
                    color = palette.primary,
                )
                preview.forecastText?.let { sentence ->
                    Text(
                        text = sentence,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = PlanFlowType.Caption),
                        color = palette.muted,
                    )
                }
            }
        }
    }
}
