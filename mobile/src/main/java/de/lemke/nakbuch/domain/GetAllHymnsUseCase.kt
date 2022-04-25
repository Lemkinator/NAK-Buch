package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class GetAllHymnsUseCase {
    operator fun invoke(
        buchMode: BuchMode,
    ) = hymnsRepo.getAllHymns(buchMode)
}