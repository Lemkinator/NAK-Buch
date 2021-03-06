package de.lemke.nakbuch.data.database

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.lemke.nakbuch.domain.model.HymnId

@Entity(
    tableName = "photo",
    foreignKeys = [
        ForeignKey(
            entity = HymnDb::class,
            parentColumns = arrayOf("hymnId"),
            childColumns = arrayOf("hymnId"),
            onDelete = ForeignKey.NO_ACTION,
        )
    ],
    indices = [Index(
        value = ["hymnId", "uri"],
        unique = true
    )],
)
data class PhotoDb(
    @PrimaryKey
    val position: Int,
    val hymnId: HymnId,
    val uri: Uri,
)