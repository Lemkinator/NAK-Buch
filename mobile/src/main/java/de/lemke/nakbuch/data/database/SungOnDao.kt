package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate

@Dao
interface SungOnDao {
    suspend fun insert(sungOns: List<SungOnDb>) {
        sungOns.forEach { insert(it) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sungOn: SungOnDb)

    @Query("SELECT * FROM sung_on WHERE hymnId = :hymnId ORDER BY date DESC")
    suspend fun getByHymnId(hymnId: Int): List<SungOnDb>

    @Query("DELETE FROM sung_on WHERE hymnId = :hymnId AND date = :date")
    suspend fun delete(hymnId: Int, date: LocalDate)

    @Query("DELETE FROM sung_on WHERE hymnId = :hymnId")
    suspend fun delete(hymnId: Int)

    @Query("DELETE FROM sung_on")
    suspend fun deleteAll()

}
