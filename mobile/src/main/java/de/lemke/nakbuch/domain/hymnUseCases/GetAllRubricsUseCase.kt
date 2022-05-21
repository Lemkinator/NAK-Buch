package de.lemke.nakbuch.domain.hymnUseCases

import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Rubric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAllRubricsUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
) {
    suspend operator fun invoke(buchMode: BuchMode): List<Rubric> = withContext(Dispatchers.Default) {
        hymnsRepository.getAllRubrics(buchMode)
    }
}