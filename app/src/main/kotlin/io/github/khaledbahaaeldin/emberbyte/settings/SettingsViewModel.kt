package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.data.SettingsRepository
import io.github.khaledbahaaeldin.emberbyte.data.StorageSnapshot
import io.github.khaledbahaaeldin.emberbyte.data.StorageStats
import io.github.khaledbahaaeldin.emberbyte.data.UsageRepository
import io.github.khaledbahaaeldin.emberbyte.data.util.SystemZoneClock
import io.github.khaledbahaaeldin.emberbyte.engine.model.CoverageStatus
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsHubState(
    val settings: Settings = Settings(),
    val todayBytes: Long = 0L,
    val lastSampleAt: Instant? = null,
    val health: MeasuringHealth = measuringHealth(
        batteryUnrestricted = true,
        usageAccess = true,
        liveOn = true,
        lastSampleAt = null,
        gapsLast24h = emptyList(),
        now = Instant.EPOCH,
    ),
    val strip: List<SampleHour> = emptyList(),
    val switches: List<SwitchUi> = switchState(Settings(), isDark = false),
    val storage: StorageSnapshot? = null,
    val isDark: Boolean = false,
)

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val usage: UsageRepository? = null,
    private val batteryUnrestricted: () -> Boolean = { true },
    private val usageAccess: () -> Boolean = { true },
    private val storageStats: StorageStats? = null,
    private val clock: Clock = SystemZoneClock(),
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)
    private val isDarkFlow = MutableStateFlow(false)
    private val storageFlow = MutableStateFlow<StorageSnapshot?>(null)

    init {
        refreshStorage()
    }

    val uiState: StateFlow<Settings> =
        settings.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Settings())

    private val coverageFlow: Flow<CoverageStatus> =
        usage?.observeCoverage() ?: flowOf(CoverageStatus(emptyList(), null))

    private val todayBytesFlow: Flow<Long> =
        usage?.observeToday()?.map { it.totalBytes } ?: flowOf(0L)

    val hubState: StateFlow<SettingsHubState> = combine(
        settings.observe(),
        coverageFlow,
        todayBytesFlow,
        refreshTick,
        isDarkFlow,
    ) { current, coverage, todayBytes, _, isDark ->
        val now = clock.instant()
        val health = measuringHealth(
            batteryUnrestricted = batteryUnrestricted(),
            usageAccess = usageAccess(),
            liveOn = current.liveNotificationEnabled,
            lastSampleAt = coverage.lastSampleAt,
            gapsLast24h = coverage.gaps,
            now = now,
        )
        SettingsHubState(
            settings = current,
            todayBytes = todayBytes,
            lastSampleAt = coverage.lastSampleAt,
            health = health,
            strip = sampleStrip(coverage, now, clock.zone),
            switches = switchState(current, isDark = isDark),
            isDark = isDark,
        )
    }.combine(storageFlow) { state, storage ->
        state.copy(storage = storage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsHubState())

    fun update(transform: (Settings) -> Settings) {
        viewModelScope.launch { settings.update(transform) }
    }

    fun setDarkTheme(isDark: Boolean) {
        isDarkFlow.value = isDark
    }

    fun recheckHealth() {
        refreshTick.update { it + 1 }
    }

    fun refreshStorage() {
        val stats = storageStats ?: return
        viewModelScope.launch {
            when (val res = stats.snapshot()) {
                is Outcome.Success -> storageFlow.value = res.value
                is Outcome.Failure -> Unit
            }
        }
    }

    /** Toggles one of the four hub switches; returns a snackbar message if blocked, else null. */
    fun toggleSwitch(id: HubSwitchId, isDark: Boolean): String? {
        val current = hubState.value.settings
        return when (id) {
            HubSwitchId.Live -> {
                update { it.copy(liveNotificationEnabled = !it.liveNotificationEnabled) }
                null
            }
            HubSwitchId.Speed -> {
                if (!current.liveNotificationEnabled) {
                    "Turn on the live notification first"
                } else {
                    update { it.copy(notificationShowsSpeed = !it.notificationShowsSpeed) }
                    null
                }
            }
            HubSwitchId.Spikes -> {
                update { it.copy(spikeAlertsEnabled = !it.spikeAlertsEnabled) }
                null
            }
            HubSwitchId.Amoled -> {
                if (!isDark) {
                    "AMOLED black only applies in dark mode"
                } else {
                    update { it.copy(amoledBlack = !it.amoledBlack) }
                    null
                }
            }
        }
    }
}
