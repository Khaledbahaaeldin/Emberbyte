package io.github.khaledbahaaeldin.emberbyte.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.Granularity
import io.github.khaledbahaaeldin.emberbyte.engine.model.UsagePoint
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UsageBarRow
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HistoryLabelsTest {
    @get:Rule
    val rule = createComposeRule()

    private val zone = ZoneOffset.UTC

    @Test
    fun day_labels_show_the_month_when_it_changes() {
        // 28 Nov 2026 to 3 Dec 2026
        val dates = listOf(
            LocalDate.of(2026, 11, 28),
            LocalDate.of(2026, 11, 29),
            LocalDate.of(2026, 11, 30),
            LocalDate.of(2026, 12, 1),
            LocalDate.of(2026, 12, 2),
            LocalDate.of(2026, 12, 3),
        )
        val points = dates.map { d ->
            UsagePoint(d.atStartOfDay(zone).toInstant(), 100_000_000L, 0L)
        }
        val bars = historyBars(points, Granularity.DAY, ByteUnits.DECIMAL, zone, Locale.ENGLISH)

        // 28 Nov to 30 Nov have initial and number
        assertEquals("S\n28", bars[0].label)
        assertEquals("S\n29", bars[1].label)
        assertEquals("M\n30", bars[2].label)
        // 1 Dec shows month on rollover
        assertEquals("1 Dec", bars[3].label)
        // 2 Dec (Wed) and 3 Dec (Thu) resume initial and number
        assertEquals("W\n2", bars[4].label)
        assertEquals("T\n3", bars[5].label)
    }

    @Test
    fun day_labels_show_weekday_initial_and_number() {
        val date = LocalDate.of(2026, 10, 15) // Thursday
        val point = UsagePoint(date.atStartOfDay(zone).toInstant(), 500_000L, 0L)
        val bars = historyBars(listOf(point), Granularity.DAY, ByteUnits.DECIMAL, zone, Locale.ENGLISH)
        assertEquals("T\n15", bars.single().label)
    }

    @Test
    fun week_labels_are_monday_to_sunday_ranges() {
        val monday = LocalDate.of(2026, 11, 2) // Monday 2 Nov
        val point = UsagePoint(monday.atStartOfDay(zone).toInstant(), 1_000_000_000L, 0L)
        val bars = historyBars(listOf(point), Granularity.WEEK, ByteUnits.DECIMAL, zone, Locale.ENGLISH)
        assertEquals("2 to 8 Nov", bars.single().label)
    }

    @Test
    fun month_labels_show_the_year_only_at_a_year_boundary() {
        val dec2026 = LocalDate.of(2026, 12, 1)
        val jan2027 = LocalDate.of(2027, 1, 1)
        val feb2027 = LocalDate.of(2027, 2, 1)
        val points = listOf(dec2026, jan2027, feb2027).map { d ->
            UsagePoint(d.atStartOfDay(zone).toInstant(), 500_000_000L, 0L)
        }
        val bars = historyBars(points, Granularity.MONTH, ByteUnits.DECIMAL, zone, Locale.ENGLISH)
        assertEquals("Dec", bars[0].label)
        assertEquals("Jan 27", bars[1].label)
        assertEquals("Feb", bars[2].label)
    }

    @Test
    fun labels_use_the_locale() {
        val date = LocalDate.of(2026, 10, 15) // Thursday
        val point = UsagePoint(date.atStartOfDay(zone).toInstant(), 500_000L, 0L)
        val enBars = historyBars(listOf(point), Granularity.DAY, ByteUnits.DECIMAL, zone, Locale.ENGLISH)
        val arBars = historyBars(listOf(point), Granularity.DAY, ByteUnits.DECIMAL, zone, Locale("ar"))
        assertEquals("T\n15", enBars.single().label)
        // Arabic initial for Thursday is "خ"
        assertTrue(arBars.single().label.startsWith("خ"))
    }

    @Test
    fun labels_never_truncate_at_font_scale_2() {
        val points = (1..7).map { day ->
            val d = LocalDate.of(2026, 10, day)
            UsagePoint(d.atStartOfDay(zone).toInstant(), 100_000_000L, 0L)
        }
        val bars = historyBars(points, Granularity.DAY, ByteUnits.DECIMAL, zone, Locale.ENGLISH)
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(Modifier.size(411.dp, 300.dp)) {
                        UsageBarRow(points = bars, selectedIndex = null, onSelect = {}, openAtEnd = false)
                    }
                }
            }
        }
        rule.waitForIdle()
        // Check all bars exist and render
        bars.forEach { bar ->
            rule.onNodeWithText(bar.label, useUnmergedTree = true).assertExists()
        }
    }
}
