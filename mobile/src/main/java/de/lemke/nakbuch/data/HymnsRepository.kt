package de.lemke.nakbuch.data

import de.lemke.nakbuch.data.database.*
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.Rubric
import javax.inject.Inject

class HymnsRepository @Inject constructor(
    private val hymnDao: HymnDao,
    private val rubricDao: RubricDao,
) {
    suspend fun getAllHymns(buchMode: BuchMode): List<Hymn> =
        hymnDao.getAll(buchMode.minId, buchMode.maxId).map { hymnFromDb(it) }

    suspend fun getHymnByNumber(hymnId: HymnId): Hymn = hymnFromDb(hymnDao.getById(hymnId.toInt()))

    suspend fun addHymns(hymns: List<Hymn>) = hymnDao.insert(hymns.map { hymnToDb(it) })

    suspend fun getAllRubrics(buchMode: BuchMode): List<Rubric> =
        rubricDao.getAll(buchMode.minId, buchMode.maxId).map { rubricFromDb(it) }

    suspend fun addRubrics(rubrics: List<Rubric>) = rubricDao.insert(rubrics.map { rubricToDb(it) })
}