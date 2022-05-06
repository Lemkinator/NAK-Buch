package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetHymnDataUseCase {
    suspend operator fun invoke(
        hymn: Hymn,
        hymnData: HymnData
    ) = withContext(Dispatchers.IO) {
        hymnDataRepo.setHymnData(hymn, hymnData)
        hymnDataRepo.writeHymnDataToPreferences(hymn.buchMode)
    }
}