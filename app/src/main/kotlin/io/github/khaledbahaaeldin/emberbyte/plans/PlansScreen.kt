@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText
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

    LaunchedEffect(state.undoArchivedPlanId) {
        if (state.undoArchivedPlanId != null) {
            delay(5_000L)
            onEvent(PlansEvent.DismissArchiveUndo)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.plan_screen_title),
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                    )
                    FilledTonalButton(
                        onClick = onNewPlan,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Text(stringResource(R.string.plan_screen_new))
                    }
                }
            }
            if (!state.loaded) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingState(label = stringResource(R.string.plan_screen_loading))
                    }
                }
            } else if (state.cards.isEmpty() && state.archivedCards.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        title = stringResource(R.string.plan_screen_empty_title),
                        body = stringResource(R.string.plan_screen_empty_body),
                        action = {
                            Button(
                                onClick = onNewPlan,
                                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                            ) {
                                Text(stringResource(R.string.plan_screen_empty_cta))
                            }
                        },
                    )
                }
            } else {
                if (state.cards.isNotEmpty()) {
                    item(key = "active_plans") {
                        PlansBento(
                            cards = state.cards,
                            onOpenPlan = onOpenPlan,
                            onPinPlan = { id, pinned -> onEvent(PlansEvent.SetPinned(id, pinned)) },
                            onArchivePlan = { id -> onEvent(PlansEvent.ArchivePlan(id)) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.effects(),
                                placementSpec = motion.spatial(),
                                fadeOutSpec = motion.effects(),
                            ),
                        )
                    }
                }

                if (state.archivedCards.isNotEmpty()) {
                    item {
                        TextButton(
                            onClick = { onEvent(PlansEvent.ToggleArchived) },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Text(
                                if (state.showArchived) {
                                    stringResource(R.string.plan_screen_hide_archived)
                                } else {
                                    stringResource(R.string.plan_screen_show_archived, state.archivedCards.size)
                                },
                            )
                        }
                    }
                    if (state.showArchived) {
                        items(state.archivedCards, key = { it.id }) { card ->
                            ArchivedPlanCardWithMenu(
                                model = card,
                                onClick = { onOpenPlan(card.id) },
                                onUnarchive = { onEvent(PlansEvent.UnarchivePlan(card.id)) },
                                onDelete = { onEvent(PlansEvent.ArchivePlan(card.id)) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = motion.effects(),
                                    placementSpec = motion.spatial(),
                                    fadeOutSpec = motion.effects(),
                                ),
                            )
                        }
                    }
                }
            }
        }

        EmberbyteAnimatedVisibility(
            visible = state.undoArchivedPlanId != null,
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
                    Text(
                        text = stringResource(R.string.plan_archived_snackbar),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.text,
                    )
                    TextButton(
                        onClick = { onEvent(PlansEvent.UndoArchive) },
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_undo),
                            color = palette.linkText,
                        )
                    }
                }
            }
        }
    }
}
