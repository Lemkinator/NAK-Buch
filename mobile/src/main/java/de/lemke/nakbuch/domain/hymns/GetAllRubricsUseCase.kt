package de.lemke.nakbuch.domain.hymns

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Rubric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllRubricsUseCase {
    suspend operator fun invoke(buchMode: BuchMode): ArrayList<Rubric> = withContext(Dispatchers.IO) {
        hymnsRepo.getAllRubrics(buchMode)
    }
}