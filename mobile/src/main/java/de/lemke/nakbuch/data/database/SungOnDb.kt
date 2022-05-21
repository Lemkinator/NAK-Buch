package de.lemke.nakbuch.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import de.lemke.nakbuch.domain.model.HymnId
import java.time.LocalDate

@Entity(
    tableName = "sung_on",
    primaryKeys = ["hymnId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = HymnDb::class,
            parentColumns = arrayOf("hymnId"),
            childColumns = arrayOf("hymnId"),
            onDelete = ForeignKey.NO_ACTION,
        )
    ],
)
data class SungOnDb(
    val hymnId: HymnId,
    val date: LocalDate,
)