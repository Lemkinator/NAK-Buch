package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    @Insert(
        onConflict = OnConflictStrategy.IGNORE,
        entity = RecentColorsDb::class
    )
    suspend fun insertColor(colorInt: Int)

    @Insert(
        onConflict = OnConflictStrategy.IGNORE,
        entity = HintDb::class
    )
    suspend fun insertHint(hint: String)
    suspend fun insertHints(items: List<String>) {
        items.forEach { insertHint(it) }
    }

    suspend fun discoverEasterEgg(easterEgg: String) = (insertEasterEgg(easterEgg) == -1L)

    @Insert(
        onConflict = OnConflictStrategy.IGNORE,
        entity = EasterEggsDb::class
    )
    suspend fun insertEasterEgg(easterEgg: String): Long

    @Query("SELECT * FROM recent_colors")
    suspend fun getRecentColors(): List<Int>

    @Query("SELECT * FROM hints")
    suspend fun getHints(): List<String>

    @Query("SELECT * FROM discovered_eastereggs")
    suspend fun getDiscoveredEasterEggs(): List<String>

    @Query("DELETE FROM discovered_eastereggs")
    suspend fun deleteDiscoveredEasterEggs()

    @Query("DELETE FROM hints")
    suspend fun deleteHints()

    @Query("DELETE FROM hints WHERE hint = :hint")
    suspend fun deleteHint(hint: String)
}
