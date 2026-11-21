package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion

private data class PeriodOption(val range: AppsRange, val label: String, val spoken: String)

private val PERIODS = listOf(
    PeriodOption(AppsRange.TODAY, "Today", "Today"),
    PeriodOption(AppsRange.LAST_7_DAYS, "7 days", "Last 7 days"),
    PeriodOption(AppsRange.LAST_30_DAYS, "30 days", "Last 30 days"),
    PeriodOption(AppsRange.CYCLE, "Cycle", "This cycle"),
)

/**
 * Four connected buttons directly under the search row: Today, 7 days, 30 days and Cycle. The selected one is
 * primary-filled and 1.25 times wider; choosing one sets the period at once.
 */
@Composable
fun PeriodButtons(
    selected: AppsRange,
    onSelect: (AppsRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = listOf(AppsShapes.PeriodStart, AppsShapes.PeriodMiddle, AppsShapes.PeriodMiddle, AppsShapes.PeriodEnd)
    // The Cycle button also stands for the older "this month" range.
    val current = if (selected == AppsRange.THIS_MONTH) AppsRange.CYCLE else selected
    Row(modifier = modifier.fillMaxWidth().testTag("period_buttons"), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        PERIODS.forEachIndexed { index, option ->
            PeriodButton(
                option = option,
                selected = option.range == current,
                shape = shapes[index],
                onClick = { onSelect(option.range) },
            )
        }
    }
}

@Composable
private fun RowScope.PeriodButton(option: PeriodOption, selected: Boolean, shape: Shape, onClick: () -> Unit) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val spec = if (reduce) snap() else tween<androidx.compose.ui.graphics.Color>(300)
    val container by animateColorAsState(if (selected) palette.primary else palette.text.copy(alpha = 0.09f), spec, label = "periodContainer")
    val content by animateColorAsState(if (selected) palette.onPrimary else palette.text, spec, label = "periodContent")
    Row(
        modifier = Modifier
            .weight(if (selected) 1.25f else 1f)
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp)
            .semantics {
                this.selected = selected
                contentDescription = option.spoken
            }
            .testTag("period_${option.range.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = option.label,
            style = EmberbyteType.grotesk(AppsType.Period, 700),
            color = content,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
