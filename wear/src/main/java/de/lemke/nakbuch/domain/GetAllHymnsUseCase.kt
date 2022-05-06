package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllHymnsUseCase {
    suspend operator fun invoke(
        buchMode: BuchMode,
    ): ArrayList<Hymn> = withContext(Dispatchers.IO) {
        hymnsRepo.getAllHymns(buchMode)
    }
}