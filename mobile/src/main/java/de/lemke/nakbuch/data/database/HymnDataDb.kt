package de.lemke.nakbuch.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.lemke.nakbuch.domain.model.HymnId


@Entity(
    tableName = "hymn_data",
    foreignKeys = [
        ForeignKey(
            entity = HymnDb::class,
            parentColumns = arrayOf("hymnId"),
            childColumns = arrayOf("hymnId"),
            onDelete = ForeignKey.NO_ACTION,
        )
    ],
)
data class HymnDataDb(
    @PrimaryKey
    val hymnId: HymnId,
    var favorite: Int,
    var notes: String,
    /*
    var sungOnList: List<LocalDate> = emptyList(),
    var photoList: List<String> = emptyList(),
    */
)

