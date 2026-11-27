package io.github.khaledbahaaeldin.emberbyte.whatsnew

import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WhatsNewAssetTest {

    private fun resolveFile(relativeFromRoot: String): File =
        listOf(File(relativeFromRoot), File("../$relativeFromRoot"))
            .firstOrNull { it.exists() }
            ?: File(relativeFromRoot)

    @Test
    fun whats_new_entries_are_newest_first_and_non_empty() {
        val asset = resolveFile("app/src/main/assets/whats_new.json")
        assertTrue("Expected whats_new.json at ${asset.path}", asset.exists())
        val entries = parseWhatsNewJson(asset.readText())
        assertTrue("whats_new.json must have at least one entry", entries.isNotEmpty())
        for (i in 0 until entries.size - 1) {
            assertTrue(
                "Entries must be newest-first: ${entries[i].versionCode} vs ${entries[i + 1].versionCode}",
                entries[i].versionCode > entries[i + 1].versionCode,
            )
        }
        assertTrue("Newest entry must have highlights", entries.first().highlights.isNotEmpty())
    }

    @Test
    fun every_highlight_is_at_most_120_characters() {
        val asset = resolveFile("app/src/main/assets/whats_new.json")
        assertTrue(asset.exists())
        val root = JSONObject(asset.readText())
        val entries = root.getJSONArray("entries")
        for (i in 0 until entries.length()) {
            val highlights = entries.getJSONObject(i).getJSONArray("highlights")
            for (j in 0 until highlights.length()) {
                val text = highlights.getJSONObject(j).getString("text")
                assertTrue("Highlight too long (${text.length} > 120): $text", text.length <= 120)
                assertTrue("Highlight must not be blank", text.isNotBlank())
            }
        }
    }

    @Test
    fun whats_new_newest_entry_matches_version_name_and_code_in_build_gradle() {
        val gradle = resolveFile("app/build.gradle.kts").readText()
        val versionCode = Regex("""versionCode\s*=\s*(\d+)""").find(gradle)!!.groupValues[1].toInt()
        val versionName = Regex("""versionName\s*=\s*"([^"]+)"""").find(gradle)!!.groupValues[1]

        val asset = resolveFile("app/src/main/assets/whats_new.json")
        assertTrue(asset.exists())
        val entries = parseWhatsNewJson(asset.readText())
        val newest = entries.first()
        assertEquals(versionCode, newest.versionCode)
        assertEquals(versionName, newest.versionName)
    }

    @Test
    fun whats_new_newest_entry_matches_the_newest_changelog_version() {
        val changelog = resolveFile("CHANGELOG.md").readText()
        val newestChangelogVersion = Regex("""##\s+\[([^\]]+)\]""").find(changelog)!!.groupValues[1]

        val asset = resolveFile("app/src/main/assets/whats_new.json")
        assertTrue(asset.exists())
        val entries = parseWhatsNewJson(asset.readText())
        assertEquals(newestChangelogVersion, entries.first().versionName)
    }
}
