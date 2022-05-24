package de.lemke.nakbuch.domain.model

enum class BuchMode {
    Gesangbuch, Chorbuch, Jugendliederbuch;

    override fun toString(): String {
        return when (this) {
            Gesangbuch -> "Gesangbuch"
            Chorbuch -> "Chorbuch"
            Jugendliederbuch -> "Jugendliederbuch"
        }
    }

    fun toCompactString(): String {
        return when (this) {
            Gesangbuch -> "GB"
            Chorbuch -> "CB"
            Jugendliederbuch -> "JB"
        }
    }

    fun toInt(): Int {
        return when (this) {
            Gesangbuch -> 0 * intStep
            Chorbuch -> 1 * intStep
            Jugendliederbuch -> 2 * intStep
        }
    }


    companion object {
        const val gesangbuchHymnsCount = 438
        const val chorbuchHymnsCount = 462
        const val jugendliederbuchHymnsCount = 3 //TODO
        const val intStep = 1000
        fun maxId(buchMode: BuchMode) = buchMode.toInt() + intStep - 1
        fun minId(buchMode: BuchMode) = buchMode.toInt()
        fun fromInt(mode: Int): BuchMode = when (mode) {
            0 * intStep -> Gesangbuch
            1 * intStep -> Chorbuch
            2 * intStep -> Jugendliederbuch
            else -> Gesangbuch
        }
    }
}