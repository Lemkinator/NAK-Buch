package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetFavoriteUseCase {
    suspend operator fun invoke(hymn: Hymn, favorite: Boolean) =
        withContext(Dispatchers.IO) {
            SetHymnDataUseCase()(hymn, GetHymnDataUseCase()(hymn).copy(favorite = favorite))
        }
}