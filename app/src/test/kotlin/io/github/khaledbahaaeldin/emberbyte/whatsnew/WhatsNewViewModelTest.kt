package io.github.khaledbahaaeldin.emberbyte.whatsnew

import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WhatsNewViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeWhatsNewRepository(
        override val entries: List<WhatsNewEntry>,
        initialLastSeen: Int? = null,
    ) : WhatsNewRepository {
        var storedLastSeen: Int? = initialLastSeen
        override suspend fun lastSeenVersionCode(): Int? = storedLastSeen
        override suspend fun markSeen(versionCode: Int) {
            storedLastSeen = versionCode
        }
    }

    private val entry3 = WhatsNewEntry(
        versionCode = 3,
        versionName = "1.0.2",
        date = LocalDate.of(2026, 11, 12),
        highlights = listOf(Highlight(Kind.Added, "Updates row in Settings.")),
    )

    @Test
    fun last_seen_is_written_only_on_dismiss() = runTest(dispatcher) {
        val repo = FakeWhatsNewRepository(listOf(entry3), initialLastSeen = null)
        val vm = WhatsNewViewModel(repo, currentVersionCode = 3)

        vm.onAppReady(onboardingCompleted = true)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showSheet)
        assertNull("lastSeen must not be written before dismiss", repo.storedLastSeen)

        vm.onDismiss()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showSheet)
        assertEquals(3, repo.storedLastSeen)
    }

    @Test
    fun fresh_install_records_current_version_silently() = runTest(dispatcher) {
        val repo = FakeWhatsNewRepository(listOf(entry3), initialLastSeen = null)
        val vm = WhatsNewViewModel(repo, currentVersionCode = 3)

        vm.onAppReady(onboardingCompleted = false)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showSheet)
        assertEquals(3, repo.storedLastSeen)

        // After onboarding completes, nothing is shown
        vm.onAppReady(onboardingCompleted = true)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showSheet)
    }

    @Test
    fun process_death_does_not_show_twice() = runTest(dispatcher) {
        val repo = FakeWhatsNewRepository(listOf(entry3), initialLastSeen = null)
        val vm1 = WhatsNewViewModel(repo, currentVersionCode = 3)
        vm1.onAppReady(onboardingCompleted = true)
        advanceUntilIdle()
        assertTrue(vm1.uiState.value.showSheet)
        vm1.onDismiss()
        advanceUntilIdle()

        // Simulate new process / recreated ViewModel reading persisted lastSeen
        val vm2 = WhatsNewViewModel(repo, currentVersionCode = 3)
        vm2.onAppReady(onboardingCompleted = true)
        advanceUntilIdle()
        assertFalse(vm2.uiState.value.showSheet)
    }

    @Test
    fun sheet_is_not_shown_over_onboarding_or_blocking_overlay() = runTest(dispatcher) {
        val repo = FakeWhatsNewRepository(listOf(entry3), initialLastSeen = null)
        val vm = WhatsNewViewModel(repo, currentVersionCode = 3)

        vm.onAppReady(onboardingCompleted = true, overlayBlocking = true)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showSheet)
        assertNull(repo.storedLastSeen)
    }

    @Test
    fun malformed_asset_does_not_crash() = runTest(dispatcher) {
        val parsed = parseWhatsNewJson("{ not valid json")
        assertTrue(parsed.isEmpty())

        val repo = FakeWhatsNewRepository(parsed, initialLastSeen = null)
        val vm = WhatsNewViewModel(repo, currentVersionCode = 3)
        vm.onAppReady(onboardingCompleted = true)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showSheet)
        assertFalse(vm.uiState.value.hasRecordedChanges)
        assertNull(vm.uiState.value.currentEntry)
    }
}
