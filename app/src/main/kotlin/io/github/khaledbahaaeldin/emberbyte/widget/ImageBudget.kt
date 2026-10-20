package io.github.khaledbahaaeldin.emberbyte.widget

import android.graphics.Bitmap

/**
 * Tracks memory budget for widget bitmap allocations (Contract 13.7).
 *
 * Each widget instance has an overall image budget of at most 1.5 MiB (1_572_864 bytes).
 * When [add] returns false, the caller falls back to cheaper rendering (e.g. system font or omitting icons).
 */
class ImageBudget(val maxBytes: Int = 1_572_864) {
    var used: Int = 0
        private set

    /**
     * Attempts to allocate [bitmap] against the budget.
     * Returns true and increments [used] if the bitmap fits within [maxBytes].
     * Returns false and leaves [used] unchanged if adding the bitmap would exceed [maxBytes].
     */
    fun add(bitmap: Bitmap): Boolean {
        val bytes = bitmap.allocationByteCount
        return if (used + bytes <= maxBytes) {
            used += bytes
            true
        } else {
            false
        }
    }
}
