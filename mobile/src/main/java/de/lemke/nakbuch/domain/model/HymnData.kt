package de.lemke.nakbuch.domain.model

import java.util.*

class HymnData (
    var favorite: Boolean = false,
    var notes: String = "",
    var sungOn: ArrayList<Date> = ArrayList<Date>()
        )