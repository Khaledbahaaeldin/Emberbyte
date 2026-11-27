package io.github.khaledbahaaeldin.emberbyte.settings

import android.net.Uri
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.UnitSystem
import io.github.khaledbahaaeldin.emberbyte.data.backup.BackupService
import io.github.khaledbahaaeldin.emberbyte.data.backup.RestoreSummary
import io.github.khaledbahaaeldin.emberbyte.data.export.ExportService
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeUsageRepository
import io.github.khaledbahaaeldin.emberbyte.engine.model.DateRange
import io.github.khaledbahaaeldin.emberbyte.engine.model.EmberbyteError
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [35])
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun ui_state_reflects_repository_and_updates() = runTest(dispatcher) {
        val repo = FakeSettingsRepository(Settings(amoledBlack = false))
        val vm = SettingsViewModel(repo)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(false, vm.uiState.value.amoledBlack)

        vm.update { it.copy(amoledBlack = true, unitSystem = UnitSystem.BINARY) }
        advanceUntilIdle()

        assertTrue(vm.uiState.value.amoledBlack)
        assertEquals(UnitSystem.BINARY, vm.uiState.value.unitSystem)
        assertEquals(UnitSystem.BINARY, repo.observe().first().unitSystem)

        job.cancel()
    }

    @Test fun hub_state_combines_health_strip_and_switches() = runTest(dispatcher) {
        val now = Instant.parse("2026-11-20T10:00:00Z")
        val repo = FakeSettingsRepository(Settings(liveNotificationEnabled = true, notificationShowsSpeed = true))
        val usage = FakeUsageRepository(clock = Clock.fixed(now, ZoneOffset.UTC))
        val vm = SettingsViewModel(
            settings = repo,
            usage = usage,
            batteryUnrestricted = { true },
            usageAccess = { true },
            clock = Clock.fixed(now, ZoneOffset.UTC),
        )
        val job = launch { vm.hubState.collect {} }
        advanceUntilIdle()

        val hub = vm.hubState.value
        assertEquals(4, hub.health.score)
        assertEquals(24, hub.strip.size)
        assertEquals(4, hub.switches.size)
        assertTrue(hub.switches.first { it.id == HubSwitchId.Live }.checked)
        job.cancel()
    }

    @Test fun returning_from_system_settings_rechecks_health() = runTest(dispatcher) {
        val now = Instant.parse("2026-11-20T10:00:00Z")
        var batteryOk = false
        val repo = FakeSettingsRepository(Settings(liveNotificationEnabled = true))
        val usage = FakeUsageRepository(clock = Clock.fixed(now, ZoneOffset.UTC))
        val vm = SettingsViewModel(
            settings = repo,
            usage = usage,
            batteryUnrestricted = { batteryOk },
            usageAccess = { true },
            clock = Clock.fixed(now, ZoneOffset.UTC),
        )
        val job = launch { vm.hubState.collect {} }
        advanceUntilIdle()
        assertEquals(3, vm.hubState.value.health.score)

        batteryOk = true
        vm.recheckHealth()
        advanceUntilIdle()
        assertEquals(4, vm.hubState.value.health.score)
        job.cancel()
    }

    @Test fun speed_switch_is_blocked_when_live_is_off() = runTest(dispatcher) {
        val repo = FakeSettingsRepository(Settings(liveNotificationEnabled = false, notificationShowsSpeed = true))
        val vm = SettingsViewModel(settings = repo)
        val job = launch { vm.hubState.collect {} }
        advanceUntilIdle()

        val message = vm.toggleSwitch(HubSwitchId.Speed, isDark = true)
        advanceUntilIdle()
        assertEquals("Turn on the live notification first", message)
        assertTrue(repo.observe().first().notificationShowsSpeed)
        job.cancel()
    }

    @Test fun successful_backup_sets_last_backup_at() = runTest(dispatcher) {
        val now = Instant.parse("2026-11-20T12:00:00Z")
        val settingsRepo = FakeSettingsRepository()
        val vm = DataViewModel(
            export = StubExportService(),
            backup = StubBackupService(succeed = true),
            opener = StubStreamOpener(),
            clock = Clock.fixed(now, ZoneOffset.UTC),
            workContext = dispatcher,
            settings = settingsRepo,
        )
        vm.createBackup(Uri.EMPTY, "passphrase1".toCharArray())
        advanceUntilIdle()
        assertEquals(now, settingsRepo.observe().first().lastBackupAt)
    }

    @Test fun failed_backup_does_not() = runTest(dispatcher) {
        val now = Instant.parse("2026-11-20T12:00:00Z")
        val settingsRepo = FakeSettingsRepository()
        val vm = DataViewModel(
            export = StubExportService(),
            backup = StubBackupService(succeed = false),
            opener = StubStreamOpener(),
            clock = Clock.fixed(now, ZoneOffset.UTC),
            workContext = dispatcher,
            settings = settingsRepo,
        )
        vm.createBackup(Uri.EMPTY, "passphrase1".toCharArray())
        advanceUntilIdle()
        assertEquals(null, settingsRepo.observe().first().lastBackupAt)
    }

    private class StubExportService : ExportService {
        override suspend fun exportCsv(range: DateRange, out: OutputStream): Outcome<Unit> = Outcome.Success(Unit)
        override suspend fun exportJson(out: OutputStream): Outcome<Unit> = Outcome.Success(Unit)
    }

    private class StubBackupService(private val succeed: Boolean) : BackupService {
        override suspend fun createBackup(out: OutputStream, passphrase: CharArray): Outcome<Unit> =
            if (succeed) Outcome.Success(Unit) else Outcome.Failure(EmberbyteError.Storage("boom"))
        override suspend fun restoreBackup(input: InputStream, passphrase: CharArray): Outcome<RestoreSummary> =
            Outcome.Success(RestoreSummary(0, 0, 0, 0))
    }

    private class StubStreamOpener : StreamOpener {
        override fun output(uri: Uri): OutputStream = ByteArrayOutputStream()
        override fun input(uri: Uri): InputStream = ByteArrayInputStream(ByteArray(0))
    }
}
