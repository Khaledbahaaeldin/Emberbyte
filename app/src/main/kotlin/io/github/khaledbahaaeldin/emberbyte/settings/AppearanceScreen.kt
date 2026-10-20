package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.common.ScreenHeader
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ExpressiveSwitch
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePreset
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets

val ThemeCardPaletteKey = SemanticsPropertyKey<EmberbytePalette>("ThemeCardPalette")
val ThemeGridColumnsKey = SemanticsPropertyKey<Int>("ThemeGridColumns")

@Composable
fun AppearanceScreen(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AppearanceContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun AppearanceContent(
    uiState: AppearanceUiState,
    onEvent: (AppearanceEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
) {
    val density = LocalDensity.current
    val columns = if (density.fontScale >= 1.5f) 2 else 3

    val modeIndex = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> 0
        ThemeMode.DARK -> 1
        ThemeMode.LIGHT -> 2
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ScreenHeader(title = "Appearance", onBack = onBack)
        }

        item {
            SectionTitle("Mode")
        }

        item {
            ConnectedButtonGroup(
                options = listOf("System", "Dark", "Light"),
                selectedIndex = modeIndex,
                onSelect = { index ->
                    val newMode = when (index) {
                        0 -> ThemeMode.SYSTEM
                        1 -> ThemeMode.DARK
                        else -> ThemeMode.LIGHT
                    }
                    onEvent(AppearanceEvent.SetMode(newMode))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            SectionTitle("Theme")
        }

        val chunks = ThemePresets.chunked(columns)
        items(chunks.size) { chunkIndex ->
            val rowPresets = chunks[chunkIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (preset in rowPresets) {
                    val previewPalette = if (isDark) preset.dark else preset.light
                    val isSelected = preset.id == uiState.themeId
                    ThemeCard(
                        preset = preset,
                        isSelected = isSelected,
                        previewPalette = previewPalette,
                        columns = columns,
                        onSelect = { onEvent(AppearanceEvent.SetTheme(preset.id)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (uiState.wallpaperSupported) {
            item {
                SwitchRow(
                    title = "Wallpaper colours",
                    subtitle = "Android 12+, replaces the theme",
                    checked = uiState.useWallpaperColors,
                    onChange = { onEvent(AppearanceEvent.SetWallpaper(it)) },
                )
            }
        }

        item {
            SwitchRow(
                title = "Pure black (AMOLED)",
                subtitle = "Dark mode only",
                checked = uiState.amoled,
                enabled = uiState.amoledEnabled,
                onChange = { onEvent(AppearanceEvent.SetAmoled(it)) },
            )
        }

        item {
            SwitchRow(
                title = "Haptics",
                subtitle = "A light tick when the navigation pill snaps",
                checked = uiState.haptics,
                onChange = { onEvent(AppearanceEvent.SetHaptics(it)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeCard(
    preset: ThemePreset,
    isSelected: Boolean,
    previewPalette: EmberbytePalette,
    columns: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentPalette = LocalEmberbytePalette.current
    val shape = RoundedCornerShape(20.dp)
    val cookieShape = MaterialShapes.Cookie9Sided.toShape()

    Box(
        modifier = modifier
            .height(88.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .background(previewPalette.surface2)
            .then(
                if (isSelected) Modifier.border(2.dp, currentPalette.primary, shape)
                else Modifier
            )
            .semantics {
                contentDescription = if (isSelected) "${preset.name} theme, selected" else "${preset.name} theme"
                set(ThemeCardPaletteKey, previewPalette)
                set(ThemeGridColumnsKey, columns)
                selected = isSelected
            }
            .testTag("theme_card_${preset.id}")
            .clickable(onClick = onSelect)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .align(Alignment.TopStart)
                .clip(cookieShape)
                .background(previewPalette.primary),
        )

        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(previewPalette.accent),
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(previewPalette.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = previewPalette.onPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = preset.name,
                color = previewPalette.text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) {}
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                )
            }
        }
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}
