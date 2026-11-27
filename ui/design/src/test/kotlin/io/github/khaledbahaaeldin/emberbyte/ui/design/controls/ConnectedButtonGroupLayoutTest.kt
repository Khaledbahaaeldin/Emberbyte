package io.github.khaledbahaaeldin.emberbyte.ui.design.controls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectedButtonGroupLayoutTest {
    @Test
    fun pack_rows_keeps_one_row_when_it_fits() {
        val rows = packRows(needed = listOf(80, 90, 100), gap = 2, available = 300)
        assertEquals(listOf(listOf(0, 1, 2)), rows)
    }

    @Test
    fun pack_rows_splits_four_into_two_by_two_when_one_row_does_not_fit() {
        val rows = packRows(needed = listOf(120, 150, 170, 130), gap = 2, available = 340)
        assertEquals(listOf(listOf(0, 1), listOf(2, 3)), rows)
    }

    @Test
    fun pack_rows_balances_the_rows_instead_of_filling_the_first() {
        // Greedy fill would give [0,1,2] [3]; balancing gives [0,1] [2,3] with a lower widest row.
        val rows = packRows(needed = listOf(60, 60, 60, 100), gap = 0, available = 200)
        assertEquals(listOf(listOf(0, 1), listOf(2, 3)), rows)
    }

    @Test
    fun pack_rows_uses_one_segment_per_row_when_nothing_else_fits() {
        val rows = packRows(needed = listOf(300, 300, 300), gap = 2, available = 310)
        assertEquals(listOf(listOf(0), listOf(1), listOf(2)), rows)
    }

    @Test
    fun pack_rows_returns_a_single_row_for_unbounded_width() {
        val rows = packRows(needed = listOf(500, 500, 500), gap = 2, available = Int.MAX_VALUE)
        assertEquals(listOf(listOf(0, 1, 2)), rows)
    }

    @Test
    fun pack_rows_keeps_the_original_order() {
        val rows = packRows(needed = listOf(100, 50, 100, 50, 100), gap = 0, available = 220)
        assertEquals(listOf(0, 1, 2, 3, 4), rows.flatten())
        assertTrue(rows.all { it.isNotEmpty() })
    }

    @Test
    fun pack_rows_handles_an_empty_list() {
        assertEquals(listOf<List<Int>>(emptyList()), packRows(needed = emptyList(), gap = 2, available = 300))
    }
}
