package de.lemke.nakbuch.domain.hymns

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSearchListUseCase {
    suspend operator fun invoke(
        buchMode: BuchMode,
        search: String
    ): ArrayList<Hymn> = withContext(Dispatchers.IO) {
        hymnsRepo.getSearchList(buchMode, search)
    }
}