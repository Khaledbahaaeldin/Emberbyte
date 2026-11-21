package io.github.khaledbahaaeldin.emberbyte.apps

import io.github.khaledbahaaeldin.emberbyte.engine.model.AppSort
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppsLogicTest {
    @Test
    fun active_filter_count_ignores_defaults() {
        assertEquals(0, activeFilterCount(AppsNetwork.ALL, AppSort.BYTES_DESC, false))
        assertEquals(1, activeFilterCount(AppsNetwork.MOBILE, AppSort.BYTES_DESC, false))
        assertEquals(2, activeFilterCount(AppsNetwork.MOBILE, AppSort.BYTES_ASC, false))
        assertEquals(3, activeFilterCount(AppsNetwork.WIFI, AppSort.NAME, true))
    }

    @Test
    fun period_label_names_each_range() {
        assertEquals("today", periodLabel(AppsRange.TODAY))
        assertEquals("in 7 days", periodLabel(AppsRange.LAST_7_DAYS))
        assertEquals("in 30 days", periodLabel(AppsRange.LAST_30_DAYS))
        assertEquals("this cycle", periodLabel(AppsRange.CYCLE))
        assertEquals("this cycle", periodLabel(AppsRange.THIS_MONTH))
    }

    @Test
    fun split_weights_keep_a_minimum_for_a_non_zero_side() {
        val (wifi, mobile) = splitWeights(wifi = 3_700_000_000L, mobile = 9_000_000L)
        assertTrue("mobile keeps a visible share ($mobile)", mobile >= 0.045f)
        assertEquals(1f, wifi + mobile, 1e-5f)
        assertEquals(0f to 1f, splitWeights(0L, 5L))
        assertEquals(1f to 0f, splitWeights(5L, 0L))
        assertEquals(0.5f to 0.5f, splitWeights(0L, 0L))
        assertEquals(0.5f to 0.5f, splitWeights(50L, 50L))
    }

    @Test
    fun most_used_is_the_top_row_by_the_selected_network() {
        val rows = listOf(
            AppRowUi("a", "A", mobileBytes = 10, wifiBytes = 500),
            AppRowUi("b", "B", mobileBytes = 300, wifiBytes = 0),
        )
        assertEquals("a", mostUsed(rows, AppsNetwork.ALL)?.packageName)
        assertEquals("a", mostUsed(rows, AppsNetwork.WIFI)?.packageName)
        assertEquals("b", mostUsed(rows, AppsNetwork.MOBILE)?.packageName)
        assertNull(mostUsed(emptyList(), AppsNetwork.ALL))
    }

    @Test
    fun app_amount_follows_the_selected_network() {
        val row = AppRowUi("a", "A", mobileBytes = 10, wifiBytes = 500)
        assertEquals(510L, networkAmount(row, AppsNetwork.ALL))
        assertEquals(10L, networkAmount(row, AppsNetwork.MOBILE))
        assertEquals(500L, networkAmount(row, AppsNetwork.WIFI))
    }

    @Test
    fun sort_label_names_the_sort() {
        assertEquals("Most data", sortLabel(AppSort.BYTES_DESC))
        assertEquals("Least data", sortLabel(AppSort.BYTES_ASC))
        assertEquals("Name", sortLabel(AppSort.NAME))
    }
}
