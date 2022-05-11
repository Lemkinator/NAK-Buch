package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RubricDao {

    @Insert
    suspend fun insert(rubric: RubricDb)

    @Query("SELECT * FROM rubric WHERE buchMode = :buchMode")
    suspend fun getAll(buchMode: Int): List<RubricDb>

    @Query("SELECT * FROM rubric WHERE buchMode = :buchMode AND rubricIndex = :rubricIndex")
    suspend fun getByIndex(buchMode: Int, rubricIndex: Int): RubricDb?
}
