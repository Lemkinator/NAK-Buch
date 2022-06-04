package de.lemke.nakbuch.data.database

import androidx.room.*
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.RubricId

/*
Support full-text search
If your app requires very quick access to databases information through full-text search (FTS),
have your entities backed by a virtual table that uses either the FTS3 or FTS4 SQLite extension module.
To use this capability, available in Room 2.1.0 and higher, add the @Fts3 or @Fts4 annotation to a given entity,
as shown in the following code snippet:

@Fts4
@Entity(tableName = "users")
data class User(
    /* Specifying a primary key for an FTS-table-backed entity is optional, but
       if you include one, it must use this type and column name. */
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "first_name") val firstName: String?
)
Note: FTS-enabled tables always use a primary key of type INTEGER and with the column name "rowid".
If your FTS-table-backed entity defines a primary key, it must use that type and column name.
 */

/*
@Fts4(contentEntity = HymnDb::class)
@Entity(tableName = "hymn_fts")
class HymnDbFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")val hymnId: HymnId,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String,
)

data class HymnAndRubricWithMatchInfo(
    @Embedded
    val hymn: HymnDb,
    @Relation(
        parentColumn = "rubricId",
        entityColumn = "rubricId",
    )
    val rubric: RubricDb,
    @ColumnInfo(name = "matchInfo")
    val matchInfo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HymnAndRubricWithMatchInfo
        if (hymn != other.hymn) return false
        if (!matchInfo.contentEquals(other.matchInfo)) return false
        return true
    }
    override fun hashCode(): Int = 31 * hymn.hashCode() + matchInfo.contentHashCode()
}

*/


@Entity(
    tableName = "hymn",
    foreignKeys = [
        ForeignKey(
            entity = RubricDb::class,
            parentColumns = arrayOf("rubricId"),
            childColumns = arrayOf("rubricId"),
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class HymnDb(
    @PrimaryKey
    val hymnId: HymnId,
    val rubricId: RubricId,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String,
    val containsCopyright: Int,
)

data class HymnAndRubric(
    @Embedded
    val hymn: HymnDb,
    @Relation(
        parentColumn = "rubricId",
        entityColumn = "rubricId",
    )
    val rubric: RubricDb,
)

data class PersonalHymnDb(
    @Embedded
    val hymn: HymnDb,
    @Relation(
        parentColumn = "hymnId",
        entityColumn = "hymnId",
    )
    val hymnData: HymnDataDb?,
    @Relation(
        parentColumn = "hymnId",
        entityColumn = "hymnId",
    )
    val sungOnList: List<SungOnDb>,
    @Relation(
        parentColumn = "hymnId",
        entityColumn = "hymnId",
    )
    val photoList: List<PhotoDb>,
)
