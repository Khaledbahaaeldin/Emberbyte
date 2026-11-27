package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
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

@Composable
fun PlanTile(
    model: PlanCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    onAnimatedSweepChanged: ((Float) -> Unit)? = null,
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
        if (model.enteredNote != null) {
            append(", ${model.enteredNote}")
        }
        if (model.pinned) {
            append(", pinned")
        }
        if (model.isEstimated) {
            append(", estimated")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (model.isDormant) palette.muted else palette.text,
                )
                if (model.pinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint = palette.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            if (model.isDormant) {
                Text(
                    text = model.startsAtText ?: "Starts next cycle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BudgetRing(
                        fraction = model.fraction,
                        state = model.ringState,
                        percentText = model.percentText,
                        delayMillis = delayMillis,
                        onAnimatedSweepChanged = onAnimatedSweepChanged,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        ForceLtr {
                            Text(
                                text = model.remainingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.text,
                            )
                        }
                        ForceLtr {
                            Text(
                                text = model.daysLeftText,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.muted,
                            )
                        }
                    }
                }
            }
            model.enteredNote?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                )
            }
        }
    }
}
