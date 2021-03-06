package de.lemke.nakbuch.data


import de.lemke.nakbuch.data.database.*
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.PersonalHymn
import java.time.LocalDateTime
import javax.inject.Inject

class HymnDataRepository @Inject constructor(
    private val hymnDataDao: HymnDataDao,
    private val historyDao: HistoryDao,
    private val photoDao: PhotoDao,
    private val sungOnDao: SungOnDao,
) {
    suspend fun deleteHistory() = historyDao.deleteAll()

    suspend fun getHistoryList(): List<Pair<Hymn, LocalDateTime>> = historyDao.getAll().map { historyFromDb(it) }

    suspend fun addToHistoryList(hymn: Hymn, date: LocalDateTime) = historyDao.fixedInsert(historyToDb(hymn, date))

    suspend fun getPersonalHymn(hymnId: HymnId): PersonalHymn = personalHymnFromDb(hymnDataDao.getPersonalHymnByHymnId(hymnId.toInt()))

    suspend fun getAllPersonalHymns(buchMode: BuchMode): List<PersonalHymn> =
        hymnDataDao.getAllPersonalHymns(buchMode.minId, buchMode.maxId).map { personalHymnFromDb(it) }

    suspend fun setPersonalHymn(personalHymn: PersonalHymn) {
        setPersonalHymnWithoutLists(personalHymn)
        sungOnDao.delete(personalHymn.hymn.hymnId.toInt())
        sungOnDao.insert(personalHymnToSungOnDbList(personalHymn))
        photoDao.delete(personalHymn.hymn.hymnId.toInt())
        photoDao.insert(personalHymnToPhotoDbList(personalHymn))
    }

    private suspend fun setPersonalHymnWithoutLists(personalHymn: PersonalHymn) = hymnDataDao.upsert(personalHymnToHymnDataDb(personalHymn))

    suspend fun setPersonalHymns(personalHymns: List<PersonalHymn>) {
        setPersonalHymnsWithoutLists(personalHymns)
        personalHymns.forEach {
            sungOnDao.delete(it.hymn.hymnId.toInt())
            sungOnDao.insert(personalHymnToSungOnDbList(it))
        }
        personalHymns.forEach {
            photoDao.delete(it.hymn.hymnId.toInt())
            photoDao.insert(personalHymnToPhotoDbList(it))
        }
    }

    suspend fun setPersonalHymnsWithoutLists(personalHymns: List<PersonalHymn>) {
        hymnDataDao.upsert(personalHymns.map { personalHymnToHymnDataDb(it) })
    }
}
