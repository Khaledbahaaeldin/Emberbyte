package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
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
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.EstimatedBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanCard(model: PlanCardUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
        if (model.enteredNote != null) {
            append(", ${model.enteredNote}")
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
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = summary },
        shape = MaterialTheme.shapes.extraLarge,
        color = if (model.isDormant) palette.surface else palette.surface2,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (model.isDormant) {
                DormantBadge()
            } else {
                BudgetRing(model.fraction, model.ringState, percentText = model.percentText)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(model.name, style = MaterialTheme.typography.titleMedium, color = if (model.isDormant) palette.muted else palette.text)
                    if (model.pinned) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                model.simLabel?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = palette.muted) }
                if (!model.isDormant) {
                    ForceLtr { Text(model.usedText, style = MaterialTheme.typography.bodyMedium, color = palette.text) }
                    ForceLtr {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(model.remainingText, style = MaterialTheme.typography.bodyMedium, color = palette.text)
                            Text("·", style = MaterialTheme.typography.bodyMedium, color = palette.muted)
                            Text(model.daysLeftText, style = MaterialTheme.typography.bodyMedium, color = palette.text)
                        }
                    }
                    model.forecastText?.let {
                        ForceLtr {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = palette.accent)
                        }
                    }
                } else {
                    Text(
                        text = model.startsAtText ?: "Starts next cycle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                model.enteredNote?.let { note ->
                    Text(note, style = MaterialTheme.typography.labelSmall, color = palette.muted)
                }
                if (model.isEstimated) EstimatedBadge()
                if (model.archived) Text("Archived", style = MaterialTheme.typography.labelMedium, color = palette.muted)
                if (model.chips.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        model.chips.forEach { AssistChip(onClick = {}, label = { Text(it, maxLines = 1) }) }
                    }
                }
            }
        }
    }
}
