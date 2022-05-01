package de.lemke.nakbuch.domain

import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetFavoriteUseCase {
    suspend operator fun invoke(
        hymn: Hymn,
        favorite: Boolean
    ) = withContext(Dispatchers.IO) {
        val hymnData : HymnData = GetHymnDataUseCase()(hymn)
        hymnData.favorite = favorite
        SetHymnDataUseCase()(hymn, hymnData)
    }
}