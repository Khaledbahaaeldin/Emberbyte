package io.github.khaledbahaaeldin.emberbyte.ui.design.plan

import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.GeneratedPalettes
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The 1.0.2 audit found "Won't run out this cycle" invisible on plan cards in light themes: the accent, a pale
 * container colour, was used as text on surface2 (1.02:1 in Dusk and Ember light).
 */
class PlanCardStatusColorTest {
    private val allPalettes = GeneratedPalettes.flatMap { (id, pair) ->
        listOf("$id dark" to pair.first, "$id light" to pair.second)
    }

    @Test
    fun plan_card_status_text_is_at_least_4_5_to_1_in_every_theme_and_mode() {
        val failures = mutableListOf<String>()
        for ((name, palette) in allPalettes) {
            for (exceeded in listOf(false, true)) {
                val colour = planStatusTextColor(palette, exceeded)
                for ((surfaceName, surface) in listOf("surface2" to palette.surface2, "surface" to palette.surface)) {
                    val ratio = contrastRatio(colour, surface)
                    if (ratio < 4.5) failures += "$name exceeded=$exceeded on $surfaceName: %.2f".format(ratio)
                }
            }
        }
        assertTrue("status text too faint:\n${failures.joinToString("\n")}", failures.isEmpty())
    }
}
