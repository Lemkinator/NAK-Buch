package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GetHistoryListUseCase {
    suspend operator fun invoke():ArrayList<Pair<Hymn, LocalDate>> = withContext(Dispatchers.IO) {
        hymnDataRepo.getHistoryList()
    }
}