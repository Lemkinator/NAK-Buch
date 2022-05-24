package de.lemke.nakbuch.data.database

import androidx.room.*
import de.lemke.nakbuch.domain.model.HymnId
import java.time.LocalDate

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = HymnDb::class,
            parentColumns = arrayOf("hymnId"),
            childColumns = arrayOf("hymnId"),
            onDelete = ForeignKey.NO_ACTION,
        )
    ],
    indices = [Index(
        value = ["hymnId", "date"],
        unique = true
    )],

)
data class HistoryDb(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hymnId: HymnId,
    val date: LocalDate,
)


data class HymnAndHistory(
    @Embedded
    val history: HistoryDb,
    @Relation(
        parentColumn = "hymnId",
        entityColumn = "hymnId",
    )
    val hymn: HymnDb,
)
