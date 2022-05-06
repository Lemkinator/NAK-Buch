package de.lemke.nakbuch.domain.model

import java.time.LocalDate

data class HymnData(
    var favorite: Boolean = false,
    var notes: String = "",
    var sungOnList: ArrayList<LocalDate> = ArrayList(),
    var photoList: ArrayList<String> = ArrayList()
)