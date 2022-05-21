package de.lemke.nakbuch.domain.model

class HymnId private constructor(
    val number: Int,
    val buchMode: BuchMode,
) {
    override fun toString():String = "$buchMode: $number"

    fun toInt(): Int {
        return number + buchMode.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HymnId
        if (toInt() != other.toInt()) return false
        return true
    }

    override fun hashCode(): Int {
        return toInt()
    }

    companion object {
        val hymnIdPlaceholder = HymnId(-1, BuchMode.Gesangbuch)
        fun create(number: Int, buchMode: BuchMode): HymnId? {
            when (buchMode) {
                BuchMode.Gesangbuch -> if (number < 1 || number > BuchMode.gesangbuchHymnsCount) return null
                BuchMode.Chorbuch -> if (number < 1 || number > BuchMode.chorbuchHymnsCount) return null
                BuchMode.Jugendliederbuch -> if (number < 1 || number > BuchMode.jugendliederbuchHymnsCount) return null
            }
            return HymnId(number, buchMode)
        }

        fun create(hymnIdInt: Int): HymnId? {
            if (hymnIdInt < 1) return null
            val number = hymnIdInt.mod(BuchMode.intStep)
            return when (hymnIdInt) {
                in BuchMode.minId(BuchMode.Gesangbuch) .. BuchMode.maxId(BuchMode.Gesangbuch) ->
                    create(number, BuchMode.Gesangbuch)
                in BuchMode.minId(BuchMode.Chorbuch) .. BuchMode.maxId(BuchMode.Chorbuch) ->
                    create(number, BuchMode.Chorbuch)
                in BuchMode.minId(BuchMode.Jugendliederbuch) .. BuchMode.maxId(BuchMode.Jugendliederbuch) ->
                    create(number, BuchMode.Jugendliederbuch)
                else -> null
            }
        }
    }
}

data class Hymn(
    val hymnId: HymnId,
    val rubric: Rubric,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Hymn
        if (hymnId != other.hymnId) return false
        return true
    }

    override fun hashCode(): Int {
        return hymnId.hashCode()
    }
    companion object {
        val hymnPlaceholder = Hymn(
            HymnId.hymnIdPlaceholder,
            Rubric.rubricPlaceholder, "Placeholder", "Placeholder", "Placeholder", "Placeholder"
        )
    }
}