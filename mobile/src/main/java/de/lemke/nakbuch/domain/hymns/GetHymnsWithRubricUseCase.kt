package de.lemke.nakbuch.domain.hymns

import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.Rubric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetHymnsWithRubricUseCase {
    suspend operator fun invoke(
        rubric: Rubric
    ): ArrayList<Hymn> = withContext(Dispatchers.IO) {
        ArrayList(hymnsRepo.getAllHymns(rubric.buchMode)).filter { it.rubric == rubric } as ArrayList<Hymn>
    }
}