package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn

class GetHymnDataUseCase {
    operator fun invoke(
        buchMode: BuchMode,
        hymn: Hymn
    ) = hymnDataRepo.getHymnData(buchMode, hymn)
}