package de.lemke.nakbuch.domain.model

import android.net.Uri
import java.time.LocalDate

data class PersonalHymn(
    val hymn: Hymn,
    var favorite: Boolean = false,
    var notes: String = "",
    var sungOnList: List<LocalDate> = emptyList(),
    var photoList: List<Uri> = emptyList(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersonalHymn
        if (hymn != other.hymn) return false
        return true
    }

    override fun hashCode(): Int {
        return hymn.hashCode()
    }

    companion object {
        val personalHymnPlaceholder = PersonalHymn(Hymn.hymnPlaceholder)
    }
}

