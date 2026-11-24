package io.github.khaledbahaaeldin.emberbyte.whatsnew

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewLogicTest {

    @Test
    fun fresh_install_does_not_show() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersionCode = 3,
                lastSeen = null,
                onboardingCompleted = false,
                hasEntryForCurrent = true,
            ),
        )
    }

    @Test
    fun upgrade_from_102_shows() {
        assertTrue(
            shouldShowWhatsNew(
                currentVersionCode = 4,
                lastSeen = null,
                onboardingCompleted = true,
                hasEntryForCurrent = true,
            ),
        )
        assertTrue(
            shouldShowWhatsNew(
                currentVersionCode = 4,
                lastSeen = 3,
                onboardingCompleted = true,
                hasEntryForCurrent = true,
            ),
        )
    }

    @Test
    fun already_seen_does_not_show() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersionCode = 3,
                lastSeen = 3,
                onboardingCompleted = true,
                hasEntryForCurrent = true,
            ),
        )
    }

    @Test
    fun missing_entry_for_current_version_does_not_show() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersionCode = 4,
                lastSeen = 3,
                onboardingCompleted = true,
                hasEntryForCurrent = false,
            ),
        )
    }

    @Test
    fun downgrade_or_same_version_does_not_show() {
        assertFalse(
            shouldShowWhatsNew(
                currentVersionCode = 3,
                lastSeen = 4,
                onboardingCompleted = true,
                hasEntryForCurrent = true,
            ),
        )
        assertFalse(
            shouldShowWhatsNew(
                currentVersionCode = 3,
                lastSeen = 3,
                onboardingCompleted = true,
                hasEntryForCurrent = true,
            ),
        )
    }
}
