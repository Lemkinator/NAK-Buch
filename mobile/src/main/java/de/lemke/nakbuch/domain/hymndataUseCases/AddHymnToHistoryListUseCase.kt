package de.lemke.nakbuch.domain.hymndataUseCases

import de.lemke.nakbuch.data.HymnDataRepository
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class AddHymnToHistoryListUseCase @Inject constructor(
    private val hymnDataRepository: HymnDataRepository,
) {
    suspend operator fun invoke(hymn: Hymn) = withContext(Dispatchers.Default) {
        hymnDataRepository.addToHistoryList(hymn, LocalDate.now())
    }
}