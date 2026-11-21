package io.github.khaledbahaaeldin.emberbyte.ui.design.number

/** How the Home hero number enters. Other screens keep [None]. */
enum class HeroEntrance { None, WeightBloom }

/** Roboto Flex `wght` range the hero may use; the overshoot of the entrance spring is clamped to it. */
internal const val HERO_WEIGHT_MIN = 100f
internal const val HERO_WEIGHT_MAX = 1000f

internal fun clampHeroWeight(wght: Float): Float = wght.coerceIn(HERO_WEIGHT_MIN, HERO_WEIGHT_MAX)

/**
 * Whether the entrance may replay every time Home is shown. The maintainer decided on 2026-11-20 that it plays once
 * per app start only.
 */
internal const val HERO_ENTRANCE_REPLAYS = false

/** Weight kick, in `wght` units per second, given to a digit whose character just changed. */
internal const val HERO_PULSE_VELOCITY = 1500f

/** The "weight bloom, digit by digit" entrance numbers (maintainer-approved mockup values). */
internal data class HeroBloomSpec(
    val startWght: Float = 100f,
    val endWght: Float = 800f,
    val staggerMs: Int = 70,
    val delayMs: Int = 150,
    val dampingRatio: Float = 0.35f,
    val stiffness: Float = 750f,
    val startScale: Float = 0.93f,
) {
    fun startAtMs(index: Int): Int = delayMs + staggerMs * index
}

/** Degrees per second the unit badge turns: 8 + 46 x speed, speed clamped to 0..5 MB/s; unknown speed idles at 8. */
fun badgeSpinDegPerSec(speedMBps: Float?): Float {
    val speed = (speedMBps ?: 0f).coerceIn(0f, 5f)
    return 8f + 46f * speed
}

/** Indices of the characters that differ; a change of length marks every index of the new text. */
internal fun changedDigitIndices(old: String, new: String): List<Int> {
    if (old.length != new.length) return new.indices.toList()
    return new.indices.filter { old[it] != new[it] }
}
