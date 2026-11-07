package io.github.khaledbahaaeldin.emberbyte.nav

import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavDiagnosticsTest {
    private var now = 100L

    @Before fun setUp() {
        NavDiagnostics.clear()
        now = 100L
        NavDiagnostics.clock = { now++ }
    }

    @After fun tearDown() {
        NavDiagnostics.clear()
        NavDiagnostics.resetClock()
    }

    @Test fun keeps_only_the_newest_300_events() {
        repeat(350) { NavDiagnostics.record("EVT", "n$it") }
        val lines = NavDiagnostics.snapshot()
        assertEquals(300, lines.size)
        assertTrue(lines.first().endsWith("EVT n50"))
        assertTrue(lines.last().endsWith("EVT n349"))
    }

    @Test fun snapshot_lines_are_time_stamped_and_ordered() {
        NavDiagnostics.record("TAB_DOWN", "x=10")
        NavDiagnostics.record("TAB_UP")
        NavDiagnostics.record("NAV_REQUEST", "route=apps")
        val lines = NavDiagnostics.snapshot()
        assertEquals("00:00.100 TAB_DOWN x=10", lines[0])
        assertEquals("00:00.101 TAB_UP", lines[1])
        assertEquals("00:00.102 NAV_REQUEST route=apps", lines[2])
    }

    @Test fun clear_empties_the_buffer() {
        NavDiagnostics.record("EVT")
        NavDiagnostics.clear()
        assertTrue(NavDiagnostics.snapshot().isEmpty())
    }

    @Test fun concurrent_records_keep_the_capacity_bound() {
        val threads = List(4) { t -> thread { repeat(200) { NavDiagnostics.record("T$t", "i$it") } } }
        threads.forEach { it.join() }
        assertEquals(300, NavDiagnostics.snapshot().size)
    }

    @Test fun the_report_is_the_header_followed_by_the_events() {
        NavDiagnostics.record("EVT", "a")
        assertEquals("header\n00:00.100 EVT a", NavDiagnostics.report("header"))
    }

    @Test fun copyToClipboard_copies_the_report_and_returns_the_event_count() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NavDiagnostics.record("EVT", "a")
        NavDiagnostics.record("EVT", "b")
        val count = NavDiagnostics.copyToClipboard(context, "Emberbyte test header")
        assertEquals(2, count)
        val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
        val text = clip?.getItemAt(0)?.text?.toString().orEmpty()
        assertTrue(text.startsWith("Emberbyte test header\n"))
        assertTrue(text.contains("EVT a"))
        assertTrue(text.contains("EVT b"))
    }
}
