package de.lemke.nakbuch.data.database

import androidx.room.*

@Dao
interface HistoryDao {

    @Transaction
    suspend fun fixedInsert(history: HistoryDb) {
        insert(history)
        if (countEntries() > 1000) //TODO
            deleteLastEntry()
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: HistoryDb)

    @Transaction
    @Query("SELECT * FROM history;") //LIMIT 1000?
    suspend fun getAll(): List<HymnAndHistory>

    @Query("SELECT COUNT(*) FROM history;")
    suspend fun countEntries(): Int


    @Query("DELETE FROM history WHERE id = (SELECT MIN(id) FROM history);")
    suspend fun deleteLastEntry()


    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
