package de.lemke.nakbuch.domain.hymnUseCases

import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetHymnUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
) {
    suspend operator fun invoke(hymnId: HymnId): Hymn =
        withContext(Dispatchers.Default) {
            hymnsRepository.getHymnByNumber(hymnId)
        }

}