package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NoHardcodedColorsDesignTest {

    private val allowList: Set<String> = setOf(
        // Empty at the start; entries need a comment with the replacement task id
    )

    private val forbiddenPattern = Regex("""Color\(0x|Color\.(Red|Blue|Green|Yellow|Gray|Cyan|Magenta)""")

    private fun findDesignMainDir(): File {
        val userDir = System.getProperty("user.dir") ?: "."
        val candidates = listOf(
            File("ui/design/src/main"),
            File("src/main"),
            File(userDir, "ui/design/src/main"),
            File(userDir, "src/main"),
        )
        return candidates.firstOrNull { it.isDirectory && it.exists() }
            ?: throw IllegalStateException("Could not find ui/design/src/main in candidates: $candidates")
    }

    @Test
    fun no_hardcoded_colors_in_design_main_sources() {
        val mainDir = findDesignMainDir()
        val violations = mutableListOf<String>()

        mainDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val normalizedPath = file.invariantSeparatorsPath
                val relativePath = file.relativeTo(mainDir).invariantSeparatorsPath
                if (normalizedPath.contains("theme/palette")) {
                    return@forEach
                }
                if (allowList.contains(relativePath) || allowList.contains(file.name)) {
                    return@forEach
                }
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        if (forbiddenPattern.containsMatchIn(line)) {
                            violations.add("${file.name}:${index + 1}: ${line.trim()}")
                        }
                    }
                }
            }

        assertTrue(
            "Found hard-coded colors in ui:design sources:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }
}
