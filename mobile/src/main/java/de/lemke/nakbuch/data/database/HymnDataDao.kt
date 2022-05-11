package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HymnDataDao {

    @Insert
    suspend fun insert(hymnData: HymnDataDb)

    @Query("SELECT * FROM hymn_data WHERE buchMode = :buchMode")
    suspend fun getAll(buchMode: Int): List<HymnDataDb>

    @Query("SELECT * FROM hymn_data WHERE buchMode = :buchMode AND number = :number")
    suspend fun getByNumber(buchMode: Int, number: Int): HymnDataDb?
}
