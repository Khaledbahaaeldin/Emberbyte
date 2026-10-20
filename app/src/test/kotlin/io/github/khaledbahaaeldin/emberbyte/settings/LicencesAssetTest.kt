package io.github.khaledbahaaeldin.emberbyte.settings

import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LicencesAssetTest {

    private fun findRepoRoot(): File {
        val userDir = System.getProperty("user.dir") ?: "."
        val candidates = listOf(
            File("."),
            File(".."),
            File(userDir),
            File(userDir, "..")
        )
        return candidates.firstOrNull { File(it, "docs/release/dependency-licences.txt").exists() }
            ?: candidates.firstOrNull { File(it, "gradlew").exists() }
            ?: File(".")
    }

    private fun findAssetFile(): File {
        val root = findRepoRoot()
        val candidates = listOf(
            File(root, "app/src/main/assets/licences.json"),
            File("app/src/main/assets/licences.json"),
            File("src/main/assets/licences.json")
        )
        return candidates.firstOrNull { it.exists() }
            ?: File(root, "app/src/main/assets/licences.json")
    }

    @Test
    fun asset_lists_every_dependency_of_dependency_licences_txt() {
        val root = findRepoRoot()
        val depFile = File(root, "docs/release/dependency-licences.txt")
        assertTrue("dependency-licences.txt must exist", depFile.exists())

        val depNames = depFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { it.split("|")[0].trim() }

        val assetFile = findAssetFile()
        assertTrue("app/src/main/assets/licences.json must exist", assetFile.exists())

        val jsonArray = JSONArray(assetFile.readText(Charsets.UTF_8))
        val entryNames = mutableSetOf<String>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            entryNames.add(obj.getString("name"))
        }

        // Compare counts and names
        for (dep in depNames) {
            assertTrue("licences.json must list dependency: $dep", entryNames.contains(dep))
        }
        assertEquals("Count of dependencies must match dependency-licences.txt", depNames.size, depNames.count { entryNames.contains(it) })
    }

    @Test
    fun asset_includes_the_nine_palettes_and_roboto_flex() {
        val assetFile = findAssetFile()
        assertTrue("app/src/main/assets/licences.json must exist", assetFile.exists())

        val jsonArray = JSONArray(assetFile.readText(Charsets.UTF_8))
        val entryNames = mutableSetOf<String>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            entryNames.add(obj.getString("name"))
        }

        val requiredPalettes = listOf(
            "Catppuccin",
            "Gruvbox",
            "Nord",
            "Rosé Pine",
            "Solarized",
            "Dracula",
            "Tokyo Night",
            "Everforest",
            "Kanagawa"
        )

        for (palette in requiredPalettes) {
            assertTrue(
                "licences.json must include palette: $palette",
                entryNames.contains(palette) || (palette == "Rosé Pine" && entryNames.contains("Rose Pine"))
            )
        }

        assertTrue("licences.json must include Roboto Flex", entryNames.contains("Roboto Flex"))
    }
}
