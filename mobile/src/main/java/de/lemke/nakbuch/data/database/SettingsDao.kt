package de.lemke.nakbuch.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE,)
    suspend fun insertColor(colorInt: RecentColorsDb)
    suspend fun insertColors(items: List<RecentColorsDb>) {
        items.forEach { insertColor(it) }
    }

    @Query("SELECT * FROM recent_colors")
    suspend fun getRecentColors(): List<RecentColorsDb>

    @Query("DELETE FROM recent_colors")
    suspend fun deleteRecentColors()




    @Insert(onConflict = OnConflictStrategy.IGNORE,)
    suspend fun insertHint(hint: HintDb)

    suspend fun insertHints(items: Set<HintDb>) {
        items.forEach { insertHint(it) }
    }

    @Query("SELECT * FROM hints")
    suspend fun getHints(): List<HintDb>

    @Query("DELETE FROM hints")
    suspend fun deleteHints()

    @Query("DELETE FROM hints WHERE hint = :hint")
    suspend fun deleteHint(hint: String)




    suspend fun discoverEasterEgg(easterEgg: EasterEggsDb):Boolean = (insertEasterEgg(easterEgg) != -1L)

    @Insert(onConflict = OnConflictStrategy.IGNORE,)
    suspend fun insertEasterEgg(easterEgg: EasterEggsDb): Long

    @Query("SELECT * FROM discovered_eastereggs")
    suspend fun getDiscoveredEasterEggs(): List<EasterEggsDb>

    @Query("DELETE FROM discovered_eastereggs")
    suspend fun deleteDiscoveredEasterEggs()
}
