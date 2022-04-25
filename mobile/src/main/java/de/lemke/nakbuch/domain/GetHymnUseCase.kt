package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class GetHymnUseCase {
    operator fun invoke(
        buchMode: BuchMode,
        hymnNr: Int
    ) = hymnsRepo.getHymnByNumber(buchMode, hymnNr)
}