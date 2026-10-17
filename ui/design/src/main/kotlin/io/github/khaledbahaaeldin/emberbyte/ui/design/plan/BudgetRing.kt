package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetRing(
    fraction: Float,
    state: BudgetStateUi,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    percentText: String? = null,
) {
    val safe = if (fraction.isNaN() || fraction < 0f) 0f else fraction
    val percent = (safe * 100f).toInt()
    val displayText = percentText ?: "$percent%"
    val descNumber = percentText?.removeSuffix("%") ?: percent.toString()
    val description = buildString {
        append("$descNumber percent of the plan used")
        when (state) {
            BudgetStateUi.Warning -> append(", nearly used up")
            BudgetStateUi.Exceeded -> append(", over the cap")
            BudgetStateUi.Ok -> Unit
        }
    }
    WavyRing(
        fraction = safe,
        state = state,
        size = size,
        label = displayText,
        labelSize = MaterialTheme.typography.labelLarge.fontSize,
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        },
    )
}
