package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetHymnUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
) {
    suspend operator fun invoke(buchMode: BuchMode, hymnNr: Int): Hymn = withContext(Dispatchers.Default) {
        hymnsRepository.getHymnByNumber(buchMode, hymnNr)
    }

}