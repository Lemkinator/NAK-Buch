package de.lemke.nakbuch.domain

import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn

class GetHymnDataUseCase {
    operator fun invoke(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences,
        hymn: Hymn
    ) = hymnDataRepo.getHymnData(gesangbuchSelected, spHymns, hymn)
}