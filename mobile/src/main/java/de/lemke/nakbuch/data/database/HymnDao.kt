package de.lemke.nakbuch.data.database

import androidx.room.*

@Dao
interface HymnDao {
    @Transaction
    suspend fun insert(hymns: List<HymnDb>) {
        hymns.forEach { insert(it) }
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hymn: HymnDb)

    @Transaction
    @Query("SELECT * FROM hymn WHERE hymnId BETWEEN :minId AND :maxId")
    suspend fun getAll(minId: Int, maxId: Int): List<HymnAndRubric>

    @Transaction
    @Query("SELECT * FROM hymn WHERE hymnId = :hymnId")
    suspend fun getById(hymnId: Int): HymnAndRubric?

    @Query("DELETE FROM hymn")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM hymn JOIN hymn_fts ON hymn.hymnId = hymn_fts.rowid " +
            "WHERE " +
            "(hymnId BETWEEN :minId AND :maxId) " +
            "AND " +
            "(hymn_fts MATCH :search || '*')" )
    suspend fun search(minId: Int, maxId: Int, search:String): List<HymnAndRubric>
}

/*
SELECT name
FROM (
      SELECT name, 1 as matched
      FROM nametable
      WHERE name MATCH 'fast'
    UNION ALL
      SELECT name, 1 as matched
      FROM nametable
      WHERE name MATCH 'food'
    UNION ALL
      SELECT name, 1 as matched
      FROM nametable
      WHERE name MATCH 'restaurant'
  )
GROUP BY name
ORDER BY SUM(matched) DESC, name
 */
