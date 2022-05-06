package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddHymnToHistoryListUseCase {
    suspend operator fun invoke(hymn: Hymn) = withContext(Dispatchers.IO) {
        hymnDataRepo.addToHistoryList(hymn)
    }
}