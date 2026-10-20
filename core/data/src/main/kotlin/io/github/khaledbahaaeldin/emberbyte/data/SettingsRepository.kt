package io.github.khaledbahaaeldin.emberbyte.data

import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import kotlinx.coroutines.flow.Flow

enum class UnitSystem { DECIMAL, BINARY }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val liveNotificationEnabled: Boolean = true,
    val notificationShowsSpeed: Boolean = true,
    val spikeAlertsEnabled: Boolean = true,
    val amoledBlack: Boolean = false,
    val themeId: String = "dusk",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useWallpaperColors: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.DECIMAL,
    val hapticsEnabled: Boolean = true,
    val promotedDismissedOn: String? = null,
)

interface SettingsRepository {
    fun observe(): Flow<Settings>
    suspend fun update(transform: (Settings) -> Settings): Outcome<Unit>
}
