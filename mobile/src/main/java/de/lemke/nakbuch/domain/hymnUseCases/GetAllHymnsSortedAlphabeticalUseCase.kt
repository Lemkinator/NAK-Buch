package de.lemke.nakbuch.domain.hymnUseCases

import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAllHymnsSortedAlphabeticalUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
) {
    suspend operator fun invoke(buchMode: BuchMode) = withContext(Dispatchers.Default) {
        hymnsRepository.getAllHymns(buchMode).sortedBy { it.title }
    }
}