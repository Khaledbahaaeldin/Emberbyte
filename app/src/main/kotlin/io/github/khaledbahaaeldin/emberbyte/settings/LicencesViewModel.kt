package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.io.InputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.json.JSONArray
import org.json.JSONObject

data class LicenceItem(
    val name: String,
    val version: String,
    val licence: String,
    val url: String,
    val category: String,
    val fullLicenceText: String,
)

class LicencesViewModel(
    private val assetStreamOpener: () -> InputStream,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allItems = MutableStateFlow<List<LicenceItem>>(emptyList())

    val filteredItems: StateFlow<List<LicenceItem>> = combine(_allItems, _searchQuery) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            val q = query.trim()
            items.filter { item ->
                item.name.contains(q, ignoreCase = true) ||
                    item.licence.contains(q, ignoreCase = true) ||
                    item.category.contains(q, ignoreCase = true) ||
                    item.version.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadLicences()
    }

    private fun loadLicences() {
        runCatching {
            assetStreamOpener().use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                parseLicencesJson(jsonString)
            }
        }.onSuccess { loaded ->
            _allItems.value = loaded
        }.onFailure {
            _allItems.value = emptyList()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    companion object {
        fun parseLicencesJson(jsonString: String): List<LicenceItem> {
            val array = JSONArray(jsonString)
            val list = mutableListOf<LicenceItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LicenceItem(
                        name = obj.optString("name"),
                        version = obj.optString("version"),
                        licence = obj.optString("licence"),
                        url = obj.optString("url"),
                        category = obj.optString("category"),
                        fullLicenceText = obj.optString("fullLicenceText"),
                    )
                )
            }
            return list
        }

        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appContext = context.applicationContext
                return LicencesViewModel {
                    appContext.assets.open("licences.json")
                } as T
            }
        }
    }
}
