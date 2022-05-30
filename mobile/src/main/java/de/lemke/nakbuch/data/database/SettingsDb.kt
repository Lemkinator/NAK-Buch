package de.lemke.nakbuch.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_colors")
data class RecentColorsDb(
    @PrimaryKey
    val colorInt: Int,
)

@Entity(tableName = "hints")
data class HintDb(
    @PrimaryKey
    val hint: String,
)

@Entity(tableName = "discovered_eastereggs")
data class EasterEggsDb(
    @PrimaryKey
    val easterEgg: String,
)
