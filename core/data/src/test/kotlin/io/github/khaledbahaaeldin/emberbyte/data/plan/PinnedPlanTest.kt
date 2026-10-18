package io.github.khaledbahaaeldin.emberbyte.data.plan

import io.github.khaledbahaaeldin.emberbyte.data.db.EmberbyteDatabase
import io.github.khaledbahaaeldin.emberbyte.data.fake.InMemoryUsageStore
import io.github.khaledbahaaeldin.emberbyte.engine.model.Cycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import io.github.khaledbahaaeldin.emberbyte.engine.model.Plan
import io.github.khaledbahaaeldin.emberbyte.engine.model.Rollover
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PinnedPlanTest {
    private val now = Instant.parse("2026-10-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private lateinit var db: EmberbyteDatabase
    private lateinit var store: RoomPlanStore
    private lateinit var repo: RoomPlanRepository

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        db = androidx.room.Room.inMemoryDatabaseBuilder(context, EmberbyteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        store = RoomPlanStore(db)
        repo = RoomPlanRepository(store, InMemoryUsageStore(), clock, ticks = flowOf(now))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun draft(name: String, pinned: Boolean = false) = Plan(
        id = 0L,
        name = name,
        subscriptionId = 1,
        capBytes = 10_000_000_000L,
        cycle = Cycle.MonthlyOnDay(1, LocalTime.MIDNIGHT, ZoneOffset.UTC),
        rollover = Rollover.None,
        pinned = pinned,
    )

    @Test
    fun pinning_one_plan_unpins_the_others_atomically() = runBlocking {
        val p1 = (repo.upsertPlan(draft("Plan 1", pinned = true)) as Outcome.Success).value
        val p2 = (repo.upsertPlan(draft("Plan 2", pinned = false)) as Outcome.Success).value
        val p3 = (repo.upsertPlan(draft("Plan 3", pinned = false)) as Outcome.Success).value

        assertTrue(repo.observePlans().first().first { it.id == p1 }.pinned)
        assertFalse(repo.observePlans().first().first { it.id == p2 }.pinned)
        assertFalse(repo.observePlans().first().first { it.id == p3 }.pinned)

        // Pin p2
        val pinOutcome = repo.setPinned(p2, true)
        assertTrue(pinOutcome is Outcome.Success)

        val plans = repo.observePlans().first()
        assertFalse(plans.first { it.id == p1 }.pinned)
        assertTrue(plans.first { it.id == p2 }.pinned)
        assertFalse(plans.first { it.id == p3 }.pinned)

        // Unpin p2
        repo.setPinned(p2, false)
        val plansAfterUnpin = repo.observePlans().first()
        assertFalse(plansAfterUnpin.first { it.id == p1 }.pinned)
        assertFalse(plansAfterUnpin.first { it.id == p2 }.pinned)
        assertFalse(plansAfterUnpin.first { it.id == p3 }.pinned)
    }

    @Test
    fun archiving_a_pinned_plan_clears_the_pin() = runBlocking {
        val p1 = (repo.upsertPlan(draft("Plan 1", pinned = true)) as Outcome.Success).value
        assertTrue(repo.observePlans(includeArchived = true).first().first { it.id == p1 }.pinned)

        val archiveOutcome = repo.archivePlan(p1)
        assertTrue(archiveOutcome is Outcome.Success)

        val archivedPlan = repo.observePlans(includeArchived = true).first().first { it.id == p1 }
        assertTrue(archivedPlan.archived)
        assertFalse(archivedPlan.pinned)
    }
}
