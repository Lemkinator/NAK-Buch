package de.lemke.nakbuch.domain.model

enum class BuchMode {
    Gesangbuch, Chorbuch;

    override fun toString(): String {
        return when (this) {
            Gesangbuch -> "GB"
            Chorbuch -> "CB"
        }
    }
}

data class Hymn(
    val buchMode: BuchMode,
    val number: Int,
    val rubric: Rubric,
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

data class PersonalHymn(
    val hymn: Hymn,
    val hymnData: HymnData
)

val hymnPlaceholder =
    Hymn(BuchMode.Gesangbuch, -1, rubricPlaceholder, "Placeholder", "Placeholder", "Placeholder", "Placeholder")


