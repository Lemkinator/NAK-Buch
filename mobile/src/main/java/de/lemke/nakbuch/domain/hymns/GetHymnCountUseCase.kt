package de.lemke.nakbuch.domain.hymns

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class GetHymnCountUseCase {
    operator fun invoke(buchMode: BuchMode): Int = hymnsRepo.hymnCount(buchMode)
}