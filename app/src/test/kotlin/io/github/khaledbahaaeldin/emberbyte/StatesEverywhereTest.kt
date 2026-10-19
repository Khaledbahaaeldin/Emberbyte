package io.github.khaledbahaaeldin.emberbyte

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class StatesEverywhereTest {

    private val allowList: Set<String> = emptySet()

    private fun findAppMainDir(): File {
        val userDir = System.getProperty("user.dir") ?: "."
        val candidates = listOf(
            File("app/src/main"),
            File("src/main"),
            File(userDir, "app/src/main"),
            File(userDir, "src/main"),
        )
        return candidates.firstOrNull { it.isDirectory && it.exists() }
            ?: throw IllegalStateException("Could not find app/src/main in candidates: $candidates")
    }

    @Test
    fun no_screen_uses_a_bare_circular_progress_indicator() {
        val mainDir = findAppMainDir()
        val violations = mutableListOf<String>()

        mainDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val relativePath = file.relativeTo(mainDir).invariantSeparatorsPath
                if (allowList.contains(relativePath) || allowList.contains(file.name)) {
                    return@forEach
                }
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        if (line.contains("CircularProgressIndicator")) {
                            violations.add("${file.name}:${index + 1}: ${line.trim()}")
                        }
                    }
                }
            }

        assertTrue(
            "Found bare CircularProgressIndicator in app/src/main (only LoadingState in ui/design may use it):\n${violations.joinToString("\n")}",
            violations.isEmpty(),
        )
    }
}
