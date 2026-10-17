package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

internal data class ContrastPair(
    val name: String,
    val foreground: (EmberbytePalette) -> Color,
    val background: (EmberbytePalette) -> Color,
    val minRatio: Double,
)

internal fun contrastRatio(fg: Color, bg: Color): Double {
    val l1 = fg.compositeOver(bg).luminance() + 0.05
    val l2 = bg.luminance() + 0.05
    return maxOf(l1, l2) / minOf(l1, l2)
}

internal fun markColor(role: Color, onRole: Color, background: Color): Color {
    val rRole = contrastRatio(role, background)
    val rOnRole = contrastRatio(onRole, background)
    return if (rRole >= rOnRole) role else onRole
}

internal object ContrastPairs {
    private val pairs = mutableListOf<ContrastPair>()

    val all: List<ContrastPair>
        get() = synchronized(pairs) { pairs.toList() }

    fun register(pair: ContrastPair) {
        synchronized(pairs) {
            if (!pairs.contains(pair)) {
                pairs.add(pair)
            }
        }
    }

    init {
        register(ContrastPair("hero text on background", { it.text }, { it.background }, 7.0))
        register(ContrastPair("muted on background", { it.muted }, { it.background }, 4.5))
        register(ContrastPair("muted on surface", { it.muted }, { it.surface }, 4.5))
        register(ContrastPair("text on surface", { it.text }, { it.surface }, 4.5))
        register(ContrastPair("text on surface2", { it.text }, { it.surface2 }, 4.5))
        register(ContrastPair("onPrimary / primary", { it.onPrimary }, { it.primary }, 4.5))
        register(ContrastPair("onAccent / accent", { it.onAccent }, { it.accent }, 4.5))
        register(ContrastPair("onWarn / warn", { it.onWarn }, { it.warn }, 4.5))
        register(ContrastPair("onDanger / danger", { it.onDanger }, { it.danger }, 4.5))
        register(ContrastPair("primary / background", { it.primary }, { it.background }, 3.0))
        register(ContrastPair("primary / surface", { it.primary }, { it.surface }, 3.0))
        register(ContrastPair("track / primary", { it.track }, { it.primary }, 2.2))
        register(ContrastPair("track on surface", { it.track }, { it.surface }, 1.3))
        register(ContrastPair("track on background", { it.track }, { it.background }, 1.3))
        register(ContrastPair("accent marks on background", { markColor(it.accent, it.onAccent, it.background) }, { it.background }, 3.0))
        register(ContrastPair("warn marks on background", { markColor(it.warn, it.onWarn, it.background) }, { it.background }, 3.0))
        register(ContrastPair("danger marks on background", { markColor(it.danger, it.onDanger, it.background) }, { it.background }, 3.0))
    }
}
