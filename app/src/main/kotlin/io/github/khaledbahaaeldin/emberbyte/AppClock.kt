package io.github.khaledbahaaeldin.emberbyte

import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object AppClock : Clock() {
    private val defaultClock = SystemZoneClock()

    @Volatile
    var fakeNow: Instant? = null

    override fun getZone(): ZoneId = defaultClock.zone

    override fun withZone(zone: ZoneId): Clock = defaultClock.withZone(zone)

    override fun instant(): Instant = fakeNow ?: defaultClock.instant()

    fun reset() {
        fakeNow = null
    }
}
