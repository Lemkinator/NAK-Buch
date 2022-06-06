package de.lemke.nakbuch.domain.model

enum class BuchMode {
    Gesangbuch, Chorbuch, Jugendliederbuch, JBErgaenzungsheft;

    override fun toString(): String = when (this) {
        Gesangbuch -> "Gesangbuch"
        Chorbuch -> "Chorbuch"
        Jugendliederbuch -> "Jugendliederbuch"
        JBErgaenzungsheft -> "JB-Ergänzungsheft"
    }

    fun toInt(): Int = when (this) {
        Gesangbuch -> 0 * intStep
        Chorbuch -> 1 * intStep
        Jugendliederbuch -> 2 * intStep
        JBErgaenzungsheft -> 3 * intStep
    }

    val hymnCount: Int
        get() = when (this) {
            Gesangbuch -> 438
            Chorbuch -> 462
            Jugendliederbuch -> 102
            JBErgaenzungsheft -> 20
        }

    val assetFileName: String
        get() = when (this) {
            Gesangbuch -> "hymnsGesangbuchNoCopyright.txt"
            Chorbuch -> "hymnsChorbuchNoCopyright.txt"
            Jugendliederbuch -> "hymnsJugendliederbuchNoCopyright.txt"
            JBErgaenzungsheft -> "hymnsJBErgaenzungsheftNoCopyright.txt"
        }

    companion object {
        const val intStep = 10000
        fun fromInt(mode: Int?): BuchMode? = when (mode) {
            0 * intStep -> Gesangbuch
            1 * intStep -> Chorbuch
            2 * intStep -> Jugendliederbuch
            3 * intStep -> JBErgaenzungsheft
            else -> null
        }
    }
}