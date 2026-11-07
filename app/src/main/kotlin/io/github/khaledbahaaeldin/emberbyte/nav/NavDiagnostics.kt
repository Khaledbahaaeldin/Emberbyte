package io.github.khaledbahaaeldin.emberbyte.nav

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import android.util.Log

/**
 * A small in-memory log of navigation events (tab touches, selections, navigation requests and results,
 * transitions, back gestures) that the maintainer can copy from Settings to report a stuck tap.
 *
 * It keeps only the newest [CAPACITY] lines, never touches the disk and never leaves the phone: there is no network
 * permission, and copying is an explicit long press. It holds route names, coordinates and times only, never usage data.
 */
object NavDiagnostics {
    const val CAPACITY = 300
    private const val TAG = "EmberNav"
    private val lock = Any()
    private val events = ArrayDeque<String>()

    internal var clock: () -> Long = { SystemClock.elapsedRealtime() }

    internal fun resetClock() {
        clock = { SystemClock.elapsedRealtime() }
    }

    fun record(kind: String, detail: String = "") {
        val line = buildString {
            append(stamp(clock()))
            append(' ')
            append(kind)
            if (detail.isNotEmpty()) append(' ').append(detail)
        }
        synchronized(lock) {
            events.addLast(line)
            while (events.size > CAPACITY) events.removeFirst()
        }
        runCatching { Log.d(TAG, line) }
    }

    fun snapshot(): List<String> = synchronized(lock) { events.toList() }

    fun clear() = synchronized(lock) { events.clear() }

    fun report(header: String): String = (listOf(header) + snapshot()).joinToString("\n")

    /** Copies [report] to the clipboard and returns how many events it contains. */
    fun copyToClipboard(context: Context, header: String): Int {
        val count = snapshot().size
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("Emberbyte navigation log", report(header)))
        return count
    }

    private fun stamp(millis: Long): String {
        val t = millis % 3_600_000L
        return "%02d:%02d.%03d".format(t / 60_000L, (t / 1_000L) % 60L, t % 1_000L)
    }
}
