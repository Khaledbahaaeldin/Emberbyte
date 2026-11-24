package io.github.khaledbahaaeldin.emberbyte.whatsnew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WhatsNewUiState(
    val showSheet: Boolean = false,
    val currentEntry: WhatsNewEntry? = null,
    val olderEntries: List<WhatsNewEntry> = emptyList(),
    val hasRecordedChanges: Boolean = false,
)

internal class WhatsNewViewModel(
    private val repository: WhatsNewRepository,
    private val currentVersionCode: Int,
) : ViewModel() {

    private val allEntries: List<WhatsNewEntry> = repository.entries
    private val currentEntry: WhatsNewEntry? =
        allEntries.firstOrNull { it.versionCode == currentVersionCode } ?: allEntries.firstOrNull()
    private val hasEntryForCurrent: Boolean =
        allEntries.any { it.versionCode == currentVersionCode }
    private val olderEntries: List<WhatsNewEntry> =
        if (currentEntry == null) emptyList() else allEntries.filter { it.versionCode != currentEntry.versionCode }

    private val _uiState = MutableStateFlow(
        WhatsNewUiState(
            showSheet = false,
            currentEntry = currentEntry,
            olderEntries = olderEntries,
            hasRecordedChanges = allEntries.isNotEmpty(),
        ),
    )
    val uiState: StateFlow<WhatsNewUiState> = _uiState.asStateFlow()

    private var evaluatedLaunch = false

    fun onAppReady(onboardingCompleted: Boolean?, overlayBlocking: Boolean = false) {
        if (onboardingCompleted == null) return
        if (!onboardingCompleted) {
            if (!evaluatedLaunch) {
                evaluatedLaunch = true
                viewModelScope.launch {
                    val lastSeen = repository.lastSeenVersionCode()
                    if (lastSeen == null || lastSeen < currentVersionCode) {
                        repository.markSeen(currentVersionCode)
                    }
                }
            }
            return
        }
        if (evaluatedLaunch || overlayBlocking) return
        evaluatedLaunch = true
        viewModelScope.launch {
            val lastSeen = repository.lastSeenVersionCode()
            if (shouldShowWhatsNew(currentVersionCode, lastSeen, onboardingCompleted = true, hasEntryForCurrent = hasEntryForCurrent)) {
                _uiState.update { it.copy(showSheet = true) }
            }
        }
    }

    fun openManually() {
        if (currentEntry == null) return
        _uiState.update { it.copy(showSheet = true) }
    }

    fun onDismiss() {
        _uiState.update { it.copy(showSheet = false) }
        viewModelScope.launch {
            repository.markSeen(currentVersionCode)
        }
    }
}
