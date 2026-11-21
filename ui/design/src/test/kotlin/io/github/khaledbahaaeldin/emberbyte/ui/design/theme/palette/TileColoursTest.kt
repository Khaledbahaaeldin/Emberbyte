package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileColoursTest {
    private fun modes(): List<Triple<String, String, EmberbytePalette>> = buildList {
        for ((id, pair) in GeneratedPalettes) {
            add(Triple(id, "dark", pair.first))
            add(Triple(id, "light", pair.second))
            add(Triple(id, "amoled", pair.first.copy(background = Color.Black, surface = Color.Black)))
        }
    }

    @Test
    fun tile_containers_meet_contrast_in_every_palette_and_mode() {
        val failures = mutableListOf<String>()
        for ((id, mode, palette) in modes()) {
            for ((name, tile) in listOf("live" to palette.liveTile(), "network" to palette.networkTile(), "upload" to palette.uploadTile())) {
                val label = contrastRatio(tile.onContainer, tile.container)
                val secondary = contrastRatio(tile.onContainerSecondary, tile.container)
                if (label < 7.0) failures += "$id/$mode $name label %.2f".format(label)
                if (secondary < 4.5) failures += "$id/$mode $name secondary %.2f".format(secondary)
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun upload_tint_differs_from_live_tint() {
        for ((id, mode, palette) in modes()) {
            assertNotEquals("$id/$mode", palette.liveTile().container, palette.uploadTile().container)
            assertNotEquals("$id/$mode", palette.liveTile().container, palette.networkTile().container)
        }
    }
}
