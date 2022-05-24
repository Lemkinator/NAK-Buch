package de.lemke.nakbuch.domain.model

data class Hymn(
    val buchMode: BuchMode,
    val number: Int,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Hymn
        if (buchMode != other.buchMode || number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        return numberAndTitle.hashCode()
    }
}


