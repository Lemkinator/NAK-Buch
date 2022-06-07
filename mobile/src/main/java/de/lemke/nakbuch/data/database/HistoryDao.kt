package de.lemke.nakbuch.data.database

import androidx.room.*

@Dao
interface HistoryDao {
    suspend fun fixedInsert(history: HistoryDb) {
        insert(history)
        if (countEntries() > 4000) deleteLastEntry()
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: HistoryDb)

    @Transaction
    @Query("SELECT * FROM history ORDER BY dateTime DESC;")
    suspend fun getAll(): List<HymnAndHistory>

    @Query("SELECT COUNT(*) FROM history;")
    suspend fun countEntries(): Int

    @Query("DELETE FROM history WHERE id = (SELECT id FROM history ORDER BY dateTime ASC LIMIT 1);")
    suspend fun deleteLastEntry()

    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
