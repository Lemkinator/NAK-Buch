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
    fun hymnCount(buchMode: BuchMode) = when (buchMode) {
        BuchMode.Gesangbuch -> BuchMode.gesangbuchHymnsCount
        BuchMode.Chorbuch -> BuchMode.chorbuchHymnsCount
        BuchMode.Jugendliederbuch -> BuchMode.jugendliederbuchHymnsCount
    }

    fun rubricCount(buchMode: BuchMode) = when (buchMode) {
        BuchMode.Gesangbuch -> BuchMode.gesangbuchRubricCount
        BuchMode.Chorbuch -> BuchMode.chorbuchRubricCount
        BuchMode.Jugendliederbuch -> BuchMode.jugendliederbuchRubricCount
    }

    suspend fun search(buchMode: BuchMode, search: String): List<Hymn> =
        hymnDao.search(BuchMode.minId(buchMode), BuchMode.maxId(buchMode), "*$search*").map { hymnFromDb(it) }

    suspend fun getAllHymns(buchMode: BuchMode): List<Hymn> =
        hymnDao.getAll(BuchMode.minId(buchMode), BuchMode.maxId(buchMode)).map { hymnFromDb(it) }

    suspend fun getHymnByNumber(hymnId: HymnId): Hymn = hymnFromDb(hymnDao.getById(hymnId.toInt()))

    suspend fun addHymn(hymn: Hymn) = hymnDao.insert(hymnToDb(hymn))

    suspend fun addHymns(hymns: List<Hymn>) = hymnDao.insert(hymns.map {
        hymnToDb(it)
    })

    suspend fun deleteAllHymns() = hymnDao.deleteAll()

    suspend fun getAllRubrics(buchMode: BuchMode): List<Rubric> =
        rubricDao.getAll(BuchMode.minId(buchMode), BuchMode.maxId(buchMode)).map { rubricFromDb(it) }

    suspend fun addRubric(rubric: Rubric) = rubricDao.insert(rubricToDb(rubric))

    suspend fun addRubrics(rubrics: List<Rubric>) = rubricDao.insert(rubrics.map { rubricToDb(it) })
}