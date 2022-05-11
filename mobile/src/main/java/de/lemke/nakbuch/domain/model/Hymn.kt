package de.lemke.nakbuch.domain.model

data class PersonalHymn(
    val hymn: Hymn,
    val hymnData: HymnData
)

data class Hymn(
    val number: Int,
    val buchMode: BuchMode,
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

val hymnPlaceholder =
    Hymn(-1, BuchMode.Gesangbuch, rubricPlaceholder, "Placeholder", "Placeholder", "Placeholder", "Placeholder")


val personalHymn = PersonalHymn(hymnPlaceholder, HymnData(hymnPlaceholder.number, hymnPlaceholder.buchMode))