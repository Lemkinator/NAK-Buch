package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RubricDao {
    suspend fun insert(rubric: List<RubricDb>) {
        rubric.forEach { insert(it) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rubric: RubricDb): Long

    @Query("SELECT * FROM rubric WHERE rubricId BETWEEN :minId AND :maxId")
    suspend fun getAll(minId: Int, maxId: Int): List<RubricDb>
}
