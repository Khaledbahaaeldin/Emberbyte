package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BentoLogicTest {
    @Test
    fun big_network_is_the_larger_one() {
        assertEquals(BigNetwork(bytes = 3_700_000_000L, isWifi = true), bigNetwork(mobile = 9_000_000L, wifi = 3_700_000_000L))
        assertEquals(BigNetwork(bytes = 800_000_000L, isWifi = false), bigNetwork(mobile = 800_000_000L, wifi = 12_000_000L))
    }

    @Test
    fun big_network_ties_pick_wifi() {
        assertEquals(BigNetwork(bytes = 5L, isWifi = true), bigNetwork(mobile = 5L, wifi = 5L))
        assertEquals(BigNetwork(bytes = 0L, isWifi = true), bigNetwork(mobile = 0L, wifi = 0L))
    }

    @Test
    fun mobile_segment_has_a_minimum_visible_width() {
        // 9 MB of 3.7 GB is under one percent: it still shows as a 14 dp sliver.
        assertEquals(14f, mobileSegmentWidth(mobile = 9_000_000L, wifi = 3_700_000_000L, barWidthDp = 200f), 0.001f)
        // A large share stays proportional.
        assertEquals(100f, mobileSegmentWidth(mobile = 50L, wifi = 50L, barWidthDp = 200f), 0.001f)
    }

    @Test
    fun mobile_segment_is_zero_when_there_is_no_mobile_data() {
        assertEquals(0f, mobileSegmentWidth(mobile = 0L, wifi = 1_000L, barWidthDp = 200f), 0f)
        assertEquals(0f, mobileSegmentWidth(mobile = 0L, wifi = 0L, barWidthDp = 200f), 0f)
    }

    @Test
    fun mobile_segment_never_exceeds_the_bar() {
        assertEquals(200f, mobileSegmentWidth(mobile = 1_000L, wifi = 0L, barWidthDp = 200f), 0.001f)
        assertEquals(14f, mobileSegmentWidth(mobile = 1L, wifi = 1_000_000L, barWidthDp = 10f), 10f)
    }

    @Test
    fun trace_scale_has_a_floor_for_an_idle_connection() {
        assertEquals(256_000L, traceScale(listOf(10L, 40L, 0L), floorBps = 256_000L))
        assertEquals(256_000L, traceScale(emptyList(), floorBps = 256_000L))
        assertEquals(5_000_000L, traceScale(listOf(1L, 5_000_000L), floorBps = 256_000L))
    }

    @Test
    fun append_sample_keeps_the_last_60_oldest_first() {
        var history = emptyList<Long>()
        for (i in 1..75) history = appendSample(history, i.toLong())
        assertEquals(60, history.size)
        assertEquals(16L, history.first())
        assertEquals(75L, history.last())
        assertEquals(listOf(1L, 2L), appendSample(listOf(1L), 2L))
    }

    @Test
    fun count_up_never_exceeds_the_target_and_ends_on_it() {
        var previous = -1.0
        for (step in 0..100) {
            val value = countUpValue(step / 100f, target = 12.45)
            assertTrue("never above the target", value <= 12.45 + 1e-9)
            assertTrue("never goes backwards", value >= previous)
            previous = value
        }
        assertEquals(0.0, countUpValue(0f, 12.45), 1e-9)
        assertEquals(12.45, countUpValue(1f, 12.45), 1e-9)
        assertEquals(12.45, countUpValue(1.7f, 12.45), 1e-9)
        assertEquals(0.0, countUpValue(-0.5f, 12.45), 1e-9)
    }
}
