package de.lemke.nakbuch.domain.hymndataUseCases

import de.lemke.nakbuch.data.HymnDataRepository
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetFavoritesFromHymnListUseCase @Inject constructor(
    private val hymnDataRepository: HymnDataRepository,
) {
        suspend operator fun invoke(hymnList: List<Hymn>, selected: Map<Int, Boolean>, favorite: Boolean) {
        withContext(Dispatchers.Default) {
            hymnDataRepository.setPersonalHymns(selected.map {
                GetPersonalHymnUseCase(hymnDataRepository)(hymnList[it.key].hymnId).copy(favorite = favorite)
            })
        }
    }
}
