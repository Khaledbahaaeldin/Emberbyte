package io.github.khaledbahaaeldin.emberbyte.apps.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.derivedStateOf
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.revealOnce
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailEvent
import io.github.khaledbahaaeldin.emberbyte.apps.AppDetailUiState
import io.github.khaledbahaaeldin.emberbyte.apps.BudgetEditorDialog
import io.github.khaledbahaaeldin.emberbyte.common.spokenBytes
import io.github.khaledbahaaeldin.emberbyte.home.emberbyteAnimateItem
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.MorphingNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.plan.BudgetRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.DetailType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.warnText
import java.time.ZoneId
import kotlin.math.roundToInt

/** The redesigned App detail screen: statistics that each answer one question, in sections. */
@Composable
fun AppDetailContent(
    state: AppDetailUiState,
    onEvent: (AppDetailEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null,
    icon: ImageBitmap? = null,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    EmberbyteCrossfade(targetState = state.isLoading, modifier = modifier.fillMaxSize()) { loading ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingState(label = "Loading app details") }
        } else {
            Box(Modifier.fillMaxSize()) {
                DetailBody(state, onEvent, onBack, onOpenSettings, icon, zone)
                if (state.showBudgetRemovedUndo) {
                    val palette = LocalEmberbytePalette.current
                    androidx.compose.material3.Surface(
                        color = palette.surface2,
                        contentColor = palette.text,
                        shape = MaterialTheme.shapes.large,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(io.github.khaledbahaaeldin.emberbyte.R.string.dialog_budget_removed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.text,
                            )
                            TextButton(
                                onClick = { onEvent(AppDetailEvent.UndoRemoveBudget) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(io.github.khaledbahaaeldin.emberbyte.R.string.dialog_undo),
                                    color = palette.linkText,
                                )
                            }
                        }
                    }
                }
            }
            if (state.editor != null) {
                BudgetEditorDialog(editor = state.editor, planOptions = state.planOptions, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun DetailBody(
    state: AppDetailUiState,
    onEvent: (AppDetailEvent) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: (() -> Unit)?,
    icon: ImageBitmap?,
    zone: ZoneId,
) {
    val palette = LocalEmberbytePalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val motion = rememberEmberbyteMotion()
    val fontScale = LocalDensity.current.fontScale
    val days = state.range.days
    val average = dailyAverage(state.totalBytes, days)
    val busiest = busiestDay(state.dailySeries, zone)
    val change = changeLabel(changeFraction(state.totalBytes, state.previousTotalBytes), days)
    val wifiShare = if (state.totalBytes > 0L) state.wifiBytes.toFloat() / state.totalBytes else 0f
    val window = busiestWindow(state.hourTotals)
    val daysWithData = state.dailySeries.count { it.totalBytes > 0L }
    val sentence = summarySentence(
        label = state.label.ifEmpty { state.packageName },
        rank = state.rank,
        shareOfAll = state.shareOfAll,
        wifiShare = wifiShare,
        busiestHour = (window.startHour + 1).coerceAtMost(23),
        daysOfData = daysWithData,
    )

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val density = LocalDensity.current
    val seen = remember { mutableSetOf<String>() }
    // How far the header has collapsed: 0 at rest, 1 once the big header is mostly scrolled away (starts after 40 dp, full at 90 dp).
    val collapse = remember {
        {
            if (listState.firstVisibleItemIndex > 0) 1f
            else ((with(density) { listState.firstVisibleItemScrollOffset.toDp().value } - 40f) / 50f).coerceIn(0f, 1f)
        }
    }
    val showMini by remember { androidx.compose.runtime.derivedStateOf { collapse() > 0.02f } }
    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "header") { DetailHeader(state, icon, onBack, emberbyteAnimateItem(motion), collapseProgress = collapse) }

        item(key = "range") {
            DetailRangeButtons(state.range, { onEvent(AppDetailEvent.SetRange(it)) }, emberbyteAnimateItem(motion))
        }

        item(key = "total") {
            val formatted = formatBytes(state.totalBytes, state.units)
            Column(emberbyteAnimateItem(motion), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Used in the last $days days", style = EmberbyteType.grotesk(DetailType.Label, 600), color = palette.muted)
                ForceLtr {
                    MorphingNumber(
                        value = formatted.value,
                        unit = formatted.unit,
                        style = EmberbyteType.grotesk(DetailType.Total, 700),
                        unitStyle = EmberbyteType.grotesk(DetailType.TotalUnit, 600),
                        color = palette.primary,
                        contentDescription = "${spokenBytes(state.totalBytes, state.units)} used in the last $days days",
                        modifier = Modifier.testTag("detail_total"),
                    )
                }
                val (tint, textColour) = when (change.direction) {
                    ChangeDirection.Up -> palette.warn.copy(alpha = 0.24f) to palette.warnText
                    ChangeDirection.Down -> palette.accent.copy(alpha = 0.34f) to palette.text
                    else -> palette.text.copy(alpha = 0.09f) to palette.text
                }
                Text(
                    text = change.text,
                    style = EmberbyteType.grotesk(DetailType.Change, 700),
                    color = textColour,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(tint)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("detail_change"),
                )
            }
        }

        if (sentence != null) {
            item(key = "sentence") {
                Text(
                    sentence,
                    style = EmberbyteType.grotesk(DetailType.Sentence, 500),
                    color = palette.text,
                    modifier = emberbyteAnimateItem(motion).testTag("detail_sentence"),
                )
            }
        }

        item(key = "chart") {
            if (state.dailySeries.isEmpty()) {
                io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.HistoryToggleOff,
                    title = "No daily usage",
                    body = "Daily breakdown will show here once usage is recorded.",
                    modifier = emberbyteAnimateItem(motion).testTag("detail_chart_empty"),
                )
            } else {
                DetailChart(state, zone, emberbyteAnimateItem(motion).revealOnce("chart", seen, 0))
            }
        }

        item(key = "tiles") {
            val avg = formatBytes(average, state.units)
            val pace = formatBytes(pace30(state.totalBytes, days), state.units)
            val previousAverage = dailyAverage(state.previousTotalBytes, days)
            val avgSub = when {
                state.previousTotalBytes <= 0L -> "Steady compared with before"
                previousAverage > average + average / 33 -> {
                    val prev = formatBytes(previousAverage, state.units)
                    "Down from ${prev.value} ${prev.unit} a day"
                }
                previousAverage < average - average / 33 -> {
                    val prev = formatBytes(previousAverage, state.units)
                    "Up from ${prev.value} ${prev.unit} a day"
                }
                else -> "Steady compared with before"
            }
            val busiestAmount = busiest?.let { formatBytes(it.totalBytes, state.units) }
            val busiestDate = busiest?.start?.atZone(zone)
            val wide = fontScale >= 1.5f
            val tiles: List<@Composable (Modifier) -> Unit> = listOf(
                { m -> StatTile("Daily average", "${avg.value} ${avg.unit}", avgSub, Corner.TopStart, m, tag = "detail_tile_average") },
                { m ->
                    StatTile(
                        "Busiest day",
                        busiestDate?.let { "${it.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale)} ${it.dayOfMonth}" } ?: "None",
                        busiestAmount?.let { "${it.value} ${it.unit}" + if (busiestDate != null && isWeekend(busiestDate.dayOfWeek)) ", a weekend" else "" } ?: "No usage yet",
                        Corner.TopEnd, m, tag = "detail_tile_busiest",
                    )
                },
                { m ->
                    StatTile(
                        "Share of all apps",
                        if (state.rank > 0) "${(state.shareOfAll * 100).roundToInt()}%" else "—",
                        if (state.rank > 0) "Rank #${state.rank} of ${state.appCount} apps" else "Not ranked",
                        Corner.BottomStart, m, container = palette.primary.copy(alpha = 0.16f), tag = "detail_tile_share",
                        trailing = {
                            WavyRing(fraction = state.shareOfAll, state = BudgetStateUi.Ok, size = 54.dp, label = "", modifier = Modifier.testTag("detail_share_ring"))
                        },
                    )
                },
                { m -> StatTile("At this pace", "${pace.value} ${pace.unit}", "in 30 days, if this ${if (days == 7) "week" else "period"} is typical", Corner.BottomEnd, m, tag = "detail_tile_pace") },
            )
            Column(emberbyteAnimateItem(motion).revealOnce("tiles", seen, 1), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (wide) {
                    tiles.forEach { it(Modifier.fillMaxWidth()) }
                } else {
                    tiles.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { it(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        item(key = "hours") { HourPattern(state.hourTotals, state.units, emberbyteAnimateItem(motion).revealOnce("hours", seen, 0)) }

        item(key = "network") { NetworkImpactCard(state, emberbyteAnimateItem(motion).revealOnce("network", seen, 0)) }

        item(key = "budget") { BudgetSection(state, average, onEvent, emberbyteAnimateItem(motion).revealOnce("budget", seen, 0)) }

        if (state.spikes.isNotEmpty()) {
            item(key = "spikes") {
                DetailCard(modifier = emberbyteAnimateItem(motion).testTag("detail_spikes"), corner = Corner.TopStart, container = palette.warn.copy(alpha = 0.16f)) {
                    Text("Spikes", style = EmberbyteType.grotesk(DetailType.CardTitle, 700), color = palette.text)
                    state.spikes.forEach { spike ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                ForceLtr { Text(spike.whenText, style = MaterialTheme.typography.bodyMedium, color = palette.text) }
                                ForceLtr { Text("${spike.bytesText} · ${spike.multipleText}", style = MaterialTheme.typography.bodySmall, color = palette.muted) }
                            }
                            TextButton(onClick = { onEvent(AppDetailEvent.DismissSpike(spike.id)) }, modifier = Modifier.heightIn(min = 48.dp)) {
                                Text("Dismiss", color = palette.linkText)
                            }
                        }
                    }
                }
            }
        }

        item(key = "actions") {
            Row(emberbyteAnimateItem(motion).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                val showSettings = !state.packageName.startsWith("uid:") && onOpenSettings != null
                Button(
                    onClick = { onEvent(AppDetailEvent.OpenBudgetEditor) },
                    shape = if (showSettings) AppsShapes.PeriodStart else AppsShapes.PeriodStart,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("detail_budget_button"),
                ) {
                    Text(if (state.budget != null) "Edit budget" else "Set a budget", style = EmberbyteType.grotesk(DetailType.Button, 700))
                }
                if (showSettings) {
                    FilledTonalButton(
                        onClick = { onOpenSettings?.invoke() },
                        shape = AppsShapes.PeriodEnd,
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("detail_settings_button"),
                    ) {
                        Text("App settings", style = EmberbyteType.grotesk(DetailType.Button, 700))
                    }
                }
            }
        }
    }
    if (showMini) {
        MiniBar(state, icon, onBack, collapse)
    }
    }
}

/** The collapsed header: back button, a 30 dp icon and the app name, fading in as the big header scrolls away. */
@Composable
private fun MiniBar(
    state: AppDetailUiState,
    icon: ImageBitmap?,
    onBack: () -> Unit,
    progress: () -> Float,
) {
    val palette = LocalEmberbytePalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = progress() }
            .background(palette.background)
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(horizontal = 12.dp)
            .testTag("detail_mini_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        androidx.compose.material3.IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(palette.primary.copy(alpha = 0.16f))
                .semantics { contentDescription = "Back" },
        ) {
            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = palette.text)
        }
        io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.AppIcon(
            label = state.label.ifEmpty { state.packageName },
            icon = icon,
            shape = io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor(state.packageName),
            size = 30.dp,
        )
        Text(
            state.label.ifEmpty { state.packageName },
            style = EmberbyteType.grotesk(DetailType.Label, 700),
            color = palette.text,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BudgetSection(
    state: AppDetailUiState,
    average: Long,
    onEvent: (AppDetailEvent) -> Unit,
    modifier: Modifier,
) {
    val palette = LocalEmberbytePalette.current
    DetailCard(modifier = modifier.testTag("detail_budget_card"), corner = Corner.BottomEnd) {
        Text("Data budget", style = EmberbyteType.grotesk(DetailType.CardTitle, 700), color = palette.text)
        val budget = state.budget
        if (budget == null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BudgetRing(fraction = 0f, state = BudgetStateUi.Ok, percentText = "—")
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("No limit yet", style = EmberbyteType.grotesk(DetailType.Label, 700), color = palette.text)
                    val suggestion = suggestedLimit(average)
                    if (suggestion > 0L) {
                        val avg = formatBytes(average, state.units)
                        val limit = formatBytes(suggestion, state.units)
                        Text(
                            "At ${avg.value} ${avg.unit} a day, a limit of ${limit.value} ${limit.unit} would catch the heavy days without nagging.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                            modifier = Modifier.testTag("detail_budget_suggestion"),
                        )
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BudgetRing(fraction = budget.fraction, state = budget.ringState, percentText = budget.percentText)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ForceLtr { Text(budget.usedText, style = EmberbyteType.grotesk(DetailType.Label, 700), color = palette.text) }
                    ForceLtr { Text(budget.periodText, style = MaterialTheme.typography.bodySmall, color = palette.muted) }
                    state.budgetProjection?.let { projection ->
                        val delta = formatBytes(projection.bytesDelta, state.units)
                        val text = if (projection.willPass) {
                            "At today's pace you will pass the limit around ${hourLabel(projection.passAtHour ?: 0)}."
                        } else {
                            "At today's pace you will stay under the limit by about ${delta.value} ${delta.unit}."
                        }
                        Text(text, style = MaterialTheme.typography.bodySmall, color = palette.text, modifier = Modifier.testTag("detail_budget_projection"))
                    }
                    Row {
                        TextButton(onClick = { onEvent(AppDetailEvent.OpenBudgetEditor) }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("Edit", color = palette.linkText)
                        }
                        TextButton(onClick = { onEvent(AppDetailEvent.RemoveBudget) }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("Remove", color = palette.danger)
                        }
                    }
                }
            }
        }
    }
}
