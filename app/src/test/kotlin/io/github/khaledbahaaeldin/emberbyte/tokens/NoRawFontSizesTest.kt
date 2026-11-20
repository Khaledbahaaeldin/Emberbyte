package io.github.khaledbahaaeldin.emberbyte.tokens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoRawFontSizesTest {
    @Test
    fun scanner_counts_raw_size_literals() {
        assertEquals(0, TokenScan.countSizes("style = MaterialTheme.typography.bodyMedium"))
        assertEquals(2, TokenScan.countSizes("EmberbyteType.heading(15.sp) and fontSize = 11.5.sp"))
        assertEquals(0, TokenScan.countSizes("val width = 12.dp + 3.spacing"))
        assertEquals(1, TokenScan.countSizes("fontSize = 14.5f.sp"))
        assertEquals(1, TokenScan.countSizes("val size = TextUnit(14f, TextUnitType.Sp)"))
    }

    @Test
    fun no_raw_sp_outside_tokens() {
        val files = TokenScan.productionFiles()
        val problems = mutableListOf<String>()
        for ((path, text) in files) {
            val found = TokenScan.countSizes(text)
            val allowed = TokenAllowLists.rawSizes[path] ?: 0
            if (found > allowed) {
                problems += "$path has $found raw .sp literals, allowed $allowed. Use a typography role."
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun allow_list_only_names_files_that_exist_and_still_need_it() {
        val files = TokenScan.productionFiles()
        val stale = TokenAllowLists.rawSizes.filter { (path, allowed) ->
            val found = files[path]?.let(TokenScan::countSizes) ?: -1
            found < 0 || found < allowed
        }
        assertTrue("Shrink the allow-list to the real counts: $stale", stale.isEmpty())
    }
}
