package de.lemke.nakbuch.domain

import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData

class EditHymnDataUseCase {
    operator fun invoke(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences,
        hymn: Hymn,
        hymnData: HymnData
    ) {
        hymnDataRepo.setHymnData(gesangbuchSelected, spHymns, hymn, hymnData)
    }

}