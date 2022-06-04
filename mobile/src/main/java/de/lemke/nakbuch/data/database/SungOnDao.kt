package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SungOnDao {
    suspend fun insert(sungOns: List<SungOnDb>) {
        sungOns.forEach { insert(it) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sungOn: SungOnDb)

    @Query("DELETE FROM sung_on WHERE hymnId = :hymnId")
    suspend fun delete(hymnId: Int)

    @Query("DELETE FROM sung_on")
    suspend fun deleteAll()
}
