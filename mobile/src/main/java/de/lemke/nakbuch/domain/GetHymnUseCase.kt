package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetHymnUseCase {
    suspend operator fun invoke(
        buchMode: BuchMode,
        hymnNr: Int
    ): Hymn = withContext(Dispatchers.IO) {
        hymnsRepo.getHymnByNumber(buchMode, hymnNr)
    }

}