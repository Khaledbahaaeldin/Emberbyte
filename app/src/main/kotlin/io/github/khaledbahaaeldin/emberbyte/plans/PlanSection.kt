package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.PlanFlowType

@Composable
internal fun EditorSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalEmberbytePalette.current
    val reduceMotion = LocalReduceMotion.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (reduceMotion) Modifier else Modifier.animateContentSize()),
        shape = mixedCorners(large = 28.dp, small = 10.dp, smallCorner = Corner.BottomEnd),
        color = palette.surface2,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = EmberbyteType.heading(PlanFlowType.Micro).copy(letterSpacing = 0.08.em),
                color = palette.muted,
                modifier = Modifier.semantics { heading() },
            )
            content()
        }
    }
}

@Composable
internal fun PlanSection(
    name: String,
    nameError: String?,
    subscriptionId: Int?,
    sims: List<SimOptionUi>,
    needsPhonePermission: Boolean,
    onSetName: (String) -> Unit,
    onSetSim: (Int?) -> Unit,
    onRequestPhonePermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorSectionCard(title = stringResource(R.string.plan_editor_plan), modifier = modifier) {
        OutlinedTextField(
            value = name,
            onValueChange = onSetName,
            label = { Text(stringResource(R.string.plan_flow_name_hint)) },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .semantics { if (nameError != null) error(nameError) }
                .testTag("editor_name_field"),
        )

        if (sims.size >= 2) {
            val selectedIndex = sims.indexOfFirst { it.subscriptionId == subscriptionId }.coerceAtLeast(0)
            ConnectedButtonGroup(
                options = sims.map { it.label },
                selectedIndex = selectedIndex,
                onSelect = { idx -> onSetSim(sims[idx].subscriptionId) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (needsPhonePermission) {
            AssistChip(
                onClick = onRequestPhonePermission,
                label = { Text(stringResource(R.string.plan_flow_sim_permission_chip)) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            )
        }
    }
}
