package io.github.khaledbahaaeldin.emberbyte.sampler

import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BudgetStateUi

object ChipText {
    fun forState(state: BudgetStateUi, usedToday: String, remaining: String?): String {
        return when (state) {
            BudgetStateUi.Exceeded -> "Over"
            BudgetStateUi.Warning -> {
                val rem = remaining.orEmpty().trim()
                val candidate = "!$rem"
                if (candidate.length <= 7) {
                    candidate
                } else {
                    val numOnly = "!" + rem.substringBefore(" ").trim()
                    if (numOnly.length <= 7) numOnly else numOnly.take(7)
                }
            }
            BudgetStateUi.Ok -> {
                val today = usedToday.trim()
                if (today.length <= 7) {
                    today
                } else {
                    val numOnly = today.substringBefore(" ").trim()
                    if (numOnly.length <= 7) numOnly else numOnly.take(7)
                }
            }
        }
    }
}
