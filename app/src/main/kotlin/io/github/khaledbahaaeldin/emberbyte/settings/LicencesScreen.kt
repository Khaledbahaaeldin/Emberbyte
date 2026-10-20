package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.common.ScreenHeader
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ForceLtr
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.mixedCorners
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.EmptyState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

@Composable
fun LicencesScreen(
    viewModel: LicencesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LicencesContent(
        items = items,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LicencesContent(
    items: List<LicenceItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Group items: "Built with", "Fonts and designs", or other
    val builtWith = remember(items) { items.filter { it.category == "Built with" } }
    val fontsAndDesigns = remember(items) { items.filter { it.category == "Fonts and designs" } }
    val others = remember(items) { items.filter { it.category != "Built with" && it.category != "Fonts and designs" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(title = "Open-source licences", onBack = onBack)
        }

        // Search field
        item {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("LicencesSearchField"),
                placeholder = {
                    Text("Search licences…", color = palette.muted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = palette.muted,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear search",
                                tint = palette.muted,
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(percent = 50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = palette.surface2,
                    unfocusedContainerColor = palette.surface2,
                    focusedTextColor = palette.text,
                    unfocusedTextColor = palette.text,
                    cursorColor = palette.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            )
        }

        // Emberbyte lead card (visible only when search is empty or matches "Emberbyte" / "GPL")
        if (searchQuery.isBlank() || "emberbyte".contains(searchQuery.trim(), ignoreCase = true) || "gpl".contains(searchQuery.trim(), ignoreCase = true)) {
            item {
                Surface(
                    shape = mixedCorners(large = 32.dp, small = 10.dp, smallCorner = Corner.BottomEnd),
                    color = palette.primary,
                    contentColor = palette.onPrimary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Emberbyte is free software",
                            style = EmberbyteType.heading(18.sp),
                            color = palette.onPrimary,
                        )
                        Text(
                            text = "GPL-3.0-or-later. You may use, study, change and share it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.onPrimary.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Search,
                    title = "No licences found",
                    body = "Try searching with a different keyword or name.",
                )
            }
        } else {
            if (builtWith.isNotEmpty()) {
                item {
                    Text(
                        text = "Built with",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = palette.muted,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 2.dp)
                            .semantics { heading() },
                    )
                }
                items(builtWith, key = { "built-${it.name}" }) { item ->
                    LicenceEntryRow(item = item, context = context)
                }
            }

            if (fontsAndDesigns.isNotEmpty()) {
                item {
                    Text(
                        text = "Fonts and designs",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = palette.muted,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 2.dp)
                            .semantics { heading() },
                    )
                }
                items(fontsAndDesigns, key = { "font-${it.name}" }) { item ->
                    LicenceEntryRow(item = item, context = context)
                }
            }

            if (others.isNotEmpty()) {
                item {
                    Text(
                        text = "Other components",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = palette.muted,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 2.dp)
                            .semantics { heading() },
                    )
                }
                items(others, key = { "other-${it.name}" }) { item ->
                    LicenceEntryRow(item = item, context = context)
                }
            }
        }
    }
}

@Composable
private fun LicenceEntryRow(
    item: LicenceItem,
    context: Context,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    var expanded by remember { mutableStateOf(false) }

    // Colour pill by licence
    val (pillBg, pillFg) = when (item.licence) {
        "Apache-2.0" -> palette.accent to palette.onAccent
        "MIT" -> palette.warn to palette.onWarn
        "OFL-1.1" -> palette.primary to palette.onPrimary
        "BSD-3-Clause" -> palette.surface2 to palette.text
        else -> palette.surface2 to palette.text
    }

    // Accessibility: each entry is one semantic node with name and licence
    val semanticDescription = buildString {
        append(item.name)
        if (item.version.isNotBlank()) append(" version ${item.version}")
        append(", ${item.licence} licence")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = palette.surface,
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize()
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = item.name,
                        style = EmberbyteType.heading(15.sp),
                        color = palette.text,
                    )
                    if (item.version.isNotBlank()) {
                        Text(
                            text = "v${item.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                // Licence pill
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = pillBg,
                    contentColor = pillFg,
                ) {
                    Text(
                        text = item.licence,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = pillFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            // Expanded view showing URL and full text
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (item.url.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("URL", item.url))
                                    Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            ForceLtr {
                                Text(
                                    text = item.url,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = palette.primary,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                            }
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }

                    if (item.fullLicenceText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.surface2,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            SelectionContainer {
                                ForceLtr {
                                    Text(
                                        text = item.fullLicenceText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                        ),
                                        color = palette.text,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
