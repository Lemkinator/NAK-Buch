package de.lemke.nakbuch.domain.hymnUseCases

import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.Rubric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetHymnsWithRubricUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
) {
    suspend operator fun invoke(rubric: Rubric) = withContext(Dispatchers.Default) {
        hymnsRepository.getAllHymns(rubric.rubricId.buchMode).filter { it.rubric == rubric }
    }
}