package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class GetAllHymnsSortedRubricUseCase {
    operator fun invoke(
        buchMode: BuchMode,
        rubricIndex: Int
    ) = hymnsRepo.getAllHymnsSortedRubric(buchMode, rubricIndex)
}