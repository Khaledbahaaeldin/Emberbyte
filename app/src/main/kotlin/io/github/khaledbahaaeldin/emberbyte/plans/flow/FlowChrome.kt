package io.github.khaledbahaaeldin.emberbyte.plans.flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.DialogAction
import io.github.khaledbahaaeldin.emberbyte.ui.design.dialog.EmberbyteDialog
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun PlanFlowProgressPill(
    position: Float,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    val effectivePos = if (reduceMotion) position.roundToInt().toFloat() else position
    val stepNum = (position.roundToInt() + 1).coerceIn(1, totalSteps.coerceAtLeast(1))
    val stepDesc = stringResource(R.string.plan_flow_step_desc, stepNum, totalSteps)

    Row(
        modifier = modifier
            .semantics { contentDescription = stepDesc }
            .testTag("plan_flow_progress_pill"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val width = flowPillWidthDp(index, effectivePos).dp
            val activeFraction = (1f - abs(effectivePos - index)).coerceIn(0f, 1f)
            val color = lerp(palette.surface2, palette.primary, activeFraction)
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
internal fun PlanFlowActionBar(
    step: Int,
    isLastStep: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val primaryLabel = if (isLastStep) {
        stringResource(R.string.plan_flow_create)
    } else {
        stringResource(R.string.plan_flow_continue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step > 1) {
            FilledTonalButton(
                onClick = onBack,
                enabled = !isSaving,
                shape = mixedCorners(large = 32.dp, small = 12.dp, smallCorner = Corner.BottomEnd),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = palette.surface2,
                    contentColor = palette.text,
                ),
                modifier = Modifier
                    .height(56.dp)
                    .defaultMinSize(minWidth = 88.dp)
                    .testTag("plan_flow_back_button"),
            ) {
                Text(
                    text = stringResource(R.string.plan_flow_back),
                    style = EmberbyteType.heading(PlanFlowType.Button),
                )
            }
        }

        Button(
            onClick = onPrimary,
            enabled = !isSaving,
            shape = mixedCorners(large = 32.dp, small = 12.dp, smallCorner = Corner.BottomStart),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = palette.background,
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("plan_flow_primary_button"),
        ) {
            if (isLastStep) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = primaryLabel,
                style = EmberbyteType.heading(PlanFlowType.Button),
            )
        }
    }
}

@Composable
internal fun PlanFlowScaffoldChrome(
    position: Float,
    totalSteps: Int,
    step: Int,
    isLastStep: Boolean,
    isSaving: Boolean,
    hasDirtyInput: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    val requestClose = {
        if (hasDirtyInput) {
            showDiscardDialog = true
        } else {
            onClose()
        }
    }

    BackHandler(enabled = true) {
        if (step > 1) {
            onBack()
        } else {
            requestClose()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = requestClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("plan_flow_close_button"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.plan_flow_close_desc),
                    tint = palette.text,
                )
            }
            PlanFlowProgressPill(
                position = position,
                totalSteps = totalSteps,
            )
            Spacer(Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = content,
        )

        PlanFlowActionBar(
            step = step,
            isLastStep = isLastStep,
            isSaving = isSaving,
            onBack = onBack,
            onPrimary = onPrimary,
        )
    }

    if (showDiscardDialog) {
        EmberbyteDialog(
            onDismiss = { showDiscardDialog = false },
            title = stringResource(R.string.plan_flow_discard_plan_title),
            icon = null,
            destructive = true,
            confirm = DialogAction(
                label = stringResource(R.string.plan_flow_discard_confirm),
                onClick = {
                    showDiscardDialog = false
                    onClose()
                },
            ),
            dismiss = DialogAction(
                label = stringResource(R.string.plan_flow_discard_keep),
                onClick = { showDiscardDialog = false },
            ),
        ) {
            Text(
                text = stringResource(R.string.plan_flow_discard_plan_body),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
        }
    }
}
