package io.github.khaledbahaaeldin.emberbyte

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.nav.Destination
import io.github.khaledbahaaeldin.emberbyte.ui.design.navbar.CompactNavBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.number.HeroNumber
import io.github.khaledbahaaeldin.emberbyte.ui.design.progress.WavyBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalEmberbytePalette
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BentoTile
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.BrandMark
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.NetworkBar
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.TileContainer

data class PaletteContrastInfo(
    val minText: String,
    val minMarks: String,
    val adjustments: String,
)

val PaletteContrastRegistry: Map<Pair<String, Boolean>, PaletteContrastInfo> = mapOf(
    ("dusk" to true) to PaletteContrastInfo("7.7", "5.4", "none"),
    ("dusk" to false) to PaletteContrastInfo("6.0", "3.9", "none"),
    ("ember" to true) to PaletteContrastInfo("7.5", "5.0", "none"),
    ("ember" to false) to PaletteContrastInfo("5.2", "3.4", "none"),
    ("monochrome" to true) to PaletteContrastInfo("7.0", "7.4", "none"),
    ("monochrome" to false) to PaletteContrastInfo("5.5", "11.2", "none"),
    ("catppuccin" to true) to PaletteContrastInfo("5.6", "3.3", "none"),
    ("catppuccin" to false) to PaletteContrastInfo("5.1", "3.1", "onAccent #FFFFFF -> #000000; onWarn #FFFFFF -> #000000; track #BCC0CC -> #BFC3CE"),
    ("gruvbox" to true) to PaletteContrastInfo("5.3", "3.8", "none"),
    ("gruvbox" to false) to PaletteContrastInfo("4.5", "3.0", "onAccent #FBF1C7 -> #FDF7DF; onWarn #FBF1C7 -> #000000; track #BDAE93 -> #C3B59C"),
    ("nord" to true) to PaletteContrastInfo("5.1", "3.7", "onDanger #FFFFFF -> #000000"),
    ("nord" to false) to PaletteContrastInfo("5.1", "3.0", "onPrimary #FFFFFF -> #000000; onDanger #FFFFFF -> #000000; track #C2CAD9 -> #DBE0E9"),
    ("rosepine" to true) to PaletteContrastInfo("5.1", "5.0", "none"),
    ("rosepine" to false) to PaletteContrastInfo("4.5", "3.0", "text #575279 -> #534E73; muted #797593 -> #716D8C; onPrimary #FFFFFF -> #000000; onAccent #FFFFFF -> #000000; onDanger #FFFFFF -> #000000; track #DFDAD9 -> #E8E5E4"),
    ("solarized" to true) to PaletteContrastInfo("4.5", "3.0", "text #93A1A1 -> #A9B4B4; muted #839496 -> #8B9B9D; onDanger #1F0505 -> #FFFFFF; track #31565F -> #DEEBEE"),
    ("solarized" to false) to PaletteContrastInfo("4.6", "3.0", "muted #586E75 -> #566B72; onPrimary #FFFFFF -> #000000; onAccent #FFFFFF -> #000000; onWarn #FFFFFF -> #000000; track #D3CBB1 -> #EDE9DF"),
    ("dracula" to true) to PaletteContrastInfo("5.8", "4.9", "track #565A6E -> #000000"),
    ("dracula" to false) to PaletteContrastInfo("5.0", "3.9", "none"),
    ("tokyonight" to true) to PaletteContrastInfo("6.3", "3.5", "none"),
    ("tokyonight" to false) to PaletteContrastInfo("4.5", "3.0", "muted #565F89 -> #525B83; onPrimary #FFFFFF -> #000000; onDanger #FFFFFF -> #000000; primary #2E7DE9 -> #2074E8; track #B6BAD0 -> #D2D4E2"),
    ("everforest" to true) to PaletteContrastInfo("4.6", "4.0", "muted #9DA9A0 -> #A0ABA3"),
    ("everforest" to false) to PaletteContrastInfo("4.5", "3.0", "text #5C6A72 -> #4A555B; muted #708089 -> #65737B; muted #65737B -> #606E75; onPrimary #FFFFFF -> #000000; onAccent #FFFFFF -> #000000; onWarn #FFFFFF -> #404040; onDanger #FFFFFF -> #000000; primary #8DA101 -> #849701; primary #849701 -> #809201; track #D3CFB7 -> #F0EFE6"),
    ("kanagawa" to true) to PaletteContrastInfo("5.1", "3.1", "track #54546D -> #F0F0F4"),
    ("kanagawa" to false) to PaletteContrastInfo("4.6", "4.3", "text #545464 -> #4B4B59; muted #716E61 -> #6C695C; muted #6C695C -> #615E53; onAccent #FFFFFF -> #000000"),
)

