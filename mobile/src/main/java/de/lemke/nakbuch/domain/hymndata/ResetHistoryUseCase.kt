package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.hymnDataRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetHistoryUseCase {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        hymnDataRepo.resetHistory()
    }
}
