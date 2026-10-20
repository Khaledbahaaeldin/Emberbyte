package io.github.khaledbahaaeldin.emberbyte.sampler

import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.formatBytes
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChipTextTest {

    @Test
    fun ok_shows_today_total() {
        val text = ChipText.forState(BudgetStateUi.Ok, usedToday = "1.4 GB", remaining = "2.6 GB")
        assertEquals("1.4 GB", text)
    }

    @Test
    fun warning_shows_exclamation_and_remaining() {
        val text = ChipText.forState(BudgetStateUi.Warning, usedToday = "3.8 GB", remaining = "1.2 GB")
        assertEquals("!1.2 GB", text)
    }

    @Test
    fun exceeded_shows_over() {
        val text = ChipText.forState(BudgetStateUi.Exceeded, usedToday = "5.2 GB", remaining = "0 B")
        assertEquals("Over", text)
    }

    @Test
    fun every_result_is_at_most_7_characters_for_values_from_0_B_to_999_TB() {
        val units = listOf(ByteUnits.DECIMAL, ByteUnits.BINARY)
        val magnitudes = listOf(
            0L, 1L, 999L, 1024L, 1500L, 10_000L, 100_000L, 1_000_000L,
            10_000_000L, 500_000_000L, 1_000_000_000L, 1_400_000_000L,
            50_000_000_000L, 999_000_000_000L, 1_000_000_000_000L,
            999_000_000_000_000L,
        )

        for (u in units) {
            for (bytes in magnitudes) {
                val formatted = formatBytes(bytes, u).let { "${it.value} ${it.unit}" }
                for (state in BudgetStateUi.entries) {
                    val res = ChipText.forState(state, usedToday = formatted, remaining = formatted)
                    assertTrue("Expected length <= 7 for $state with value '$formatted', got '$res'", res.length <= 7)
                }
            }
        }
    }

    @Test
    fun the_three_states_have_different_texts() {
        val ok = ChipText.forState(BudgetStateUi.Ok, usedToday = "1.4 GB", remaining = "1.2 GB")
        val warn = ChipText.forState(BudgetStateUi.Warning, usedToday = "1.4 GB", remaining = "1.2 GB")
        val exc = ChipText.forState(BudgetStateUi.Exceeded, usedToday = "1.4 GB", remaining = "1.2 GB")

        assertNotEquals(ok, warn)
        assertNotEquals(warn, exc)
        assertNotEquals(ok, exc)
    }
}
