package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.common.ScreenHeader
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * Header section for History screen (Mockup 06-history option B).
 *
 * Contains:
 * - Screen title ("History") and Back navigation button
 * - Period selector as ConnectedButtonGroup (Day / Week / Month)
 * - Period total as HeroNumber with accessible one-node description
 * - Comparison against previous period ("12% more than last week") with an icon, never colour alone
 */
@Composable
fun HistoryHeader(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val granularityOptions = listOf("Day", "Week", "Month")
    val selectedGranularityIndex = when (state.granularity) {
        Granularity.DAY -> 0
        Granularity.WEEK -> 1
        Granularity.MONTH -> 2
        else -> 0
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader("History", onBack)

        ConnectedButtonGroup(
            options = granularityOptions,
            selectedIndex = selectedGranularityIndex,
            onSelect = { idx ->
                val newGranularity = when (idx) {
                    0 -> Granularity.DAY
                    1 -> Granularity.WEEK
                    2 -> Granularity.MONTH
                    else -> Granularity.DAY
                }
                onEvent(HistoryEvent.SetGranularity(newGranularity))
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // Period total as HeroNumber (or selected day/week/month when a bar is selected)
        val selectedPoint = state.selectedIndex?.let { state.points.getOrNull(it) }
        val displayBytes = selectedPoint?.totalBytes ?: state.totalBytes
        val formatted = formatBytes(displayBytes, state.units)

        val spokenTotal = io.github.khaledbahaaeldin.emberbyte.common.spokenBytes(displayBytes, state.units)
        val totalDescription = when (state.granularity) {
            Granularity.WEEK -> "$spokenTotal this week"
            Granularity.MONTH -> "$spokenTotal this month"
            else -> "$spokenTotal total"
        }

        HeroNumber(
            value = formatted.value,
            unit = formatted.unit,
            contentDescription = state.heroDescription ?: totalDescription,
            modifier = Modifier.fillMaxWidth(),
        )

        // Comparison against previous period
        state.comparisonText?.let { compText ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.semantics(mergeDescendants = true) {},
            ) {
                val icon = when {
                    compText.contains("more than", ignoreCase = true) -> Icons.AutoMirrored.Rounded.TrendingUp
                    compText.contains("less than", ignoreCase = true) -> Icons.AutoMirrored.Rounded.TrendingDown
                    else -> Icons.AutoMirrored.Rounded.TrendingFlat
                }
                val iconDesc = when {
                    compText.contains("more than", ignoreCase = true) -> "Increased usage"
                    compText.contains("less than", ignoreCase = true) -> "Decreased usage"
                    else -> "Steady usage"
                }
                Icon(
                    imageVector = icon,
                    contentDescription = iconDesc,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp),
                )
                ForceLtr {
                    Text(
                        text = compText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
            }
        }
    }
}
