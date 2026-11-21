package io.github.khaledbahaaeldin.emberbyte.ui.design.number

import org.junit.Assert.assertEquals
import org.junit.Test

class HeroBloomLogicTest {
    @Test
    fun badge_spin_speed_follows_the_formula() {
        assertEquals(8f, badgeSpinDegPerSec(null), 0.001f)
        assertEquals(8f, badgeSpinDegPerSec(0f), 0.001f)
        assertEquals(63.2f, badgeSpinDegPerSec(1.2f), 0.001f)
        assertEquals(238f, badgeSpinDegPerSec(5f), 0.001f)
        assertEquals(238f, badgeSpinDegPerSec(9f), 0.001f)
        assertEquals(8f, badgeSpinDegPerSec(-3f), 0.001f)
    }

    @Test
    fun changed_digit_indices_marks_only_changed_digits() {
        assertEquals(listOf(3), changedDigitIndices("3.71", "3.72"))
        assertEquals(listOf(2, 3), changedDigitIndices("3.79", "3.80"))
        assertEquals(listOf(0, 1, 2, 3, 4), changedDigitIndices("9.99", "10.00"))
        assertEquals(emptyList<Int>(), changedDigitIndices("3.71", "3.71"))
    }

    @Test
    fun bloom_spec_staggers_digits_70ms_after_150ms() {
        val spec = HeroBloomSpec()
        assertEquals(150, spec.startAtMs(0))
        assertEquals(220, spec.startAtMs(1))
        assertEquals(360, spec.startAtMs(3))
        assertEquals(100f, spec.startWght, 0f)
        assertEquals(800f, spec.endWght, 0f)
        assertEquals(0.35f, spec.dampingRatio, 0f)
        assertEquals(750f, spec.stiffness, 0f)
    }

    @Test
    fun weight_is_clamped_to_the_font_range() {
        assertEquals(100f, clampHeroWeight(-40f), 0f)
        assertEquals(1000f, clampHeroWeight(1240f), 0f)
        assertEquals(800f, clampHeroWeight(800f), 0f)
    }
}
