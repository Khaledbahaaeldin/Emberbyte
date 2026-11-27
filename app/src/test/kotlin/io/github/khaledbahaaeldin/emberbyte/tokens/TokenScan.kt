package io.github.khaledbahaaeldin.emberbyte.tokens

import java.io.File

/** Counts raw design values in production sources. Shared by [NoRawRadiiTest] and [NoRawFontSizesTest]. */
internal object TokenScan {
    private val radius = Regex("""RoundedCornerShape\(""")
    private val size = Regex("""(?<![\w.])\d+(?:\.\d+)?f?\.sp\b|\bTextUnit\(""")

    /** Files that define the tokens themselves and may use raw values. */
    private val tokenFiles = setOf("EmberbyteShapeScale.kt", "EmberbyteShapes.kt", "EmberbyteTypography.kt")

    fun countRadii(text: String): Int = radius.findAll(text).count()

    fun countSizes(text: String): Int = size.findAll(text).count()

    /** Repo-relative path (forward slashes) to its text, for every production Kotlin file outside the token files. */
    fun productionFiles(): Map<String, String> {
        val root = File("..").canonicalFile // tests run with the module (app) as working directory
        val result = linkedMapOf<String, String>()
        for (dir in listOf("app/src/main/kotlin", "ui/design/src/main/kotlin")) {
            File(root, dir).walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.name !in tokenFiles }
                .forEach { f ->
                    val relative = f.relativeTo(root).path.replace('\\', '/')
                    result[relative] = f.readText()
                }
        }
        return result
    }
}
