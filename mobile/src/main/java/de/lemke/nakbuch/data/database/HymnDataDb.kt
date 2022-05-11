package de.lemke.nakbuch.data.database

import androidx.room.Entity


@Entity(
    tableName = "hymn_data",
    primaryKeys = ["number", "buchMode"],
    /*foreignKeys = [
        ForeignKey(
            entity = HymnDb::class,
            parentColumns = arrayOf("number", "buchMode"),
            childColumns = arrayOf("number", "buchMode"),
            onDelete = ForeignKey.CASCADE,
        )
    ],*/
)
data class HymnDataDb(
    val number: Int,
    val buchMode: Int,
    var favorite: Int,
    var notes: String,
    /*
    var sungOnList: List<LocalDate> = emptyList(),
    var photoList: List<String> = emptyList(),
    */
)

/*
data class PersonalHymnDb(
    @Embedded
    val hymnData: HymnDataDb,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id",
    )
    val hymn: HymnDb,
)
 */
