package de.lemke.nakbuch.domain.model

import java.time.LocalDate

data class HymnData(
    val number: Int,
    val buchMode: BuchMode,
    var favorite: Boolean = false,
    var notes: String = "",
    var sungOnList: MutableList<LocalDate> = mutableListOf(),
    var photoList: MutableList<String> = mutableListOf(),
)
