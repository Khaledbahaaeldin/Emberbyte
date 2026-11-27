package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.engine.plan.UsageSlice
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanPreviewTest {
    private val zone = ZoneOffset.UTC
    private val now = Instant.parse("2026-11-15T12:00:00Z")
    private val usage = listOf(
        UsageSlice(
            start = Instant.parse("2026-11-01T00:00:00Z"),
            end = Instant.parse("2026-11-15T12:00:00Z"),
            bytes = 5_000_000_000L,
            packageName = null,
        ),
    )

    @Test
    fun preview_moves_with_the_amount() {
        val form10 = PlanEditorForm(name = "Home", capText = "10", capUnit = CapUnit.GB, dayOfMonth = 1)
        val form20 = form10.copy(capText = "20")

        val p10 = previewFor(form10, usage, now, zone)
        val p20 = previewFor(form20, usage, now, zone)

        assertNotNull(p10)
        assertNotNull(p20)
        assertEquals(0.5f, p10!!.card.fraction, 0.01f)
        assertEquals(0.25f, p20!!.card.fraction, 0.01f)
        assertEquals("50%", p10.card.percentText)
        assertEquals("25%", p20.card.percentText)
    }

    @Test
    fun preview_uses_the_entered_first_cycle_number() {
        val form = PlanEditorForm(
            name = "Home",
            capText = "20",
            capUnit = CapUnit.GB,
            dayOfMonth = 1,
            firstCycle = FirstCycle.Entered(
                cycleStart = Instant.parse("2026-11-01T00:00:00Z"),
                enteredAt = now,
                usedBytes = 12_000_000_000L,
            ),
            firstCycleChoice = FirstCycleChoice.Entered,
            enteredAmount = "12",
            enteredUnit = CapUnit.GB,
        )

        val preview = previewFor(form, usage, now, zone)!!
        assertEquals(0.6f, preview.card.fraction, 0.01f)
        assertEquals("60%", preview.card.percentText)
        assertFalse(preview.card.isEstimated)
    }

    @Test
    fun preview_marks_estimated_when_the_phone_record_is_used() {
        val form = PlanEditorForm(
            name = "Home",
            capText = "20",
            capUnit = CapUnit.GB,
            dayOfMonth = 1,
            firstCycle = FirstCycle.PhoneRecord,
            firstCycleChoice = FirstCycleChoice.PhoneRecord,
        )

        val preview = previewFor(form, usage, now, zone)!!
        assertTrue(preview.card.isEstimated)
    }

    @Test
    fun preview_of_a_dormant_plan_shows_the_start_date() {
        val nextStart = Instant.parse("2026-12-01T00:00:00Z")
        val form = PlanEditorForm(
            name = "Home",
            capText = "20",
            capUnit = CapUnit.GB,
            dayOfMonth = 1,
            anchor = LocalDate.of(2026, 11, 1),
            firstCycle = FirstCycle.StartsNextCycle(startsAt = nextStart),
            firstCycleChoice = FirstCycleChoice.StartNextCycle,
        )

        val preview = previewFor(form, usage, now, zone)!!
        assertTrue(preview.card.isDormant)
        assertEquals("Starts 1 Dec", preview.card.startsAtText)
        assertEquals(0f, preview.card.fraction, 0.001f)
    }
}
