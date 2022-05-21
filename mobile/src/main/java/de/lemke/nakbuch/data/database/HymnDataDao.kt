package de.lemke.nakbuch.data.database

import androidx.room.*

@Dao
interface HymnDataDao {
    @Transaction
    suspend fun upsert(hymnData: HymnDataDb) {
        val rowId = insert(hymnData)
        if (rowId == -1L) {
            update(hymnData)
        }
    }

    @Transaction
    suspend fun upsert(hymnDatas: List<HymnDataDb>) {
        hymnDatas.forEach { upsert(it) }
    }

    @Update
    suspend fun update(hymnData: HymnDataDb)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(hymnData: HymnDataDb): Long

    @Transaction
    @Query("SELECT * FROM hymn WHERE hymnId BETWEEN :minId AND :maxId")
    suspend fun getAllPersonalHymns(minId: Int, maxId: Int): List<PersonalHymnDb>

    @Transaction
    @Query("SELECT * FROM hymn WHERE hymnId = :hymnId")
    suspend fun getPersonalHymnById(hymnId: Int): PersonalHymnDb?
}
