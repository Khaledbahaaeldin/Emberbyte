package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.data.ArchivedPlansRestoreRepository
import io.github.khaledbahaaeldin.emberbyte.data.PlanRepository
import io.github.khaledbahaaeldin.emberbyte.data.PlanSnapshot
import io.github.khaledbahaaeldin.emberbyte.engine.model.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory coordinator for one-time archived plan restoration notice and pending plan deletion Undo
 * across the Plan Editor and Plans screen.
 */
class PlansSessionCoordinator {
    private val _pendingDelete = MutableStateFlow<PlanSnapshot?>(null)
    val pendingDelete: StateFlow<PlanSnapshot?> = _pendingDelete.asStateFlow()

    private val _restoredCount = MutableStateFlow(0)
    val restoredCount: StateFlow<Int> = _restoredCount.asStateFlow()

    fun setPendingDelete(snapshot: PlanSnapshot?) {
        _pendingDelete.value = snapshot
    }

    fun clearPendingDelete(): PlanSnapshot? {
        val current = _pendingDelete.value
        _pendingDelete.value = null
        return current
    }

    fun setRestoredCount(count: Int) {
        if (count > 0) {
            _restoredCount.value = count
        }
    }

    fun dismissRestoredNotice() {
        _restoredCount.value = 0
    }
}

suspend fun runArchivedPlansRestoreOnce(
    plans: PlanRepository,
    restoreRepo: ArchivedPlansRestoreRepository,
    coordinator: PlansSessionCoordinator,
): Int {
    if (restoreRepo.isRestored()) return 0
    return when (val result = plans.restoreArchivedPlans()) {
        is Outcome.Success -> {
            restoreRepo.markRestored()
            if (result.value > 0) {
                coordinator.setRestoredCount(result.value)
            }
            result.value
        }
        is Outcome.Failure -> 0
    }
}
