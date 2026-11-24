package io.github.khaledbahaaeldin.emberbyte.whatsnew

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewGuardTest {

    private fun resolveDir(relativeFromRoot: String): File =
        listOf(File(relativeFromRoot), File("../$relativeFromRoot"))
            .firstOrNull { it.exists() }
            ?: File(relativeFromRoot)

    @Test
    fun whats_new_adds_no_network_permission_or_client() {
        val manifest = resolveDir("app/src/main/AndroidManifest.xml").readText()
        assertFalse("Manifest must never declare INTERNET", manifest.contains("android.permission.INTERNET"))

        val whatsNewDir = resolveDir("app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/whatsnew")
        assertTrue("Expected whatsnew package at ${whatsNewDir.path}", whatsNewDir.exists())
        val forbidden = listOf("HttpURLConnection", "OkHttp", "URL(", "java.net.Socket")
        whatsNewDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            for (token in forbidden) {
                assertFalse("Forbidden network token '$token' found in ${file.name}", text.contains(token))
            }
        }
    }
}
