package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn

class GetFavoriteHymnsUseCase {
    operator fun invoke(
        buchMode: BuchMode,
    ): ArrayList<Hymn> {
        return hymnsRepo.getAllHymns(buchMode).filter { hymn ->
            hymnDataRepo.getAllHymnData(
                buchMode
            )[hymn.number - 1]?.favorite ?: false
        } as ArrayList
    }
}