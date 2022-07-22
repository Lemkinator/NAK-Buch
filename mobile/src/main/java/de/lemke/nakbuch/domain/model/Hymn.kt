package de.lemke.nakbuch.domain.model

data class Hymn(
    val hymnId: HymnId,
    val rubric: Rubric,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String,
    val containsCopyright: Boolean,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Hymn
        if (hymnId != other.hymnId) return false
        return true
    }

    override fun hashCode(): Int = hymnId.hashCode()
}