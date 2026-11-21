package io.github.khaledbahaaeldin.emberbyte.ui.design.usage

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.spokenUnit
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.pressMorph
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.UsageType
import java.time.format.TextStyle
import java.util.Locale

/** Test-only readouts of one strip bar (index 0..6). */
internal val StripBarFractionKey = SemanticsPropertyKey<Float>("StripBarFraction")

/** Bars start growing this long after the card appears, and each next one 60 ms later. */
internal const val STRIP_ENTRANCE_START_MS = 420
internal const val STRIP_ENTRANCE_STEP_MS = 60

/**
 * The week strip and day picker: seven buttons for the last seven days (oldest first). Each shows its value above a
 * bar and the weekday initial in a disc below. The selected bar is solid primary and its disc is filled; today's disc
 * has a ring.
 */
@Composable
fun UsageWeekStrip(
    days: List<DayUsage>,
    selectedIndex: Int,
    units: ByteUnits,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    playEntrance: Boolean = false,
    locale: Locale = Locale.getDefault(),
) {
    val fractions = remember(days) { stripBarFractions(days.map { it.totalBytes }) }
    ForceLtr {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .testTag("usage_strip"),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEachIndexed { index, day ->
                DayButton(
                    day = day,
                    fraction = fractions[index],
                    selected = index == selectedIndex,
                    isToday = index == days.lastIndex,
                    units = units,
                    onClick = { onSelect(index) },
                    entranceDelayMs = STRIP_ENTRANCE_START_MS + STRIP_ENTRANCE_STEP_MS * index,
                    playEntrance = playEntrance,
                    locale = locale,
                    modifier = Modifier.weight(1f).testTag("usage_day_$index"),
                )
            }
        }
    }
}

@Composable
private fun DayButton(
    day: DayUsage,
    fraction: Float,
    selected: Boolean,
    isToday: Boolean,
    units: ByteUnits,
    onClick: () -> Unit,
    entranceDelayMs: Int,
    playEntrance: Boolean,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val reduce = LocalReduceMotion.current
    val motion = rememberEmberbyteMotion()
    val barShape = RoundedCornerShape(13.dp)
    val interaction = remember { MutableInteractionSource() }

    // Bars grow from 6 percent on first show (once), and glide to a new height on a spatial spring.
    val shown = remember { Animatable(if (playEntrance && !reduce) 0.06f else fraction) }
    var entered by remember { mutableStateOf(!playEntrance || reduce) }
    LaunchedEffect(fraction, reduce) {
        if (reduce) {
            shown.snapTo(fraction)
        } else {
            if (!entered) {
                kotlinx.coroutines.delay(entranceDelayMs.toLong())
                entered = true
            }
            shown.animateTo(fraction, motion.spatial())
        }
    }
    val colourSpec = if (reduce) snap() else tween<androidx.compose.ui.graphics.Color>(300)
    val barColour by animateColorAsState(
        if (selected) palette.primary else palette.primary.copy(alpha = 0.28f), colourSpec, label = "stripBar",
    )
    val discColour by animateColorAsState(
        if (selected) palette.primary else androidx.compose.ui.graphics.Color.Transparent, colourSpec, label = "stripDisc",
    )

    val weekday = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val amount = io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes(day.totalBytes, units)
    val description = "$weekday, ${amount.value} ${spokenUnit(amount.unit)}"

    Column(
        modifier = modifier
            .pressMorph(RoundedCornerShape(16.dp), pressedScale = 0.97f, pressedCorner = 12.dp, interactionSource = interaction)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                this.selected = selected
                set(StripBarFractionKey, shown.value)
            }
            .heightIn(min = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stripLabel(day.totalBytes, units),
            style = EmberbyteType.grotesk(if (selected) UsageType.StripValueSelected else UsageType.StripValue, if (selected) 700 else 500),
            color = palette.text,
            maxLines = 1,
            modifier = Modifier.clearAndSetSemantics { },
        )
        Box(Modifier.height(74.dp).widthIn(max = 34.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(shown.value.coerceIn(0.06f, 1.2f).coerceAtMost(1f))
                    .clip(barShape)
                    .background(barColour),
            )
        }
        Box(
            Modifier
                .size(26.dp)
                .then(if (isToday) Modifier.border(2.dp, palette.primary, CircleShape) else Modifier)
                .clip(CircleShape)
                .background(discColour),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = weekday.take(1).uppercase(locale),
                style = EmberbyteType.grotesk(UsageType.DiscLetter, 700),
                color = if (selected) palette.onPrimary else palette.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}
