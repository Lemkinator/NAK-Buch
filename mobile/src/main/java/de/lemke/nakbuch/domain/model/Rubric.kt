package de.lemke.nakbuch.domain.model

class RubricId private constructor(
    val index: Int,
    val buchMode: BuchMode,
) {
    fun toInt(): Int {
        return index + buchMode.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RubricId
        if (toInt() != other.toInt()) return false
        return true
    }

    override fun hashCode(): Int {
        return toInt()
    }

    companion object {
        val rubricIdPlaceholder = RubricId(-1, BuchMode.Gesangbuch)
        fun create(index: Int, buchMode: BuchMode): RubricId? {
            if (index < 0 || index >= getRubricListItemList(buchMode).size) return null
            if (index < 0 || index >= getRubricTitlesList(buchMode).size) return null
            return RubricId(index, buchMode)
        }

        fun create(rubricIdInt: Int): RubricId? {
            val index = rubricIdInt.mod(BuchMode.intStep)
            val buchMode = if (rubricIdInt > BuchMode.Jugendliederbuch.toInt()) BuchMode.Jugendliederbuch
            else if (rubricIdInt > BuchMode.Chorbuch.toInt()) BuchMode.Chorbuch
            else BuchMode.Gesangbuch
            return create(index, buchMode)
        }
    }
}

data class Rubric(
    val rubricId: RubricId,
    val name: String,
    val isMain: Boolean,
) {
    constructor(rubricId: RubricId) :
            this(
                rubricId,
                getRubricName(rubricId),
                isMainRubric(rubricId),
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Rubric
        if (rubricId != other.rubricId) return false
        return true
    }

    override fun hashCode(): Int {
        return rubricId.hashCode()
    }
    companion object {
        val rubricPlaceholder = Rubric(RubricId.rubricIdPlaceholder, "Placeholder", true)
    }
}

private fun isMainRubric(rubricId: RubricId): Boolean {
    return getRubricListItemList(rubricId.buchMode)[rubricId.index] == 0
}

private fun getRubricName(rubricId: RubricId): String {
    return getRubricTitlesList(rubricId.buchMode)[rubricId.index]
}

fun getRubricListItemList(buchMode: BuchMode): List<Int> {
    return when (buchMode) {
        BuchMode.Gesangbuch -> listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1)
        BuchMode.Chorbuch -> listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1)
        BuchMode.Jugendliederbuch -> listOf(1,1,1) //TODO
    }

}

fun getRubricTitlesList(buchMode: BuchMode): List<String> {
    return when (buchMode) {
        BuchMode.Gesangbuch -> {
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
        BuchMode.Chorbuch -> {
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
        BuchMode.Jugendliederbuch -> listOf("Jugendliederbuch Rubrik", "Rubrik 1", "Rubrik 2") //TODO
    }
}