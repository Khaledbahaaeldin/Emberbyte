package io.github.khaledbahaaeldin.emberbyte.settings

import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageGap
import io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageStatus
import io.github.khaledbahaaeldin.emberbyte.engine.model.GapReason
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsStatsTest {
    private val now = Instant.parse("2026-11-20T10:00:00Z")
    private val recentSample = now.minusSeconds(10 * 60L)
    private val staleSample = now.minusSeconds(90 * 60L)

    @Test
    fun health_score_counts_passing_checks() {
        val h1 = measuringHealth(
            batteryUnrestricted = true,
            usageAccess = true,
            liveOn = true,
            lastSampleAt = recentSample,
            gapsLast24h = emptyList(),
            now = now,
        )
        assertEquals(4, h1.score)

        val h2 = measuringHealth(
            batteryUnrestricted = false,
            usageAccess = true,
            liveOn = false,
            lastSampleAt = staleSample,
            gapsLast24h = emptyList(),
            now = now,
        )
        assertEquals(1, h2.score)
    }

    @Test
    fun battery_failing_with_a_gap_says_android_paused_you() {
        val gap = CoverageGap(now.minusSeconds(5 * 3600L), now.minusSeconds(2 * 3600L), GapReason.SERVICE_KILLED)
        val h = measuringHealth(
            batteryUnrestricted = false,
            usageAccess = false,
            liveOn = false,
            lastSampleAt = recentSample,
            gapsLast24h = listOf(gap),
            now = now,
        )
        assertEquals(HealthAction.FixBattery, h.primary)
        assertTrue(h.pausedWithGap)
    }

    @Test
    fun live_off_offers_turn_on() {
        val h = measuringHealth(
            batteryUnrestricted = true,
            usageAccess = false,
            liveOn = false,
            lastSampleAt = recentSample,
            gapsLast24h = emptyList(),
            now = now,
        )
        assertEquals(HealthAction.TurnOnLive, h.primary)
    }

    @Test
    fun usage_access_missing_offers_allow() {
        val h = measuringHealth(
            batteryUnrestricted = true,
            usageAccess = false,
            liveOn = true,
            lastSampleAt = recentSample,
            gapsLast24h = emptyList(),
            now = now,
        )
        assertEquals(HealthAction.AllowUsageAccess, h.primary)
    }

    @Test
    fun all_passing_has_no_action() {
        val h = measuringHealth(
            batteryUnrestricted = true,
            usageAccess = true,
            liveOn = true,
            lastSampleAt = recentSample,
            gapsLast24h = emptyList(),
            now = now,
        )
        assertNull(h.primary)
    }

    @Test
    fun sample_strip_has_24_hours_oldest_first() {
        val coverage = CoverageStatus(gaps = emptyList(), lastSampleAt = recentSample)
        val strip = sampleStrip(coverage, now, ZoneOffset.UTC)
        assertEquals(24, strip.size)
        for (i in 1 until strip.size) {
            assertTrue(strip[i].hourStart.isAfter(strip[i - 1].hourStart))
        }
        assertTrue(sampleStrip(CoverageStatus(emptyList(), null), now, ZoneOffset.UTC).isEmpty())
    }

    @Test
    fun strip_marks_gap_hours() {
        val gap = CoverageGap(
            Instant.parse("2026-11-20T02:00:00Z"),
            Instant.parse("2026-11-20T05:00:00Z"),
            GapReason.SERVICE_KILLED,
        )
        val strip = sampleStrip(CoverageStatus(listOf(gap), recentSample), now, ZoneOffset.UTC)
        assertEquals(3, missedHours(strip))
    }

    @Test
    fun missed_range_formats_hours_across_midnight() {
        val gap = CoverageGap(
            Instant.parse("2026-11-19T23:00:00Z"),
            Instant.parse("2026-11-20T02:00:00Z"),
            GapReason.SERVICE_KILLED,
        )
        val strip = sampleStrip(CoverageStatus(listOf(gap), recentSample), now, ZoneOffset.UTC)
        assertEquals("23:00 to 02:00", missedRange(strip, ZoneOffset.UTC))
    }

    @Test
    fun search_matches_name_and_area_case_insensitively() {
        val index = defaultSettingsSearchIndex()
        val byName = searchSettings(index, "bAtTeRy")
        assertTrue(byName.any { it.id == "battery" })
        val byArea = searchSettings(index, "LOOK AND FEEL")
        assertTrue(byArea.any { it.id == "amoled" })
    }

    @Test
    fun search_empty_query_returns_all() {
        val index = defaultSettingsSearchIndex()
        assertEquals(index, searchSettings(index, ""))
        assertEquals(index, searchSettings(index, "   "))
    }

    @Test
    fun switch_state_disables_speed_when_live_is_off() {
        val switches = switchState(Settings(liveNotificationEnabled = false, notificationShowsSpeed = true), isDark = true)
        val speed = switches.first { it.id == HubSwitchId.Speed }
        assertFalse(speed.enabled)
        assertEquals("Needs live", speed.caption)
    }

    @Test
    fun switch_state_disables_amoled_in_light() {
        val switches = switchState(Settings(amoledBlack = true), isDark = false)
        val amoled = switches.first { it.id == HubSwitchId.Amoled }
        assertFalse(amoled.enabled)
        assertEquals("Dark only", amoled.caption)
    }

    @Test
    fun strength_scores_length_case_digit_and_symbol() {
        assertEquals(0, passphraseStrength("Ab1!".toCharArray()))
        assertEquals(1, passphraseStrength("abcdefgh".toCharArray()))
        assertEquals(2, passphraseStrength("abcdefghijkl".toCharArray()))
        assertEquals(3, passphraseStrength("Abcdefghijkl".toCharArray()))
        assertEquals(4, passphraseStrength("Abcdefghij1!".toCharArray()))
    }

    @Test
    fun backup_requires_eight_characters() {
        assertFalse(backupAllowed("1234567".toCharArray()))
        assertTrue(backupAllowed("12345678".toCharArray()))
    }

    @Test
    fun storage_breakdown_sums_to_the_file_size() {
        val total = 11_800_003L
        val breakdown = storageBreakdown(
            fileBytes = total,
            rowCounts = StorageRowCounts(hourly = 1000L, daily = 300L, plans = 15L, other = 85L),
        )
        assertEquals(
            total,
            breakdown.hourlyBytes + breakdown.dailyBytes + breakdown.plansBytes + breakdown.otherBytes,
        )
    }
}
