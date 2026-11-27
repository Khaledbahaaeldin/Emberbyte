package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppsShapes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.AppsType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette

/**
 * The search field and the Filters button as one connected pair, 6 dp apart and both 56 dp tall. Focus outlines the field
 * and rounds all its corners; the Filters button shows a badge with the number of non-default filters.
 */
@Composable
internal fun AppsSearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilterCount: Int,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalEmberbytePalette.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val fieldShape = if (focused) AppsShapes.SearchFieldFocused else AppsShapes.SearchField

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search apps", style = EmberbyteType.grotesk(AppsType.Search, 500), color = palette.muted) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.muted) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = palette.muted)
                    }
                }
            },
            singleLine = true,
            interactionSource = interaction,
            textStyle = EmberbyteType.grotesk(AppsType.Search, 500),
            shape = fieldShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = palette.text.copy(alpha = 0.09f),
                unfocusedContainerColor = palette.text.copy(alpha = 0.09f),
                focusedTextColor = palette.text,
                unfocusedTextColor = palette.text,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = palette.primary,
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .then(if (focused) Modifier.border(2.5.dp, palette.primary, fieldShape) else Modifier)
                .testTag("apps_search_field"),
        )

        Box(
            modifier = Modifier
                .width(64.dp)
                .height(56.dp)
                .clip(AppsShapes.FiltersButton)
                .background(palette.primary)
                .clickable(role = Role.Button, onClick = onOpenFilters)
                .semantics {
                    contentDescription = if (activeFilterCount > 0) "Filters, $activeFilterCount active" else "Filters"
                }
                .testTag("apps_filters_button"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Tune, contentDescription = null, tint = palette.onPrimary)
            if (activeFilterCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .clip(CircleShape)
                        .background(palette.onPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = activeFilterCount.toString(),
                        style = EmberbyteType.grotesk(AppsType.Badge, 800),
                        color = palette.primary,
                        modifier = Modifier.testTag("apps_filters_badge"),
                    )
                }
            }
        }
    }
}
