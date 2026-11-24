@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.common.plainBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyRing
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun PlansScreen(
    viewModel: PlansViewModel,
    onOpenPlan: (Long) -> Unit,
    onNewPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlansContent(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenPlan = onOpenPlan,
        onNewPlan = onNewPlan,
        modifier = modifier,
    )
}

@Composable
internal fun PlansContent(
    state: PlansUiState,
    onEvent: (PlansEvent) -> Unit,
    onOpenPlan: (Long) -> Unit,
    onNewPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val motion = rememberEmberbyteMotion()
    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0] ?: Locale.ENGLISH
    val singleColumn = configuration.fontScale >= 1.5f

    LaunchedEffect(state.deletedPlanName) {
        if (state.deletedPlanName != null) {
            delay(4_500L)
            onEvent(PlansEvent.DismissDeleteUndo)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.plan_screen_title),
                            style = MaterialTheme.typography.headlineLargeEmphasized.copy(fontWeight = FontWeight.Bold),
                            color = palette.text,
                            modifier = Modifier.semantics { heading() },
                        )
                        Button(
                            onClick = onNewPlan,
                            shape = mixedCorners(large = 26.dp, small = 10.dp, smallCorner = Corner.BottomStart),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.onPrimary,
                            ),
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 52.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.plan_screen_new))
                        }
                    }

                    state.summary?.let { sum ->
                        val remText = plainBytes(sum.totalRemainingBytes, ByteUnits.DECIMAL)
                        val renewalText = sum.nextRenewal?.let {
                            DateTimeFormatter.ofPattern("d MMM", locale).format(it.atZone(java.time.ZoneId.systemDefault()))
                        }
                        val line = buildString {
                            append("$remText left across ${sum.activeCount} ")
                            append(if (sum.activeCount == 1) "plan" else "plans")
                            if (renewalText != null) {
                                append(". Next renewal $renewalText.")
                            } else {
                                append(".")
                            }
                        }
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted,
                        )
                    }
                }
            }

            if (!state.loaded) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingState(label = stringResource(R.string.plan_screen_loading))
                    }
                }
            } else if (state.front == null && state.tiles.isEmpty() && state.upcoming.isEmpty()) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        WavyRing(
                            fraction = 0.7f,
                            state = io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi.Ok,
                            size = 170.dp,
                            label = "?",
                        )
                        Text(
                            text = stringResource(R.string.plan_empty_redesign_title),
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = palette.text,
                        )
                        Text(
                            text = stringResource(R.string.plan_screen_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted,
                        )
                        Button(
                            onClick = onNewPlan,
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Text(stringResource(R.string.plan_screen_empty_cta))
                        }
                    }
                }
            } else {
                state.front?.let { front ->
                    item(key = "front_${front.id}") {
                        FrontPlanCard(
                            model = front,
                            expandedDetails = state.expandedDetails,
                            whatIfExtraGb = state.whatIfExtraGb,
                            whatIfSentence = state.whatIfSentence,
                            onOpenPlan = { onOpenPlan(front.id) },
                            onPinToggle = { onEvent(PlansEvent.SetPinned(front.id, !front.pinned)) },
                            onDelete = { onEvent(PlansEvent.Delete(front.id)) },
                            onToggleDetails = { onEvent(PlansEvent.ToggleDetails) },
                            onSelectWhatIf = { gb -> onEvent(PlansEvent.SetWhatIf(gb)) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }
                }

                if (state.tiles.isNotEmpty()) {
                    if (singleColumn) {
                        items(state.tiles, key = { "tile_${it.id}" }) { tile ->
                            PlanTileCard(
                                model = tile,
                                index = 0,
                                onPinToFront = { onEvent(PlansEvent.SetPinned(tile.id, true)) },
                                onEdit = { onOpenPlan(tile.id) },
                                onDelete = { onEvent(PlansEvent.Delete(tile.id)) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = motion.effects(),
                                    placementSpec = motion.spatial(),
                                    fadeOutSpec = motion.effects(),
                                ),
                            )
                        }
                    } else {
                        val rows = state.tiles.chunked(2)
                        items(rows, key = { pair -> "tilerow_${pair.joinToString("_") { it.id.toString() }}" }) { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = motion.effects(),
                                        placementSpec = motion.spatial(),
                                        fadeOutSpec = motion.effects(),
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    PlanTileCard(
                                        model = pair[0],
                                        index = 0,
                                        onPinToFront = { onEvent(PlansEvent.SetPinned(pair[0].id, true)) },
                                        onEdit = { onOpenPlan(pair[0].id) },
                                        onDelete = { onEvent(PlansEvent.Delete(pair[0].id)) },
                                    )
                                }
                                if (pair.size > 1) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        PlanTileCard(
                                            model = pair[1],
                                            index = 1,
                                            onPinToFront = { onEvent(PlansEvent.SetPinned(pair[1].id, true)) },
                                            onEdit = { onOpenPlan(pair[1].id) },
                                            onDelete = { onEvent(PlansEvent.Delete(pair[1].id)) },
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                items(state.upcoming, key = { "upcoming_${it.id}" }) { upcoming ->
                    UpcomingPlanCard(
                        model = upcoming,
                        onEdit = { onOpenPlan(upcoming.id) },
                        onDelete = { onEvent(PlansEvent.Delete(upcoming.id)) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = motion.effects(),
                            placementSpec = motion.spatial(),
                            fadeOutSpec = motion.effects(),
                        ),
                    )
                }

                state.insight?.let { insight ->
                    item(key = "insight") {
                        PlansInsightCard(
                            insight = insight,
                            onOpenWorstPlan = onOpenPlan,
                        )
                    }
                }

                if (state.renewals.isNotEmpty()) {
                    item(key = "renewals") {
                        RenewalTimelineCard(events = state.renewals)
                    }
                }
            }
        }

        // One-time restored archived plans notice or Delete Undo snackbar
        val restoredMsg = restoredNotice(state.restoredCount)
        val bannerVisible = state.deletedPlanName != null || restoredMsg != null
        EmberbyteAnimatedVisibility(
            visible = bannerVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 96.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = palette.surface2,
                tonalElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.deletedPlanName != null) {
                        Text(
                            text = stringResource(R.string.plan_deleted_snackbar, state.deletedPlanName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.text,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { onEvent(PlansEvent.UndoDelete) },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.dialog_undo),
                                color = palette.linkText,
                            )
                        }
                    } else if (restoredMsg != null) {
                        Text(
                            text = restoredMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.text,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { onEvent(PlansEvent.DismissRestoredNotice) },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.settings_dismiss))
                        }
                    }
                }
            }
        }
    }
}
