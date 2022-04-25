package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class GetAllHymnsSortedAlphabeticalUseCase {
    operator fun invoke(
        buchMode: BuchMode,
    ) = hymnsRepo.getAllHymnsSortedAlphabetic(buchMode)
}