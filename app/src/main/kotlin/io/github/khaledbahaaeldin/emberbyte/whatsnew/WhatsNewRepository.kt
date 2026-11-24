package io.github.khaledbahaaeldin.emberbyte.whatsnew

import android.content.Context
import io.github.khaledbahaaeldin.emberbyte.data.WhatsNewPrefsRepository
import java.time.LocalDate
import org.json.JSONObject

data class WhatsNewEntry(
    val versionCode: Int,
    val versionName: String,
    val date: LocalDate,
    val highlights: List<Highlight>,
)

data class Highlight(
    val kind: Kind,
    val text: String,
)

enum class Kind {
    Added,
    Changed,
    Fixed,
}

internal interface WhatsNewRepository {
    val entries: List<WhatsNewEntry>
    suspend fun lastSeenVersionCode(): Int?
    suspend fun markSeen(versionCode: Int)
}

fun shouldShowWhatsNew(
    currentVersionCode: Int,
    lastSeen: Int?,
    onboardingCompleted: Boolean,
    hasEntryForCurrent: Boolean,
): Boolean = onboardingCompleted && hasEntryForCurrent && (lastSeen == null || lastSeen < currentVersionCode)

internal fun parseWhatsNewJson(jsonText: String): List<WhatsNewEntry> = runCatching {
    val root = JSONObject(jsonText)
    val array = root.getJSONArray("entries")
    buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val versionCode = obj.getInt("versionCode")
            val versionName = obj.getString("versionName")
            val date = LocalDate.parse(obj.getString("date"))
            val highlightsArray = obj.getJSONArray("highlights")
            val highlights = buildList {
                for (j in 0 until highlightsArray.length()) {
                    val h = highlightsArray.getJSONObject(j)
                    val kind = when (h.getString("kind").lowercase()) {
                        "added" -> Kind.Added
                        "changed" -> Kind.Changed
                        "fixed" -> Kind.Fixed
                        else -> continue
                    }
                    val text = h.getString("text").trim()
                    if (text.isNotEmpty()) {
                        add(Highlight(kind = kind, text = text))
                    }
                }
            }
            add(
                WhatsNewEntry(
                    versionCode = versionCode,
                    versionName = versionName,
                    date = date,
                    highlights = highlights,
                ),
            )
        }
    }.sortedByDescending { it.versionCode }
}.getOrElse { emptyList() }

internal class AssetWhatsNewRepository(
    context: Context,
    private val prefs: WhatsNewPrefsRepository,
    assetName: String = "whats_new.json",
) : WhatsNewRepository {
    override val entries: List<WhatsNewEntry> = runCatching {
        context.assets.open(assetName).bufferedReader().use { it.readText() }
    }.mapCatching(::parseWhatsNewJson).getOrElse { emptyList() }

    override suspend fun lastSeenVersionCode(): Int? = prefs.lastSeenVersionCode()

    override suspend fun markSeen(versionCode: Int) = prefs.markSeen(versionCode)
}
