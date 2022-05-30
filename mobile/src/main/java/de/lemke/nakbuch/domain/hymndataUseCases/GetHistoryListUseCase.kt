package de.lemke.nakbuch.domain.hymndataUseCases

import de.lemke.nakbuch.data.HymnDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetHistoryListUseCase @Inject constructor(
    private val hymnDataRepository: HymnDataRepository,
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        hymnDataRepository.getHistoryList()
    }
}