package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

@Composable
fun PlanTile(
    model: PlanCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val summary = buildString {
        if (model.isDormant) {
            append("${model.name}, ${model.startsAtText ?: "Starts next cycle"}")
        } else {
            append("${model.name}, ${model.percentText.removeSuffix("%")} percent used, ${model.remainingText}, ${model.daysLeftText}")
            when (model.ringState) {
                BudgetStateUi.Warning -> append(", nearly used up")
                BudgetStateUi.Exceeded -> append(", over the cap")
                BudgetStateUi.Ok -> Unit
            }
        }
        if (model.pinned) {
            append(", pinned")
        }
        if (model.isEstimated) {
            append(", estimated")
        }
        if (model.archived) {
            append(", archived")
        }
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = summary
            },
        shape = MaterialTheme.shapes.large,
        color = if (model.isDormant) palette.surface else palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (model.isDormant) palette.muted else palette.text,
                )
                if (!model.isDormant) {
                    BudgetRing(model.fraction, model.ringState, percentText = model.percentText)
                }
            }
            if (model.isDormant) {
                Text(
                    text = model.startsAtText ?: "Starts next cycle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            } else {
                ForceLtr {
                    Text(
                        text = model.remainingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.text,
                    )
                }
            }
        }
    }
}
