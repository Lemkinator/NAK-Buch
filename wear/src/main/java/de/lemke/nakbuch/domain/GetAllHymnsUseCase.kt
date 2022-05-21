package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllHymnsUseCase {
    suspend operator fun invoke(
        buchMode: BuchMode,
    ) = withContext(Dispatchers.IO) {
        hymnsRepo.getAllHymns(buchMode)
    }
}