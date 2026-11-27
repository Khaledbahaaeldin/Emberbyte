package io.github.khaledbahaaeldin.emberbyte.onboarding

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Keeps the onboarding sources honest: texts in resources, titles not heavier or tighter than the design allows. */
class OnboardingSourceGuardTest {
    private val files: List<File> by lazy {
        val roots = listOf(File("src/main/kotlin"), File("app/src/main/kotlin"))
        val dir = roots.map { File(it, "io/github/khaledbahaaeldin/emberbyte/onboarding") }.first { it.exists() }
        dir.listFiles { f -> f.extension == "kt" }!!.toList()
    }

    @Test
    fun there_are_exactly_three_step_pages() {
        val names = files.map { it.name }
        assertTrue(names.containsAll(listOf("WelcomeStep.kt", "AccessStep.kt", "PlanStep.kt")))
        assertTrue("old steps are gone", names.none { it in setOf("SeeYourDataStep.kt", "StayUnderCapStep.kt", "AllowAccessStep.kt", "SharedLayer.kt") })
    }

    @Test
    fun no_user_visible_text_is_a_literal() {
        val allowed = setOf("\"E\"", "\"0\"")
        val offenders = files.flatMap { f ->
            Regex("""(?:text = |Text\()("[^"]*[A-Za-z][^"]*")""").findAll(f.readText())
                .map { it.groupValues[1] }
                .filterNot { it in allowed }
                .map { "${f.name}: $it" }
                .toList()
        }
        assertTrue("literal texts: $offenders", offenders.isEmpty())
    }

    @Test
    fun titles_are_never_tighter_than_minus_002_em_or_heavier_than_600() {
        val text = files.joinToString("\n") { it.readText() }
        assertTrue(Regex("""\(-0\.0[3-9]\d*\)\.em""").find(text) == null)
        assertTrue(text.contains("grotesk(size, 600)"))
    }
}
