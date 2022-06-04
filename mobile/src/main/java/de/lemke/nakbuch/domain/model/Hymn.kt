package de.lemke.nakbuch.domain.model

class HymnId private constructor(
    val number: Int,
    val buchMode: BuchMode,
) {
    override fun toString(): String = "$buchMode: $number"

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
            if (number < 1 || number > buchMode.hymnCount) return null
            return HymnId(number, buchMode)
        }

        fun create(hymnIdInt: Int): HymnId? {
            if (hymnIdInt < 1) return null
            val number = hymnIdInt.mod(BuchMode.intStep)
            val buchMode = BuchMode.fromHymnId(hymnIdInt) ?: return null
            return create(number, buchMode)
        }
    }
}

data class Hymn(
    val hymnId: HymnId,
    val rubric: Rubric,
    val numberAndTitle: String,
    val title: String,
    val text: String,
    val copyright: String,
    val containsCopyright: Boolean,
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
            Rubric.rubricPlaceholder, "Placeholder", "Placeholder", "Placeholder", "Placeholder", false
        )
    }
}