package de.lemke.nakbuch.domain.model

import java.util.*

class HymnData (
    val favorite: Boolean = false,
    val notes: String = "",
    val sungOn: ArrayList<Date> = ArrayList<Date>()
        )