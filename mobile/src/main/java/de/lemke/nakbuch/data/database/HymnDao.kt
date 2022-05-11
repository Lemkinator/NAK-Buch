package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HymnDao {

    @Insert
    suspend fun insert(product: HymnDb)

    @Query("SELECT * FROM hymn WHERE buchMode = :buchMode")
    suspend fun getAll(buchMode: Int): List<HymnDb>

    @Query("SELECT * FROM hymn WHERE buchMode = :buchMode AND number = :number")
    suspend fun getByNumber(buchMode: Int, number: Int): HymnDb?
}
