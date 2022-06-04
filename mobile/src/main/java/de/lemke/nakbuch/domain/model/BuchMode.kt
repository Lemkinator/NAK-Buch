package de.lemke.nakbuch.domain.model

enum class BuchMode {
    Gesangbuch, Chorbuch, Jugendliederbuch;

    override fun toString(): String = when (this) {
        Gesangbuch -> "Gesangbuch"
        Chorbuch -> "Chorbuch"
        Jugendliederbuch -> "Jugendliederbuch"
    }

    fun toCompactString(): String = when (this) {
        Gesangbuch -> "GB"
        Chorbuch -> "CB"
        Jugendliederbuch -> "JB"
    }

    fun toInt(): Int = when (this) {
        Gesangbuch -> 0 * intStep
        Chorbuch -> 1 * intStep
        Jugendliederbuch -> 2 * intStep
    }

    val hymnCount: Int
        get() = when (this) {
            Gesangbuch -> 438
            Chorbuch -> 462
            Jugendliederbuch -> 3 //TODO
        }

    val rubricCount: Int
        get() = when (this) {
            Gesangbuch -> 29
            Chorbuch -> 31
            Jugendliederbuch -> 3 //TODO
        }

    val assetFileName: String
        get() = when (this) {
            Gesangbuch -> "hymnsGesangbuchNoCopyright.txt"
            Chorbuch -> "hymnsChorbuchNoCopyright.txt"
            Jugendliederbuch -> "hymnsJugendliederbuchNoCopyright.txt"
        }

    val rubricListItemList: List<Int>
        get() = when (this) {
            Gesangbuch -> listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1)
            Chorbuch -> listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1)
            Jugendliederbuch -> listOf(1, 1, 1) //TODO
        }


    val rubricTitlesList: List<String>
        get() = when (this) {
            Gesangbuch -> {
                listOf(
                    "Das geistliche Jahr",
                    "Advent",
                    "Weihnachten",
                    "Jahreswende",
                    "Palmsonntag",
                    "Karfreitag - Christi Leiden",
                    "Ostern",
                    "Christi Himmelfahrt",
                    "Pfingsten",
                    "Bußtag - Einsicht und Umkehr",
                    "Gottesdienst",
                    "Einladung - Heilsverlangen - Heiligung",
                    "Glaube - Vertrauen - Trost",
                    "Gottes Liebe - Nächstenliebe",
                    "Vergebung - Gnade",
                    "Lob - Dank - Anbetung",
                    "Sakramente",
                    "Heilige Taufe",
                    "Heiliges Abendmahl",
                    "Heilige Versiegelung",
                    "Segenshandlungen",
                    "Konfirmation",
                    "Trauung",
                    "Den Glauben leben",
                    "Morgen und Abend",
                    "Gemeinde - Gemeinschaft",
                    "Sendung - Nachfolge - Bekenntnis",
                    "Verheißung - Erwartung - Erfüllung",
                    "Sterben - Ewiges Leben"
                )
            }
            Chorbuch -> {
                listOf(
                    "Das geistliche Jahr",
                    "Advent",
                    "Weihnachten",
                    "Jahreswechsel",
                    "Palmsonntag",
                    "Passion",
                    "Ostern",
                    "Himmelfahrt",
                    "Pfingsten",
                    "Erntedank",
                    "Gottesdienst",
                    "Einladung - Heilsverlangen - Heiligung",
                    "Anbetung",
                    "Glaube - Vertrauen",
                    "Trost - Mut - Frieden",
                    "Gnade - Vergebung",
                    "Lobpreis Gottes",
                    "Sakramente",
                    "Heilige Taufe",
                    "Heiliges Abendmahl",
                    "Heilige Versiegelung",
                    "Segenshandlungen",
                    "Konfirmation",
                    "Trauung",
                    "Den Glauben leben",
                    "Morgen - Abend",
                    "Gottes Liebe - Nächstenliebe",
                    "Mitarbeit - Gemeinschaft",
                    "Sendung - Nachfolge - Bekenntnis",
                    "Verheißung - Erwartung - Erfüllung",
                    "Sterben - Ewiges Leben"
                )
            }
            Jugendliederbuch -> listOf("Jugendliederbuch Rubrik", "Rubrik 1", "Rubrik 2") //TODO

        }

    val maxId: Int get() = this.toInt() + intStep - 1
    val minId: Int get() = this.toInt()

    companion object {
        const val intStep = 10000
        fun fromInt(mode: Int?): BuchMode? = when (mode) {
            0 * intStep -> Gesangbuch
            1 * intStep -> Chorbuch
            2 * intStep -> Jugendliederbuch
            else -> null
        }

        fun fromHymnId(hymnId: Int?): BuchMode? = when (hymnId) {
            in Gesangbuch.minId..Gesangbuch.maxId -> Gesangbuch
            in Chorbuch.minId..Chorbuch.maxId -> Chorbuch
            in Jugendliederbuch.minId..Jugendliederbuch.maxId -> Jugendliederbuch
            else -> null
        }
    }
}