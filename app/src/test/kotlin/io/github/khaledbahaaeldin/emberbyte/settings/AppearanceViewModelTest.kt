package io.github.khaledbahaaeldin.emberbyte.settings

import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import io.github.khaledbahaaeldin.emberbyte.data.fake.FakeSettingsRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun setting_a_theme_updates_settings() = runTest(dispatcher) {
        val repo = FakeSettingsRepository(Settings(themeId = "dusk", themeMode = ThemeMode.SYSTEM))
        val vm = AppearanceViewModel(repo)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("dusk", vm.uiState.value.themeId)

        vm.onEvent(AppearanceEvent.SetTheme("ember"))
        advanceUntilIdle()

        assertEquals("ember", vm.uiState.value.themeId)
        assertEquals("ember", repo.observe().first().themeId)

        vm.onEvent(AppearanceEvent.SetMode(ThemeMode.DARK))
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, vm.uiState.value.themeMode)
        assertEquals(ThemeMode.DARK, repo.observe().first().themeMode)

        vm.onEvent(AppearanceEvent.SetWallpaper(true))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.useWallpaperColors)
        assertTrue(repo.observe().first().useWallpaperColors)

        vm.onEvent(AppearanceEvent.SetAmoled(true))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.amoled)
        assertTrue(repo.observe().first().amoledBlack)

        vm.onEvent(AppearanceEvent.SetHaptics(false))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.haptics)
        assertFalse(repo.observe().first().hapticsEnabled)

        job.cancel()
    }

    @Test
    fun amoled_is_disabled_when_the_effective_mode_is_light() = runTest(dispatcher) {
        // Case 1: ThemeMode is explicitly LIGHT -> amoledEnabled must be false
        val repoLight = FakeSettingsRepository(Settings(themeMode = ThemeMode.LIGHT))
        val vmLight = AppearanceViewModel(repoLight, isSystemDark = { true })
        val job1 = launch { vmLight.uiState.collect {} }
        advanceUntilIdle()
        assertFalse("amoled must be disabled when theme mode is LIGHT", vmLight.uiState.value.amoledEnabled)
        job1.cancel()

        // Case 2: ThemeMode is SYSTEM, system is light -> amoledEnabled must be false
        val repoSysLight = FakeSettingsRepository(Settings(themeMode = ThemeMode.SYSTEM))
        val vmSysLight = AppearanceViewModel(repoSysLight, isSystemDark = { false })
        val job2 = launch { vmSysLight.uiState.collect {} }
        advanceUntilIdle()
        assertFalse("amoled must be disabled when system is light", vmSysLight.uiState.value.amoledEnabled)
        job2.cancel()

        // Case 3: ThemeMode is DARK -> amoledEnabled must be true
        val repoDark = FakeSettingsRepository(Settings(themeMode = ThemeMode.DARK))
        val vmDark = AppearanceViewModel(repoDark, isSystemDark = { false })
        val job3 = launch { vmDark.uiState.collect {} }
        advanceUntilIdle()
        assertTrue("amoled must be enabled when theme mode is DARK", vmDark.uiState.value.amoledEnabled)
        job3.cancel()

        // Case 4: ThemeMode is SYSTEM, system is dark -> amoledEnabled must be true
        val repoSysDark = FakeSettingsRepository(Settings(themeMode = ThemeMode.SYSTEM))
        val vmSysDark = AppearanceViewModel(repoSysDark, isSystemDark = { true })
        val job4 = launch { vmSysDark.uiState.collect {} }
        advanceUntilIdle()
        assertTrue("amoled must be enabled when system is dark", vmSysDark.uiState.value.amoledEnabled)
        job4.cancel()
    }

    @Test
    fun wallpaper_switch_is_unsupported_below_sdk_31() = runTest(dispatcher) {
        val repo = FakeSettingsRepository(Settings())
        val vmSdk30 = AppearanceViewModel(repo, sdkInt = 30)
        val job1 = launch { vmSdk30.uiState.collect {} }
        advanceUntilIdle()
        assertFalse("wallpaperSupported must be false on SDK 30", vmSdk30.uiState.value.wallpaperSupported)
        job1.cancel()

        val vmSdk31 = AppearanceViewModel(repo, sdkInt = 31)
        val job2 = launch { vmSdk31.uiState.collect {} }
        advanceUntilIdle()
        assertTrue("wallpaperSupported must be true on SDK 31", vmSdk31.uiState.value.wallpaperSupported)
        job2.cancel()

        val vmSdk34 = AppearanceViewModel(repo, sdkInt = 34)
        val job3 = launch { vmSdk34.uiState.collect {} }
        advanceUntilIdle()
        assertTrue("wallpaperSupported must be true on SDK 34", vmSdk34.uiState.value.wallpaperSupported)
        job3.cancel()
    }
}
