package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData

class SetHymnDataUseCase {
    operator fun invoke(
        buchMode: BuchMode,
        hymn: Hymn,
        hymnData: HymnData
    ) {
        hymnDataRepo.setHymnData(buchMode, hymn, hymnData)
    }

}