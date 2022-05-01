package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetHymnDataUseCase {
    suspend operator fun invoke(
        hymn: Hymn
    ): HymnData = withContext(Dispatchers.IO) {
        hymnDataRepo.getHymnData(hymn)
    }
}