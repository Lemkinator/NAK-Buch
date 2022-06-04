package de.lemke.nakbuch.domain.hymnUseCases

import android.util.Log
import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSearchListUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
    private val getUserSettings: GetUserSettingsUseCase
) {
    suspend operator fun invoke(
        buchMode: BuchMode,
        search: String?
    ): List<Hymn> = withContext(Dispatchers.Default) {
        return@withContext if (search.isNullOrBlank()) emptyList()
        else if (search.startsWith("\"") && search.endsWith("\"") && search.length > 2) {
            val newSearch = search.substring(1, search.length - 1)
            //fts with leading wildcard not working :( //hymnsRepository.search(buchMode, sanitizeSearchQuery(newSearch))
            if (getUserSettings().alternativeSearchModeEnabled)
                hymnsRepository.getAllHymns(buchMode).filter { hymnContainsKeywords(it, setOf(newSearch)) }
            else
                hymnsRepository.getAllHymns(buchMode).filter { hymnContainsKeywords(it, newSearch.trim().split(" ").toSet()) }
        } else {
            //hymnsRepository.search(buchMode, sanitizeSearchQuery(search))
            if (getUserSettings().alternativeSearchModeEnabled)
                hymnsRepository.getAllHymns(buchMode).filter { hymnContainsKeywords(it, search.trim().split(" ").toSet()) }
            else
                hymnsRepository.getAllHymns(buchMode).filter { hymnContainsKeywords(it, setOf(search)) }
        }
    }

    private fun hymnContainsKeywords(hymn: Hymn, keywords: Set<String>): Boolean {
        for (search in keywords) {
            if (hymn.text.contains(search, ignoreCase = true) ||
                hymn.title.contains(search, ignoreCase = true) ||
                hymn.copyright.contains(search, ignoreCase = true)
            ) return true
        }
        return false
    }

    private fun sanitizeSearchQuery(query: String): String {
        val newQuery = ":*${query.replace(Regex.fromLiteral("\""), "\"\"")}:*"
        Log.d("test", newQuery)
        return newQuery
    }
}
