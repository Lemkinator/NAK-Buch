package de.lemke.nakbuch.domain.hymndataUseCases

import de.lemke.nakbuch.data.HymnDataRepository
import de.lemke.nakbuch.domain.model.PersonalHymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetFavoritesFromPersonalHymnListUseCase @Inject constructor(
    private val hymnDataRepository: HymnDataRepository,
) {
    suspend operator fun invoke(personalHymnList: List<PersonalHymn>) {
        withContext(Dispatchers.Default) {
            hymnDataRepository.setPersonalHymnsWithoutLists(personalHymnList)
        }
    }
}
