package de.lemke.nakbuch.domain.model

data class Hymn (
    val buchMode: BuchMode,
    val number: Int,
    val rubric: Rubric,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String
        )



