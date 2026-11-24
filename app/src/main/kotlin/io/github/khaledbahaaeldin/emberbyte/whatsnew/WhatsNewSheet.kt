@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.whatsnew

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteAnimatedVisibility
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.ShapedBox
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.toShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

internal const val WHATS_NEW_CHANGELOG_URL =
    "https://github.com/Khaledbahaaeldin/Emberbyte/blob/main/CHANGELOG.md"

@Composable
fun WhatsNewSheet(
    entry: WhatsNewEntry,
    olderEntries: List<WhatsNewEntry>,
    onDismiss: () -> Unit,
    onOpenChangelog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = palette.surface,
        contentColor = palette.text,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted.copy(alpha = 0.5f)) },
        modifier = modifier,
    ) {
        WhatsNewSheetContent(
            entry = entry,
            olderEntries = olderEntries,
            onDismiss = onDismiss,
            onOpenChangelog = onOpenChangelog,
        )
    }
}

@Composable
internal fun WhatsNewSheetContent(
    entry: WhatsNewEntry,
    olderEntries: List<WhatsNewEntry>,
    onDismiss: () -> Unit,
    onOpenChangelog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val locale: Locale = LocalConfiguration.current.locales[0] ?: Locale.ENGLISH
    val formattedDate = remember(entry.date, locale) {
        DateTimeFormatter.ofPattern("d MMM yyyy", locale).format(entry.date)
    }
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        yield()
        runCatching { titleFocusRequester.requestFocus() }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShapedBox(
                    shape = AppShape.Cookie.toShape(),
                    color = palette.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(48.dp),
                ) {
                    BrandMark(size = 28.dp)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.whatsnew_title, entry.versionName),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = palette.text,
                        modifier = Modifier
                            .focusRequester(titleFocusRequester)
                            .semantics { heading() },
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = palette.surface2,
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.muted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.whatsnew_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )

            // Kind cards in order: Added, Changed, Fixed
            val orderedKinds = listOf(Kind.Added, Kind.Changed, Kind.Fixed)
            val grouped = orderedKinds.mapNotNull { kind ->
                val items = entry.highlights.filter { it.kind == kind }
                if (items.isEmpty()) null else kind to items
            }

            grouped.forEachIndexed { index, (kind, items) ->
                WhatsNewKindCard(
                    kind = kind,
                    items = items,
                    staggerIndex = index,
                )
            }

            // Footer buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onOpenChangelog,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Text(stringResource(R.string.whatsnew_see_all_changes))
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.sizeIn(minWidth = 96.dp, minHeight = 48.dp),
                ) {
                    Text(stringResource(R.string.whatsnew_got_it))
                }
            }

            // Earlier versions
            if (olderEntries.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.whatsnew_earlier_versions),
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.muted,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { heading() },
                )
                olderEntries.forEach { older ->
                    OlderVersionCard(entry = older, locale = locale)
                }
            }
        }
    }
}

@Composable
private fun WhatsNewKindCard(
    kind: Kind,
    items: List<Highlight>,
    staggerIndex: Int,
) {
    val palette = LocalEmberbytePalette.current
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current

    val title = when (kind) {
        Kind.Added -> stringResource(R.string.whatsnew_kind_added)
        Kind.Changed -> stringResource(R.string.whatsnew_kind_changed)
        Kind.Fixed -> stringResource(R.string.whatsnew_kind_fixed)
    }
    val icon: ImageVector = when (kind) {
        Kind.Added -> Icons.Rounded.AutoAwesome
        Kind.Changed -> Icons.Rounded.Tune
        Kind.Fixed -> Icons.Rounded.Build
    }
    val tileShape: AppShape = when (kind) {
        Kind.Added -> AppShape.Cookie
        Kind.Changed -> AppShape.Burst
        Kind.Fixed -> AppShape.Sunny
    }
    val summaryDescription = stringResource(R.string.whatsnew_kind_summary, title, items.size)

    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            progress.snapTo(1f)
        } else {
            delay(staggerIndex * 40L)
            progress.animateTo(1f, motion.spatial())
        }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surface2,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("whatsnew_card_${kind.name.lowercase()}")
            .graphicsLayer {
                val p = progress.value.coerceIn(0f, 1f)
                alpha = p
                translationY = (1f - p) * 16.dp.toPx()
            },
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = summaryDescription
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShapedBox(
                    shape = tileShape.toShape(),
                    color = palette.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                    modifier = Modifier.semantics { heading() },
                )
            }

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.primary,
                    )
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.text,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OlderVersionCard(
    entry: WhatsNewEntry,
    locale: Locale,
) {
    val palette = LocalEmberbytePalette.current
    var expanded by rememberSaveable(entry.versionCode) { mutableStateOf(false) }
    val dateText = remember(entry.date, locale) {
        DateTimeFormatter.ofPattern("d MMM yyyy", locale).format(entry.date)
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surface2,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.versionName,
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.text,
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.muted,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = palette.muted,
                )
            }
            EmberbyteAnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    entry.highlights.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {},
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.primary,
                            )
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.text,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
