package io.github.khaledbahaaeldin.emberbyte.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppearanceEvent {
    data class SetTheme(val id: String) : AppearanceEvent
    data class SetMode(val mode: ThemeMode) : AppearanceEvent
    data class SetWallpaper(val on: Boolean) : AppearanceEvent
    data class SetAmoled(val on: Boolean) : AppearanceEvent
    data class SetHaptics(val on: Boolean) : AppearanceEvent
}

data class AppearanceUiState(
    val themeId: String = "dusk",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useWallpaperColors: Boolean = false,
    val amoled: Boolean = false,
    val haptics: Boolean = true,
    val wallpaperSupported: Boolean = true,
    val amoledEnabled: Boolean = true,
)

class AppearanceViewModel(
    private val settings: SettingsRepository,
    private val isSystemDark: () -> Boolean = { false },
    private val sdkInt: Int = Build.VERSION.SDK_INT,
    private val widgetRefresh: suspend () -> Unit = {},
) : ViewModel() {

    private fun toUiState(s: Settings): AppearanceUiState {
        val effectiveDark = when (s.themeMode) {
            ThemeMode.SYSTEM -> isSystemDark()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        return AppearanceUiState(
            themeId = s.themeId,
            themeMode = s.themeMode,
            useWallpaperColors = s.useWallpaperColors,
            amoled = s.amoledBlack,
            haptics = s.hapticsEnabled,
            wallpaperSupported = sdkInt >= 31,
            amoledEnabled = effectiveDark,
        )
    }

    val uiState: StateFlow<AppearanceUiState> =
        settings.observe()
            .map { toUiState(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                toUiState(Settings()),
            )

    fun onEvent(event: AppearanceEvent) {
        viewModelScope.launch {
            when (event) {
                is AppearanceEvent.SetTheme -> settings.update { it.copy(themeId = event.id) }
                is AppearanceEvent.SetMode -> settings.update { it.copy(themeMode = event.mode) }
                is AppearanceEvent.SetWallpaper -> settings.update { it.copy(useWallpaperColors = event.on) }
                is AppearanceEvent.SetAmoled -> settings.update { it.copy(amoledBlack = event.on) }
                is AppearanceEvent.SetHaptics -> settings.update { it.copy(hapticsEnabled = event.on) }
            }
            if (event !is AppearanceEvent.SetHaptics) {
                widgetRefresh()
            }
        }
    }

    fun setTheme(id: String) = onEvent(AppearanceEvent.SetTheme(id))
    fun setMode(mode: ThemeMode) = onEvent(AppearanceEvent.SetMode(mode))
    fun setWallpaper(on: Boolean) = onEvent(AppearanceEvent.SetWallpaper(on))
    fun setAmoled(on: Boolean) = onEvent(AppearanceEvent.SetAmoled(on))
    fun setHaptics(on: Boolean) = onEvent(AppearanceEvent.SetHaptics(on))
}
