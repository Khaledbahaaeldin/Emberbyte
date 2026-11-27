package io.github.khaledbahaaeldin.emberbyte.tokens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoRawRadiiTest {
    @Test
    fun scanner_counts_raw_radius_sites() {
        assertEquals(0, TokenScan.countRadii("val s = MaterialTheme.shapes.medium"))
        assertEquals(2, TokenScan.countRadii("a = RoundedCornerShape(20.dp)\nb = RoundedCornerShape(topStart = 8.dp)"))
    }

    @Test
    fun no_raw_rounded_corner_shape_outside_tokens() {
        val files = TokenScan.productionFiles()
        val problems = mutableListOf<String>()
        for ((path, text) in files) {
            val found = TokenScan.countRadii(text)
            val allowed = TokenAllowLists.rawRadii[path] ?: 0
            if (found > allowed) {
                problems += "$path has $found raw RoundedCornerShape( sites, allowed $allowed. Use MaterialTheme.shapes or AppShape."
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun allow_list_only_names_files_that_exist_and_still_need_it() {
        val files = TokenScan.productionFiles()
        val stale = TokenAllowLists.rawRadii.filter { (path, allowed) ->
            val found = files[path]?.let(TokenScan::countRadii) ?: -1
            found < 0 || found < allowed
        }
        assertTrue("Shrink the allow-list to the real counts: $stale", stale.isEmpty())
    }
}
