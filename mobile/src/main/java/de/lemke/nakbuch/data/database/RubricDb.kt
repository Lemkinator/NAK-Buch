package de.lemke.nakbuch.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.lemke.nakbuch.domain.model.RubricId

@Entity(tableName = "rubric",)
data class RubricDb(
    @PrimaryKey
    val rubricId: RubricId,
    val name: String,
    val isMain: Int,
)
