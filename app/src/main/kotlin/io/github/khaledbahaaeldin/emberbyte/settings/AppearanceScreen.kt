@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.khaledbahaaeldin.emberbyte.R
import io.github.khaledbahaaeldin.emberbyte.common.EmberbyteScreenScaffold
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.ui.design.controls.ConnectedButtonGroup
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.rememberEmberbyteMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.morphShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.EmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePreset
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.linkText

val ThemeCardPaletteKey = SemanticsPropertyKey<EmberbytePalette>("ThemeCardPalette")
val ThemeGridColumnsKey = SemanticsPropertyKey<Int>("ThemeGridColumns")
val ThemeCardSwatchMorphProgressKey = SemanticsPropertyKey<Float>("ThemeCardSwatchMorphProgress")
val ThemeCardSelectionAlphaKey = SemanticsPropertyKey<Float>("ThemeCardSelectionAlpha")

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val ThemeSwatchMorph = Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided)

@Composable
fun AppearanceScreen(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemDark = isSystemInDarkTheme()
    LaunchedEffect(viewModel, systemDark) {
        snapshotFlow { systemDark }.collect { viewModel.setSystemDark(it) }
    }
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

    EmberbyteScreenScaffold(
        title = stringResource(R.string.settings_appearance_title),
        onBack = onBack,
        modifier = modifier,
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = scaffoldPadding.calculateTopPadding(),
                bottom = scaffoldPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "mode_header") {
                SectionTitle(stringResource(R.string.settings_mode_section))
            }

            item(key = "mode_group") {
                ConnectedButtonGroup(
                    options = listOf(
                        stringResource(R.string.settings_mode_system),
                        stringResource(R.string.settings_mode_dark),
                        stringResource(R.string.settings_mode_light),
                    ),
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

            item(key = "theme_header") {
                SectionTitle(stringResource(R.string.settings_theme_section))
            }

            val chunks = ThemePresets.chunked(columns)
            items(
                count = chunks.size,
                key = { chunkIndex -> "theme_row_${chunks[chunkIndex].joinToString("_") { it.id }}" },
            ) { chunkIndex ->
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
                item(key = "wallpaper_row") {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_wallpaper_title),
                        subtitle = stringResource(R.string.settings_wallpaper_subtitle),
                        checked = uiState.useWallpaperColors,
                        iconVector = Icons.Rounded.Palette,
                        onCheckedChange = { onEvent(AppearanceEvent.SetWallpaper(it)) },
                    )
                }
            }

            item(key = "amoled_row") {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_amoled_title),
                    subtitle = stringResource(R.string.settings_amoled_subtitle),
                    checked = uiState.amoled,
                    enabled = uiState.amoledEnabled,
                    iconVector = Icons.Rounded.DarkMode,
                    onCheckedChange = { onEvent(AppearanceEvent.SetAmoled(it)) },
                )
            }

            item(key = "haptics_row") {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_haptics_title),
                    subtitle = stringResource(R.string.settings_haptics_subtitle),
                    checked = uiState.haptics,
                    iconVector = Icons.Rounded.Tune,
                    onCheckedChange = { onEvent(AppearanceEvent.SetHaptics(it)) },
                )
            }
        }
    }
}

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
    val shape = MaterialTheme.shapes.large
    val motion = rememberEmberbyteMotion()
    val reduceMotion = LocalReduceMotion.current

    val progress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = motion.spatial(),
        label = "theme_swatch_morph",
    )
    val selectionAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = if (reduceMotion) snap() else motion.effects(),
        label = "theme_card_selection_alpha",
    )
    val swatchShape = morphShape(ThemeSwatchMorph, progress)
    val cardDescription = if (isSelected) {
        stringResource(R.string.settings_theme_selected_desc, preset.name)
    } else {
        stringResource(R.string.settings_theme_desc, preset.name)
    }

    Box(
        modifier = modifier
            .height(88.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .background(previewPalette.surface2)
            .then(
                if (selectionAlpha > 0f) {
                    Modifier.border(2.dp, currentPalette.primary.copy(alpha = selectionAlpha), shape)
                } else {
                    Modifier
                },
            )
            .semantics {
                contentDescription = cardDescription
                set(ThemeCardPaletteKey, previewPalette)
                set(ThemeGridColumnsKey, columns)
                set(ThemeCardSwatchMorphProgressKey, progress)
                set(ThemeCardSelectionAlphaKey, selectionAlpha)
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
                .clip(swatchShape)
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
            if (selectionAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            alpha = selectionAlpha
                            scaleX = selectionAlpha
                            scaleY = selectionAlpha
                        }
                        .clip(CircleShape)
                        .background(previewPalette.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = if (isSelected) stringResource(R.string.settings_selected) else null,
                        tint = previewPalette.onPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = preset.name,
                color = previewPalette.text,
                style = MaterialTheme.typography.labelMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    val palette = LocalEmberbytePalette.current
    Text(
        title,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = palette.linkText,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}
