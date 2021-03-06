package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {
    suspend fun insert(photos: List<PhotoDb>) {
        photos.forEach { insert(it) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: PhotoDb)

    @Query("DELETE FROM photo WHERE hymnId = :hymnId")
    suspend fun delete(hymnId: Int)

    @Query("DELETE FROM photo")
    suspend fun deleteAll()
}
