package de.lemke.nakbuch.data.database

import androidx.room.Entity

@Entity(
    tableName = "hymn",
    primaryKeys = ["number", "buchMode"],
)
data class HymnDb(
    val number: Int,
    val buchMode: Int,
    val rubric: Int,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String,
)
