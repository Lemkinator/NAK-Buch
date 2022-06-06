package de.lemke.nakbuch.data.database

import androidx.room.*
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.RubricId

@Entity(
    tableName = "hymn",
    foreignKeys = [
        ForeignKey(
            entity = RubricDb::class,
            parentColumns = arrayOf("rubricId"),
            childColumns = arrayOf("rubricId"),
            onDelete = ForeignKey.NO_ACTION,
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