class ThemeGalleryActivity : ComponentActivity() {
    private var currentTheme by mutableStateOf("dusk")
    private var currentDark by mutableStateOf(true)
    private var currentAmoled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateFromIntent(intent)
        setContent {
            ThemeGalleryContent(
                themeId = currentTheme,
                dark = currentDark,
                amoled = currentAmoled,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateFromIntent(intent)
    }

    private fun updateFromIntent(intent: Intent?) {
        currentTheme = intent?.getStringExtra("theme") ?: "dusk"
        currentDark = intent?.getBooleanExtra("dark", true) ?: true
        currentAmoled = intent?.getBooleanExtra("amoled", false) ?: false
    }
}

@Composable
fun ThemeGalleryContent(
    themeId: String,
    dark: Boolean,
    amoled: Boolean,
    modifier: Modifier = Modifier,
) {
    EmberbyteTheme(
        spec = EmberbyteThemeSpec(
            presetId = themeId,
            darkTheme = dark,
            amoled = amoled,
            wallpaperColors = false,
        )
    ) {
        val palette = LocalEmberbytePalette.current
        val preset = ThemePresets.firstOrNull { it.id == themeId } ?: ThemePresets.first()
        val navItems = remember { Destination.entries.map { it.toNavBarItem() } }
        val contrast = PaletteContrastRegistry[themeId to dark]
            ?: PaletteContrastInfo("4.5", "3.0", "none")

        Surface(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("theme_gallery_root"),
            color = palette.background,
            contentColor = palette.text,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header row: BrandMark + Today + Mode pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BrandMark(size = 32.dp)
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = palette.surface2,
                    ) {
                        Text(
                            text = "${preset.name} · ${if (amoled) "AMOLED" else if (dark) "Dark" else "Light"}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }

                // HeroNumber "1.38 GB"
                HeroNumber(
                    value = "1.38",
                    unit = "GB",
                    contentDescription = "1.38 gigabytes used today, 12.4 gigabytes left",
                    color = palette.primary,
                )

                // The plan line
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "12.4 GB left ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.text,
                    )
                    Text(
                        text = "of 20 GB · renews in 18 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }

                // WavyBar
                WavyBar(
                    fraction = 0.62f,
                    color = palette.primary,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Bento row: Forecast & Live tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BentoTile(
                        title = "Forecast",
                        modifier = Modifier.weight(1f),
                        container = TileContainer.Accent,
                        corner = Corner.TopStart,
                    ) {
                        Text(
                            text = "Safe",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.onAccent,
                        )
                        Text(
                            text = "Lasts to renewal",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.onAccent.copy(alpha = 0.8f),
                        )
                    }

                    BentoTile(
                        title = "Live · Wi-Fi",
                        modifier = Modifier.weight(1f),
                        container = TileContainer.Standard,
                        corner = Corner.TopEnd,
                    ) {
                        Text(
                            text = "↓ 0 B/s",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary,
                        )
                        Text(
                            text = "Current speed",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                    }
                }

                // NetworkBar tile
                BentoTile(
                    title = "Today by network",
                    modifier = Modifier.fillMaxWidth(),
                    container = TileContainer.Standard,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Mobile vs Wi-Fi",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                        Text(
                            text = "1.38 GB",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.text,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    NetworkBar(
                        mobileBytes = 180_000_000L,
                        wifiBytes = 1_200_000_000L,
                        mobileColor = palette.danger,
                        wifiColor = palette.primary,
                    )
                }

                // CompactNavBar
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CompactNavBar(
                        items = navItems,
                        selectedId = "home",
                        onSelect = {},
                        visible = true,
                    )
                }

                // Role swatch strip
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Role Swatches",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.muted,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        val swatches = listOf(
                            palette.background,
                            palette.surface,
                            palette.surface2,
                            palette.primary,
                            palette.accent,
                            palette.warn,
                            palette.danger,
                            palette.track,
                        )
                        for (color in swatches) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color)
                            )
                        }
                    }
                }

                // Registry contrast numbers
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "${if (dark) "Dark" else "Light"}: text ≥ ${contrast.minText}:1 (needs 4.5), marks ≥ ${contrast.minMarks}:1 (needs 3)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.text,
                    )
                    Text(
                        text = if (contrast.adjustments == "none") "No adjustment needed" else "Adjustments: ${contrast.adjustments}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                    )
                }
            }
        }
    }
}
