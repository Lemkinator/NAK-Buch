package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class GetAllHymnsSearchListUseCase {
    operator fun invoke(
        buchMode: BuchMode,
        search: String
    ) = hymnsRepo.getAllHymnsSearchList(buchMode, search)
}