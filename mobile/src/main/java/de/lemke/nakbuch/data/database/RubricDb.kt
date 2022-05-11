package de.lemke.nakbuch.data.database

import androidx.room.Entity

@Entity(
    tableName = "rubric",
    primaryKeys = ["rubricIndex", "buchMode"],
)
data class RubricDb(
    val rubricIndex: Int,
    val buchMode: Int,
    val name: String,
    val isMain: Int,
)
