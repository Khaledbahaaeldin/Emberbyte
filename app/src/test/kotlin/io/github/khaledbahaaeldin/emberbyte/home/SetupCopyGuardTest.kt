package io.github.khaledbahaaeldin.emberbyte.home

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** The merged setup card is Home-only: onboarding and the widgets keep their own "Set up a plan" copy. */
class SetupCopyGuardTest {
    private fun source(path: String): String {
        val roots = listOf(File("src/main/kotlin"), File("app/src/main/kotlin"))
        val file = roots.map { File(it, path) }.firstOrNull { it.exists() } ?: error("missing $path")
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun widgets_and_onboarding_keep_their_setup_copy() {
        val base = "io/github/khaledbahaaeldin/emberbyte"
        assertTrue(source("$base/onboarding/StayUnderCapStep.kt").contains("Set up a plan"))
        val widgetFiles = File("src/main/kotlin/$base/widget").takeIf { it.exists() }
            ?: File("app/src/main/kotlin/$base/widget")
        val widgetText = widgetFiles.walkTopDown().filter { it.extension == "kt" }.joinToString("\n") { it.readText() }
        assertTrue("widgets keep a setup prompt", widgetText.contains("Set up a plan") || widgetText.contains("set up a plan", ignoreCase = true))
    }
}
