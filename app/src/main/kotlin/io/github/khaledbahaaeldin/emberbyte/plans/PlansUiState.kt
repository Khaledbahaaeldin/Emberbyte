package io.github.khaledbahaaeldin.emberbyte.plans

import io.github.khaledbahaaeldin.emberbyte.ui.design.model.PlanCardUi

data class PlansUiState(
    val front: PlanCardUi? = null,
    val tiles: List<PlanCardUi> = emptyList(),
    val upcoming: List<PlanCardUi> = emptyList(),
    val summary: PlansSummary? = null,
    val insight: PlansInsight? = null,
    val renewals: List<RenewalEvent> = emptyList(),
    val restoredCount: Int = 0,
    val deletedPlanName: String? = null,
    val whatIfExtraGb: Int = 0,
    val whatIfSentence: String? = null,
    val expandedDetails: Boolean = false,
    val loaded: Boolean = false,
)

sealed interface PlansEvent {
    data class SetPinned(val id: Long, val pinned: Boolean) : PlansEvent
    data class Delete(val id: Long) : PlansEvent
    data object UndoDelete : PlansEvent
    data object DismissDeleteUndo : PlansEvent
    data object DismissRestoredNotice : PlansEvent
    data object ToggleDetails : PlansEvent
    data class SetWhatIf(val extraGb: Int) : PlansEvent
}
