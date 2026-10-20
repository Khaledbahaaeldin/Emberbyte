package io.github.khaledbahaaeldin.emberbyte.nav

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NoBareBackHandlerTest {

    @Test
    fun no_back_handler_is_enabled_unconditionally() {
        val srcDir = File("src/main/kotlin")
        val violations = mutableListOf<String>()

        srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            val lines = text.lines()
            for ((idx, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) continue

                // Check for BackHandler(enabled = true)
                if (line.contains("BackHandler(enabled = true)") || line.contains("BackHandler(enabled=true)")) {
                    violations.add("${file.name}:${idx + 1} uses BackHandler(enabled = true)")
                }

                // Check for BackHandler { without enabled parameter
                if (Regex("""BackHandler\s*\{""").containsMatchIn(line)) {
                    violations.add("${file.name}:${idx + 1} uses bare BackHandler { without enabled argument")
                }

                // Check for onBackPressed override
                if (line.contains("override fun onBackPressed") || line.contains("override suspend fun onBackPressed")) {
                    violations.add("${file.name}:${idx + 1} overrides onBackPressed")
                }
            }
        }

        assertTrue(
            "Found unconditional or bare BackHandlers:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }
}
